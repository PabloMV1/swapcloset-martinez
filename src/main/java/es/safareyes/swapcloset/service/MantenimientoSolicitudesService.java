package es.safareyes.swapcloset.service;

import java.time.LocalDateTime;

public interface MantenimientoSolicitudesService {

    /**
     * Mantenimiento diario de las solicitudes. Se puede ejecutar varias veces seguidas sin duplicar nada:
     * cada paso solo actúa sobre lo que todavía no se ha procesado.
     */
    ResumenMantenimiento procesar(LocalDateTime ahora);
}
