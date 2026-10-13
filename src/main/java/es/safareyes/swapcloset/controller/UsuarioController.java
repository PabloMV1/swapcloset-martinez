package es.safareyes.swapcloset.controller;

import es.safareyes.swapcloset.dto.request.PerfilActualizarRequest;
import es.safareyes.swapcloset.dto.response.PerfilAfinResponse;
import es.safareyes.swapcloset.dto.response.PerfilPublicoResponse;
import es.safareyes.swapcloset.dto.response.PerfilResponse;
import es.safareyes.swapcloset.entity.Rol;
import es.safareyes.swapcloset.security.Actor;
import es.safareyes.swapcloset.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** TEMPORAL (Fase 3): el actor llega por cabeceras; en la Fase 4 saldrá del token JWT. */
@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Perfil, tallas, estilos y personas afines")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/afines")
    @Operation(summary = "Perfiles con más estilos en común y mejor reputación")
    public List<PerfilAfinResponse> afines(
            @RequestHeader("X-Usuario-Id") Long usuarioId,
            @RequestHeader(value = "X-Rol", defaultValue = "USUARIO") Rol rol,
            @RequestParam(defaultValue = "10") int limite) {
        return usuarioService.buscarAfines(new Actor(usuarioId, rol), limite);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Perfil público: estilos, tallas, reputación y prendas")
    public PerfilPublicoResponse perfil(
            @PathVariable Long id,
            @RequestHeader("X-Usuario-Id") Long usuarioId) {
        return usuarioService.obtenerPerfil(id);
    }

    @PutMapping("/me")
    @Operation(summary = "Editar mi perfil, tallas y estilos (solo cambian los campos informados)")
    public PerfilResponse actualizarMiPerfil(
            @RequestHeader("X-Usuario-Id") Long usuarioId,
            @RequestHeader(value = "X-Rol", defaultValue = "USUARIO") Rol rol,
            @Valid @RequestBody PerfilActualizarRequest request) {
        return usuarioService.actualizarMiPerfil(new Actor(usuarioId, rol), request);
    }
}
