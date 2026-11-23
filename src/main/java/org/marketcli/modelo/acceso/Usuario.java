package org.marketcli.modelo.acceso;

import org.marketcli.util.validacion.Validador;

import java.util.Objects;

/**
 * Representa una cuenta precargada con credenciales y un rol fijo.
 */
public final class Usuario {

    protected final String nombreUsuario;
    protected final String contrasena;
    protected final Rol rol;

    /**
     * Crea una cuenta a partir de sus credenciales y su rol.
     *
     * @param nombreUsuario identificador de la cuenta
     * @param contrasena contraseña de la cuenta
     * @param rol rol asociado a la cuenta
     * @throws IllegalArgumentException si el nombre de usuario o la contraseña están vacíos
     * @throws NullPointerException si no se proporciona un rol
     */
    public Usuario(String nombreUsuario, String contrasena, Rol rol) {
        Validador.validarTextoObligatorio(nombreUsuario, "nombre de usuario");
        Validador.validarTextoObligatorio(contrasena, "contraseña");
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
        this.rol = Objects.requireNonNull(rol, "rol");
    }

    /**
     * Devuelve el identificador de la cuenta.
     *
     * @return el nombre de usuario
     */
    public String getNombreUsuario() {
        return nombreUsuario;
    }

    /**
     * Devuelve el rol asignado a la cuenta.
     *
     * @return el rol de la cuenta
     */
    public Rol getRol() {
        return rol;
    }

    /**
     * Comprueba si la contraseña recibida coincide con la de la cuenta.
     *
     * @param contrasena contraseña que se comprueba
     * @return true si las contraseñas coinciden
     */
    public boolean verificarContrasena(String contrasena) {
        return this.contrasena.equals(contrasena);
    }
}
