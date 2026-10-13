package es.safareyes.swapcloset.mapper;

import es.safareyes.swapcloset.dto.response.NotificacionResponse;
import es.safareyes.swapcloset.entity.Notificacion;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificacionMapper {

    NotificacionResponse toResponse(Notificacion notificacion);
}
