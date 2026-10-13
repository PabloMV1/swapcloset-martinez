package es.safareyes.swapcloset.scheduler;

import es.safareyes.swapcloset.service.MantenimientoSolicitudesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** Dispara el mantenimiento diario. La hora sale de app.scheduling.cron-solicitudes (por defecto, las 08:00). */
@Slf4j
@Component
@RequiredArgsConstructor
public class SolicitudesScheduler {

    private final MantenimientoSolicitudesService mantenimiento;

    @Scheduled(cron = "${app.scheduling.cron-solicitudes}", zone = "Europe/Madrid")
    public void ejecutar() {
        try {
            mantenimiento.procesar(LocalDateTime.now());
        } catch (RuntimeException e) {
            // Una tarea programada nunca debe tirar la aplicación: se registra y se reintenta en la siguiente
            log.error("Ha fallado el mantenimiento de solicitudes", e);
        }
    }
}
