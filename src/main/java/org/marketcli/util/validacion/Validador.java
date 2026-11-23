package org.marketcli.util.validacion;

import java.math.BigDecimal;

/**
 * Comprueba precondiciones generales de texto y números decimales.
 */
public final class Validador {

    private Validador() {
    }

    /**
     * Comprueba que un texto obligatorio tenga contenido.
     *
     * @param valor texto que se valida
     * @param campo nombre del campo que se mostrará en un error
     * @throws IllegalArgumentException si el texto es nulo, vacío o solo contiene espacios
     */
    public static void validarTextoObligatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo %s es obligatorio.".formatted(campo));
        }
    }

    /**
     * Comprueba que un valor decimal sea estrictamente mayor que cero.
     *
     * @param valor decimal que se valida
     * @param campo nombre del campo que se mostrará en un error
     * @throws IllegalArgumentException si el valor es nulo, cero o negativo
     */
    public static void validarPositivo(BigDecimal valor, String campo) {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("El campo %s debe ser mayor que cero.".formatted(campo));
        }
    }
}
