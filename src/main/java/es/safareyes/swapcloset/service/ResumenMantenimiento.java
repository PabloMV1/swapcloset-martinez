package es.safareyes.swapcloset.service;

/** Qué ha hecho una ejecución de la tarea programada. */
public record ResumenMantenimiento(int caducadas, int retrasadas, int avisosVencimiento, int avisosRetraso) {
}
