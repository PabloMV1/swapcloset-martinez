package es.safareyes.swapcloset.service;

import es.safareyes.swapcloset.dto.response.NotificacionResponse;
import es.safareyes.swapcloset.entity.Notificacion;
import es.safareyes.swapcloset.entity.TipoNotificacion;
import es.safareyes.swapcloset.exception.ProhibidoException;
import es.safareyes.swapcloset.exception.RecursoNoEncontradoException;
import es.safareyes.swapcloset.mapper.NotificacionMapper;
import es.safareyes.swapcloset.repository.NotificacionRepository;
import es.safareyes.swapcloset.repository.UsuarioRepository;
import es.safareyes.swapcloset.security.Actor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificacionServiceImpl implements NotificacionService {

    private final NotificacionRepository notificaciones;
    private final UsuarioRepository usuarios;
    private final NotificacionMapper mapper;

    @Override
    @Transactional
    public void notificar(Long usuarioId, TipoNotificacion tipo, String mensaje, String recurso, Long recursoId) {
        String texto = mensaje.length() > 255 ? mensaje.substring(0, 255) : mensaje;
        notificaciones.save(Notificacion.builder()
                .usuario(usuarios.getReferenceById(usuarioId))
                .tipo(tipo)
                .mensaje(texto)
                .recurso(recurso)
                .recursoId(recursoId)
                .build());
    }

    @Override
    @Transactional
    public boolean notificarUnaVez(Long usuarioId, TipoNotificacion tipo, String mensaje, String recurso, Long recursoId) {
        if (notificaciones.existsByUsuarioIdAndTipoAndRecursoId(usuarioId, tipo, recursoId)) {
            return false;
        }
        notificar(usuarioId, tipo, mensaje, recurso, recursoId);
        return true;
    }

    @Override
    public Page<NotificacionResponse> listar(Actor actor, Boolean leida, Pageable pageable) {
        Page<Notificacion> pagina = leida == null
                ? notificaciones.findByUsuarioId(actor.id(), pageable)
                : notificaciones.findByUsuarioIdAndLeida(actor.id(), leida, pageable);
        return pagina.map(mapper::toResponse);
    }

    @Override
    @Transactional
    public NotificacionResponse marcarLeida(Actor actor, Long id) {
        Notificacion notificacion = notificaciones.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la notificación con id " + id));
        if (!notificacion.getUsuario().getId().equals(actor.id())) {
            throw new ProhibidoException("Solo puedes marcar como leídas tus propias notificaciones");
        }
        notificacion.setLeida(true);
        return mapper.toResponse(notificacion);
    }
}
