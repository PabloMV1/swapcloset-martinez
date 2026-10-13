package es.safareyes.swapcloset.service;

import es.safareyes.swapcloset.dto.request.ValoracionCrearRequest;
import es.safareyes.swapcloset.dto.response.ValoracionResponse;
import es.safareyes.swapcloset.security.Actor;

public interface ValoracionService {

    ValoracionResponse valorar(Actor actor, Long solicitudId, ValoracionCrearRequest request);
}
