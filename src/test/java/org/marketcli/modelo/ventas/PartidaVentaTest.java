package org.marketcli.modelo.ventas;

import org.junit.jupiter.api.Test;
import org.marketcli.modelo.inventario.TipoMedida;
import org.marketcli.soporte.ProductoDePrueba;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PartidaVentaTest {

    @Test
    void guardaSkuMedidaYPrecioAplicados() {
        ProductoDePrueba producto = new ProductoDePrueba(
                101, "Jamón", new BigDecimal("20.04"), TipoMedida.KILOGRAMO);

        PartidaVenta partida = new PartidaVenta(producto, new BigDecimal("0.250"));

        assertEquals(101, partida.getSku());
        assertEquals("Jamón", partida.getNombreProducto());
        assertEquals(TipoMedida.KILOGRAMO, partida.getMedida());
        assertEquals(0, new BigDecimal("0.250").compareTo(partida.getCantidad()));
        assertEquals(0, new BigDecimal("20.04").compareTo(partida.getPrecioUnitario()));
        assertEquals(0, new BigDecimal("5.01").compareTo(partida.getImporte()));
    }
}
