package es.safareyes.swapcloset.service;

import es.safareyes.swapcloset.entity.EstadoSolicitud;
import es.safareyes.swapcloset.entity.Solicitud;
import es.safareyes.swapcloset.entity.TipoNotificacion;
import es.safareyes.swapcloset.entity.TipoSolicitud;
import es.safareyes.swapcloset.repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MantenimientoSolicitudesServiceImpl implements MantenimientoSolicitudesService {

    private static final int DIAS_PARA_CADUCAR = 7;
    private static final String RECURSO = "SOLICITUD";

    private final SolicitudRepository solicitudes;
    private final NotificacionService notificaciones;

    /** Datos mínimos de un préstamo: se copian antes de la actualización masiva, que vacía el contexto de JPA. */
    private record Prestamo(Long id, String prenda, Long solicitanteId, Long propietariaId) {
        static Prestamo de(Solicitud s) {
            return new Prestamo(s.getId(), s.getPrenda().getTitulo(), s.getSolicitante().getId(),
                    s.getPropietaria().getId());
        }
    }

    @Override
    @Transactional
    public ResumenMantenimiento procesar(LocalDateTime ahora) {
        LocalDate hoy = ahora.toLocalDate();

        // 1) Avisar a las dos personas de los préstamos que vencen mañana
        int avisosVencimiento = 0;
        for (Prestamo p : copiar(solicitudes.findByTipoAndEstadoAndFechaFin(
                TipoSolicitud.PRESTAMO, EstadoSolicitud.ACEPTADA, hoy.plusDays(1)))) {
            String texto = "«" + p.prenda() + "» vence mañana";
            avisosVencimiento += avisar(p, TipoNotificacion.PRESTAMO_VENCE_MANANA, texto);
        }

        // 2) Los préstamos aceptados cuya fecha de fin ya pasó pasan a RETRASADA y se avisa
        List<Prestamo> vencidos = copiar(solicitudes.findByTipoAndEstadoAndFechaFinLessThan(
                TipoSolicitud.PRESTAMO, EstadoSolicitud.ACEPTADA, hoy));
        int retrasadas = solicitudes.marcarPrestamosRetrasados(TipoSolicitud.PRESTAMO, EstadoSolicitud.ACEPTADA,
                EstadoSolicitud.RETRASADA, hoy, ahora);
        int avisosRetraso = 0;
        for (Prestamo p : vencidos) {
            String texto = "El préstamo de «" + p.prenda() + "» está retrasado";
            avisosRetraso += avisar(p, TipoNotificacion.PRESTAMO_RETRASADO, texto);
        }

        // 3) Las solicitudes pendientes de más de 7 días caducan
        int caducadas = solicitudes.caducarPendientesAnterioresA(ahora.minusDays(DIAS_PARA_CADUCAR),
                EstadoSolicitud.PENDIENTE, EstadoSolicitud.CADUCADA, ahora);

        ResumenMantenimiento resumen = new ResumenMantenimiento(caducadas, retrasadas, avisosVencimiento, avisosRetraso);
        log.info("Mantenimiento de solicitudes: {}", resumen);
        return resumen;
    }

    private List<Prestamo> copiar(List<Solicitud> lista) {
        return lista.stream().map(Prestamo::de).toList();
    }

    /** Avisa a solicitante y propietaria, una sola vez por persona, tipo y solicitud. Devuelve cuántos avisos nuevos. */
    private int avisar(Prestamo p, TipoNotificacion tipo, String texto) {
        int nuevos = 0;
        if (notificaciones.notificarUnaVez(p.solicitanteId(), tipo, texto, RECURSO, p.id())) nuevos++;
        if (notificaciones.notificarUnaVez(p.propietariaId(), tipo, texto, RECURSO, p.id())) nuevos++;
        return nuevos;
    }
}
