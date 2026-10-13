package es.safareyes.swapcloset.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class EdadMinimaValidator implements ConstraintValidator<EdadMinima, LocalDate> {

    private int anios;

    @Override
    public void initialize(EdadMinima anotacion) {
        this.anios = anotacion.anios();
    }

    /** Un valor null se considera válido: para exigirlo se combina con @NotNull. */
    @Override
    public boolean isValid(LocalDate nacimiento, ConstraintValidatorContext contexto) {
        if (nacimiento == null) {
            return true;
        }
        // Cumple la edad si nació hoy-16 años o antes (también rechaza fechas futuras)
        return !nacimiento.isAfter(LocalDate.now().minusYears(anios));
    }
}
