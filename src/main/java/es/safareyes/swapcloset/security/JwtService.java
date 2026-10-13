package es.safareyes.swapcloset.security;

import es.safareyes.swapcloset.entity.Rol;
import es.safareyes.swapcloset.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/** Crea y lee los tokens JWT. El token lleva el id, el nombre y el rol de la persona. */
@Service
public class JwtService {

    private final SecretKey clave;
    private final Duration duracion;

    public JwtService(@Value("${app.jwt.secret}") String secreto,
                      @Value("${app.jwt.expiration-minutes}") long minutos) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8)); // exige >= 32 caracteres
        this.duracion = Duration.ofMinutes(minutos);
    }

    public String generar(Usuario usuario) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(usuario.getId()))
                .claim("username", usuario.getUsername())
                .claim("rol", usuario.getRol().name())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(duracion)))
                .signWith(clave)
                .compact();
    }

    /** Devuelve quién es el dueño del token, o vacío si está caducado, manipulado o mal formado. */
    public Optional<Actor> leer(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
            Long id = Long.valueOf(claims.getSubject());
            Rol rol = Rol.valueOf(claims.get("rol", String.class));
            return Optional.of(new Actor(id, rol));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public long segundosDeValidez() {
        return duracion.toSeconds();
    }
}
