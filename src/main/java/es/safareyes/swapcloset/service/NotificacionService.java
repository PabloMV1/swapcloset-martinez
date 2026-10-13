package es.safareyes.swapcloset.service;

import es.safareyes.swapcloset.dto.response.NotificacionResponse;
import es.safareyes.swapcloset.entity.TipoNotificacion;
import es.safareyes.swapcloset.security.Actor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificacionService {

    /** Crea un aviso para una persona. Lo usan las solicitudes, el chat y las valoraciones. */
    void notificar(Long usuarioId, TipoNotificacion tipo, String mensaje, String recurso, Long recursoId);

    /**
     * Crea el aviso solo si esa persona no tiene ya uno del mismo tipo para ese recurso.
     * Lo usa la tarea programada para ser idempotente. Devuelve true si lo ha creado.
     */
    boolean notificarUnaVez(Long usuarioId, TipoNotificacion tipo, String mensaje, String recurso, Long recursoId);

    /** Endpoint 19: mis notificaciones, paginadas; leida = null las trae todas. */
    Page<NotificacionResponse> listar(Actor actor, Boolean leida, Pageable pageable);

    /** Endpoint 20: marcar una notificación propia como leída (idempotente). */
    NotificacionResponse marcarLeida(Actor actor, Long id);
}
