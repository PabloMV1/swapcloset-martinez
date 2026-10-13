package es.safareyes.swapcloset.service;

import es.safareyes.swapcloset.dto.response.InformeActividadResponse;
import es.safareyes.swapcloset.security.Actor;

import java.time.LocalDate;

public interface InformeService {

    InformeActividadResponse actividad(Actor actor, LocalDate desde, LocalDate hasta, int limiteRanking);
}
