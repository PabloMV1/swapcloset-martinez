package es.safareyes.swapcloset.controller;

import es.safareyes.swapcloset.dto.request.LoginRequest;
import es.safareyes.swapcloset.dto.request.RegistroRequest;
import es.safareyes.swapcloset.dto.response.AuthResponse;
import es.safareyes.swapcloset.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@SecurityRequirements // estas dos rutas son públicas: sin candado en Swagger
@Tag(name = "Autenticación", description = "Registro y login (devuelven un token JWT)")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear una cuenta (rol USUARIO) y recibir ya el token",
            description = "Mayor de 16 años, email y usuario únicos, 1 a 5 estilos.")
    public AuthResponse registro(@Valid @RequestBody RegistroRequest request) {
        return authService.registrar(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión con usuario (o email) y contraseña",
            description = "Devuelve un token válido 60 minutos. Se envía como: Authorization: Bearer <token>")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
