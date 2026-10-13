package es.safareyes.swapcloset.security;

import es.safareyes.swapcloset.repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Comprobaciones reutilizables desde @PreAuthorize, por ejemplo: @PreAuthorize("@seguridad.esParticipante(#id, #actor)"). */
@Component("seguridad")
@RequiredArgsConstructor
public class SeguridadExpresiones {

    private final SolicitudRepository solicitudes;

    /**
     * RN-08: solo las dos personas de la solicitud. Si la solicitud no existe devuelve true para que
     * el servicio responda con un 404 (y no con un 403 que delataría qué ids existen).
     */
    public boolean esParticipante(Long solicitudId, Actor actor) {
        if (actor == null || solicitudId == null) {
            return false;
        }
        return !solicitudes.existsById(solicitudId) || solicitudes.participa(solicitudId, actor.id());
    }
}
