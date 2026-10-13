package es.safareyes.swapcloset.controller;

import com.jayway.jsonpath.JsonPath;
import es.safareyes.swapcloset.entity.Disponibilidad;
import es.safareyes.swapcloset.repository.PrendaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static es.safareyes.swapcloset.support.TestAuth.como;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Endpoints de fotos. Los ficheros de prueba van a target/test-uploads y se borran al revertir cada test. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FotoControllerTest {

    @Autowired MockMvc mvc;
    @Autowired PrendaRepository prendas;

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 1, 2, 3, 4, 5, 6, 7, 8};
    private static final byte[] JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 16, 'J', 'F', 'I', 'F'};
    private static final byte[] WEBP = {'R', 'I', 'F', 'F', 1, 2, 3, 4, 'W', 'E', 'B', 'P', 'V', 'P', '8', ' '};

    private ResultActions subir(Long usuario, long prenda, String nombre, String tipo, byte[] bytes) throws Exception {
        var peticion = multipart("/api/v1/prendas/" + prenda + "/fotos")
                .file(new MockMultipartFile("foto", nombre, tipo, bytes));
        if (usuario != null) {
            peticion.with(como(usuario));
        }
        return mvc.perform(peticion);
    }

    // ---------------------------------------------------------------- subir

    @Test
    void laPrimeraFotoPublicaElBorrador() throws Exception {
        // la prenda 20 de sofia_m (6) es un borrador sin fotos
        mvc.perform(get("/api/v1/prendas/20")).andExpect(status().isNotFound());
        subir(6L, 20, "jersey.png", "image/png", PNG)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orden").value(1))
                .andExpect(jsonPath("$.url").value(org.hamcrest.Matchers.startsWith("/api/v1/fotos/")))
                .andExpect(jsonPath("$.claveDisco").doesNotExist());
        assertEquals(Disponibilidad.DISPONIBLE, prendas.findById(20L).orElseThrow().getDisponibilidad());
        mvc.perform(get("/api/v1/prendas/20")).andExpect(status().isOk());
    }

    @Test
    void lasSiguientesFotosSumanOrden() throws Exception {
        // la prenda 18 de lucia_s (4) ya tiene una foto de los datos de prueba
        subir(4L, 18, "a.jpg", "image/jpeg", JPG).andExpect(status().isCreated()).andExpect(jsonPath("$.orden").value(2));
        subir(4L, 18, "b.webp", "image/webp", WEBP).andExpect(status().isCreated()).andExpect(jsonPath("$.orden").value(3));
    }

    @Test
    void laSextaFotoDa422() throws Exception {
        // la prenda 1 de lucia_s ya tiene 5 fotos
        subir(4L, 1, "extra.png", "image/png", PNG).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void soloLaPropietariaPuedeSubir() throws Exception {
        subir(5L, 18, "a.png", "image/png", PNG).andExpect(status().isForbidden());
    }

    @Test
    void sinTokenDa401() throws Exception {
        subir(null, 18, "a.png", "image/png", PNG).andExpect(status().isUnauthorized());
    }

    @Test
    void prendaInexistenteORetiradaDa404() throws Exception {
        subir(4L, 9999, "a.png", "image/png", PNG).andExpect(status().isNotFound());
        subir(5L, 19, "a.png", "image/png", PNG).andExpect(status().isNotFound()); // la 19 está retirada
    }

    @Test
    void tipoNoPermitidoDa415() throws Exception {
        subir(4L, 18, "nota.txt", "text/plain", "hola".getBytes()).andExpect(status().isUnsupportedMediaType());
        subir(4L, 18, "anim.gif", "image/gif", PNG).andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void unFicheroQueMienteSobreSuTipoDa415() throws Exception {
        // dice ser PNG pero sus bytes son texto
        subir(4L, 18, "falso.png", "image/png", "no soy una imagen".getBytes()).andExpect(status().isUnsupportedMediaType());
        // dice ser JPEG pero es un PNG
        subir(4L, 18, "otro.jpg", "image/jpeg", PNG).andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void ficheroVacioDa422() throws Exception {
        subir(4L, 18, "vacio.png", "image/png", new byte[0]).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void masDe5MbDa413() throws Exception {
        byte[] grande = new byte[5 * 1024 * 1024 + 1];
        System.arraycopy(PNG, 0, grande, 0, PNG.length);
        subir(4L, 18, "grande.png", "image/png", grande).andExpect(status().isPayloadTooLarge());
    }

    @Test
    void sinLaParteFotoDa400() throws Exception {
        mvc.perform(multipart("/api/v1/prendas/18/fotos").with(como(4))).andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------- descargar

    @Test
    void laFotoSubidaSeDescargaSinCuentaYIgualQueSeSubio() throws Exception {
        String json = subir(4L, 18, "pañuelo.png", "image/png", PNG).andReturn().getResponse().getContentAsString();
        String url = JsonPath.read(json, "$.url");
        byte[] bajada = mvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andReturn().getResponse().getContentAsByteArray();
        assertArrayEquals(PNG, bajada);
    }

    @Test
    void laFotoDeLosDatosDePruebaNoTieneFicheroYDa404() throws Exception {
        mvc.perform(get("/api/v1/fotos/1")).andExpect(status().isNotFound());
    }

    @Test
    void fotoInexistenteDa404() throws Exception {
        mvc.perform(get("/api/v1/fotos/99999")).andExpect(status().isNotFound());
    }

    @Test
    void lasFotosDeUnaPrendaRetiradaSoloLasVeLaDuena() throws Exception {
        String prenda = mvc.perform(post("/api/v1/prendas").with(como(4)).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Falda plisada","descripcion":"Muy cómoda","categoriaId":1,"estiloId":3,
                                 "talla":"S","estado":"COMO_NUEVA","modalidad":"AMBAS"}
                                """))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        int prendaId = JsonPath.read(prenda, "$.id");
        String foto = subir(4L, prendaId, "falda.png", "image/png", PNG).andReturn().getResponse().getContentAsString();
        String url = JsonPath.read(foto, "$.url");

        mvc.perform(delete("/api/v1/prendas/" + prendaId).with(como(4))).andExpect(status().isNoContent());

        mvc.perform(get(url)).andExpect(status().isNotFound());                       // sin cuenta
        mvc.perform(get(url).with(como(5))).andExpect(status().isNotFound());         // otra persona
        mvc.perform(get(url).with(como(4))).andExpect(status().isOk());               // su dueña
        mvc.perform(get(url).with(como(2, "MODERADOR"))).andExpect(status().isOk()); // la moderación
    }
}
