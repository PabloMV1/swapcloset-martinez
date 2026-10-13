package es.safareyes.swapcloset.service;

import es.safareyes.swapcloset.dto.projection.PerfilAfinView;
import es.safareyes.swapcloset.dto.projection.ReputacionView;
import es.safareyes.swapcloset.dto.request.PerfilActualizarRequest;
import es.safareyes.swapcloset.dto.response.PerfilAfinResponse;
import es.safareyes.swapcloset.dto.response.PerfilPublicoResponse;
import es.safareyes.swapcloset.dto.response.PerfilResponse;
import es.safareyes.swapcloset.dto.response.PrendaResumenResponse;
import es.safareyes.swapcloset.entity.Estilo;
import es.safareyes.swapcloset.entity.Usuario;
import es.safareyes.swapcloset.exception.RecursoNoEncontradoException;
import es.safareyes.swapcloset.exception.ReglaNegocioException;
import es.safareyes.swapcloset.mapper.PrendaMapper;
import es.safareyes.swapcloset.mapper.UsuarioMapper;
import es.safareyes.swapcloset.repository.EstiloRepository;
import es.safareyes.swapcloset.repository.FotoPrendaRepository;
import es.safareyes.swapcloset.repository.PrendaRepository;
import es.safareyes.swapcloset.repository.UsuarioRepository;
import es.safareyes.swapcloset.repository.ValoracionRepository;
import es.safareyes.swapcloset.security.Actor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioServiceImpl implements UsuarioService {

    private static final int PRENDAS_EN_PERFIL = 20;
    private static final int CANDIDATOS_AFINES = 100;
    private static final int MAX_AFINES = 50;

    private final UsuarioRepository usuarios;
    private final EstiloRepository estilos;
    private final PrendaRepository prendas;
    private final FotoPrendaRepository fotos;
    private final ValoracionRepository valoraciones;
    private final UsuarioMapper usuarioMapper;
    private final PrendaMapper prendaMapper;

    // ------------------------------------------------------------------ endpoint 7
    @Override
    public PerfilPublicoResponse obtenerPerfil(Long id) {
        Usuario usuario = usuarios.findConEstilosById(id).orElseThrow(() -> noExiste(id));

        var pagina = prendas.listarTarjetasDePropietaria(id,
                PageRequest.of(0, PRENDAS_EN_PERFIL, Sort.by("creadaEn").descending()));
        List<Long> ids = pagina.getContent().stream().map(v -> v.getId()).toList();
        Map<Long, Long> portadas = ids.isEmpty() ? Map.of()
                : fotos.findPortadas(ids).stream()
                        .collect(Collectors.toMap(v -> v.getPrendaId(), v -> v.getId()));
        List<PrendaResumenResponse> tarjetas = pagina.getContent().stream()
                .map(v -> prendaMapper.toResumen(v, portadas.get(v.getId())))
                .toList();

        Double reputacion = redondear(valoraciones.reputacionMedia(id));
        long total = valoraciones.countByValoradoId(id);
        return usuarioMapper.toPerfilPublico(usuario, reputacion, total, tarjetas);
    }

    // ------------------------------------------------------------------ endpoint 8
    @Override
    @Transactional
    public PerfilResponse actualizarMiPerfil(Actor actor, PerfilActualizarRequest request) {
        Usuario usuario = usuarios.findConEstilosById(actor.id()).orElseThrow(() -> noExiste(actor.id()));

        usuarioMapper.actualizar(request, usuario);

        if (request.estilosIds() != null) {
            List<Estilo> encontrados = estilos.findAllById(request.estilosIds());
            if (encontrados.size() != request.estilosIds().size()) {
                throw new ReglaNegocioException("Alguno de los estilos indicados no existe"); // RN-02
            }
            usuario.setEstilos(new HashSet<>(encontrados));
        }
        log.info("Perfil del usuario {} actualizado", actor.id());
        return usuarioMapper.toPerfil(usuario);
    }

    // ------------------------------------------------------------------ endpoint 9
    @Override
    public List<PerfilAfinResponse> buscarAfines(Actor actor, int limite) {
        int tope = Math.min(Math.max(limite, 1), MAX_AFINES);

        // Q2: ordenados por estilos en común; la reputación sirve para desempatar
        List<PerfilAfinView> candidatos = usuarios.buscarAfines(actor.id(), PageRequest.of(0, CANDIDATOS_AFINES));
        if (candidatos.isEmpty()) {
            return List.of();
        }
        List<Long> ids = candidatos.stream().map(PerfilAfinView::getId).toList();
        Map<Long, Double> medias = valoraciones.reputaciones(ids).stream()
                .collect(Collectors.toMap(ReputacionView::getUsuarioId, ReputacionView::getMedia));

        Comparator<PerfilAfinView> orden = Comparator
                .comparing(PerfilAfinView::getEstilosComunes, Comparator.reverseOrder())
                .thenComparing(v -> medias.get(v.getId()), Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(PerfilAfinView::getUsername);

        return candidatos.stream()
                .sorted(orden)
                .limit(tope)
                .map(v -> usuarioMapper.toAfin(v, redondear(medias.get(v.getId()))))
                .toList();
    }

    // ------------------------------------------------------------------ auxiliares
    private Double redondear(Double media) {
        return media == null ? null : Math.round(media * 10) / 10.0; // un decimal (RN-09)
    }

    private RecursoNoEncontradoException noExiste(Long id) {
        return new RecursoNoEncontradoException("No existe el usuario con id " + id);
    }
}
