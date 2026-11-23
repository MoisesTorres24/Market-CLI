package org.marketcli.vista.acceso;

import org.marketcli.controlador.acceso.ControladorAcceso;
import org.marketcli.modelo.acceso.Rol;
import org.marketcli.modelo.acceso.Usuario;
import org.marketcli.util.lectura.LectorConsola;
import org.marketcli.vista.Menu;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

/**
 * Presenta el acceso e invoca el menú del rol autenticado.
 */
public final class PantallaAcceso {

    protected final LectorConsola lector;
    protected final ControladorAcceso controladorAcceso;
    protected final Map<Rol, Menu> menus;

    /**
     * Crea la pantalla con los componentes de acceso y los menús de la aplicación.
     *
     * @param lector lector compartido de consola
     * @param controladorAcceso controlador de inicio y cierre de sesión
     * @param menus menús asociados a cada rol
     * @throws IllegalArgumentException si falta el menú de algún rol
     * @throws NullPointerException si algún argumento o entrada del mapa es nulo
     */
    public PantallaAcceso(LectorConsola lector, ControladorAcceso controladorAcceso, Map<Rol, Menu> menus) {
        this.lector = Objects.requireNonNull(lector, "lector");
        this.controladorAcceso = Objects.requireNonNull(controladorAcceso, "controladorAcceso");
        this.menus = Map.copyOf(menus);
        if (this.menus.size() != Rol.values().length) {
            throw new IllegalArgumentException("Debe haber un menú para cada rol.");
        }
    }

    /**
     * Mantiene disponible el acceso hasta que el usuario sale o se agota la entrada.
     */
    public void ejecutar() {
        try {
            while (pedirOpcionDeAcceso() == 1) {
                autenticarYMostrarMenu();
            }
        } catch (NoSuchElementException finDeEntrada) {
            // Al finalizar la entrada ya no hay opciones que atender.
        }
    }

    private int pedirOpcionDeAcceso() {
        System.out.println("1) Iniciar sesión");
        System.out.println("0) Salir");
        return lector.pedirOpcion("Opción: ", 0, 1);
    }

    private void autenticarYMostrarMenu() {
        String nombreUsuario = lector.pedirTexto("Usuario: ");
        String contrasena = lector.pedirTexto("Contraseña: ");
        Optional<Usuario> resultado = controladorAcceso.iniciarSesion(nombreUsuario, contrasena);
        if (resultado.isEmpty()) {
            System.out.println("Credenciales inválidas.");
            return;
        }

        Usuario usuario = resultado.orElseThrow();
        try {
            menus.get(usuario.getRol()).ejecutar(usuario);
        } finally {
            controladorAcceso.cerrarSesion();
        }
    }
}
