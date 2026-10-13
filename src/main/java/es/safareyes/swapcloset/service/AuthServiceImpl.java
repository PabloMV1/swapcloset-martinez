package es.safareyes.swapcloset.service;

import es.safareyes.swapcloset.dto.request.LoginRequest;
import es.safareyes.swapcloset.dto.request.RegistroRequest;
import es.safareyes.swapcloset.dto.response.AuthResponse;
import es.safareyes.swapcloset.dto.response.SesionUsuarioResponse;
import es.safareyes.swapcloset.entity.Estilo;
import es.safareyes.swapcloset.entity.Rol;
import es.safareyes.swapcloset.entity.Usuario;
import es.safareyes.swapcloset.exception.ConflictoException;
import es.safareyes.swapcloset.exception.ReglaNegocioException;
import es.safareyes.swapcloset.repository.EstiloRepository;
import es.safareyes.swapcloset.repository.UsuarioRepository;
import es.safareyes.swapcloset.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarios;
    private final EstiloRepository estilos;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // ------------------------------------------------------------------ auth 1
    @Override
    @Transactional
    public AuthResponse registrar(RegistroRequest r) {
        String username = r.username().trim();
        String email = r.email().trim();
        if (usuarios.existsByUsernameIgnoreCase(username)) {
            throw new ConflictoException("Ya existe una cuenta con ese nombre de usuario"); // RN-01
        }
        if (usuarios.existsByEmailIgnoreCase(email)) {
            throw new ConflictoException("Ya existe una cuenta con ese email"); // RN-01
        }
        List<Estilo> encontrados = estilos.findAllById(r.estilosIds());
        if (encontrados.size() != r.estilosIds().size()) {
            throw new ReglaNegocioException("Alguno de los estilos indicados no existe"); // RN-02
        }

        Usuario nuevo = usuarios.save(Usuario.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(r.password()))   // nunca se guarda en claro
                .fechaNacimiento(r.fechaNacimiento())
                .ciudad(r.ciudad())
                .tallaCamiseta(r.tallaCamiseta())
                .tallaPantalon(r.tallaPantalon())
                .tallaCalzado(r.tallaCalzado())
                .rol(Rol.USUARIO)                                 // el rol nunca lo elige quien se registra
                .estilos(new HashSet<>(encontrados))
                .build());
        log.info("Usuario {} registrado", nuevo.getId());
        return respuesta(nuevo);
    }

    // ------------------------------------------------------------------ auth 2
    @Override
    public AuthResponse login(LoginRequest r) {
        String identificador = r.username().trim();
        Optional<Usuario> encontrado = identificador.contains("@")
                ? usuarios.findByEmailIgnoreCase(identificador)
                : usuarios.findByUsernameIgnoreCase(identificador);

        // Mismo mensaje si no existe o si la contraseña falla: no se revela qué cuentas existen
        Usuario usuario = encontrado
                .filter(u -> passwordEncoder.matches(r.password(), u.getPassword()))
                .orElseThrow(() -> new BadCredentialsException("Usuario o contraseña incorrectos"));
        log.info("Login correcto del usuario {}", usuario.getId());
        return respuesta(usuario);
    }

    private AuthResponse respuesta(Usuario u) {
        return new AuthResponse(jwtService.generar(u), "Bearer", jwtService.segundosDeValidez(),
                new SesionUsuarioResponse(u.getId(), u.getUsername(), u.getRol()));
    }
}
