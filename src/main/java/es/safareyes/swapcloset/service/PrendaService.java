package es.safareyes.swapcloset.service;

import es.safareyes.swapcloset.dto.request.PrendaActualizarRequest;
import es.safareyes.swapcloset.dto.request.PrendaCrearRequest;
import es.safareyes.swapcloset.dto.response.CategoriaResponse;
import es.safareyes.swapcloset.dto.response.PrendaDetalleResponse;
import es.safareyes.swapcloset.dto.response.PrendaResumenResponse;
import es.safareyes.swapcloset.entity.EstadoPrenda;
import es.safareyes.swapcloset.entity.Modalidad;
import es.safareyes.swapcloset.security.Actor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PrendaService {

    Page<PrendaResumenResponse> buscar(Long categoriaId, String talla, EstadoPrenda estado, Long estiloId,
                                       String ciudad, Modalidad modalidad, Pageable pageable);

    /** actor puede ser null (endpoint público). */
    PrendaDetalleResponse obtener(Long id, Actor actor);

    PrendaDetalleResponse crear(Actor actor, PrendaCrearRequest request);

    PrendaDetalleResponse actualizar(Actor actor, Long id, PrendaActualizarRequest request);

    void retirar(Actor actor, Long id);

    List<CategoriaResponse> listarCategorias();
}
