package es.safareyes.swapcloset.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UsuarioControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    void perfilPublicoIncluyeEstilosReputacionYPrendas() throws Exception {
        mvc.perform(get("/api/v1/usuarios/4").header("X-Usuario-Id", 5))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("lucia_s"))
                .andExpect(jsonPath("$.estilos.length()").value(3))
                .andExpect(jsonPath("$.reputacion").value(5.0))
                .andExpect(jsonPath("$.valoraciones").value(1))
                .andExpect(jsonPath("$.prendas.length()").value(4))
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void perfilPublicoNoMuestraBorradores() throws Exception {
        // la usuaria 6 tiene la prenda 20 en BORRADOR; sus demás prendas sí salen
        mvc.perform(get("/api/v1/usuarios/6").header("X-Usuario-Id", 5))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prendas[?(@.id==20)]").isEmpty());
    }

    @Test
    void perfilInexistenteDa404() throws Exception {
        mvc.perform(get("/api/v1/usuarios/9999").header("X-Usuario-Id", 5))
                .andExpect(status().isNotFound());
    }

    @Test
    void sinCabeceraDeUsuarioDa400() throws Exception {
        mvc.perform(get("/api/v1/usuarios/4")).andExpect(status().isBadRequest());
    }

    @Test
    void editarPerfilCambiaSoloLoInformado() throws Exception {
        mvc.perform(put("/api/v1/usuarios/me").header("X-Usuario-Id", 8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ciudad\":\"Cádiz\",\"estilosIds\":[1,2]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ciudad").value("Cádiz"))
                .andExpect(jsonPath("$.biografia").value("Casual y minimalista."))
                .andExpect(jsonPath("$.estilos.length()").value(2))
                .andExpect(jsonPath("$.email").value("ana@correo.es"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void masDeCincoEstilosDa400() throws Exception {
        mvc.perform(put("/api/v1/usuarios/me").header("X-Usuario-Id", 8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estilosIds\":[1,2,3,4,5,6]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores[?(@.campo=='estilosIds')]").exists());
    }

    @Test
    void tallaDePantalonFueraDeRangoDa400() throws Exception {
        mvc.perform(put("/api/v1/usuarios/me").header("X-Usuario-Id", 8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tallaPantalon\":60}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores[?(@.campo=='tallaPantalon')]").exists());
    }

    @Test
    void estiloInexistenteDa422() throws Exception {
        mvc.perform(put("/api/v1/usuarios/me").header("X-Usuario-Id", 8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estilosIds\":[999]}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void afinesExcluyeATodaPersonaSinEstilosComunesYAUnoMismo() throws Exception {
        mvc.perform(get("/api/v1/usuarios/afines").header("X-Usuario-Id", 4))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$[0].estilosComunes").isNumber())
                .andExpect(jsonPath("$[?(@.id==4)]").isEmpty())
                .andExpect(jsonPath("$[?(@.estilosComunes<1)]").isEmpty());
    }

    @Test
    void afinesRespetaElLimite() throws Exception {
        mvc.perform(get("/api/v1/usuarios/afines?limite=2").header("X-Usuario-Id", 4))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
