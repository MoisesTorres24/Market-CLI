package org.marketcli.soporte;

import org.marketcli.modelo.inventario.Producto;
import org.marketcli.modelo.inventario.TipoMedida;

import java.math.BigDecimal;

/**
 * Producto de prueba reutilizable en las pruebas de Ventas, con las reglas de precisión
 * de cada {@link TipoMedida}.
 */
public final class ProductoDePrueba extends Producto {

    private static final int DECIMALES_MAXIMOS_KILOGRAMO = 3;

    /**
     * Crea un producto de prueba activo.
     *
     * @param sku identificador del producto
     * @param nombre nombre del producto
     * @param precio precio de venta por la medida indicada
     * @param medida medida fija del producto
     * @throws IllegalArgumentException si el nombre está vacío o el precio no es positivo
     * @throws NullPointerException si la medida es nula
     */
    public ProductoDePrueba(int sku, String nombre, BigDecimal precio, TipoMedida medida) {
        super(sku, nombre, precio, medida);
    }

    /**
     * Acepta enteros para {@code UNIDAD} y hasta tres decimales significativos para
     * {@code KILOGRAMO}.
     *
     * @param cantidad cantidad positiva cuya precisión se debe comprobar
     * @throws IllegalArgumentException si la cantidad tiene una precisión incompatible con la medida
     */
    @Override
    protected void validarPrecision(BigDecimal cantidad) {
        int decimales = cantidad.stripTrailingZeros().scale();
        switch (medida) {
            case UNIDAD -> {
                if (decimales > 0) {
                    throw new IllegalArgumentException("La cantidad por unidad debe ser entera.");
                }
            }
            case KILOGRAMO -> {
                if (decimales > DECIMALES_MAXIMOS_KILOGRAMO) {
                    throw new IllegalArgumentException("La cantidad en kilogramos admite hasta tres decimales.");
                }
            }
        }
    }
}
