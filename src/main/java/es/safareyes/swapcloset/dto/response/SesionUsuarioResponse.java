package es.safareyes.swapcloset.dto.response;

import es.safareyes.swapcloset.entity.Rol;

public record SesionUsuarioResponse(Long id, String username, Rol rol) {
}
