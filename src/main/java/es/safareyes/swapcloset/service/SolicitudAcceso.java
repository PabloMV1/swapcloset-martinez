package es.safareyes.swapcloset.service;

import es.safareyes.swapcloset.entity.Solicitud;
import es.safareyes.swapcloset.exception.ProhibidoException;
import es.safareyes.swapcloset.exception.RecursoNoEncontradoException;
import es.safareyes.swapcloset.repository.SolicitudRepository;
import es.safareyes.swapcloset.security.Actor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Regla común (RN-08): solo las dos personas de una solicitud pueden verla, escribir en su chat o valorarla. */
@Component
@RequiredArgsConstructor
public class SolicitudAcceso {

    private final SolicitudRepository solicitudes;

    public Solicitud cargarParaParticipante(Long solicitudId, Actor actor) {
        Solicitud solicitud = solicitudes.findConRelacionesById(solicitudId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la solicitud con id " + solicitudId));
        if (!esParticipante(solicitud, actor)) {
            throw new ProhibidoException("Solo las dos personas de la solicitud pueden acceder a ella");
        }
        return solicitud;
    }

    public boolean esSolicitante(Solicitud solicitud, Actor actor) {
        return solicitud.getSolicitante().getId().equals(actor.id());
    }

    public boolean esPropietaria(Solicitud solicitud, Actor actor) {
        return solicitud.getPropietaria().getId().equals(actor.id());
    }

    public boolean esParticipante(Solicitud solicitud, Actor actor) {
        return esSolicitante(solicitud, actor) || esPropietaria(solicitud, actor);
    }
}
