package es.safareyes.swapcloset.mapper;

import es.safareyes.swapcloset.dto.projection.PerfilAfinView;
import es.safareyes.swapcloset.dto.request.PerfilActualizarRequest;
import es.safareyes.swapcloset.dto.response.PerfilAfinResponse;
import es.safareyes.swapcloset.dto.response.PerfilPublicoResponse;
import es.safareyes.swapcloset.dto.response.PerfilResponse;
import es.safareyes.swapcloset.dto.response.PrendaResumenResponse;
import es.safareyes.swapcloset.entity.Estilo;
import es.safareyes.swapcloset.entity.Usuario;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(target = "estilos", source = "estilos")
    PerfilResponse toPerfil(Usuario usuario);

    @Mapping(target = "id", source = "usuario.id")
    @Mapping(target = "username", source = "usuario.username")
    @Mapping(target = "ciudad", source = "usuario.ciudad")
    @Mapping(target = "biografia", source = "usuario.biografia")
    @Mapping(target = "tallaCamiseta", source = "usuario.tallaCamiseta")
    @Mapping(target = "tallaPantalon", source = "usuario.tallaPantalon")
    @Mapping(target = "tallaCalzado", source = "usuario.tallaCalzado")
    @Mapping(target = "estilos", source = "usuario.estilos")
    @Mapping(target = "reputacion", source = "reputacion")
    @Mapping(target = "valoraciones", source = "valoraciones")
    @Mapping(target = "prendas", source = "prendas")
    PerfilPublicoResponse toPerfilPublico(Usuario usuario, Double reputacion, Long valoraciones,
                                          List<PrendaResumenResponse> prendas);

    @Mapping(target = "id", source = "vista.id")
    @Mapping(target = "username", source = "vista.username")
    @Mapping(target = "ciudad", source = "vista.ciudad")
    @Mapping(target = "estilosComunes", source = "vista.estilosComunes")
    @Mapping(target = "reputacion", source = "reputacion")
    PerfilAfinResponse toAfin(PerfilAfinView vista, Double reputacion);

    /** Actualización parcial: los null del request no pisan los datos actuales. */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "fechaNacimiento", ignore = true)
    @Mapping(target = "rol", ignore = true)
    @Mapping(target = "estilos", ignore = true)
    @Mapping(target = "creadoEn", ignore = true)
    @Mapping(target = "actualizadoEn", ignore = true)
    void actualizar(PerfilActualizarRequest request, @MappingTarget Usuario usuario);

    /** Conversión de los estilos a sus nombres, ordenados alfabéticamente. */
    default List<String> nombresDeEstilos(Set<Estilo> estilos) {
        if (estilos == null) {
            return List.of();
        }
        return estilos.stream().map(Estilo::getNombre).sorted(Comparator.naturalOrder()).toList();
    }
}
