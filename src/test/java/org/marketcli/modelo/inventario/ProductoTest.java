package org.marketcli.modelo.inventario;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductoTest {

    @Test
    void distingueCantidadPorUnidadYGranel() {
        Producto unidad = new ProductoUnidad(1, "Jabón", new BigDecimal("18.50"));
        Producto granel = new ProductoGranel(2, "Frijol", new BigDecimal("32.00"));
        List<Producto> productos = List.of(unidad, granel);
        BigDecimal cantidad = new BigDecimal("1.5");

        for (Producto producto : productos) {
            if (producto == unidad) {
                assertThrows(IllegalArgumentException.class, () -> producto.validarCantidad(cantidad));
            } else {
                assertDoesNotThrow(() -> producto.validarCantidad(cantidad));
            }
        }
    }

    @Test
    void rechazaCuatroDecimalesEnGranel() {
        Producto granel = new ProductoGranel(2, "Frijol", new BigDecimal("32.00"));

        assertThrows(IllegalArgumentException.class, () -> granel.validarCantidad(new BigDecimal("0.1234")));
    }
}
