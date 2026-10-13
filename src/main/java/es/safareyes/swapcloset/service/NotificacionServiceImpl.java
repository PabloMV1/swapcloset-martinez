package es.safareyes.swapcloset.service;

import es.safareyes.swapcloset.entity.Notificacion;
import es.safareyes.swapcloset.entity.TipoNotificacion;
import es.safareyes.swapcloset.repository.NotificacionRepository;
import es.safareyes.swapcloset.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificacionServiceImpl implements NotificacionService {

    private final NotificacionRepository notificaciones;
    private final UsuarioRepository usuarios;

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
}
