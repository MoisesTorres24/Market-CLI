package org.marketcli.modelo.inventario;

import org.marketcli.util.validacion.Validador;

/**
 * Reúne los datos de un proveedor identificado por un id único.
 */
public final class Proveedor {

    protected final int id;
    protected String nombre;
    protected boolean activo;

    /**
     * Crea un proveedor activo con un id fijo.
     *
     * @param id identificador único del proveedor
     * @param nombre nombre del proveedor
     * @throws IllegalArgumentException si el nombre está vacío
     */
    public Proveedor(int id, String nombre) {
        Validador.validarTextoObligatorio(nombre, "nombre");
        this.id = id;
        this.nombre = nombre;
        this.activo = true;
    }

    /**
     * Devuelve el id único del proveedor.
     *
     * @return el id del proveedor
     */
    public int getId() {
        return id;
    }

    /**
     * Devuelve el nombre vigente del proveedor.
     *
     * @return el nombre del proveedor
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Indica si el proveedor sigue activo.
     *
     * @return true si el proveedor está activo
     */
    public boolean isActivo() {
        return activo;
    }

    /**
     * Cambia el nombre del proveedor y conserva su id.
     *
     * @param nombre nuevo nombre del proveedor
     * @throws IllegalArgumentException si el nombre está vacío
     */
    void cambiarNombre(String nombre) {
        Validador.validarTextoObligatorio(nombre, "nombre");
        this.nombre = nombre;
    }

    /**
     * Marca al proveedor como inactivo.
     */
    void darDeBaja() {
        activo = false;
    }
}
