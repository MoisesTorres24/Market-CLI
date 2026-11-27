package org.marketcli.soporte;

import org.marketcli.modelo.inventario.InventarioParaVenta;
import org.marketcli.modelo.inventario.Producto;
import org.marketcli.util.excepcion.StockInsuficienteException;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Doble de prueba de {@link InventarioParaVenta} para verificar el módulo de Ventas de forma aislada.
 */
public final class InventarioDePrueba implements InventarioParaVenta {

    private final Map<Integer, Producto> productos = new LinkedHashMap<>();
    private final Map<Integer, BigDecimal> existencias = new LinkedHashMap<>();
    private int solicitudesDeDescuento;
    private Integer skuRechazoProximoLote;

    /**
     * Crea un inventario de prueba vacío sin solicitudes registradas.
     */
    public InventarioDePrueba() {
    }

    /**
     * Registra o reemplaza un producto en el doble con el saldo indicado.
     *
     * @param producto producto que se expondrá bajo su SKU
     * @param saldo existencias disponibles para el SKU (cero o mayor)
     * @throws NullPointerException si el producto es nulo
     * @throws IllegalArgumentException si el saldo es nulo o negativo
     */
    public void configurarProducto(Producto producto, BigDecimal saldo) {
        Objects.requireNonNull(producto, "producto");
        if (saldo == null || saldo.signum() < 0) {
            throw new IllegalArgumentException("El saldo debe ser cero o mayor.");
        }
        productos.put(producto.getSku(), producto);
        existencias.put(producto.getSku(), saldo);
    }

    /**
     * Devuelve cuántas veces se ha invocado {@link #descontarLote(Map)}.
     *
     * @return el número acumulado de solicitudes de descuento en lote
     */
    public int getSolicitudesDeDescuento() {
        return solicitudesDeDescuento;
    }

    /**
     * Fuerza que la próxima llamada a {@link #descontarLote(Map)} falle por stock insuficiente
     * para el SKU indicado sin alterar ningún saldo.
     *
     * @param sku identificador del producto con el que se simulará el rechazo del lote
     */
    public void rechazarProximoLote(int sku) {
        this.skuRechazoProximoLote = sku;
    }

    /**
     * Lista los productos activos con existencias positivas en el orden configurado.
     *
     * @return una lista no modificable de productos disponibles
     */
    @Override
    public List<Producto> listarProductosDisponibles() {
        return productos.values().stream()
                .filter(Producto::isActivo)
                .filter(producto -> existencias.get(producto.getSku()).compareTo(BigDecimal.ZERO) > 0)
                .toList();
    }

    /**
     * Consulta un producto activo por su SKU.
     *
     * @param sku identificador del producto
     * @return el producto activo correspondiente
     * @throws IllegalArgumentException si el SKU no existe o el producto está inactivo
     */
    @Override
    public Producto consultarProductoActivo(int sku) {
        Producto producto = productos.get(sku);
        if (producto == null) {
            throw new IllegalArgumentException("No existe un producto con SKU %d.".formatted(sku));
        }
        if (!producto.isActivo()) {
            throw new IllegalArgumentException("El producto con SKU %d está inactivo.".formatted(sku));
        }
        return producto;
    }

    /**
     * Consulta el saldo configurado para un SKU registrado.
     *
     * @param sku identificador del producto
     * @return el saldo actual del SKU
     * @throws IllegalArgumentException si el SKU no existe
     */
    @Override
    public BigDecimal consultarExistencias(int sku) {
        if (!productos.containsKey(sku)) {
            throw new IllegalArgumentException("No existe un producto con SKU %d.".formatted(sku));
        }
        return existencias.get(sku);
    }

    /**
     * Valida y descuenta en una sola operación las cantidades solicitadas por SKU,
     * conservando todos los saldos si ocurre un rechazo.
     *
     * @param cantidadesPorSku cantidades acumuladas por SKU
     * @throws IllegalArgumentException si el lote es nulo, contiene un SKU nulo/inexistente/inactivo
     *                                  o alguna cantidad es inválida
     * @throws StockInsuficienteException si se programó un rechazo o si un SKU no tiene saldo suficiente
     */
    @Override
    public void descontarLote(Map<Integer, BigDecimal> cantidadesPorSku) {
        solicitudesDeDescuento++;
        if (cantidadesPorSku == null) {
            throw new IllegalArgumentException("El lote es obligatorio.");
        }
        for (Map.Entry<Integer, BigDecimal> partida : cantidadesPorSku.entrySet()) {
            if (partida.getKey() == null) {
                throw new IllegalArgumentException("El lote contiene un SKU nulo.");
            }
            consultarProductoActivo(partida.getKey()).validarCantidad(partida.getValue());
        }

        if (skuRechazoProximoLote != null) {
            int skuFallido = skuRechazoProximoLote;
            skuRechazoProximoLote = null;
            BigDecimal disponible = existencias.getOrDefault(skuFallido, BigDecimal.ZERO);
            throw new StockInsuficienteException(skuFallido, disponible);
        }

        Map<Integer, BigDecimal> saldosNuevos = new LinkedHashMap<>();
        for (Map.Entry<Integer, BigDecimal> partida : cantidadesPorSku.entrySet()) {
            int sku = partida.getKey();
            BigDecimal saldo = existencias.get(sku);
            if (saldo.compareTo(partida.getValue()) < 0) {
                throw new StockInsuficienteException(sku, saldo);
            }
            saldosNuevos.put(sku, saldo.subtract(partida.getValue()));
        }
        existencias.putAll(saldosNuevos);
    }
}
