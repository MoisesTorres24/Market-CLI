package org.marketcli.util.lectura;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LectorConsolaTest {

    @Test
    void reintentaDecimalInvalido() {
        try (Scanner scanner = new Scanner("texto\n1,234.56\n1,25\n1.25\n")) {
            LectorConsola lector = new LectorConsola(scanner);

            assertEquals(new BigDecimal("1.25"), lector.pedirDecimal("Cantidad: "));
            assertEquals(new BigDecimal("1.25"), lector.pedirDecimal("Cantidad: "));
        }
    }

    @Test
    void pideEnterosPositivosYOpcionesEnRango() {
        try (Scanner scanner = new Scanner("-1\n0\n3\n9\n0\n2\n")) {
            LectorConsola lector = new LectorConsola(scanner);

            assertEquals(3, lector.pedirEnteroPositivo("SKU: "));
            assertEquals(0, lector.pedirOpcion("Opción: ", 0, 2));
            assertEquals(2, lector.pedirOpcion("Opción: ", 0, 2));
        }
    }

    @Test
    void devuelveTextoSinEspaciosExteriores() {
        try (Scanner scanner = new Scanner("   \n  Leche  \n")) {
            LectorConsola lector = new LectorConsola(scanner);

            assertEquals("Leche", lector.pedirTexto("Nombre: "));
        }
    }

    @Test
    void propagaFinDeEntradaAlReintentar() {
        try (Scanner scanner = new Scanner("texto\n")) {
            LectorConsola lector = new LectorConsola(scanner);

            assertThrows(NoSuchElementException.class, () -> lector.pedirDecimal("Cantidad: "));
        }
    }
}
