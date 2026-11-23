package org.marketcli.controlador.acceso;

import org.junit.jupiter.api.Test;
import org.marketcli.modelo.acceso.Rol;
import org.marketcli.modelo.acceso.Usuario;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ControladorAccesoTest {

    @Test
    void autenticaYCierraSesion() {
        Usuario administrador = new Usuario("admin", "clave-admin", Rol.ADMINISTRADOR);
        Usuario cajero = new Usuario("caja", "clave-caja", Rol.CAJERO);
        ControladorAcceso controlador = new ControladorAcceso(administrador, cajero);

        assertTrue(controlador.iniciarSesion("admin", "incorrecta").isEmpty());
        assertTrue(controlador.iniciarSesion("desconocido", "clave-caja").isEmpty());
        assertSame(administrador, controlador.iniciarSesion("admin", "clave-admin").orElseThrow());
        assertThrows(IllegalStateException.class, () -> controlador.iniciarSesion("caja", "clave-caja"));

        controlador.cerrarSesion();

        assertSame(cajero, controlador.iniciarSesion("caja", "clave-caja").orElseThrow());
    }
}
