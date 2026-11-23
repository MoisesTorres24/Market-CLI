package org.marketcli.util.validacion;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidadorTest {

    @Test
    void rechazaTextoEnBlancoYDecimalNoPositivo() {
        IllegalArgumentException errorTexto = assertThrows(IllegalArgumentException.class,
                () -> Validador.validarTextoObligatorio("   ", "nombre"));
        IllegalArgumentException errorPrecio = assertThrows(IllegalArgumentException.class,
                () -> Validador.validarPositivo(BigDecimal.ZERO, "precio"));

        assertTrue(errorTexto.getMessage().contains("nombre"));
        assertTrue(errorPrecio.getMessage().contains("precio"));
        assertThrows(IllegalArgumentException.class, () -> Validador.validarTextoObligatorio("", "nombre"));
        assertThrows(IllegalArgumentException.class, () -> Validador.validarPositivo(new BigDecimal("-1"), "precio"));
        assertDoesNotThrow(() -> Validador.validarTextoObligatorio("Leche", "nombre"));
        assertDoesNotThrow(() -> Validador.validarPositivo(new BigDecimal("0.125"), "precio"));
    }
}
