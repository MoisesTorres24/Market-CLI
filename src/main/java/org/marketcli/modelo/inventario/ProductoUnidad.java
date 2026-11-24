package org.marketcli.modelo.inventario;

import java.math.BigDecimal;

/**
 * Representa un producto que se registra y vende en piezas enteras.
 */
// HERENCIA: reutiliza los datos y reglas comunes de Producto.
public final class ProductoUnidad extends Producto {

    /**
     * Crea un producto por unidad activo.
     *
     * @param sku identificador asignado por el catálogo
     * @param nombre nombre del producto
     * @param precio precio de venta por pieza
     * @throws IllegalArgumentException si el nombre está vacío o el precio no es positivo
     */
    public ProductoUnidad(int sku, String nombre, BigDecimal precio) {
        super(sku, nombre, precio, TipoMedida.UNIDAD);
    }

    /**
     * Acepta solo cantidades enteras.
     *
     * @param cantidad cantidad positiva cuya precisión se debe comprobar
     * @throws IllegalArgumentException si la cantidad tiene decimales
     */
    @Override
    protected void validarPrecision(BigDecimal cantidad) {
        if (cantidad.stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException("La cantidad de un producto por unidad debe ser entera.");
        }
    }
}
