package es.safareyes.swapcloset.security;

import es.safareyes.swapcloset.entity.Rol;

/**
 * Quién hace la petición. En la Fase 3 llega por cabeceras X-Usuario-Id y X-Rol (solo para probar);
 * en la Fase 4 se construye a partir del token JWT y los servicios no cambian.
 */
public record Actor(Long id, Rol rol) {

    public boolean esStaff() {
        return rol == Rol.MODERADOR || rol == Rol.ADMIN;
    }
}
