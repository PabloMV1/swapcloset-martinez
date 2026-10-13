package es.safareyes.swapcloset.controller;

import es.safareyes.swapcloset.dto.response.CategoriaResponse;
import es.safareyes.swapcloset.service.PrendaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categorias")
@RequiredArgsConstructor
@Tag(name = "Categorías")
public class CategoriaController {

    private final PrendaService prendaService;

    @GetMapping
    @Operation(summary = "Categorías con el número de prendas disponibles")
    public List<CategoriaResponse> listar() {
        return prendaService.listarCategorias();
    }
}
