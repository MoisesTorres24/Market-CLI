package org.marketcli.modelo.inventario;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventarioTest {

    @Test
    void altaYConsultaDeProductoAgotado() {
        Inventario inventario = new Inventario();

        Producto jabon = inventario.registrarProducto("Jabón", TipoMedida.UNIDAD, new BigDecimal("18.50"));
        Producto frijol = inventario.registrarProducto("Frijol", TipoMedida.KILOGRAMO, new BigDecimal("32.00"));

        assertNotEquals(jabon.getSku(), frijol.getSku());
        assertInstanceOf(ProductoUnidad.class, jabon);
        assertInstanceOf(ProductoGranel.class, frijol);
        assertSame(jabon, inventario.consultarProductoActivo(jabon.getSku()));
        assertSame(frijol, inventario.consultarProducto(frijol.getSku()));
        assertEquals(0, inventario.consultarExistencias(jabon.getSku()).compareTo(BigDecimal.ZERO));
        assertEquals(0, inventario.consultarExistencias(frijol.getSku()).compareTo(BigDecimal.ZERO));
        assertEquals(2, inventario.listarProductosActivos().size());
        assertTrue(inventario.listarProductosDisponibles().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> inventario.consultarExistencias(99));
        assertThrows(UnsupportedOperationException.class, () -> inventario.listarProductos().clear());
    }
}
