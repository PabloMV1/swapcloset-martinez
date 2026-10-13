package es.safareyes.swapcloset.mapper;

import es.safareyes.swapcloset.dto.projection.PrendaTarjetaView;
import es.safareyes.swapcloset.dto.request.PrendaActualizarRequest;
import es.safareyes.swapcloset.dto.request.PrendaCrearRequest;
import es.safareyes.swapcloset.dto.response.PrendaDetalleResponse;
import es.safareyes.swapcloset.dto.response.PrendaResumenResponse;
import es.safareyes.swapcloset.dto.response.PropietariaResponse;
import es.safareyes.swapcloset.entity.Prenda;
import es.safareyes.swapcloset.entity.Usuario;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/** Demuestra: campos anidados, ignore, varios orígenes, @MappingTarget con IGNORE, colecciones y uses. */
@Mapper(componentModel = "spring", uses = FotoMapper.class)
public interface PrendaMapper {

    // ----- entrada -----
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "propietaria", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "estilo", ignore = true)
    @Mapping(target = "disponibilidad", ignore = true)
    @Mapping(target = "activa", ignore = true)
    @Mapping(target = "fotos", ignore = true)
    @Mapping(target = "creadaEn", ignore = true)
    @Mapping(target = "actualizadaEn", ignore = true)
    Prenda toEntity(PrendaCrearRequest request);

    /** Actualización parcial: los campos null del request no pisan los valores actuales. */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "propietaria", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "estilo", ignore = true)
    @Mapping(target = "disponibilidad", ignore = true)
    @Mapping(target = "activa", ignore = true)
    @Mapping(target = "fotos", ignore = true)
    @Mapping(target = "creadaEn", ignore = true)
    @Mapping(target = "actualizadaEn", ignore = true)
    void actualizar(PrendaActualizarRequest request, @MappingTarget Prenda prenda);

    // ----- salida -----
    @Mapping(target = "id", source = "prenda.id")
    @Mapping(target = "categoria", source = "prenda.categoria.nombre")
    @Mapping(target = "propietaria", source = "prenda.propietaria.username")
    @Mapping(target = "ciudad", source = "prenda.propietaria.ciudad")
    @Mapping(target = "fotoPortadaId", source = "fotoPortadaId")
    PrendaResumenResponse toResumen(Prenda prenda, Long fotoPortadaId);

    /** El servicio calcula la reputación; el mapper solo ensambla. */
    @Mapping(target = "id", source = "usuario.id")
    @Mapping(target = "username", source = "usuario.username")
    @Mapping(target = "ciudad", source = "usuario.ciudad")
    @Mapping(target = "reputacion", source = "reputacion")
    @Mapping(target = "valoraciones", source = "valoraciones")
    PropietariaResponse toPropietaria(Usuario usuario, Double reputacion, Long valoraciones);

    @Mapping(target = "id", source = "prenda.id")
    @Mapping(target = "categoriaId", source = "prenda.categoria.id")
    @Mapping(target = "categoria", source = "prenda.categoria.nombre")
    @Mapping(target = "estiloId", source = "prenda.estilo.id")
    @Mapping(target = "estilo", source = "prenda.estilo.nombre")
    @Mapping(target = "propietaria", source = "propietaria")
    @Mapping(target = "fotos", source = "prenda.fotos")
    PrendaDetalleResponse toDetalle(Prenda prenda, PropietariaResponse propietaria);

    /** Desde la proyección ligera del perfil público (sin cargar la entidad). */
    @Mapping(target = "id", source = "vista.id")
    @Mapping(target = "fotoPortadaId", source = "fotoPortadaId")
    PrendaResumenResponse toResumen(PrendaTarjetaView vista, Long fotoPortadaId);
}
