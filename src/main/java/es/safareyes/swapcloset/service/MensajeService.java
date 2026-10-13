package es.safareyes.swapcloset.service;

import es.safareyes.swapcloset.dto.request.MensajeCrearRequest;
import es.safareyes.swapcloset.dto.response.MensajeResponse;
import es.safareyes.swapcloset.security.Actor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MensajeService {

    MensajeResponse enviar(Actor actor, Long solicitudId, MensajeCrearRequest request);

    Page<MensajeResponse> listar(Actor actor, Long solicitudId, Pageable pageable);
}
