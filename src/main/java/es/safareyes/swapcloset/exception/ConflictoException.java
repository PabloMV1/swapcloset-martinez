package es.safareyes.swapcloset.exception;

/** 409: duplicados o estado incompatible. */
public class ConflictoException extends RuntimeException {
    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
