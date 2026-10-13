package es.safareyes.swapcloset.dto.response;

import java.time.LocalDate;
import java.util.List;

/** ranking: solicitudes enviadas y recibidas de cada persona, contando todo el histórico. */
public record InformeActividadResponse(
        LocalDate desde,
        LocalDate hasta,
        List<ActividadMensualResponse> porMes,
        List<ActividadCategoriaDto> porCategoria,
        List<RankingUsuarioResponse> ranking) {
}
