package es.safareyes.swapcloset.controller;

import com.jayway.jsonpath.JsonPath;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Registro, login y seguridad con tokens JWT reales (sin atajos). Cada test se revierte. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired MockMvc mvc;
    @Value("${app.jwt.secret}") String secreto;

    private static final String REGISTRO_VALIDO = """
            {"username":"nueva_user","email":"nueva@correo.es","password":"Clave1234",
             "fechaNacimiento":"2000-01-01","ciudad":"Sevilla","estilosIds":[1,2]}
            """;

    private ResultActions login(String usuario, String password) throws Exception {
        return mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + usuario + "\",\"password\":\"" + password + "\"}"));
    }

    private ResultActions registro(String cuerpo) throws Exception {
        return mvc.perform(post("/api/v1/auth/registro").contentType(MediaType.APPLICATION_JSON).content(cuerpo));
    }

    private String tokenDe(String usuario) throws Exception {
        String json = login(usuario, "Password1!").andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, "$.token");
    }

    private String tokenConEstado(long idUsuario, long milisDeVida, String secretoUsado) {
        Date ahora = new Date();
        return Jwts.builder().subject(String.valueOf(idUsuario)).claim("rol", "USUARIO")
                .issuedAt(new Date(ahora.getTime() - 3_600_000))
                .expiration(new Date(ahora.getTime() + milisDeVida))
                .signWith(Keys.hmacShaKeyFor(secretoUsado.getBytes(StandardCharsets.UTF_8))).compact();
    }

    // ---------------------------------------------------------------- login

    @Test
    void loginCorrectoDevuelveToken() throws Exception {
        login("lucia_s", "Password1!")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.expiraEnSegundos").value(3600))
                .andExpect(jsonPath("$.usuario.id").value(4))
                .andExpect(jsonPath("$.usuario.rol").value("USUARIO"));
    }

    @Test
    void loginTambienAdmiteElEmailYNoDistingueMayusculas() throws Exception {
        login("LUCIA@correo.es", "Password1!").andExpect(status().isOk()).andExpect(jsonPath("$.usuario.username").value("lucia_s"));
        login("Lucia_S", "Password1!").andExpect(status().isOk());
    }

    @Test
    void passwordIncorrectaDa401() throws Exception {
        login("lucia_s", "otraCosa1").andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("No autenticado"));
    }

    @Test
    void usuarioInexistenteDa401ConElMismoMensaje() throws Exception {
        String conUsuarioReal = login("lucia_s", "mala").andReturn().getResponse().getContentAsString();
        String conUsuarioFalso = login("no_existo", "mala").andReturn().getResponse().getContentAsString();
        org.junit.jupiter.api.Assertions.assertEquals(
                (String) JsonPath.read(conUsuarioReal, "$.detail"), (String) JsonPath.read(conUsuarioFalso, "$.detail"));
    }

    @Test
    void loginSinDatosDa400() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------- registro

    @Test
    void registroCreaLaCuentaYDevuelveToken() throws Exception {
        registro(REGISTRO_VALIDO)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.usuario.username").value("nueva_user"))
                .andExpect(jsonPath("$.usuario.rol").value("USUARIO"));
        // y ya se puede entrar con esa contraseña
        login("nueva_user", "Clave1234").andExpect(status().isOk());
    }

    @Test
    void elTokenDelRegistroYaSirveParaUsarLaApi() throws Exception {
        String json = registro(REGISTRO_VALIDO).andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(json, "$.token");
        mvc.perform(get("/api/v1/usuarios/afines").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void registroIgnoraUnRolEnviadoPorElCliente() throws Exception {
        registro(REGISTRO_VALIDO.replace("\"ciudad\"", "\"rol\":\"ADMIN\",\"ciudad\""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.rol").value("USUARIO"));
    }

    @Test
    void menorDe16Da400ConElCampoIndicado() throws Exception {
        String anioReciente = java.time.LocalDate.now().minusYears(10).toString();
        registro(REGISTRO_VALIDO.replace("2000-01-01", anioReciente))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores[?(@.campo=='fechaNacimiento')]").isNotEmpty());
    }

    @Test
    void emailDuplicadoDa409() throws Exception {
        registro(REGISTRO_VALIDO.replace("nueva@correo.es", "Lucia@correo.es")).andExpect(status().isConflict());
    }

    @Test
    void usernameDuplicadoDa409SinImportarMayusculas() throws Exception {
        registro(REGISTRO_VALIDO.replace("nueva_user", "LUCIA_S")).andExpect(status().isConflict());
    }

    @Test
    void passwordDebilDa400() throws Exception {
        registro(REGISTRO_VALIDO.replace("Clave1234", "sololetras")).andExpect(status().isBadRequest());
        registro(REGISTRO_VALIDO.replace("Clave1234", "a1")).andExpect(status().isBadRequest());
    }

    @Test
    void masDeCincoEstilosDa400() throws Exception {
        registro(REGISTRO_VALIDO.replace("[1,2]", "[1,2,3,4,5,6]")).andExpect(status().isBadRequest());
    }

    @Test
    void sinEstilosDa400() throws Exception {
        registro(REGISTRO_VALIDO.replace("[1,2]", "[]")).andExpect(status().isBadRequest());
    }

    @Test
    void estiloInexistenteDa422() throws Exception {
        registro(REGISTRO_VALIDO.replace("[1,2]", "[1,999]")).andExpect(status().isUnprocessableEntity());
    }

    // ---------------------------------------------------------------- seguridad de las rutas

    @Test
    void sinTokenLasRutasProtegidasDan401() throws Exception {
        mvc.perform(get("/api/v1/solicitudes"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.title").value("No autenticado"));
        mvc.perform(post("/api/v1/prendas").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void conTokenValidoSePuedeEntrar() throws Exception {
        mvc.perform(get("/api/v1/solicitudes").header("Authorization", "Bearer " + tokenDe("sofia_m")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(3));
    }

    @Test
    void tokenManipuladoDa401() throws Exception {
        String token = tokenDe("lucia_s");
        String manipulado = token.substring(0, token.length() - 2) + (token.endsWith("AA") ? "BB" : "AA");
        mvc.perform(get("/api/v1/solicitudes").header("Authorization", "Bearer " + manipulado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenFirmadoConOtraClaveDa401() throws Exception {
        String falso = tokenConEstado(4, 60_000, "otra-clave-distinta-de-al-menos-32-caracteres!!");
        mvc.perform(get("/api/v1/solicitudes").header("Authorization", "Bearer " + falso))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenCaducadoDa401() throws Exception {
        String caducado = tokenConEstado(4, -1_000, secreto);
        mvc.perform(get("/api/v1/solicitudes").header("Authorization", "Bearer " + caducado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cabeceraSinBearerOTokenBasuraDa401() throws Exception {
        mvc.perform(get("/api/v1/solicitudes").header("Authorization", "Basic abc")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/solicitudes").header("Authorization", "Bearer basura")).andExpect(status().isUnauthorized());
    }

    @Test
    void elCatalogoYSwaggerSonPublicos() throws Exception {
        mvc.perform(get("/api/v1/prendas")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/categorias")).andExpect(status().isOk());
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }

    @Test
    void elRolDelTokenDecideElAccesoAlInforme() throws Exception {
        mvc.perform(get("/api/v1/informes/actividad").header("Authorization", "Bearer " + tokenDe("lucia_s")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Acceso denegado"));
        mvc.perform(get("/api/v1/informes/actividad").header("Authorization", "Bearer " + tokenDe("mariana")))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/informes/actividad").header("Authorization", "Bearer " + tokenDe("laura_mod")))
                .andExpect(status().isOk());
    }

    @Test
    void laIdentidadSaleDelTokenNoDeLaPeticion() throws Exception {
        // lucia_s (4) no participa en la solicitud 3 (javi_t y carlos_r): 403 aunque lo intente
        mvc.perform(get("/api/v1/solicitudes/3").header("Authorization", "Bearer " + tokenDe("lucia_s")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/solicitudes/3").header("Authorization", "Bearer " + tokenDe("javi_t")))
                .andExpect(status().isOk());
    }
}
