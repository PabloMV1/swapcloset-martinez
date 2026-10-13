package es.safareyes.swapcloset.controller;

import es.safareyes.swapcloset.dto.request.PerfilActualizarRequest;
import es.safareyes.swapcloset.dto.response.PerfilAfinResponse;
import es.safareyes.swapcloset.dto.response.PerfilPublicoResponse;
import es.safareyes.swapcloset.dto.response.PerfilResponse;
import es.safareyes.swapcloset.security.Actor;
import es.safareyes.swapcloset.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Perfil, tallas, estilos y personas afines")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/afines")
    @Operation(summary = "Perfiles con más estilos en común y mejor reputación")
    public List<PerfilAfinResponse> afines(
            @AuthenticationPrincipal Actor actor,
            @RequestParam(defaultValue = "10") int limite) {
        return usuarioService.buscarAfines(actor, limite);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Perfil público: estilos, tallas, reputación y prendas")
    public PerfilPublicoResponse perfil(
            @PathVariable Long id) {
        return usuarioService.obtenerPerfil(id);
    }

    @PutMapping("/me")
    @Operation(summary = "Editar mi perfil, tallas y estilos (solo cambian los campos informados)")
    public PerfilResponse actualizarMiPerfil(
            @AuthenticationPrincipal Actor actor,
            @Valid @RequestBody PerfilActualizarRequest request) {
        return usuarioService.actualizarMiPerfil(actor, request);
    }
}
