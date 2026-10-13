package es.safareyes.swapcloset.service;

import es.safareyes.swapcloset.dto.request.PerfilActualizarRequest;
import es.safareyes.swapcloset.dto.response.PerfilAfinResponse;
import es.safareyes.swapcloset.dto.response.PerfilPublicoResponse;
import es.safareyes.swapcloset.dto.response.PerfilResponse;
import es.safareyes.swapcloset.security.Actor;

import java.util.List;

public interface UsuarioService {

    PerfilPublicoResponse obtenerPerfil(Long id);

    PerfilResponse actualizarMiPerfil(Actor actor, PerfilActualizarRequest request);

    List<PerfilAfinResponse> buscarAfines(Actor actor, int limite);
}
