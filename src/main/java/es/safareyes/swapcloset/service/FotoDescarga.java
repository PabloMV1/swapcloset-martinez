package es.safareyes.swapcloset.service;

import org.springframework.core.io.Resource;

public record FotoDescarga(Resource recurso, String tipoContenido, String nombre) {
}
