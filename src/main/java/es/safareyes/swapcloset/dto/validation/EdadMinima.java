package es.safareyes.swapcloset.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/** Validador propio: la fecha de nacimiento indica una edad mínima (RN-01: 16 años). */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EdadMinimaValidator.class)
public @interface EdadMinima {

    String message() default "Debes tener al menos {anios} años";

    int anios() default 16;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
