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

/** Endpoint 15 (valoraciones) y endpoint 16 (informe). Cada test se revierte. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ValoracionControllerTest {

    @Autowired MockMvc mvc;

    private org.springframework.test.web.servlet.ResultActions valorar(long usuario, long solicitud, String cuerpo)
            throws Exception {
        return mvc.perform(post("/api/v1/solicitudes/" + solicitud + "/valoraciones").with(como(usuario))
                .contentType(MediaType.APPLICATION_JSON).content(cuerpo));
    }

    @Test
    void valorarTrasFinalizarDa201() throws Exception {
        mvc.perform(patch("/api/v1/solicitudes/4/estado").with(como(7))
                .contentType(MediaType.APPLICATION_JSON).content("{\"accion\":\"FINALIZAR\"}"))
                .andExpect(status().isOk());
        valorar(6, 4, "{\"puntuacion\":5,\"comentario\":\"Genial\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.autor.username").value("sofia_m"))
                .andExpect(jsonPath("$.valorado.username").value("pablo_v"))
                .andExpect(jsonPath("$.puntuacion").value(5));
    }

    @Test
    void valorarUnaSolicitudSinFinalizarDa422() throws Exception {
        valorar(9, 3, "{\"puntuacion\":4}").andExpect(status().isUnprocessableEntity());
    }

    @Test
    void valorarDosVecesDa409() throws Exception {
        // marta_l (10) ya valoró la solicitud 7 en los datos de prueba
        valorar(10, 7, "{\"puntuacion\":3}").andExpect(status().isConflict());
    }

    @Test
    void valorarSinSerParticipanteDa403() throws Exception {
        valorar(12, 7, "{\"puntuacion\":3}").andExpect(status().isForbidden());
    }

    @Test
    void puntuacionFueraDeRangoDa400() throws Exception {
        valorar(10, 7, "{\"puntuacion\":6}").andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------- informe (endpoint 16)

    @Test
    void informeParaLaAdministracion() throws Exception {
        mvc.perform(get("/api/v1/informes/actividad").with(como(1, "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.porMes").isArray())
                .andExpect(jsonPath("$.porMes[0].categoria").isString())
                .andExpect(jsonPath("$.porCategoria[0].solicitudes").isNumber())
                .andExpect(jsonPath("$.ranking.length()").value(5))
                .andExpect(jsonPath("$.ranking[0].username").isString());
    }

    @Test
    void informeAcotaElRanking() throws Exception {
        mvc.perform(get("/api/v1/informes/actividad?limite=2").with(como(2, "MODERADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ranking.length()").value(2));
    }

    @Test
    void informeNoEsParaUsuariasNormales() throws Exception {
        mvc.perform(get("/api/v1/informes/actividad").with(como(4)))
                .andExpect(status().isForbidden());
    }

    @Test
    void informeConFechasInvertidasDa422() throws Exception {
        mvc.perform(get("/api/v1/informes/actividad?desde=2026-02-01&hasta=2026-01-01")
                        .with(como(1, "ADMIN")))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void informeConFechaMalFormadaDa400() throws Exception {
        mvc.perform(get("/api/v1/informes/actividad?desde=ayer").with(como(1, "ADMIN")))
                .andExpect(status().isBadRequest());
    }
}
