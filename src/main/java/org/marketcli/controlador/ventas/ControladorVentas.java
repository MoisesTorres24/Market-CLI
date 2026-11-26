package org.marketcli.controlador.ventas;

import org.marketcli.modelo.acceso.Rol;
import org.marketcli.modelo.acceso.Usuario;
import org.marketcli.modelo.inventario.InventarioParaVenta;
import org.marketcli.modelo.inventario.Producto;
import org.marketcli.modelo.ventas.PartidaVenta;
import org.marketcli.modelo.ventas.Venta;
import org.marketcli.util.excepcion.StockInsuficienteException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Coordina la validación de partidas, el cálculo del total, el cobro en efectivo con descuento en lote
 * y la consulta del historial de ventas en memoria.
 */
public final class ControladorVentas {

    // AGREGACIÓN: colabora con el inventario mediante su contrato para ventas sin controlar su ciclo de vida.
    protected final InventarioParaVenta inventario;
    // COMPOSICIÓN: el controlador resguarda internamente el historial de ventas confirmadas.
    protected final List<Venta> ventas;
    protected int siguienteVentaId;

    /**
     * Crea el controlador de ventas asociado al inventario disponible para caja.
     *
     * @param inventario contrato de consulta y descuento en lote del inventario
     * @throws NullPointerException si el inventario es nulo
     */
    public ControladorVentas(InventarioParaVenta inventario) {
        this.inventario = Objects.requireNonNull(inventario, "inventario");
        this.ventas = new ArrayList<>();
        this.siguienteVentaId = 1;
    }

    /**
     * Lista los productos activos que cuentan con existencias positivas para la venta.
     *
     * @return una lista no modificable de los productos disponibles
     */
    public List<Producto> listarProductosDisponibles() {
        return List.copyOf(inventario.listarProductosDisponibles());
    }

    /**
     * Consulta las existencias disponibles de un producto registrado por su SKU.
     *
     * @param sku identificador del producto
     * @return el saldo disponible del producto
     * @throws IllegalArgumentException si el SKU no existe
     */
    public BigDecimal consultarExistencias(int sku) {
        return inventario.consultarExistencias(sku);
    }

    /**
     * Suma una cantidad por agregar a la cantidad actual de un SKU y comprueba que el acumulado
     * sea válido para el producto y no supere las existencias disponibles.
     *
     * @param sku identificador del producto
     * @param cantidadActual cantidad previamente capturada para el SKU (cero si aún no está en la venta)
     * @param cantidadAAgregar cantidad positiva que se desea sumar a la partida
     * @return la nueva cantidad acumulada validada para el SKU
     * @throws IllegalArgumentException si el SKU no existe o está inactivo, si la cantidad actual es nula
     *                                  o negativa, o si alguna cantidad incumple la precisión del producto
     * @throws StockInsuficienteException si la suma supera las existencias disponibles del SKU
     */
    public BigDecimal validarAdicion(int sku, BigDecimal cantidadActual, BigDecimal cantidadAAgregar) {
        if (cantidadActual == null || cantidadActual.signum() < 0) {
            throw new IllegalArgumentException("La cantidad actual debe ser cero o mayor.");
        }
        Producto producto = inventario.consultarProductoActivo(sku);
        if (cantidadActual.signum() > 0) {
            producto.validarCantidad(cantidadActual);
        }
        producto.validarCantidad(cantidadAAgregar);

        BigDecimal cantidadAcumulada = cantidadActual.add(cantidadAAgregar);
        validarPartida(sku, cantidadAcumulada);
        return cantidadAcumulada;
    }

    /**
     * Valida que un producto esté activo, que la cantidad acumulada respete su medida y que
     * existan unidades o kilogramos suficientes para cubrirla.
     *
     * @param sku identificador del producto
     * @param cantidadAcumulada cantidad total solicitada para la partida
     * @throws IllegalArgumentException si el SKU no existe, está inactivo o la cantidad es inválida
     * @throws StockInsuficienteException si la cantidad acumulada excede las existencias disponibles
     */
    public void validarPartida(int sku, BigDecimal cantidadAcumulada) {
        Producto producto = inventario.consultarProductoActivo(sku);
        producto.validarCantidad(cantidadAcumulada);

        BigDecimal disponible = inventario.consultarExistencias(sku);
        if (disponible.compareTo(cantidadAcumulada) < 0) {
            throw new StockInsuficienteException(sku, disponible);
        }
    }

    /**
     * Calcula el total de una captura sumando el importe redondeado de la cantidad acumulada por cada SKU.
     *
     * @param cantidadesPorSku cantidades acumuladas por SKU
     * @return el total calculado de la captura, o {@link BigDecimal#ZERO} si el mapa está vacío
     * @throws IllegalArgumentException si el mapa es nulo, contiene un SKU nulo, inexistente o inactivo,
     *                                  o alguna cantidad es inválida
     */
    public BigDecimal calcularTotal(Map<Integer, BigDecimal> cantidadesPorSku) {
        if (cantidadesPorSku == null) {
            throw new IllegalArgumentException("Las partidas de la venta son obligatorias.");
        }
        if (cantidadesPorSku.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Integer, BigDecimal> partida : cantidadesPorSku.entrySet()) {
            if (partida.getKey() == null) {
                throw new IllegalArgumentException("La captura contiene un SKU nulo.");
            }
            Producto producto = inventario.consultarProductoActivo(partida.getKey());
            total = total.add(producto.calcularImporte(partida.getValue()));
        }
        return total;
    }

    /**
     * Devuelve el historial de ventas confirmadas en el orden en que se registraron.
     *
     * @return una lista no modificable de ventas confirmadas
     */
    public List<Venta> listarVentas() {
        return List.copyOf(ventas);
    }

    /**
     * Confirma una venta en efectivo, descuenta todas sus partidas en una sola operación de lote
     * y registra la venta en el historial. Si ocurre cualquier rechazo, conserva intactos el stock,
     * el historial de ventas y el mapa de captura del llamador.
     *
     * @param cajero cuenta autenticada con rol {@link Rol#CAJERO}
     * @param cantidadesPorSku cantidades acumuladas por SKU capturadas para la venta
     * @param efectivo efectivo entregado por el cliente
     * @return la venta confirmada y almacenada en el historial
     * @throws NullPointerException si el cajero es nulo
     * @throws IllegalArgumentException si la cuenta no corresponde al rol {@link Rol#CAJERO},
     *                                  la captura es nula o vacía, algún SKU/cantidad es inválido
     *                                  o el efectivo es insuficiente para cubrir el total
     * @throws StockInsuficienteException si el lote no puede descontarse por falta de existencias
     */
    public Venta confirmarVenta(Usuario cajero, Map<Integer, BigDecimal> cantidadesPorSku, BigDecimal efectivo) {
        // DEPENDENCIA: verifica el rol del cajero y toma su nombre para registrar la venta.
        Objects.requireNonNull(cajero, "cajero");
        if (cajero.getRol() != Rol.CAJERO) {
            throw new IllegalArgumentException("Solo una cuenta con rol CAJERO puede confirmar ventas.");
        }
        if (cantidadesPorSku == null || cantidadesPorSku.isEmpty()) {
            throw new IllegalArgumentException("La venta debe incluir al menos una partida.");
        }

        Map<Integer, BigDecimal> lote = new LinkedHashMap<>();
        for (Map.Entry<Integer, BigDecimal> entrada : cantidadesPorSku.entrySet()) {
            if (entrada.getKey() == null) {
                throw new IllegalArgumentException("La venta contiene un SKU nulo.");
            }
            lote.put(entrada.getKey(), entrada.getValue());
        }

        List<PartidaVenta> partidas = new ArrayList<>(lote.size());
        for (Map.Entry<Integer, BigDecimal> entrada : lote.entrySet()) {
            Producto producto = inventario.consultarProductoActivo(entrada.getKey());
            partidas.add(new PartidaVenta(producto, entrada.getValue()));
        }

        Venta venta = new Venta(
                siguienteVentaId,
                LocalDateTime.now(),
                cajero.getNombreUsuario(),
                partidas,
                efectivo);

        inventario.descontarLote(Collections.unmodifiableMap(lote));
        ventas.add(venta);
        siguienteVentaId++;
        return venta;
    }
}
