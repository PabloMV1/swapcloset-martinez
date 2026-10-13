package es.safareyes.swapcloset.mapper;

import es.safareyes.swapcloset.dto.projection.ActividadMensualView;
import es.safareyes.swapcloset.dto.projection.RankingUsuarioView;
import es.safareyes.swapcloset.dto.response.*;
import es.safareyes.swapcloset.entity.Mensaje;
import es.safareyes.swapcloset.entity.Prenda;
import es.safareyes.swapcloset.entity.Solicitud;
import es.safareyes.swapcloset.entity.Usuario;
import es.safareyes.swapcloset.entity.Valoracion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SolicitudMapper {

    SolicitudResponse toResponse(Solicitud solicitud);

    PrendaBasicaResponse toPrenda(Prenda prenda);

    UsuarioResumenResponse toUsuario(Usuario usuario);

    @Mapping(target = "solicitudId", source = "solicitud.id")
    MensajeResponse toMensaje(Mensaje mensaje);

    @Mapping(target = "solicitudId", source = "solicitud.id")
    ValoracionResponse toValoracion(Valoracion valoracion);

    ActividadMensualResponse toActividad(ActividadMensualView vista);

    List<ActividadMensualResponse> toActividades(List<ActividadMensualView> vistas);

    RankingUsuarioResponse toRanking(RankingUsuarioView vista);

    List<RankingUsuarioResponse> toRankings(List<RankingUsuarioView> vistas);
}
