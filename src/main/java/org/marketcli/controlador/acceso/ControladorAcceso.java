package org.marketcli.controlador.acceso;

import org.marketcli.modelo.acceso.Rol;
import org.marketcli.modelo.acceso.Usuario;

import java.util.Objects;
import java.util.Optional;

/**
 * Autentica las dos cuentas precargadas y mantiene una sola sesión activa.
 */
public final class ControladorAcceso {

    protected final Usuario administrador;
    protected final Usuario cajero;
    protected Usuario usuarioEnSesion;

    /**
     * Crea el controlador con las cuentas que se usan en la aplicación.
     *
     * @param administrador cuenta con rol de administrador
     * @param cajero cuenta con rol de cajero
     * @throws NullPointerException si falta alguna cuenta
     * @throws IllegalArgumentException si el rol no corresponde o se repite el nombre de usuario
     */
    public ControladorAcceso(Usuario administrador, Usuario cajero) {
        this.administrador = Objects.requireNonNull(administrador, "administrador");
        this.cajero = Objects.requireNonNull(cajero, "cajero");
        if (administrador.getRol() != Rol.ADMINISTRADOR || cajero.getRol() != Rol.CAJERO) {
            throw new IllegalArgumentException("Las cuentas deben corresponder a sus roles.");
        }
        if (administrador.getNombreUsuario().equals(cajero.getNombreUsuario())) {
            throw new IllegalArgumentException("Las cuentas deben tener nombres de usuario distintos.");
        }
    }

    /**
     * Abre una sesión cuando las credenciales corresponden a una cuenta precargada.
     *
     * @param nombreUsuario nombre recibido en el acceso
     * @param contrasena contraseña recibida en el acceso
     * @return el usuario autenticado o un resultado vacío si las credenciales son inválidas
     * @throws IllegalStateException si ya existe una sesión activa
     */
    public Optional<Usuario> iniciarSesion(String nombreUsuario, String contrasena) {
        if (usuarioEnSesion != null) {
            throw new IllegalStateException("Ya existe una sesión activa.");
        }
        if (coincidenCredenciales(administrador, nombreUsuario, contrasena)) {
            usuarioEnSesion = administrador;
        } else if (coincidenCredenciales(cajero, nombreUsuario, contrasena)) {
            usuarioEnSesion = cajero;
        }
        return Optional.ofNullable(usuarioEnSesion);
    }

    /**
     * Cierra la sesión activa y permite un nuevo inicio de sesión.
     */
    public void cerrarSesion() {
        usuarioEnSesion = null;
    }

    private boolean coincidenCredenciales(Usuario usuario, String nombreUsuario, String contrasena) {
        return usuario.getNombreUsuario().equals(nombreUsuario) && usuario.verificarContrasena(contrasena);
    }
}
