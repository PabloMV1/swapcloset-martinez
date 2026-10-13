package es.safareyes.swapcloset.controller;

import es.safareyes.swapcloset.dto.response.InformeActividadResponse;
import es.safareyes.swapcloset.entity.Rol;
import es.safareyes.swapcloset.security.Actor;
import es.safareyes.swapcloset.service.InformeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/** TEMPORAL (Fase 3): el actor llega por cabeceras; en la Fase 4 saldrá del token JWT. */
@RestController
@RequestMapping("/api/v1/informes")
@RequiredArgsConstructor
@Tag(name = "Informes", description = "Estadísticas para moderación y administración")
public class InformeController {

    private final InformeService informeService;

    @GetMapping("/actividad")
    @Operation(summary = "Actividad por mes y categoría, resumen por categoría y ranking de personas más activas",
            description = "Solo MODERADOR y ADMIN. Fechas en formato AAAA-MM-DD; por defecto, los últimos 12 meses.")
    public InformeActividadResponse actividad(
            @RequestHeader("X-Usuario-Id") Long usuarioId,
            @RequestHeader(value = "X-Rol", defaultValue = "USUARIO") Rol rol,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "5") int limite) {
        return informeService.actividad(new Actor(usuarioId, rol), desde, hasta, limite);
    }
}
