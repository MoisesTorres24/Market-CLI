package org.marketcli.vista.inventario;

import org.junit.jupiter.api.Test;
import org.marketcli.controlador.inventario.ControladorInventario;
import org.marketcli.modelo.inventario.EntradaMercancia;
import org.marketcli.modelo.inventario.Inventario;
import org.marketcli.modelo.inventario.TipoMedida;
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

class DialogoInventarioTest {

    @Test
    void muestraPrecioMedidaYStockCero() {
        ControladorInventario controlador = new ControladorInventario(new Inventario());
        int skuA = controlador.registrarProducto("Jabón", TipoMedida.UNIDAD, new BigDecimal("30")).getSku();
        int skuB = controlador.registrarProducto("Frijol", TipoMedida.KILOGRAMO, new BigDecimal("20.04")).getSku();
        int proveedorId = controlador.registrarProveedor("Granos del Valle").getId();
        controlador.registrarEntrada(skuB, proveedorId, new BigDecimal("2.750"));

        PrintStream salidaOriginal = System.out;
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        try (Scanner scanner = new Scanner("%d%n%d%n".formatted(skuA, skuB))) {
            System.setOut(new PrintStream(salida, true, StandardCharsets.UTF_8));
            DialogoInventario dialogo = new DialogoInventario(new LectorConsola(scanner), controlador);

            dialogo.consultarPrecioYExistencias();
            dialogo.consultarPrecioYExistencias();
        } finally {
            System.setOut(salidaOriginal);
        }

        String texto = salida.toString(StandardCharsets.UTF_8);
        String consultaA = texto.substring(0, texto.indexOf("Nombre: Frijol"));
        String consultaB = texto.substring(texto.indexOf("Nombre: Frijol"));
        assertTrue(consultaA.contains("Nombre: Jabón"));
        assertTrue(consultaA.contains("Precio: $30.00"));
        assertTrue(consultaA.contains("Medida: Unidad"));
        assertTrue(consultaA.contains("Existencias: 0"));
        assertTrue(consultaB.contains("Precio: $20.04"));
        assertTrue(consultaB.contains("Medida: Kilogramo"));
        assertTrue(consultaB.contains("Existencias: 2.750"));
    }

    @Test
    void muestraPrecioUnitarioSinRedondearlo() {
        ControladorInventario controlador = new ControladorInventario(new Inventario());
        int sku = controlador.registrarProducto("Jabón", TipoMedida.UNIDAD, new BigDecimal("1.234")).getSku();
        PrintStream salidaOriginal = System.out;
        ByteArrayOutputStream salida = new ByteArrayOutputStream();

        try (Scanner scanner = new Scanner(Integer.toString(sku));
             PrintStream consola = new PrintStream(salida, true, StandardCharsets.UTF_8)) {
            System.setOut(consola);
            DialogoInventario dialogo = new DialogoInventario(new LectorConsola(scanner), controlador);

            dialogo.consultarPrecioYExistencias();
            dialogo.consultarProductos();
        } finally {
            System.setOut(salidaOriginal);
        }

        String texto = salida.toString(StandardCharsets.UTF_8);
        assertTrue(texto.lines().anyMatch("Precio: $1.234"::equals));
        assertTrue(texto.contains("| $1.234 |"));
    }

    @Test
    void muestraDatosOriginalesDeEntrada() {
        ControladorInventario controlador = new ControladorInventario(new Inventario());
        int sku = controlador.registrarProducto("Frijol", TipoMedida.KILOGRAMO, new BigDecimal("20.04")).getSku();
        int proveedorId = controlador.registrarProveedor("Granos del Valle").getId();
        String guion = """
                %d
                %d
                2.750
                %d
                Granos del Norte
                """.formatted(sku, proveedorId, proveedorId);

        PrintStream salidaOriginal = System.out;
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        String historial;
        try (Scanner scanner = new Scanner(guion)) {
            System.setOut(new PrintStream(salida, true, StandardCharsets.UTF_8));
            DialogoInventario dialogo = new DialogoInventario(new LectorConsola(scanner), controlador);

            dialogo.registrarEntrada();
            dialogo.modificarProveedor();
            salida.reset();
            dialogo.mostrarHistorialEntradas();
            historial = salida.toString(StandardCharsets.UTF_8);
        } finally {
            System.setOut(salidaOriginal);
        }

        assertEquals("Granos del Norte", controlador.listarProveedoresActivos().getFirst().getNombre());
        EntradaMercancia entrada = controlador.listarEntradas().getFirst();
        String fecha = entrada.getFechaHora().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        assertTrue(historial.contains("Entrada " + entrada.getId()));
        assertTrue(historial.contains("Fecha: " + fecha));
        assertTrue(historial.contains("SKU: " + sku));
        assertTrue(historial.contains("Cantidad: 2.750"));
        assertTrue(historial.contains("Medida: Kilogramo"));
        assertTrue(historial.contains("Proveedor: %d - Granos del Valle".formatted(proveedorId)));
        assertFalse(historial.contains("Granos del Norte"));
    }
}
