package org.marketcli.modelo.inventario;

import org.marketcli.util.excepcion.StockInsuficienteException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Administra el catálogo de productos y proveedores, y las existencias por SKU.
 */
public final class Inventario implements InventarioParaVenta {

    protected final Map<Integer, Producto> productos;
    protected final Map<Integer, Proveedor> proveedores;
    protected final Map<Integer, BigDecimal> existencias;
    protected final List<EntradaMercancia> entradas;
    protected int siguienteSku;
    protected int siguienteProveedorId;
    protected int siguienteEntradaId;

    /**
     * Crea un inventario vacío.
     */
    public Inventario() {
        this.productos = new LinkedHashMap<>();
        this.proveedores = new LinkedHashMap<>();
        this.existencias = new LinkedHashMap<>();
        this.entradas = new ArrayList<>();
        this.siguienteSku = 1;
        this.siguienteProveedorId = 1;
        this.siguienteEntradaId = 1;
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
        Objects.requireNonNull(medida, "medida");
        int sku = siguienteSku;
        Producto producto = switch (medida) {
            case UNIDAD -> new ProductoUnidad(sku, nombre, precio);
            case KILOGRAMO -> new ProductoGranel(sku, nombre, precio);
        };
        productos.put(sku, producto);
        existencias.put(sku, BigDecimal.ZERO);
        siguienteSku++;
        return producto;
    }

    /**
     * Lista todos los productos registrados, activos o no.
     *
     * @return una lista no modificable de los productos
     */
    public List<Producto> listarProductos() {
        return List.copyOf(productos.values());
    }

    /**
     * Lista los productos activos, tengan o no existencias.
     *
     * @return una lista no modificable de los productos activos
     */
    public List<Producto> listarProductosActivos() {
        return productos.values().stream()
                .filter(Producto::isActivo)
                .toList();
    }

    /**
     * Consulta un producto registrado por su SKU, activo o no.
     *
     * @param sku identificador del producto
     * @return el producto que corresponde al SKU
     * @throws IllegalArgumentException si el SKU no existe
     */
    public Producto consultarProducto(int sku) {
        Producto producto = productos.get(sku);
        if (producto == null) {
            throw new IllegalArgumentException("No existe un producto con SKU %d.".formatted(sku));
        }
        return producto;
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
        consultarProducto(sku).actualizarDatos(nombre, precio);
    }

    /**
     * Da de baja un producto sin existencias y conserva su SKU y su historial.
     *
     * @param sku identificador del producto
     * @throws IllegalArgumentException si el SKU no existe
     * @throws IllegalStateException si el producto todavía tiene existencias
     */
    public void darDeBajaProducto(int sku) {
        Producto producto = consultarProducto(sku);
        if (existencias.get(sku).compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException(
                    "El producto con SKU %d todavía tiene existencias.".formatted(sku));
        }
        producto.darDeBaja();
    }

    /**
     * Registra un proveedor activo con un id nuevo.
     *
     * @param nombre nombre del proveedor
     * @return el proveedor registrado
     * @throws IllegalArgumentException si el nombre está vacío
     */
    public Proveedor registrarProveedor(String nombre) {
        Proveedor proveedor = new Proveedor(siguienteProveedorId, nombre);
        proveedores.put(proveedor.getId(), proveedor);
        siguienteProveedorId++;
        return proveedor;
    }

    /**
     * Lista los proveedores activos.
     *
     * @return una lista no modificable de los proveedores activos
     */
    public List<Proveedor> listarProveedoresActivos() {
        return proveedores.values().stream()
                .filter(Proveedor::isActivo)
                .toList();
    }

    /**
     * Cambia el nombre de un proveedor y conserva su id.
     *
     * @param proveedorId identificador del proveedor
     * @param nombre nuevo nombre del proveedor
     * @throws IllegalArgumentException si el proveedor no existe o el nombre está vacío
     */
    public void modificarProveedor(int proveedorId, String nombre) {
        consultarProveedor(proveedorId).cambiarNombre(nombre);
    }

    /**
     * Da de baja un proveedor y conserva su id.
     *
     * @param proveedorId identificador del proveedor
     * @throws IllegalArgumentException si el proveedor no existe
     */
    public void darDeBajaProveedor(int proveedorId) {
        consultarProveedor(proveedorId).darDeBaja();
    }

    /**
     * Registra la recepción de mercancía de un proveedor y suma la cantidad al saldo del producto.
     * Un rechazo no cambia ni el saldo ni el historial.
     *
     * @param sku identificador del producto recibido
     * @param proveedorId identificador del proveedor que entrega
     * @param cantidad cantidad recibida en la medida del producto
     * @return la entrada registrada
     * @throws IllegalArgumentException si el producto o el proveedor no existen o están inactivos,
     *                                  o si la cantidad es inválida
     */
    public EntradaMercancia registrarEntrada(int sku, int proveedorId, BigDecimal cantidad) {
        Producto producto = consultarProductoActivo(sku);
        Proveedor proveedor = consultarProveedor(proveedorId);
        if (!proveedor.isActivo()) {
            throw new IllegalArgumentException(
                    "El proveedor con id %d está inactivo.".formatted(proveedorId));
        }
        producto.validarCantidad(cantidad);

        EntradaMercancia entrada = new EntradaMercancia(
                siguienteEntradaId, LocalDateTime.now(), producto, proveedor, cantidad);
        existencias.put(sku, existencias.get(sku).add(cantidad));
        entradas.add(entrada);
        siguienteEntradaId++;
        return entrada;
    }

    /**
     * Lista el historial de entradas en el orden en que se registraron.
     *
     * @return una copia no modificable del historial de entradas
     */
    public List<EntradaMercancia> listarEntradas() {
        return List.copyOf(entradas);
    }

    /**
     * Lista los productos activos con existencias positivas.
     *
     * @return una lista no modificable de los productos disponibles
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
     * @return el producto activo que corresponde al SKU
     * @throws IllegalArgumentException si el SKU no existe o el producto está inactivo
     */
    @Override
    public Producto consultarProductoActivo(int sku) {
        Producto producto = consultarProducto(sku);
        if (!producto.isActivo()) {
            throw new IllegalArgumentException("El producto con SKU %d está inactivo.".formatted(sku));
        }
        return producto;
    }

    /**
     * Consulta el saldo de un SKU registrado.
     *
     * @param sku identificador del producto
     * @return las existencias disponibles, con cero si el producto está agotado
     * @throws IllegalArgumentException si el SKU no existe
     */
    @Override
    public BigDecimal consultarExistencias(int sku) {
        consultarProducto(sku);
        return existencias.get(sku);
    }

    /**
     * Valida todas las partidas y calcula los saldos nuevos antes de descontar el lote.
     * Un rechazo conserva todos los saldos.
     *
     * @param cantidadesPorSku cantidades acumuladas por SKU
     * @throws IllegalArgumentException si el lote es nulo, o si un SKU no existe, está inactivo
     *                                  o su cantidad es inválida
     * @throws StockInsuficienteException si un SKU no tiene existencias suficientes
     */
    @Override
    public void descontarLote(Map<Integer, BigDecimal> cantidadesPorSku) {
        if (cantidadesPorSku == null) {
            throw new IllegalArgumentException("El lote es obligatorio.");
        }
        for (Map.Entry<Integer, BigDecimal> partida : cantidadesPorSku.entrySet()) {
            if (partida.getKey() == null) {
                throw new IllegalArgumentException("El lote contiene un SKU nulo.");
            }
            consultarProductoActivo(partida.getKey()).validarCantidad(partida.getValue());
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

    private Proveedor consultarProveedor(int proveedorId) {
        Proveedor proveedor = proveedores.get(proveedorId);
        if (proveedor == null) {
            throw new IllegalArgumentException(
                    "No existe un proveedor con id %d.".formatted(proveedorId));
        }
        return proveedor;
    }
}
