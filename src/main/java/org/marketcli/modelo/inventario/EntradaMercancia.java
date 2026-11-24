package org.marketcli.modelo.inventario;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Registra la recepción de mercancía y copia los datos del producto y del proveedor de ese momento.
 */
public final class EntradaMercancia {

    protected final int id;
    protected final LocalDateTime fechaHora;
    protected final int sku;
    protected final TipoMedida medida;
    protected final BigDecimal cantidad;
    protected final int proveedorId;
    protected final String nombreProveedor;

    /**
     * Crea el registro de una entrada y copia el SKU, la medida, el id y el nombre del proveedor.
     *
     * @param id identificador de la entrada
     * @param fechaHora momento en que se recibió la mercancía
     * @param producto producto recibido
     * @param proveedor proveedor que entregó la mercancía
     * @param cantidad cantidad recibida en la medida del producto
     * @throws NullPointerException si la fecha, el producto, el proveedor o la cantidad son nulos
     */
    public EntradaMercancia(int id, LocalDateTime fechaHora, Producto producto,
                            Proveedor proveedor, BigDecimal cantidad) {
        Objects.requireNonNull(producto, "producto");
        Objects.requireNonNull(proveedor, "proveedor");
        this.id = id;
        this.fechaHora = Objects.requireNonNull(fechaHora, "fechaHora");
        this.sku = producto.getSku();
        this.medida = producto.getMedida();
        this.cantidad = Objects.requireNonNull(cantidad, "cantidad");
        this.proveedorId = proveedor.getId();
        this.nombreProveedor = proveedor.getNombre();
    }

    /**
     * Devuelve el identificador de la entrada.
     *
     * @return el id de la entrada
     */
    public int getId() {
        return id;
    }

    /**
     * Devuelve el momento en que se recibió la mercancía.
     *
     * @return la fecha y hora de la entrada
     */
    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    /**
     * Devuelve el SKU del producto recibido.
     *
     * @return el SKU del producto
     */
    public int getSku() {
        return sku;
    }

    /**
     * Devuelve la medida del producto al momento de la entrada.
     *
     * @return la medida del producto
     */
    public TipoMedida getMedida() {
        return medida;
    }

    /**
     * Devuelve la cantidad recibida.
     *
     * @return la cantidad en la medida del producto
     */
    public BigDecimal getCantidad() {
        return cantidad;
    }

    /**
     * Devuelve el id del proveedor que entregó la mercancía.
     *
     * @return el id del proveedor
     */
    public int getProveedorId() {
        return proveedorId;
    }

    /**
     * Devuelve el nombre que tenía el proveedor al momento de la entrada.
     *
     * @return el nombre histórico del proveedor
     */
    public String getNombreProveedor() {
        return nombreProveedor;
    }
}
