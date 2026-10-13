package es.safareyes.swapcloset.repository;

import es.safareyes.swapcloset.entity.Notificacion;
import es.safareyes.swapcloset.entity.TipoNotificacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    /** Q6: mis notificaciones, paginadas. */
    Page<Notificacion> findByUsuarioId(Long usuarioId, Pageable pageable);

    /** Q6: filtradas por leída / no leída. */
    Page<Notificacion> findByUsuarioIdAndLeida(Long usuarioId, boolean leida, Pageable pageable);

    /** Q1: idempotencia de la tarea programada (no duplicar avisos). */
    boolean existsByUsuarioIdAndTipoAndRecursoId(Long usuarioId, TipoNotificacion tipo, Long recursoId);

    /** Q1 */
    long countByUsuarioIdAndLeidaFalse(Long usuarioId);
}
