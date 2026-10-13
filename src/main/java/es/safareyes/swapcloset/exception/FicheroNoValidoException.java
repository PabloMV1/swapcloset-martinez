package es.safareyes.swapcloset.exception;

/** 415: tipo de fichero no permitido (se usará en la Fase 5). */
public class FicheroNoValidoException extends RuntimeException {
    public FicheroNoValidoException(String mensaje) {
        super(mensaje);
    }
}
