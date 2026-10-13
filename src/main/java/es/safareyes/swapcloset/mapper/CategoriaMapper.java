package es.safareyes.swapcloset.mapper;

import es.safareyes.swapcloset.dto.projection.CategoriaConteoView;
import es.safareyes.swapcloset.dto.response.CategoriaResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoriaMapper {

    CategoriaResponse toResponse(CategoriaConteoView vista);

    List<CategoriaResponse> toResponseList(List<CategoriaConteoView> vistas);
}
