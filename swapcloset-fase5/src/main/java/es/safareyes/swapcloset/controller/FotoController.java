package es.safareyes.swapcloset.controller;

import es.safareyes.swapcloset.dto.response.FotoResponse;
import es.safareyes.swapcloset.security.Actor;
import es.safareyes.swapcloset.service.FotoDescarga;
import es.safareyes.swapcloset.service.FotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@RestController
@RequiredArgsConstructor
@Tag(name = "Fotos", description = "Subida y descarga de las fotos de las prendas")
public class FotoController {

    private final FotoService fotoService;

    @PostMapping(value = "/api/v1/prendas/{prendaId}/fotos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Subir una foto a una prenda propia (JPEG, PNG o WebP, máximo 5 MB y 5 fotos)",
            description = "Campo del formulario: foto. La primera foto publica la prenda (BORRADOR -> DISPONIBLE).")
    public FotoResponse subir(
            @PathVariable Long prendaId,
            @AuthenticationPrincipal Actor actor,
            @RequestPart("foto") MultipartFile foto) {
        return fotoService.subir(actor, prendaId, foto);
    }

    @GetMapping("/api/v1/fotos/{id}")
    @Operation(summary = "Descargar una foto",
            description = "Las fotos de prendas publicadas se ven sin cuenta; las de borradores y prendas retiradas, "
                    + "solo su propietaria o la moderación.")
    public ResponseEntity<Resource> descargar(@PathVariable Long id, @AuthenticationPrincipal Actor actor) {
        FotoDescarga d = fotoService.descargar(actor, id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(d.tipoContenido()))
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePrivate())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(d.nombre(), StandardCharsets.UTF_8).build().toString())
                .body(d.recurso());
    }
}
