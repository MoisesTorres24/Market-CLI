package org.marketcli.modelo.acceso;

/**
 * Identifica el menú y las operaciones asignadas a una cuenta.
 */
public enum Rol {
    /** Gestiona los catálogos, el inventario y los historiales. */
    ADMINISTRADOR,

    /** Consulta los productos y realiza ventas. */
    CAJERO
}
