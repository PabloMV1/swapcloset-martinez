package es.safareyes.swapcloset.service;

import es.safareyes.swapcloset.entity.EstadoSolicitud;
import es.safareyes.swapcloset.repository.NotificacionRepository;
import es.safareyes.swapcloset.repository.SolicitudRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** La tarea programada contra los datos de prueba (solicitudes 1, 4, 5, 6 y 11 preparadas para ella). */
@SpringBootTest
@Transactional
class MantenimientoSolicitudesTest {

    @Autowired MantenimientoSolicitudesService mantenimiento;
    @Autowired SolicitudRepository solicitudes;
    @Autowired NotificacionRepository notificaciones;

    @Test
    void caducaLasPendientesAntiguasYMarcaLosPrestamosVencidos() {
        ResumenMantenimiento resumen = mantenimiento.procesar(LocalDateTime.now());

        assertEquals(1, resumen.caducadas());      // la solicitud 1 era pendiente desde hacía 10 días
        assertEquals(1, resumen.retrasadas());     // la solicitud 5 venció hace 3 días
        assertEquals(2, resumen.avisosRetraso());  // avisa a las dos personas del préstamo 5
        assertEquals(EstadoSolicitud.CADUCADA, solicitudes.findById(1L).orElseThrow().getEstado());
        assertEquals(EstadoSolicitud.RETRASADA, solicitudes.findById(5L).orElseThrow().getEstado());
        assertEquals(EstadoSolicitud.PENDIENTE, solicitudes.findById(2L).orElseThrow().getEstado()); // reciente: no se toca
        assertEquals(EstadoSolicitud.ACEPTADA, solicitudes.findById(4L).orElseThrow().getEstado());  // vence mañana: sigue aceptada
    }

    @Test
    void esIdempotenteLaSegundaEjecucionNoHaceNada() {
        mantenimiento.procesar(LocalDateTime.now());
        long avisosTrasLaPrimera = notificaciones.count();

        ResumenMantenimiento segunda = mantenimiento.procesar(LocalDateTime.now());

        assertEquals(new ResumenMantenimiento(0, 0, 0, 0), segunda);
        assertEquals(avisosTrasLaPrimera, notificaciones.count());
    }

    @Test
    void noDuplicaLosAvisosQueYaExistenEnLosDatosDePrueba() {
        long antes = notificaciones.count();
        mantenimiento.procesar(LocalDateTime.now());
        // solo se crean los 2 avisos de retraso de la solicitud 5: los de la 4 (vence mañana) y la 6 ya existían
        assertEquals(antes + 2, notificaciones.count());
    }
}
