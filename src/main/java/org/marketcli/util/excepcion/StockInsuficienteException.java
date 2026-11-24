package org.marketcli.util.excepcion;

import java.math.BigDecimal;

/**
 * Señala que la existencia de un producto no alcanza para la cantidad solicitada.
 */
public final class StockInsuficienteException extends RuntimeException {

    protected final int sku;
    protected final BigDecimal disponible;

    /**
     * Crea la excepción con el producto afectado y su existencia disponible.
     *
     * @param sku identificador del producto sin existencia suficiente
     * @param disponible existencia disponible al momento de la solicitud
     */
    public StockInsuficienteException(int sku, BigDecimal disponible) {
        super("Stock insuficiente para el SKU " + sku + ". Disponible: " + disponible);
        this.sku = sku;
        this.disponible = disponible;
    }

    /**
     * Devuelve el SKU del producto sin existencia suficiente.
     *
     * @return el SKU del producto
     */
    public int getSku() {
        return sku;
    }

    /**
     * Devuelve la existencia disponible al momento de la solicitud.
     *
     * @return la existencia disponible
     */
    public BigDecimal getDisponible() {
        return disponible;
    }
}
