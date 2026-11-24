package org.marketcli.modelo.inventario;

import java.math.BigDecimal;

/**
 * Representa un producto que se registra y vende en kilogramos.
 */
// HERENCIA: reutiliza los datos y reglas comunes de Producto.
public final class ProductoGranel extends Producto {

    private static final int DECIMALES_MAXIMOS = 3;

    /**
     * Crea un producto a granel activo.
     *
     * @param sku identificador asignado por el catálogo
     * @param nombre nombre del producto
     * @param precio precio de venta por kilogramo
     * @throws IllegalArgumentException si el nombre está vacío o el precio no es positivo
     */
    public ProductoGranel(int sku, String nombre, BigDecimal precio) {
        super(sku, nombre, precio, TipoMedida.KILOGRAMO);
    }

    /**
     * Acepta cantidades con hasta tres decimales.
     *
     * @param cantidad cantidad positiva cuya precisión se debe comprobar
     * @throws IllegalArgumentException si la cantidad tiene más de tres decimales
     */
    @Override
    protected void validarPrecision(BigDecimal cantidad) {
        if (cantidad.stripTrailingZeros().scale() > DECIMALES_MAXIMOS) {
            throw new IllegalArgumentException("La cantidad a granel admite hasta tres decimales.");
        }
    }
}
