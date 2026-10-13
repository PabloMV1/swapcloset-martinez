package es.safareyes.swapcloset.service;

import es.safareyes.swapcloset.dto.request.MensajeCrearRequest;
import es.safareyes.swapcloset.dto.response.MensajeResponse;
import es.safareyes.swapcloset.entity.Mensaje;
import es.safareyes.swapcloset.entity.Solicitud;
import es.safareyes.swapcloset.entity.TipoNotificacion;
import es.safareyes.swapcloset.entity.Usuario;
import es.safareyes.swapcloset.mapper.SolicitudMapper;
import es.safareyes.swapcloset.repository.MensajeRepository;
import es.safareyes.swapcloset.security.Actor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MensajeServiceImpl implements MensajeService {

    private final MensajeRepository mensajes;
    private final SolicitudAcceso acceso;
    private final NotificacionService notificaciones;
    private final SolicitudMapper mapper;

    // ------------------------------------------------------------------ endpoint 13
    @Override
    @Transactional
    public MensajeResponse enviar(Actor actor, Long solicitudId, MensajeCrearRequest request) {
        Solicitud solicitud = acceso.cargarParaParticipante(solicitudId, actor);
        boolean escribeSolicitante = acceso.esSolicitante(solicitud, actor);
        Usuario autor = escribeSolicitante ? solicitud.getSolicitante() : solicitud.getPropietaria();
        Usuario destinataria = escribeSolicitante ? solicitud.getPropietaria() : solicitud.getSolicitante();

        Mensaje guardado = mensajes.save(Mensaje.builder()
                .solicitud(solicitud)
                .autor(autor)
                .texto(request.texto().trim())
                .build());
        notificaciones.notificar(destinataria.getId(), TipoNotificacion.NUEVO_MENSAJE,
                "Nuevo mensaje de " + autor.getUsername() + " en la solicitud " + solicitudId,
                "SOLICITUD", solicitudId);
        return mapper.toMensaje(guardado);
    }

    // ------------------------------------------------------------------ endpoint 14
    @Override
    public Page<MensajeResponse> listar(Actor actor, Long solicitudId, Pageable pageable) {
        acceso.cargarParaParticipante(solicitudId, actor); // RN-08: 404 si no existe, 403 si no participa
        return mensajes.findBySolicitudId(solicitudId, pageable).map(mapper::toMensaje);
    }
}
