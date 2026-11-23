package org.marketcli.vista;

import org.marketcli.modelo.acceso.Usuario;

/**
 * Define la operación común de los menús que reciben a un usuario autenticado.
 */
@FunctionalInterface
public interface Menu {

    /**
     * Ejecuta el menú correspondiente al rol del usuario hasta que este decide salir.
     *
     * @param usuario cuenta autenticada que usa el menú
     */
    void ejecutar(Usuario usuario);
}
