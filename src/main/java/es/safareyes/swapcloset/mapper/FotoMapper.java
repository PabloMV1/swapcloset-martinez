package es.safareyes.swapcloset.mapper;

import es.safareyes.swapcloset.dto.response.FotoResponse;
import es.safareyes.swapcloset.entity.FotoPrenda;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FotoMapper {

    @Mapping(target = "url", expression = "java(\"/api/v1/fotos/\" + foto.getId())")
    FotoResponse toResponse(FotoPrenda foto);

    List<FotoResponse> toResponseList(List<FotoPrenda> fotos);
}
