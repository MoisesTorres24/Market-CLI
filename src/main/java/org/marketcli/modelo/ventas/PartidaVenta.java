package org.marketcli.modelo.ventas;

import org.marketcli.modelo.inventario.Producto;
import org.marketcli.modelo.inventario.TipoMedida;
import org.marketcli.util.validacion.Validador;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Renglón de una venta. Guarda una copia de los datos del producto y del precio aplicado,
 * de modo que el historial no cambia si el catálogo se modifica después.
 */
public final class PartidaVenta {

    protected final int sku;
    protected final String nombreProducto;
    protected final TipoMedida medida;
    protected final BigDecimal cantidad;
    protected final BigDecimal precioUnitario;
    protected final BigDecimal importe;

    /**
     * Crea una partida con el precio vigente del producto y su importe redondeado.
     *
     * @param producto producto vendido
     * @param cantidad cantidad vendida, válida para la medida del producto
     * @throws NullPointerException si el producto es nulo
     * @throws IllegalArgumentException si la cantidad es nula, no es positiva o su precisión
     *                                  es inválida para el producto
     */
    public PartidaVenta(Producto producto, BigDecimal cantidad) {
        Objects.requireNonNull(producto, "producto");
        Validador.validarPositivo(cantidad, "cantidad");
        producto.validarCantidad(cantidad);
        this.sku = producto.getSku();
        this.nombreProducto = producto.getNombre();
        this.medida = producto.getMedida();
        this.cantidad = cantidad;
        this.precioUnitario = producto.getPrecio();
        this.importe = producto.calcularImporte(cantidad);
    }

    /**
     * Devuelve el SKU del producto vendido.
     *
     * @return el SKU registrado en la partida
     */
    public int getSku() {
        return sku;
    }

    /**
     * Devuelve el nombre que tenía el producto al venderse.
     *
     * @return el nombre registrado en la partida
     */
    public String getNombreProducto() {
        return nombreProducto;
    }

    /**
     * Devuelve la medida del producto vendido.
     *
     * @return la medida registrada en la partida
     */
    public TipoMedida getMedida() {
        return medida;
    }

    /**
     * Devuelve la cantidad vendida.
     *
     * @return la cantidad de la partida
     */
    public BigDecimal getCantidad() {
        return cantidad;
    }

    /**
     * Devuelve el precio unitario aplicado al momento de la venta.
     *
     * @return el precio unitario histórico
     */
    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    /**
     * Devuelve el importe de la partida, redondeado a dos decimales.
     *
     * @return el importe de la partida
     */
    public BigDecimal getImporte() {
        return importe;
    }
}
