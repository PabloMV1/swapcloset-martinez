package es.safareyes.swapcloset.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Lee la cabecera "Authorization: Bearer ...". Si el token es válido, deja a la persona identificada
 * (el principal es un {@link Actor}). Si no hay token o no vale, no hace nada y la petición sigue sin
 * identificar: Spring Security responderá 401 en las rutas protegidas.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecera != null && cabecera.startsWith(PREFIJO)) {
            jwtService.leer(cabecera.substring(PREFIJO.length()).trim()).ifPresent(actor -> {
                var autenticacion = new UsernamePasswordAuthenticationToken(actor, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + actor.rol().name())));
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            });
        }
        chain.doFilter(request, response);
    }
}
