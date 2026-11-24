package org.marketcli.modelo.ventas;

import org.junit.jupiter.api.Test;
import org.marketcli.modelo.inventario.TipoMedida;
import org.marketcli.soporte.ProductoDePrueba;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VentaTest {

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 11, 24, 12, 0);

    private static PartidaVenta partidaDeCincoConUnCentavo() {
        ProductoDePrueba producto = new ProductoDePrueba(
                101, "Jamón", new BigDecimal("20.04"), TipoMedida.KILOGRAMO);
        return new PartidaVenta(producto, new BigDecimal("0.250"));
    }

    @Test
    void copiaPartidasYCalculaCambio() {
        List<PartidaVenta> original = new ArrayList<>(List.of(partidaDeCincoConUnCentavo()));

        Venta venta = new Venta(1, FECHA, "Brayan", original, new BigDecimal("10.00"));

        assertEquals(0, new BigDecimal("5.01").compareTo(venta.getTotal()));
        assertEquals(0, new BigDecimal("4.99").compareTo(venta.getCambio()));

        original.clear();
        assertEquals(1, venta.getPartidas().size());
        assertThrows(UnsupportedOperationException.class,
                () -> venta.getPartidas().add(partidaDeCincoConUnCentavo()));
    }

    @Test
    void rechazaPagoInferiorAlTotal() {
        List<PartidaVenta> partidas = List.of(partidaDeCincoConUnCentavo());

        assertThrows(IllegalArgumentException.class,
                () -> new Venta(2, FECHA, "Brayan", partidas, new BigDecimal("4.00")));
    }
}
