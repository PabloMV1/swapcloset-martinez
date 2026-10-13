package es.safareyes.swapcloset.support;

import es.safareyes.swapcloset.entity.Rol;
import es.safareyes.swapcloset.security.Actor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

/** Atajo para los tests: hace la petición "como" una persona ya identificada, sin pasar por el login. */
public final class TestAuth {

    private TestAuth() {
    }

    public static RequestPostProcessor como(long usuarioId) {
        return como(usuarioId, "USUARIO");
    }

    public static RequestPostProcessor como(long usuarioId, String rol) {
        Rol r = Rol.valueOf(rol);
        return authentication(new UsernamePasswordAuthenticationToken(new Actor(usuarioId, r), null,
                List.of(new SimpleGrantedAuthority("ROLE_" + r.name()))));
    }
}
