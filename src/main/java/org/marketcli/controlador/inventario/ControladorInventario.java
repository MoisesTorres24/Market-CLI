package org.marketcli.controlador.inventario;

import org.marketcli.modelo.inventario.EntradaMercancia;
import org.marketcli.modelo.inventario.Inventario;
import org.marketcli.modelo.inventario.Producto;
import org.marketcli.modelo.inventario.Proveedor;
import org.marketcli.modelo.inventario.TipoMedida;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Expone a la vista las operaciones de inventario y las delega al modelo.
 */
public final class ControladorInventario {

    protected final Inventario inventario;

    /**
     * Crea el controlador sobre un inventario.
     *
     * @param inventario inventario al que se delegan las operaciones
     * @throws NullPointerException si el inventario es nulo
     */
    public ControladorInventario(Inventario inventario) {
        this.inventario = Objects.requireNonNull(inventario, "inventario");
    }

    /**
     * Registra un producto activo con un SKU nuevo y saldo cero.
     *
     * @param nombre nombre del producto
     * @param medida medida con la que se registra y vende el producto
     * @param precio precio de venta por la medida indicada
     * @return el producto registrado
     * @throws IllegalArgumentException si el nombre está vacío o el precio no es positivo
     * @throws NullPointerException si la medida es nula
     */
    public Producto registrarProducto(String nombre, TipoMedida medida, BigDecimal precio) {
        return inventario.registrarProducto(nombre, medida, precio);
    }

    /**
     * Lista todos los productos registrados, activos o no.
     *
     * @return una lista no modificable de los productos
     */
    public List<Producto> listarProductos() {
        return inventario.listarProductos();
    }

    /**
     * Lista los productos activos, tengan o no existencias.
     *
     * @return una lista no modificable de los productos activos
     */
    public List<Producto> listarProductosActivos() {
        return inventario.listarProductosActivos();
    }

    /**
     * Consulta un producto registrado por su SKU, activo o no.
     *
     * @param sku identificador del producto
     * @return el producto que corresponde al SKU
     * @throws IllegalArgumentException si el SKU no existe
     */
    public Producto consultarProducto(int sku) {
        return inventario.consultarProducto(sku);
    }

    /**
     * Consulta el saldo de un SKU registrado.
     *
     * @param sku identificador del producto
     * @return las existencias disponibles, con cero si el producto está agotado
     * @throws IllegalArgumentException si el SKU no existe
     */
    public BigDecimal consultarExistencias(int sku) {
        return inventario.consultarExistencias(sku);
    }

    /**
     * Cambia el nombre y el precio de un producto, y conserva su SKU, medida y saldo.
     *
     * @param sku identificador del producto
     * @param nombre nuevo nombre del producto
     * @param precio nuevo precio de venta
     * @throws IllegalArgumentException si el SKU no existe, el nombre está vacío o el precio no es positivo
     */
    public void modificarProducto(int sku, String nombre, BigDecimal precio) {
        inventario.modificarProducto(sku, nombre, precio);
    }

    /**
     * Da de baja un producto sin existencias y conserva su SKU y su historial.
     *
     * @param sku identificador del producto
     * @throws IllegalArgumentException si el SKU no existe
     * @throws IllegalStateException si el producto todavía tiene existencias
     */
    public void darDeBajaProducto(int sku) {
        inventario.darDeBajaProducto(sku);
    }

    /**
     * Registra un proveedor activo con un id nuevo.
     *
     * @param nombre nombre del proveedor
     * @return el proveedor registrado
     * @throws IllegalArgumentException si el nombre está vacío
     */
    public Proveedor registrarProveedor(String nombre) {
        return inventario.registrarProveedor(nombre);
    }

    /**
     * Lista los proveedores activos.
     *
     * @return una lista no modificable de los proveedores activos
     */
    public List<Proveedor> listarProveedoresActivos() {
        return inventario.listarProveedoresActivos();
    }

    /**
     * Cambia el nombre de un proveedor y conserva su id.
     *
     * @param proveedorId identificador del proveedor
     * @param nombre nuevo nombre del proveedor
     * @throws IllegalArgumentException si el proveedor no existe o el nombre está vacío
     */
    public void modificarProveedor(int proveedorId, String nombre) {
        inventario.modificarProveedor(proveedorId, nombre);
    }

    /**
     * Da de baja un proveedor y conserva su id.
     *
     * @param proveedorId identificador del proveedor
     * @throws IllegalArgumentException si el proveedor no existe
     */
    public void darDeBajaProveedor(int proveedorId) {
        inventario.darDeBajaProveedor(proveedorId);
    }

    /**
     * Registra la recepción de mercancía de un proveedor y suma la cantidad al saldo del producto.
     *
     * @param sku identificador del producto recibido
     * @param proveedorId identificador del proveedor que entrega
     * @param cantidad cantidad recibida en la medida del producto
     * @return la entrada registrada
     * @throws IllegalArgumentException si el producto o el proveedor no existen o están inactivos,
     *                                  o si la cantidad es inválida
     */
    public EntradaMercancia registrarEntrada(int sku, int proveedorId, BigDecimal cantidad) {
        return inventario.registrarEntrada(sku, proveedorId, cantidad);
    }

    /**
     * Lista el historial de entradas en el orden en que se registraron.
     *
     * @return una copia no modificable del historial de entradas
     */
    public List<EntradaMercancia> listarEntradas() {
        return inventario.listarEntradas();
    }
}
