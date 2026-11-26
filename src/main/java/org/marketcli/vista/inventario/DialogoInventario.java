package org.marketcli.vista.inventario;

import org.marketcli.controlador.inventario.ControladorInventario;
import org.marketcli.modelo.inventario.EntradaMercancia;
import org.marketcli.modelo.inventario.Producto;
import org.marketcli.modelo.inventario.Proveedor;
import org.marketcli.modelo.inventario.TipoMedida;
import org.marketcli.util.lectura.LectorConsola;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * Captura los datos de inventario en consola y muestra los resultados del controlador.
 */
public final class DialogoInventario {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    protected final LectorConsola lector;
    protected final ControladorInventario controladorInventario;

    /**
     * Crea el diálogo con el lector de consola y el controlador de inventario.
     *
     * @param lector lector compartido de consola
     * @param controladorInventario controlador al que se delegan las operaciones
     * @throws NullPointerException si algún argumento es nulo
     */
    public DialogoInventario(LectorConsola lector, ControladorInventario controladorInventario) {
        this.lector = Objects.requireNonNull(lector, "lector");
        this.controladorInventario = Objects.requireNonNull(controladorInventario, "controladorInventario");
    }

    /**
     * Pide nombre, medida y precio, y registra el producto.
     */
    public void registrarProducto() {
        atenderRechazos(() -> {
            String nombre = lector.pedirTexto("Nombre: ");
            TipoMedida medida = pedirMedida();
            BigDecimal precio = lector.pedirDecimal("Precio: ");
            Producto producto = controladorInventario.registrarProducto(nombre, medida, precio);
            System.out.println("Producto registrado con SKU %d.".formatted(producto.getSku()));
        });
    }

    /**
     * Muestra todos los productos registrados con su precio, medida, existencias y estado.
     */
    public void consultarProductos() {
        atenderRechazos(() -> {
            List<Producto> productos = controladorInventario.listarProductos();
            if (productos.isEmpty()) {
                System.out.println("No hay productos registrados.");
                return;
            }
            for (Producto producto : productos) {
                System.out.println("SKU %d | %s | %s | %s | Existencias: %s | %s".formatted(
                        producto.getSku(),
                        producto.getNombre(),
                        formatearPrecio(producto.getPrecio()),
                        describirMedida(producto.getMedida()),
                        formatearExistencias(controladorInventario.consultarExistencias(producto.getSku())),
                        producto.isActivo() ? "Activo" : "Inactivo"));
            }
        });
    }

    /**
     * Pide un SKU, y luego el nombre y el precio nuevos del producto.
     */
    public void modificarProducto() {
        atenderRechazos(() -> {
            int sku = lector.pedirEnteroPositivo("SKU: ");
            controladorInventario.consultarProducto(sku);
            String nombre = lector.pedirTexto("Nuevo nombre: ");
            BigDecimal precio = lector.pedirDecimal("Nuevo precio: ");
            controladorInventario.modificarProducto(sku, nombre, precio);
            System.out.println("Producto actualizado.");
        });
    }

    /**
     * Pide un SKU y da de baja el producto si no tiene existencias.
     */
    public void darDeBajaProducto() {
        atenderRechazos(() -> {
            int sku = lector.pedirEnteroPositivo("SKU: ");
            controladorInventario.darDeBajaProducto(sku);
            System.out.println("Producto dado de baja.");
        });
    }

    /**
     * Pide un nombre y registra el proveedor.
     */
    public void registrarProveedor() {
        atenderRechazos(() -> {
            String nombre = lector.pedirTexto("Nombre del proveedor: ");
            Proveedor proveedor = controladorInventario.registrarProveedor(nombre);
            System.out.println("Proveedor registrado con id %d.".formatted(proveedor.getId()));
        });
    }

    /**
     * Pide el id de un proveedor y su nombre nuevo.
     */
    public void modificarProveedor() {
        atenderRechazos(() -> {
            int proveedorId = lector.pedirEnteroPositivo("Id del proveedor: ");
            String nombre = lector.pedirTexto("Nuevo nombre: ");
            controladorInventario.modificarProveedor(proveedorId, nombre);
            System.out.println("Proveedor actualizado.");
        });
    }

    /**
     * Pide el id de un proveedor y lo da de baja.
     */
    public void darDeBajaProveedor() {
        atenderRechazos(() -> {
            int proveedorId = lector.pedirEnteroPositivo("Id del proveedor: ");
            controladorInventario.darDeBajaProveedor(proveedorId);
            System.out.println("Proveedor dado de baja.");
        });
    }

    /**
     * Muestra los productos y proveedores activos, y luego pide SKU, proveedor y cantidad.
     * Si no hay productos o proveedores activos, avisa y no pide datos.
     */
    public void registrarEntrada() {
        atenderRechazos(() -> {
            List<Producto> productos = controladorInventario.listarProductosActivos();
            List<Proveedor> proveedores = controladorInventario.listarProveedoresActivos();
            if (productos.isEmpty()) {
                System.out.println("No hay productos activos.");
                return;
            }
            if (proveedores.isEmpty()) {
                System.out.println("No hay proveedores activos.");
                return;
            }

            System.out.println("Productos activos:");
            for (Producto producto : productos) {
                System.out.println("%d) %s (%s)".formatted(
                        producto.getSku(), producto.getNombre(), describirMedida(producto.getMedida())));
            }
            System.out.println("Proveedores activos:");
            for (Proveedor proveedor : proveedores) {
                System.out.println("%d) %s".formatted(proveedor.getId(), proveedor.getNombre()));
            }

            int sku = lector.pedirEnteroPositivo("SKU: ");
            int proveedorId = lector.pedirEnteroPositivo("Id del proveedor: ");
            BigDecimal cantidad = lector.pedirDecimal("Cantidad: ");
            EntradaMercancia entrada = controladorInventario.registrarEntrada(sku, proveedorId, cantidad);
            System.out.println("Entrada %d registrada. Existencias: %s".formatted(
                    entrada.getId(), formatearExistencias(controladorInventario.consultarExistencias(sku))));
        });
    }

    /**
     * Pide un SKU y muestra su nombre, precio, medida y existencias.
     */
    public void consultarPrecioYExistencias() {
        atenderRechazos(() -> {
            int sku = lector.pedirEnteroPositivo("SKU: ");
            Producto producto = controladorInventario.consultarProducto(sku);
            BigDecimal existencias = controladorInventario.consultarExistencias(sku);
            System.out.println("Nombre: " + producto.getNombre());
            System.out.println("Precio: " + formatearPrecio(producto.getPrecio()));
            System.out.println("Medida: " + describirMedida(producto.getMedida()));
            System.out.println("Existencias: " + formatearExistencias(existencias));
        });
    }

    /**
     * Muestra cada entrada con los datos de producto y proveedor guardados al registrarla.
     */
    public void mostrarHistorialEntradas() {
        atenderRechazos(() -> {
            List<EntradaMercancia> entradas = controladorInventario.listarEntradas();
            if (entradas.isEmpty()) {
                System.out.println("No hay entradas registradas.");
                return;
            }
            for (EntradaMercancia entrada : entradas) {
                System.out.println(
                        "Entrada %d | Fecha: %s | SKU: %d | Cantidad: %s | Medida: %s | Proveedor: %d - %s".formatted(
                                entrada.getId(),
                                entrada.getFechaHora().format(FORMATO_FECHA),
                                entrada.getSku(),
                                entrada.getCantidad().toPlainString(),
                                describirMedida(entrada.getMedida()),
                                entrada.getProveedorId(),
                                entrada.getNombreProveedor()));
            }
        });
    }

    private TipoMedida pedirMedida() {
        System.out.println("1) Unidad");
        System.out.println("2) Kilogramo");
        return lector.pedirOpcion("Medida: ", 1, 2) == 1 ? TipoMedida.UNIDAD : TipoMedida.KILOGRAMO;
    }

    private static void atenderRechazos(Runnable operacion) {
        try {
            operacion.run();
        } catch (IllegalArgumentException | IllegalStateException rechazo) {
            System.out.println(rechazo.getMessage());
        }
    }

    private static String describirMedida(TipoMedida medida) {
        return switch (medida) {
            case UNIDAD -> "Unidad";
            case KILOGRAMO -> "Kilogramo";
        };
    }

    private static String formatearPrecio(BigDecimal precio) {
        BigDecimal precioVisible = precio.stripTrailingZeros();
        int decimales = Math.max(2, precioVisible.scale());
        return "$" + precioVisible.setScale(decimales).toPlainString();
    }

    private static String formatearExistencias(BigDecimal existencias) {
        return existencias.signum() == 0 ? "0" : existencias.toPlainString();
    }
}
