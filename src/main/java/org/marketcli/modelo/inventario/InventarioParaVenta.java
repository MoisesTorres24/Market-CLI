package org.marketcli.modelo.inventario;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Expone a Ventas las consultas y el descuento de stock que necesita para confirmar una venta.
 */
public interface InventarioParaVenta {

    /**
     * Lista los productos activos con existencias positivas.
     *
     * @return una lista no modificable de los productos disponibles
     */
    List<Producto> listarProductosDisponibles();

    /**
     * Consulta un producto activo por su SKU.
     *
     * @param sku identificador del producto
     * @return el producto activo que corresponde al SKU
     * @throws IllegalArgumentException si el SKU no existe o el producto está inactivo
     */
    Producto consultarProductoActivo(int sku);

    /**
     * Consulta el saldo de un SKU registrado.
     *
     * @param sku identificador del producto
     * @return las existencias disponibles, con cero si el producto está agotado
     * @throws IllegalArgumentException si el SKU no existe
     */
    BigDecimal consultarExistencias(int sku);

    /**
     * Valida todas las partidas antes de descontar las cantidades del lote.
     * Si algún SKU tiene stock insuficiente, lanza {@code StockInsuficienteException}
     * con el SKU y la cantidad disponible. Un rechazo conserva todos los saldos.
     *
     * @param cantidadesPorSku cantidades acumuladas por SKU
     * @throws IllegalArgumentException si un SKU no existe, está inactivo o su cantidad es inválida
     */
    void descontarLote(Map<Integer, BigDecimal> cantidadesPorSku);
}
