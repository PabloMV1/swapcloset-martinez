package es.safareyes.swapcloset.service;

import es.safareyes.swapcloset.dto.response.InformeActividadResponse;
import es.safareyes.swapcloset.exception.ProhibidoException;
import es.safareyes.swapcloset.exception.ReglaNegocioException;
import es.safareyes.swapcloset.mapper.SolicitudMapper;
import es.safareyes.swapcloset.repository.SolicitudRepository;
import es.safareyes.swapcloset.security.Actor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InformeServiceImpl implements InformeService {

    private final SolicitudRepository solicitudes;
    private final SolicitudMapper mapper;

    // ------------------------------------------------------------------ endpoint 16
    @Override
    public InformeActividadResponse actividad(Actor actor, LocalDate desde, LocalDate hasta, int limiteRanking) {
        if (!actor.esStaff()) {
            throw new ProhibidoException("El informe de actividad es solo para moderación y administración");
        }
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        LocalDate inicio = desde != null ? desde : fin.minusMonths(12);
        if (inicio.isAfter(fin)) {
            throw new ReglaNegocioException("La fecha 'desde' no puede ser posterior a 'hasta'");
        }
        LocalDateTime d = inicio.atStartOfDay();
        LocalDateTime h = fin.plusDays(1).atStartOfDay(); // límite superior exclusivo: incluye todo el día 'hasta'
        int limite = Math.max(1, Math.min(limiteRanking, 20));

        return new InformeActividadResponse(inicio, fin,
                mapper.toActividades(solicitudes.actividadPorCategoriaYMes(d, h)),
                solicitudes.resumenPorCategoria(d, h),
                mapper.toRankings(solicitudes.rankingUsuariasActivas(limite)));
    }
}
