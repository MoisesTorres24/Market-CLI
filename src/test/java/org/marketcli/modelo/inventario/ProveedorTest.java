package org.marketcli.modelo.inventario;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProveedorTest {

    @Test
    void cambiarNombreConservaIdYBajaDesactiva() {
        Proveedor proveedor = new Proveedor(7, "Abarrotes del Sur");
        assertTrue(proveedor.isActivo());

        proveedor.cambiarNombre("Abarrotes del Norte");
        assertEquals(7, proveedor.getId());
        assertEquals("Abarrotes del Norte", proveedor.getNombre());

        assertThrows(IllegalArgumentException.class, () -> proveedor.cambiarNombre(" "));
        assertEquals("Abarrotes del Norte", proveedor.getNombre());

        proveedor.darDeBaja();
        assertFalse(proveedor.isActivo());
        assertEquals(7, proveedor.getId());
    }
}
