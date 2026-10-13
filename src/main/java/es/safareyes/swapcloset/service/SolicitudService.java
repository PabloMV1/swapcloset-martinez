package es.safareyes.swapcloset.service;

import es.safareyes.swapcloset.dto.request.AccionSolicitud;
import es.safareyes.swapcloset.dto.request.SolicitudCrearRequest;
import es.safareyes.swapcloset.dto.request.VistaSolicitudes;
import es.safareyes.swapcloset.dto.response.SolicitudResponse;
import es.safareyes.swapcloset.entity.EstadoSolicitud;
import es.safareyes.swapcloset.security.Actor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SolicitudService {

    SolicitudResponse crear(Actor actor, SolicitudCrearRequest request);

    Page<SolicitudResponse> listar(Actor actor, VistaSolicitudes vista, EstadoSolicitud estado, Pageable pageable);

    SolicitudResponse obtener(Actor actor, Long id);

    SolicitudResponse cambiarEstado(Actor actor, Long id, AccionSolicitud accion);
}
