package es.safareyes.swapcloset.validation;

import es.safareyes.swapcloset.dto.validation.EdadMinima;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class EdadMinimaValidatorTest {

    static class Muestra {
        @EdadMinima(anios = 16)
        LocalDate nacimiento;

        Muestra(LocalDate nacimiento) {
            this.nacimiento = nacimiento;
        }
    }

    private final Validator validador = Validation.buildDefaultValidatorFactory().getValidator();

    private boolean esValida(LocalDate fecha) {
        return validador.validate(new Muestra(fecha)).isEmpty();
    }

    @Test
    void aceptaAQuienCumple16Hoy() {
        assertThat(esValida(LocalDate.now().minusYears(16))).isTrue();
    }

    @Test
    void aceptaMayoresDe16() {
        assertThat(esValida(LocalDate.now().minusYears(30))).isTrue();
    }

    @Test
    void rechazaAQuienCumple16Manana() {
        assertThat(esValida(LocalDate.now().minusYears(16).plusDays(1))).isFalse();
    }

    @Test
    void rechazaMenoresYFechasFuturas() {
        assertThat(esValida(LocalDate.now().minusYears(15))).isFalse();
        assertThat(esValida(LocalDate.now().plusDays(1))).isFalse();
    }

    @Test
    void nullSeConsideraValidoParaCombinarloConNotNull() {
        assertThat(esValida(null)).isTrue();
    }

    @Test
    void elMensajeIncluyeLaEdad() {
        var errores = validador.validate(new Muestra(LocalDate.now().minusYears(10)));
        assertThat(errores.iterator().next().getMessage()).isEqualTo("Debes tener al menos 16 años");
    }
}
