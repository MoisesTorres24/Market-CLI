package org.marketcli.vista.ventas;

import org.marketcli.controlador.ventas.ControladorVentas;
import org.marketcli.modelo.acceso.Rol;
import org.marketcli.modelo.acceso.Usuario;
import org.marketcli.modelo.inventario.Producto;
import org.marketcli.modelo.inventario.TipoMedida;
import org.marketcli.modelo.ventas.PartidaVenta;
import org.marketcli.modelo.ventas.Venta;
import org.marketcli.util.excepcion.StockInsuficienteException;
import org.marketcli.util.lectura.LectorConsola;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Conduce en consola la captura, edición, cancelación y cobro de una venta,
 * así como la consulta del historial de ventas confirmadas.
 */
public final class DialogoVenta {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // AGREGACIÓN: emplea el lector y el controlador recibidos sin administrar su ciclo de vida.
    protected final LectorConsola lector;
    protected final ControladorVentas controladorVentas;

    /**
     * Crea el diálogo de ventas con el lector de consola y el controlador de ventas.
     *
     * @param lector lector compartido de consola
     * @param controladorVentas controlador al que se delegan las validaciones y confirmaciones
     * @throws NullPointerException si algún argumento es nulo
     */
    public DialogoVenta(LectorConsola lector, ControladorVentas controladorVentas) {
        this.lector = Objects.requireNonNull(lector, "lector");
        this.controladorVentas = Objects.requireNonNull(controladorVentas, "controladorVentas");
    }

    /**
     * Ejecuta el flujo interactivo para armar una venta en curso permitiendo agregar productos
     * con stock, cambiar cantidades, retirar partidas, consultar el total, cobrar en efectivo
     * o cancelar descartando la captura sin afectar el inventario ni el historial.
     *
     * @param cajero usuario autenticado con rol {@link Rol#CAJERO}
     * @throws NullPointerException si el cajero es nulo
     * @throws IllegalArgumentException si la cuenta no tiene rol {@link Rol#CAJERO}
     */
    public void prepararVenta(Usuario cajero) {
        Objects.requireNonNull(cajero, "cajero");
        if (cajero.getRol() != Rol.CAJERO) {
            throw new IllegalArgumentException("Solo una cuenta con rol CAJERO puede preparar ventas.");
        }

        Map<Integer, BigDecimal> captura = new LinkedHashMap<>();
        while (true) {
            mostrarMenuPreparacion();
            int opcion = lector.pedirOpcion("Opción: ", 0, 5);
            switch (opcion) {
                case 1 -> agregarProducto(captura);
                case 2 -> cambiarCantidad(captura);
                case 3 -> retirarPartida(captura);
                case 4 -> consultarTotalActual(captura);
                case 5 -> {
                    if (intentarCobro(cajero, captura)) {
                        return;
                    }
                }
                case 0 -> {
                    captura.clear();
                    System.out.println("Venta cancelada.");
                    return;
                }
                default -> throw new IllegalStateException("Opción no reconocida: " + opcion);
            }
        }
    }

    /**
     * Muestra todas las ventas confirmadas junto con el detalle histórico de sus partidas.
     */
    public void mostrarHistorialVentas() {
        List<Venta> ventas = controladorVentas.listarVentas();
        if (ventas.isEmpty()) {
            System.out.println("No hay ventas registradas.");
            return;
        }
        for (Venta venta : ventas) {
            System.out.println("Venta %d | Fecha: %s | Cajero: %s | Total: %s | Efectivo: %s | Cambio: %s".formatted(
                    venta.getId(),
                    venta.getFechaHora().format(FORMATO_FECHA),
                    venta.getNombreCajero(),
                    formatearMoneda(venta.getTotal()),
                    formatearMoneda(venta.getEfectivo()),
                    formatearMoneda(venta.getCambio())));
            for (PartidaVenta partida : venta.getPartidas()) {
                System.out.println(
                        "  SKU: %d | Producto: %s | Medida: %s | Cantidad: %s | Precio unitario: %s | Importe: %s"
                                .formatted(
                                        partida.getSku(),
                                        partida.getNombreProducto(),
                                        describirMedida(partida.getMedida()),
                                        formatearCantidad(partida.getCantidad()),
                                        formatearMoneda(partida.getPrecioUnitario()),
                                        formatearMoneda(partida.getImporte())));
            }
        }
    }

    private static void mostrarMenuPreparacion() {
        System.out.println("1) Agregar producto");
        System.out.println("2) Cambiar cantidad");
        System.out.println("3) Retirar partida");
        System.out.println("4) Consultar total");
        System.out.println("5) Cobrar");
        System.out.println("0) Cancelar");
    }

    private void agregarProducto(Map<Integer, BigDecimal> captura) {
        List<Producto> disponibles = controladorVentas.listarProductosDisponibles();
        if (disponibles.isEmpty()) {
            System.out.println("No hay productos disponibles con existencias.");
            return;
        }
        System.out.println("Productos disponibles:");
        for (Producto producto : disponibles) {
            BigDecimal saldo = controladorVentas.consultarExistencias(producto.getSku());
            System.out.println("SKU %d | %s | Precio: %s | %s | Disponible: %s".formatted(
                    producto.getSku(),
                    producto.getNombre(),
                    formatearMoneda(producto.getPrecio()),
                    describirMedida(producto.getMedida()),
                    formatearCantidad(saldo)));
        }

        int sku = lector.pedirEnteroPositivo("SKU: ");
        BigDecimal cantidadAAgregar = lector.pedirDecimal("Cantidad: ");
        BigDecimal cantidadActual = captura.getOrDefault(sku, BigDecimal.ZERO);
        try {
            BigDecimal cantidadAcumulada = controladorVentas.validarAdicion(sku, cantidadActual, cantidadAAgregar);
            captura.put(sku, cantidadAcumulada);
            System.out.println("Partida actualizada. SKU %d | Cantidad: %s".formatted(
                    sku, formatearCantidad(cantidadAcumulada)));
        } catch (StockInsuficienteException sinStock) {
            informarStockInsuficiente(sinStock);
        } catch (IllegalArgumentException rechazo) {
            System.out.println(rechazo.getMessage());
        }
    }

    private void cambiarCantidad(Map<Integer, BigDecimal> captura) {
        if (captura.isEmpty()) {
            System.out.println("No hay partidas en la venta actual.");
            return;
        }
        mostrarCapturaActual(captura);
        int sku = lector.pedirEnteroPositivo("SKU: ");
        if (!captura.containsKey(sku)) {
            System.out.println("El SKU %d no forma parte de la venta actual.".formatted(sku));
            return;
        }
        BigDecimal nuevaCantidad = lector.pedirDecimal("Nueva cantidad: ");
        try {
            controladorVentas.validarPartida(sku, nuevaCantidad);
            captura.put(sku, nuevaCantidad);
            System.out.println("Cantidad actualizada. SKU %d | Cantidad: %s".formatted(
                    sku, formatearCantidad(nuevaCantidad)));
        } catch (StockInsuficienteException sinStock) {
            informarStockInsuficiente(sinStock);
        } catch (IllegalArgumentException rechazo) {
            System.out.println(rechazo.getMessage());
        }
    }

    private void retirarPartida(Map<Integer, BigDecimal> captura) {
        if (captura.isEmpty()) {
            System.out.println("No hay partidas en la venta actual.");
            return;
        }
        mostrarCapturaActual(captura);
        int sku = lector.pedirEnteroPositivo("SKU: ");
        if (captura.remove(sku) == null) {
            System.out.println("El SKU %d no forma parte de la venta actual.".formatted(sku));
            return;
        }
        System.out.println("Partida con SKU %d retirada.".formatted(sku));
    }

    private void consultarTotalActual(Map<Integer, BigDecimal> captura) {
        try {
            if (!captura.isEmpty()) {
                mostrarCapturaActual(captura);
            }
            BigDecimal total = controladorVentas.calcularTotal(captura);
            System.out.println("Total: " + formatearMoneda(total));
        } catch (IllegalArgumentException rechazo) {
            System.out.println(rechazo.getMessage());
        }
    }

    private boolean intentarCobro(Usuario cajero, Map<Integer, BigDecimal> captura) {
        if (captura.isEmpty()) {
            System.out.println("La venta debe incluir al menos una partida.");
            return false;
        }
        try {
            BigDecimal total = controladorVentas.calcularTotal(captura);
            System.out.println("Total a cobrar: " + formatearMoneda(total));
            BigDecimal efectivo = lector.pedirDecimal("Efectivo: ");
            Venta venta = controladorVentas.confirmarVenta(cajero, captura, efectivo);
            captura.clear();
            System.out.println("Venta %d confirmada | Total: %s | Efectivo: %s | Cambio: %s".formatted(
                    venta.getId(),
                    formatearMoneda(venta.getTotal()),
                    formatearMoneda(venta.getEfectivo()),
                    formatearMoneda(venta.getCambio())));
            return true;
        } catch (StockInsuficienteException sinStock) {
            informarStockInsuficiente(sinStock);
            return false;
        } catch (IllegalArgumentException rechazo) {
            System.out.println(rechazo.getMessage());
            return false;
        }
    }

    private static void mostrarCapturaActual(Map<Integer, BigDecimal> captura) {
        System.out.println("Partidas en captura:");
        for (Map.Entry<Integer, BigDecimal> partida : captura.entrySet()) {
            System.out.println("SKU %d | Cantidad: %s".formatted(
                    partida.getKey(), formatearCantidad(partida.getValue())));
        }
    }

    private static void informarStockInsuficiente(StockInsuficienteException sinStock) {
        System.out.println("Stock insuficiente para el SKU %d. Disponible: %s".formatted(
                sinStock.getSku(), formatearCantidad(sinStock.getDisponible())));
    }

    private static String describirMedida(TipoMedida medida) {
        return switch (medida) {
            case UNIDAD -> "Unidad";
            case KILOGRAMO -> "Kilogramo";
        };
    }

    private static String formatearMoneda(BigDecimal monto) {
        BigDecimal montoVisible = monto.stripTrailingZeros();
        int decimales = Math.max(2, montoVisible.scale());
        return "$" + montoVisible.setScale(decimales).toPlainString();
    }

    private static String formatearCantidad(BigDecimal cantidad) {
        return cantidad.signum() == 0 ? "0" : cantidad.toPlainString();
    }
}
