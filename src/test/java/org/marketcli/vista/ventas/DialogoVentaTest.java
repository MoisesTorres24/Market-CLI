package org.marketcli.vista.ventas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.marketcli.controlador.ventas.ControladorVentas;
import org.marketcli.modelo.acceso.Rol;
import org.marketcli.modelo.acceso.Usuario;
import org.marketcli.modelo.inventario.TipoMedida;
import org.marketcli.modelo.ventas.Venta;
import org.marketcli.soporte.InventarioDePrueba;
import org.marketcli.soporte.ProductoDePrueba;
import org.marketcli.util.lectura.LectorConsola;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DialogoVentaTest {

    private InventarioDePrueba inventario;
    private ControladorVentas controlador;
    private Usuario cajero;

    @BeforeEach
    void prepararContexto() {
        inventario = new InventarioDePrueba();
        controlador = new ControladorVentas(inventario);
        cajero = new Usuario("cajero1", "clave123", Rol.CAJERO);
    }

    @Test
    void muestraProductosDisponiblesConPrecioYSaldo() {
        inventario.configurarProducto(
                new ProductoDePrueba(1, "Frijol", new BigDecimal("20.04"), TipoMedida.KILOGRAMO),
                new BigDecimal("2.500"));
        inventario.configurarProducto(
                new ProductoDePrueba(2, "Azúcar", new BigDecimal("25.00"), TipoMedida.KILOGRAMO),
                BigDecimal.ZERO);

        String guion = """
                1
                1
                0.250
                0
                """;
        String texto = ejecutarConEntrada(guion, dialogo -> dialogo.prepararVenta(cajero));

        assertTrue(texto.contains("SKU 1"));
        assertTrue(texto.contains("Frijol"));
        assertTrue(texto.contains("$20.04"));
        assertTrue(texto.contains("Disponible: 2.500"));
        assertFalse(texto.contains("Azúcar"));
    }

    @Test
    void descartaCapturaAlCancelar() {
        inventario.configurarProducto(
                new ProductoDePrueba(1, "Aceite", new BigDecimal("30.00"), TipoMedida.UNIDAD),
                new BigDecimal("5"));

        String guion = """
                1
                1
                2
                4
                0
                """;
        String texto = ejecutarConEntrada(guion, dialogo -> dialogo.prepararVenta(cajero));

        assertTrue(texto.contains("Total: $60.00"));
        assertTrue(texto.contains("Venta cancelada."));
        assertEquals(0, inventario.getSolicitudesDeDescuento());
        assertEquals(0, new BigDecimal("5").compareTo(inventario.consultarExistencias(1)));
        assertTrue(controlador.listarVentas().isEmpty());
    }

    @Test
    void conservaCantidadAnteriorAlExcederStock() {
        inventario.configurarProducto(
                new ProductoDePrueba(1, "Leche", new BigDecimal("25.00"), TipoMedida.UNIDAD),
                new BigDecimal("3"));

        String guion = """
                1
                1
                2
                1
                1
                2
                5
                50.00
                """;
        String texto = ejecutarConEntrada(guion, dialogo -> dialogo.prepararVenta(cajero));

        assertTrue(texto.contains("Disponible: 3"));
        assertEquals(1, controlador.listarVentas().size());
        Venta venta = controlador.listarVentas().getFirst();
        assertEquals(0, new BigDecimal("2").compareTo(venta.getPartidas().getFirst().getCantidad()));
        assertEquals(new BigDecimal("50.00"), venta.getTotal());
        assertEquals(new BigDecimal("0.00"), venta.getCambio());
        assertEquals(0, new BigDecimal("1").compareTo(inventario.consultarExistencias(1)));
    }

    @Test
    void permiteCorregirPagoSinRecapturarPartidas() {
        inventario.configurarProducto(
                new ProductoDePrueba(1, "Frijol", new BigDecimal("20.04"), TipoMedida.KILOGRAMO),
                new BigDecimal("2.000"));

        String guion = """
                1
                1
                0.250
                5
                4.00
                5
                10.00
                """;
        String texto = ejecutarConEntrada(guion, dialogo -> dialogo.prepararVenta(cajero));

        assertTrue(texto.contains("Cambio: $4.99"));
        assertEquals(1, inventario.getSolicitudesDeDescuento());
        assertEquals(1, controlador.listarVentas().size());
        Venta venta = controlador.listarVentas().getFirst();
        assertEquals(new BigDecimal("5.01"), venta.getTotal());
        assertEquals(new BigDecimal("4.99"), venta.getCambio());
        assertEquals(0, new BigDecimal("1.750").compareTo(inventario.consultarExistencias(1)));
    }

    @Test
    void permiteEditarYRetirarPartidas() {
        inventario.configurarProducto(
                new ProductoDePrueba(1, "Aceite", new BigDecimal("30.00"), TipoMedida.UNIDAD),
                new BigDecimal("5"));
        inventario.configurarProducto(
                new ProductoDePrueba(2, "Sal", new BigDecimal("20.00"), TipoMedida.UNIDAD),
                new BigDecimal("4"));

        String guion = """
                1
                1
                1
                1
                2
                2
                2
                1
                3
                3
                2
                5
                100.00
                """;
        String texto = ejecutarConEntrada(guion, dialogo -> dialogo.prepararVenta(cajero));

        assertTrue(texto.contains("Cambio: $10.00"));
        assertEquals(1, controlador.listarVentas().size());
        Venta venta = controlador.listarVentas().getFirst();
        assertEquals(1, venta.getPartidas().size());
        assertEquals(1, venta.getPartidas().getFirst().getSku());
        assertEquals(0, new BigDecimal("3").compareTo(venta.getPartidas().getFirst().getCantidad()));
        assertEquals(new BigDecimal("90.00"), venta.getTotal());
        assertEquals(0, new BigDecimal("2").compareTo(inventario.consultarExistencias(1)));
        assertEquals(0, new BigDecimal("4").compareTo(inventario.consultarExistencias(2)));
    }

    @Test
    void muestraPrecioHistoricoEnDetalle() {
        inventario.configurarProducto(
                new ProductoDePrueba(1, "Frijol", new BigDecimal("20.04"), TipoMedida.KILOGRAMO),
                new BigDecimal("2.000"));

        String guionVenta = """
                1
                1
                0.250
                5
                10.00
                """;

        PrintStream salidaOriginal = System.out;
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        String historial;
        try (Scanner scanner = new Scanner(guionVenta);
             PrintStream consola = new PrintStream(salida, true, StandardCharsets.UTF_8)) {
            System.setOut(consola);
            DialogoVenta dialogo = new DialogoVenta(new LectorConsola(scanner), controlador);

            dialogo.prepararVenta(cajero);

            inventario.configurarProducto(
                    new ProductoDePrueba(1, "Frijol selecto", new BigDecimal("30.00"), TipoMedida.KILOGRAMO),
                    new BigDecimal("1.750"));

            salida.reset();
            dialogo.mostrarHistorialVentas();
            historial = salida.toString(StandardCharsets.UTF_8);
        } finally {
            System.setOut(salidaOriginal);
        }

        Venta venta = controlador.listarVentas().getFirst();
        String fecha = venta.getFechaHora().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        assertTrue(historial.contains("Venta " + venta.getId()));
        assertTrue(historial.contains("Fecha: " + fecha));
        assertTrue(historial.contains("Cajero: cajero1"));
        assertTrue(historial.contains("Total: $5.01"));
        assertTrue(historial.contains("Efectivo: $10.00"));
        assertTrue(historial.contains("Cambio: $4.99"));
        assertTrue(historial.contains("SKU: 1"));
        assertTrue(historial.contains("Producto: Frijol"));
        assertTrue(historial.contains("Medida: Kilogramo"));
        assertTrue(historial.contains("Cantidad: 0.250"));
        assertTrue(historial.contains("Precio unitario: $20.04"));
        assertTrue(historial.contains("Importe: $5.01"));
        assertFalse(historial.contains("Frijol selecto"));
        assertFalse(historial.contains("$30.00"));
    }

    private String ejecutarConEntrada(String entrada, java.util.function.Consumer<DialogoVenta> accion) {
        PrintStream salidaOriginal = System.out;
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        try (Scanner scanner = new Scanner(entrada);
             PrintStream consola = new PrintStream(salida, true, StandardCharsets.UTF_8)) {
            System.setOut(consola);
            DialogoVenta dialogo = new DialogoVenta(new LectorConsola(scanner), controlador);
            accion.accept(dialogo);
            return salida.toString(StandardCharsets.UTF_8);
        } finally {
            System.setOut(salidaOriginal);
        }
    }
}
