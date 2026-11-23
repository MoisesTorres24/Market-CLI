package org.marketcli.modelo.inventario;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductoBaseTest {

    @Test
    void validaImporteConRedondeoPorPartida() {
        ProductoDePrueba producto = new ProductoDePrueba(new BigDecimal("20.04"));
        BigDecimal cantidad = new BigDecimal("0.125");

        assertEquals(new BigDecimal("2.51"), producto.calcularImporte(cantidad));
        assertEquals(cantidad, producto.cantidadValidada);
        assertEquals(new BigDecimal("5.01"), producto.calcularImporte(new BigDecimal("0.250")));
    }

    @Test
    void rechazaCantidadNoPositivaYPrecioInvalido() {
        ProductoDePrueba producto = new ProductoDePrueba(new BigDecimal("20.04"));

        assertThrows(IllegalArgumentException.class, () -> producto.validarCantidad(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> producto.validarCantidad(new BigDecimal("-1")));
        assertNull(producto.cantidadValidada);
        assertThrows(IllegalArgumentException.class, () -> new ProductoDePrueba(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new ProductoDePrueba(new BigDecimal("-1")));
    }

    @Test
    void conservaDatosSiLaEdicionSeRechaza() {
        BigDecimal precioOriginal = new BigDecimal("20.04");
        Producto producto = new ProductoDePrueba(precioOriginal);

        assertThrows(IllegalArgumentException.class, () -> producto.actualizarDatos("Frijol nuevo", BigDecimal.ZERO));
        assertEquals("Frijol", producto.getNombre());
        assertEquals(precioOriginal, producto.getPrecio());

        producto.actualizarDatos("Frijol nuevo", new BigDecimal("25.00"));
        assertEquals("Frijol nuevo", producto.getNombre());
        assertEquals(new BigDecimal("25.00"), producto.getPrecio());
        assertEquals(1, producto.getSku());
        assertEquals(TipoMedida.KILOGRAMO, producto.getMedida());
        assertTrue(producto.isActivo());

        producto.darDeBaja();
        assertFalse(producto.isActivo());
        assertEquals(1, producto.getSku());
    }

    private static final class ProductoDePrueba extends Producto {

        private BigDecimal cantidadValidada;

        private ProductoDePrueba(BigDecimal precio) {
            super(1, "Frijol", precio, TipoMedida.KILOGRAMO);
        }

        @Override
        protected void validarPrecision(BigDecimal cantidad) {
            cantidadValidada = cantidad;
        }
    }
}
