package org.marketcli.vista.acceso;

import org.junit.jupiter.api.Test;
import org.marketcli.controlador.acceso.ControladorAcceso;
import org.marketcli.modelo.acceso.Rol;
import org.marketcli.modelo.acceso.Usuario;
import org.marketcli.util.lectura.LectorConsola;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class PantallaAccesoTest {

    @Test
    void despachaAlMenuDelRolYVuelveAlAcceso() {
        Usuario administrador = new Usuario("admin", "clave-admin", Rol.ADMINISTRADOR);
        Usuario cajero = new Usuario("caja", "clave-caja", Rol.CAJERO);
        ControladorAcceso controlador = new ControladorAcceso(administrador, cajero);
        List<String> menusAbiertos = new ArrayList<>();
        String guion = """
                1
                admin
                clave-admin
                1
                caja
                incorrecta
                1
                caja
                clave-caja
                0
                """;

        try (Scanner scanner = new Scanner(guion)) {
            PantallaAcceso pantalla = new PantallaAcceso(new LectorConsola(scanner), controlador, Map.of(
                    Rol.ADMINISTRADOR, usuario -> menusAbiertos.add("Administrador: " + usuario.getNombreUsuario()),
                    Rol.CAJERO, usuario -> menusAbiertos.add("Cajero: " + usuario.getNombreUsuario())
            ));

            pantalla.ejecutar();
        }

        assertEquals(List.of("Administrador: admin", "Cajero: caja"), menusAbiertos);
        assertSame(administrador, controlador.iniciarSesion("admin", "clave-admin").orElseThrow());
    }

    @Test
    void liberaSesionSiLaEntradaTerminaDentroDelMenu() {
        Usuario administrador = new Usuario("admin", "clave-admin", Rol.ADMINISTRADOR);
        Usuario cajero = new Usuario("caja", "clave-caja", Rol.CAJERO);
        ControladorAcceso controlador = new ControladorAcceso(administrador, cajero);

        try (Scanner scanner = new Scanner("1\nadmin\nclave-admin\n")) {
            LectorConsola lector = new LectorConsola(scanner);
            PantallaAcceso pantalla = new PantallaAcceso(lector, controlador, Map.of(
                    Rol.ADMINISTRADOR, usuario -> lector.pedirTexto("Dato: "),
                    Rol.CAJERO, usuario -> lector.pedirTexto("Dato: ")
            ));

            pantalla.ejecutar();
        }

        assertSame(cajero, controlador.iniciarSesion("caja", "clave-caja").orElseThrow());
    }
}
