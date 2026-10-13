package es.safareyes.swapcloset.controller;

import es.safareyes.swapcloset.entity.Disponibilidad;
import es.safareyes.swapcloset.entity.Prenda;
import es.safareyes.swapcloset.repository.NotificacionRepository;
import es.safareyes.swapcloset.repository.PrendaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Endpoints 10-12 (solicitudes) contra la base real con los datos de prueba. Cada test se revierte. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SolicitudControllerTest {

    @Autowired MockMvc mvc;
    @Autowired PrendaRepository prendas;
    @Autowired NotificacionRepository notificaciones;

    private static String prestamo(long prendaId, int diasInicio, int diasFin) {
        return """
                {"tipo":"PRESTAMO","prendaId":%d,"fechaInicio":"%s","fechaFin":"%s"}
                """.formatted(prendaId, LocalDate.now().plusDays(diasInicio), LocalDate.now().plusDays(diasFin));
    }

    private static String intercambio(long prendaId, Long ofrecidaId) {
        String ofrecida = ofrecidaId == null ? "" : ",\"prendaOfrecidaId\":" + ofrecidaId;
        return "{\"tipo\":\"INTERCAMBIO\",\"prendaId\":" + prendaId + ofrecida + "}";
    }

    private org.springframework.test.web.servlet.ResultActions post(long usuario, String cuerpo) throws Exception {
        return mvc.perform(post("/api/v1/solicitudes").header("X-Usuario-Id", usuario)
                .contentType(MediaType.APPLICATION_JSON).content(cuerpo));
    }

    private org.springframework.test.web.servlet.ResultActions accion(long usuario, long solicitud, String accion)
            throws Exception {
        return mvc.perform(patch("/api/v1/solicitudes/" + solicitud + "/estado").header("X-Usuario-Id", usuario)
                .contentType(MediaType.APPLICATION_JSON).content("{\"accion\":\"" + accion + "\"}"));
    }

    // ---------------------------------------------------------------- crear (endpoint 10)

    @Test
    void crearPrestamoDevuelve201() throws Exception {
        post(4, prestamo(12, 1, 8))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.tipo").value("PRESTAMO"))
                .andExpect(jsonPath("$.solicitante.username").value("lucia_s"))
                .andExpect(jsonPath("$.propietaria.username").value("marta_l"))
                .andExpect(jsonPath("$.prendaOfrecida").doesNotExist());
    }

    @Test
    void crearIntercambioDevuelve201YAvisaALaPropietaria() throws Exception {
        long antes = notificaciones.countByUsuarioIdAndLeidaFalse(4);
        post(9, intercambio(2, 10))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("INTERCAMBIO"))
                .andExpect(jsonPath("$.prendaOfrecida.id").value(10))
                .andExpect(jsonPath("$.fechaInicio").doesNotExist());
        assertEquals(antes + 1, notificaciones.countByUsuarioIdAndLeidaFalse(4));
    }

    @Test
    void solicitarPrendaPropiaDa422() throws Exception {
        post(4, prestamo(1, 1, 8)).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void segundaSolicitudPendienteDeLaMismaPrendaDa409() throws Exception {
        // la solicitud 2 de ana_g (8) sobre la prenda 12 ya está pendiente
        post(8, prestamo(12, 1, 8)).andExpect(status().isConflict());
    }

    @Test
    void prendaNoDisponibleDa422() throws Exception {
        post(4, prestamo(3, 1, 8)).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void modalidadIncompatibleDa422() throws Exception {
        // la prenda 11 solo se presta; no admite intercambio
        post(4, intercambio(11, 2)).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void cuartoPrestamoActivoDa422() throws Exception {
        // sofia_m (6) ya tiene 3 préstamos activos
        post(6, prestamo(17, 1, 8)).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void prestamoDeMasDe30DiasDa422() throws Exception {
        post(4, prestamo(12, 1, 40)).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void intercambioSinPrendaOfrecidaDa422() throws Exception {
        post(9, intercambio(2, null)).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void sinCabeceraDeUsuarioDa400() throws Exception {
        mvc.perform(post("/api/v1/solicitudes").contentType(MediaType.APPLICATION_JSON)
                .content(prestamo(12, 1, 8))).andExpect(status().isBadRequest());
    }

    @Test
    void cuerpoSinTipoDa400() throws Exception {
        post(4, "{\"prendaId\":12}").andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------- listar (endpoint 11)

    @Test
    void listaLasEnviadas() throws Exception {
        mvc.perform(get("/api/v1/solicitudes?vista=ENVIADAS").header("X-Usuario-Id", 6))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(3));
    }

    @Test
    void listaLasRecibidasFiltradasPorEstado() throws Exception {
        mvc.perform(get("/api/v1/solicitudes?vista=RECIBIDAS&estado=ACEPTADA").header("X-Usuario-Id", 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(4));
    }

    // ---------------------------------------------------------------- estados (endpoint 12)

    @Test
    void aceptarIntercambioReservaLasDosPrendas() throws Exception {
        accion(7, 1, "ACEPTAR")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACEPTADA"));
        assertEquals(Disponibilidad.RESERVADA, prendas.findById(6L).orElseThrow().getDisponibilidad());
        assertEquals(Disponibilidad.RESERVADA, prendas.findById(18L).orElseThrow().getDisponibilidad());
    }

    @Test
    void aceptarRechazaLasDemasPendientesDeLaPrenda() throws Exception {
        post(4, prestamo(12, 1, 8)).andExpect(status().isCreated());   // lucia_s compite con ana_g por la prenda 12
        accion(10, 2, "ACEPTAR").andExpect(status().isOk());           // marta_l acepta la de ana_g
        mvc.perform(get("/api/v1/solicitudes?vista=ENVIADAS&estado=RECHAZADA").header("X-Usuario-Id", 4))
                .andExpect(jsonPath("$.content[?(@.prenda.id==12)]").isNotEmpty());
        assertEquals(Disponibilidad.PRESTADA, prendas.findById(12L).orElseThrow().getDisponibilidad());
    }

    @Test
    void aceptarSiendoLaSolicitanteDa403() throws Exception {
        accion(8, 2, "ACEPTAR").andExpect(status().isForbidden());
    }

    @Test
    void aceptarSiendoAjenaDa403() throws Exception {
        accion(4, 2, "ACEPTAR").andExpect(status().isForbidden());
    }

    @Test
    void aceptarUnaFinalizadaDa422() throws Exception {
        accion(4, 7, "ACEPTAR").andExpect(status().isUnprocessableEntity());
    }

    @Test
    void solicitudInexistenteDa404() throws Exception {
        accion(4, 9999, "ACEPTAR").andExpect(status().isNotFound());
    }

    @Test
    void accionDesconocidaDa400() throws Exception {
        accion(10, 2, "VOLAR").andExpect(status().isBadRequest());
    }

    @Test
    void rechazarAvisaYCambiaElEstado() throws Exception {
        accion(10, 2, "RECHAZAR")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RECHAZADA"));
    }

    @Test
    void cancelarSoloLaSolicitante() throws Exception {
        accion(8, 2, "CANCELAR")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));
    }

    @Test
    void cancelarSiendoLaPropietariaDa403() throws Exception {
        accion(10, 2, "CANCELAR").andExpect(status().isForbidden());
    }

    @Test
    void finalizarPrestamoLiberaLaPrenda() throws Exception {
        accion(7, 4, "FINALIZAR")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("FINALIZADA"));
        assertEquals(Disponibilidad.DISPONIBLE, prendas.findById(7L).orElseThrow().getDisponibilidad());
    }

    @Test
    void finalizarPrestamoSiendoLaSolicitanteDa403() throws Exception {
        accion(6, 4, "FINALIZAR").andExpect(status().isForbidden());
    }

    @Test
    void finalizarIntercambioCambiaDeDuena() throws Exception {
        accion(9, 3, "FINALIZAR").andExpect(status().isOk());
        Prenda zapatillas = prendas.findById(4L).orElseThrow();
        Prenda polo = prendas.findById(21L).orElseThrow();
        assertEquals(9L, zapatillas.getPropietaria().getId());
        assertEquals(5L, polo.getPropietaria().getId());
        assertEquals(Disponibilidad.DISPONIBLE, zapatillas.getDisponibilidad());
        assertEquals(Disponibilidad.DISPONIBLE, polo.getDisponibilidad());
    }

    @Test
    void finalizarUnPrestamoRetrasado() throws Exception {
        accion(8, 6, "FINALIZAR")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("FINALIZADA"));
    }

    @Test
    void detalleSoloParaParticipantes() throws Exception {
        mvc.perform(get("/api/v1/solicitudes/3").header("X-Usuario-Id", 9)).andExpect(status().isOk());
        mvc.perform(get("/api/v1/solicitudes/3").header("X-Usuario-Id", 12)).andExpect(status().isForbidden());
    }
}
