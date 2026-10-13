package es.safareyes.swapcloset.service;

import es.safareyes.swapcloset.entity.TipoNotificacion;

public interface NotificacionService {

    /** Crea un aviso para una persona. Lo usan las solicitudes, el chat y las valoraciones. */
    void notificar(Long usuarioId, TipoNotificacion tipo, String mensaje, String recurso, Long recursoId);
}
