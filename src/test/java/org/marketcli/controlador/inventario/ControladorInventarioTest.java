package org.marketcli.controlador.inventario;

import org.junit.jupiter.api.Test;
import org.marketcli.modelo.inventario.Inventario;
import org.marketcli.modelo.inventario.TipoMedida;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ControladorInventarioTest {

    @Test
    void consultaProductoAgotadoYNoLoConfundeConSkuInexistente() {
        ControladorInventario controlador = new ControladorInventario(new Inventario());
        int sku = controlador.registrarProducto("Jabón", TipoMedida.UNIDAD, new BigDecimal("18.50")).getSku();

        assertEquals(0, controlador.consultarExistencias(sku).compareTo(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> controlador.consultarExistencias(sku + 1));
    }
}
