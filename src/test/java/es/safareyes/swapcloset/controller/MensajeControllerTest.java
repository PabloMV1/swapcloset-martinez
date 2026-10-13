package es.safareyes.swapcloset.controller;

import es.safareyes.swapcloset.repository.NotificacionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Endpoints 13-14 (chat). Cada test se revierte. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MensajeControllerTest {

    @Autowired MockMvc mvc;
    @Autowired NotificacionRepository notificaciones;

    private org.springframework.test.web.servlet.ResultActions enviar(long usuario, long solicitud, String cuerpo)
            throws Exception {
        return mvc.perform(post("/api/v1/solicitudes/" + solicitud + "/mensajes").header("X-Usuario-Id", usuario)
                .contentType(MediaType.APPLICATION_JSON).content(cuerpo));
    }

    @Test
    void laConversacionSeListaEnOrdenYPaginada() throws Exception {
        mvc.perform(get("/api/v1/solicitudes/3/mensajes").header("X-Usuario-Id", 9))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.content[0].autor.username").value("javi_t"))
                .andExpect(jsonPath("$.content[1].autor.username").value("carlos_r"));
    }

    @Test
    void unaPersonaAjenaNoVeLaConversacion() throws Exception {
        mvc.perform(get("/api/v1/solicitudes/3/mensajes").header("X-Usuario-Id", 12))
                .andExpect(status().isForbidden());
    }

    @Test
    void laModeracionTampocoEntraEnElChat() throws Exception {
        mvc.perform(get("/api/v1/solicitudes/3/mensajes").header("X-Usuario-Id", 1).header("X-Rol", "ADMIN"))
                .andExpect(status().isForbidden());
    }

    @Test
    void solicitudInexistenteDa404() throws Exception {
        mvc.perform(get("/api/v1/solicitudes/9999/mensajes").header("X-Usuario-Id", 9))
                .andExpect(status().isNotFound());
    }

    @Test
    void enviarMensajeDa201YAvisaALaOtraPersona() throws Exception {
        long antes = notificaciones.countByUsuarioIdAndLeidaFalse(9L);
        enviar(5, 3, "{\"texto\":\"Hasta el sábado\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.autor.username").value("carlos_r"))
                .andExpect(jsonPath("$.texto").value("Hasta el sábado"));
        assertEquals(antes + 1, notificaciones.countByUsuarioIdAndLeidaFalse(9L));
    }

    @Test
    void mensajeEnBlancoDa400() throws Exception {
        enviar(5, 3, "{\"texto\":\"   \"}").andExpect(status().isBadRequest());
    }

    @Test
    void mensajeDeMasDe1000CaracteresDa400() throws Exception {
        enviar(5, 3, "{\"texto\":\"" + "a".repeat(1001) + "\"}").andExpect(status().isBadRequest());
    }

    @Test
    void personaAjenaNoPuedeEscribir() throws Exception {
        enviar(12, 3, "{\"texto\":\"hola\"}").andExpect(status().isForbidden());
    }
}

