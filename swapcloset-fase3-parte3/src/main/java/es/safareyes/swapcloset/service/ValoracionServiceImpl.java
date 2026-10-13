package es.safareyes.swapcloset.service;

import es.safareyes.swapcloset.dto.request.ValoracionCrearRequest;
import es.safareyes.swapcloset.dto.response.ValoracionResponse;
import es.safareyes.swapcloset.entity.EstadoSolicitud;
import es.safareyes.swapcloset.entity.Solicitud;
import es.safareyes.swapcloset.entity.TipoNotificacion;
import es.safareyes.swapcloset.entity.Usuario;
import es.safareyes.swapcloset.entity.Valoracion;
import es.safareyes.swapcloset.exception.ConflictoException;
import es.safareyes.swapcloset.exception.ReglaNegocioException;
import es.safareyes.swapcloset.mapper.SolicitudMapper;
import es.safareyes.swapcloset.repository.ValoracionRepository;
import es.safareyes.swapcloset.security.Actor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ValoracionServiceImpl implements ValoracionService {

    private final ValoracionRepository valoraciones;
    private final SolicitudAcceso acceso;
    private final NotificacionService notificaciones;
    private final SolicitudMapper mapper;

    // ------------------------------------------------------------------ endpoint 15
    @Override
    @Transactional
    public ValoracionResponse valorar(Actor actor, Long solicitudId, ValoracionCrearRequest request) {
        Solicitud solicitud = acceso.cargarParaParticipante(solicitudId, actor);
        // RN-09: solo en solicitudes finalizadas y una sola vez por participante
        if (solicitud.getEstado() != EstadoSolicitud.FINALIZADA) {
            throw new ReglaNegocioException("Solo se puede valorar una solicitud FINALIZADA");
        }
        if (valoraciones.existsBySolicitudIdAndAutorId(solicitudId, actor.id())) {
            throw new ConflictoException("Ya has valorado esta solicitud");
        }
        boolean valoraElSolicitante = acceso.esSolicitante(solicitud, actor);
        Usuario autor = valoraElSolicitante ? solicitud.getSolicitante() : solicitud.getPropietaria();
        Usuario valorado = valoraElSolicitante ? solicitud.getPropietaria() : solicitud.getSolicitante();

        String comentario = request.comentario() == null || request.comentario().isBlank()
                ? null : request.comentario().trim();
        Valoracion guardada = valoraciones.save(Valoracion.builder()
                .solicitud(solicitud)
                .autor(autor)
                .valorado(valorado)
                .puntuacion(request.puntuacion())
                .comentario(comentario)
                .build());
        notificaciones.notificar(valorado.getId(), TipoNotificacion.NUEVA_VALORACION,
                autor.getUsername() + " te ha valorado con " + request.puntuacion() + "/5",
                "SOLICITUD", solicitudId);
        return mapper.toValoracion(guardada);
    }
}
