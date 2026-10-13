package es.safareyes.swapcloset.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static es.safareyes.swapcloset.support.TestAuth.como;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Prueba de integración de los endpoints 1-6 contra la base real. Cada test se revierte. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PrendaControllerTest {

    @Autowired
    MockMvc mvc;

    private static final String CUERPO_VALIDO = """
            {"titulo":"Falda plisada","descripcion":"Muy cómoda","categoriaId":1,"estiloId":3,
             "talla":"S","estado":"COMO_NUEVA","modalidad":"AMBAS"}
            """;

    @Test
    void listadoNoIncluyeBorradoresNiRetiradas() throws Exception {
        mvc.perform(get("/api/v1/prendas?size=50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id==20)]").isEmpty())
                .andExpect(jsonPath("$.content[?(@.id==19)]").isEmpty())
                .andExpect(jsonPath("$.page.totalElements").isNumber());
    }

    @Test
    void listadoFiltraPorCiudadYModalidad() throws Exception {
        mvc.perform(get("/api/v1/prendas?ciudad=sevilla&modalidad=PRESTAMO&size=50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].ciudad").value(org.hamcrest.Matchers.everyItem(
                        org.hamcrest.Matchers.is("Sevilla"))));
    }

    @Test
    void detalleIncluyeFotosYReputacion() throws Exception {
        mvc.perform(get("/api/v1/prendas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fotos.length()").value(5))
                .andExpect(jsonPath("$.fotos[0].url").value(org.hamcrest.Matchers.startsWith("/api/v1/fotos/")))
                .andExpect(jsonPath("$.propietaria.username").value("lucia_s"))
                .andExpect(jsonPath("$.propietaria.reputacion").value(5.0))
                .andExpect(jsonPath("$.fotos[0].claveDisco").doesNotExist());
    }

    @Test
    void prendaInexistenteDa404ConProblemDetail() throws Exception {
        mvc.perform(get("/api/v1/prendas/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso no encontrado"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.instance").value("/api/v1/prendas/9999"));
    }

    @Test
    void borradorSoloLoVeSuDuena() throws Exception {
        mvc.perform(get("/api/v1/prendas/20")).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/prendas/20").with(como(5))).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/prendas/20").with(como(6))).andExpect(status().isOk());
    }

    @Test
    void categoriasConConteo() throws Exception {
        mvc.perform(get("/api/v1/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6))
                .andExpect(jsonPath("$[0].disponibles").isNumber());
    }

    @Test
    void crearDevuelve201ConLocationYQuedaEnBorrador() throws Exception {
        mvc.perform(post("/api/v1/prendas").with(como(4))
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.disponibilidad").value("BORRADOR"))
                .andExpect(jsonPath("$.propietaria.username").value("lucia_s"));
    }

    @Test
    void crearConDatosInvalidosDa400ConListaDeCampos() throws Exception {
        mvc.perform(post("/api/v1/prendas").with(como(4))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"\",\"talla\":\"S\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores").isArray())
                .andExpect(jsonPath("$.errores[?(@.campo=='titulo')]").exists());
    }

    @Test
    void modificarPrendaAjenaDa403() throws Exception {
        mvc.perform(put("/api/v1/prendas/1").with(como(5))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"titulo\":\"Hackeada\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void modificarParcialSoloCambiaLoInformado() throws Exception {
        mvc.perform(put("/api/v1/prendas/1").with(como(4))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"titulo\":\"Vaquera 90s\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Vaquera 90s"))
                .andExpect(jsonPath("$.marca").value("Levi's"));
    }

    @Test
    void modificarPrendaPrestadaDa422() throws Exception {
        mvc.perform(put("/api/v1/prendas/3").with(como(5))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"titulo\":\"Otro\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void retirarDa204YDejaDeVerse() throws Exception {
        mvc.perform(delete("/api/v1/prendas/12").with(como(10))).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/prendas/12")).andExpect(status().isNotFound());
    }

    @Test
    void moderadorPuedeRetirarPrendaAjena() throws Exception {
        mvc.perform(delete("/api/v1/prendas/13").with(como(2, "MODERADOR")))
                .andExpect(status().isNoContent());
    }

    @Test
    void usuarioNormalNoPuedeRetirarPrendaAjena() throws Exception {
        mvc.perform(delete("/api/v1/prendas/13").with(como(5))).andExpect(status().isForbidden());
    }
}
