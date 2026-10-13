package es.safareyes.swapcloset.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static es.safareyes.swapcloset.support.TestAuth.como;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Endpoints 19 y 20 (notificaciones) con los datos de prueba. Cada test se revierte. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class NotificacionControllerTest {

    @Autowired MockMvc mvc;

    @Test
    void listaSoloLasMias() throws Exception {
        // sofia_m (6) tiene 3 notificaciones de prueba, todas sin leer
        mvc.perform(get("/api/v1/notificaciones").with(como(6)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.content[0].mensaje").isString())
                .andExpect(jsonPath("$.content[0].leida").value(false));
    }

    @Test
    void filtraPorLeida() throws Exception {
        // javi_t (9) solo tiene una, y ya está leída
        mvc.perform(get("/api/v1/notificaciones?leida=true").with(como(9)))
                .andExpect(jsonPath("$.page.totalElements").value(1));
        mvc.perform(get("/api/v1/notificaciones?leida=false").with(como(9)))
                .andExpect(jsonPath("$.page.totalElements").value(0));
    }

    @Test
    void vienenOrdenadasDeLaMasRecienteALaMasAntigua() throws Exception {
        // la de hace 2 horas (vence mañana) va antes que la de hace 6 días (nuevo mensaje)
        mvc.perform(get("/api/v1/notificaciones").with(como(6)))
                .andExpect(jsonPath("$.content[0].tipo").value("PRESTAMO_VENCE_MANANA"))
                .andExpect(jsonPath("$.content[2].tipo").value("NUEVO_MENSAJE"));
    }

    @Test
    void sinTokenDa401() throws Exception {
        mvc.perform(get("/api/v1/notificaciones")).andExpect(status().isUnauthorized());
        mvc.perform(patch("/api/v1/notificaciones/1/leida")).andExpect(status().isUnauthorized());
    }

    @Test
    void marcarComoLeidaCambiaElFiltro() throws Exception {
        // pablo_v (7) tiene dos sin leer; la 1 es suya
        mvc.perform(get("/api/v1/notificaciones?leida=false").with(como(7)))
                .andExpect(jsonPath("$.page.totalElements").value(2));
        mvc.perform(patch("/api/v1/notificaciones/1/leida").with(como(7)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leida").value(true));
        mvc.perform(get("/api/v1/notificaciones?leida=false").with(como(7)))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    void marcarDosVecesEsInofensivo() throws Exception {
        mvc.perform(patch("/api/v1/notificaciones/1/leida").with(como(7))).andExpect(status().isOk());
        mvc.perform(patch("/api/v1/notificaciones/1/leida").with(como(7))).andExpect(status().isOk());
    }

    @Test
    void noSePuedeMarcarLaDeOtraPersona() throws Exception {
        mvc.perform(patch("/api/v1/notificaciones/1/leida").with(como(6))).andExpect(status().isForbidden());
    }

    @Test
    void notificacionInexistenteDa404() throws Exception {
        mvc.perform(patch("/api/v1/notificaciones/99999/leida").with(como(6))).andExpect(status().isNotFound());
    }
}
