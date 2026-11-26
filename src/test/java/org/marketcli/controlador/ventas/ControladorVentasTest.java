package org.marketcli.controlador.ventas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.marketcli.modelo.acceso.Rol;
import org.marketcli.modelo.acceso.Usuario;
import org.marketcli.modelo.inventario.TipoMedida;
import org.marketcli.modelo.ventas.Venta;
import org.marketcli.soporte.InventarioDePrueba;
import org.marketcli.soporte.ProductoDePrueba;
import org.marketcli.util.excepcion.StockInsuficienteException;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ControladorVentasTest {

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
    void acumulaAntesDeRedondear() {
        ProductoDePrueba frijol = new ProductoDePrueba(
                1, "Frijol", new BigDecimal("20.04"), TipoMedida.KILOGRAMO);
        inventario.configurarProducto(frijol, new BigDecimal("5.000"));

        BigDecimal cantidad = controlador.validarAdicion(
                1, BigDecimal.ZERO, new BigDecimal("0.125"));
        cantidad = controlador.validarAdicion(
                1, cantidad, new BigDecimal("0.125"));

        assertEquals(0, new BigDecimal("0.250").compareTo(cantidad));
        BigDecimal total = controlador.calcularTotal(Map.of(1, cantidad));
        assertEquals(new BigDecimal("5.01"), total);
        assertEquals(BigDecimal.ZERO, controlador.calcularTotal(Map.of()));
    }

    @Test
    void rechazaExcesoEInformaSaldo() {
        ProductoDePrueba leche = new ProductoDePrueba(
                10, "Leche", new BigDecimal("28.50"), TipoMedida.UNIDAD);
        inventario.configurarProducto(leche, new BigDecimal("3"));

        StockInsuficienteException error = assertThrows(
                StockInsuficienteException.class,
                () -> controlador.validarAdicion(10, new BigDecimal("2"), new BigDecimal("2")));

        assertEquals(10, error.getSku());
        assertEquals(0, new BigDecimal("3").compareTo(error.getDisponible()));
        assertEquals(0, inventario.getSolicitudesDeDescuento());
        assertEquals(0, new BigDecimal("3").compareTo(inventario.consultarExistencias(10)));
        assertTrue(controlador.listarVentas().isEmpty());
    }

    @Test
    void confirmaDosPartidasConUnSoloDescuento() {
        ProductoDePrueba productoA = new ProductoDePrueba(
                1, "Aceite", new BigDecimal("30.00"), TipoMedida.UNIDAD);
        ProductoDePrueba productoB = new ProductoDePrueba(
                2, "Sal", new BigDecimal("20.00"), TipoMedida.UNIDAD);
        inventario.configurarProducto(productoA, new BigDecimal("5"));
        inventario.configurarProducto(productoB, new BigDecimal("1"));

        Map<Integer, BigDecimal> captura = new LinkedHashMap<>();
        captura.put(1, new BigDecimal("2"));
        captura.put(2, new BigDecimal("1"));

        Venta venta = controlador.confirmarVenta(cajero, captura, new BigDecimal("100.00"));

        assertEquals(1, venta.getId());
        assertEquals("cajero1", venta.getNombreCajero());
        assertEquals(2, venta.getPartidas().size());
        assertEquals(new BigDecimal("80.00"), venta.getTotal());
        assertEquals(new BigDecimal("100.00"), venta.getEfectivo());
        assertEquals(new BigDecimal("20.00"), venta.getCambio());
        assertEquals(1, inventario.getSolicitudesDeDescuento());
        assertEquals(0, new BigDecimal("3").compareTo(inventario.consultarExistencias(1)));
        assertEquals(0, BigDecimal.ZERO.compareTo(inventario.consultarExistencias(2)));
        assertEquals(1, controlador.listarVentas().size());
    }

    @Test
    void rechazaPagoInsuficienteSinDescontar() {
        ProductoDePrueba frijol = new ProductoDePrueba(
                1, "Frijol", new BigDecimal("20.04"), TipoMedida.KILOGRAMO);
        inventario.configurarProducto(frijol, new BigDecimal("2.000"));

        Map<Integer, BigDecimal> captura = new LinkedHashMap<>();
        captura.put(1, new BigDecimal("0.250"));

        assertThrows(
                IllegalArgumentException.class,
                () -> controlador.confirmarVenta(cajero, captura, new BigDecimal("4.00")));

        assertEquals(0, inventario.getSolicitudesDeDescuento());
        assertEquals(0, new BigDecimal("2.000").compareTo(inventario.consultarExistencias(1)));
        assertTrue(controlador.listarVentas().isEmpty());
    }

    @Test
    void rechazaConfirmacionVacia() {
        Map<Integer, BigDecimal> capturaVacia = new LinkedHashMap<>();

        assertThrows(
                IllegalArgumentException.class,
                () -> controlador.confirmarVenta(cajero, capturaVacia, new BigDecimal("50.00")));

        assertEquals(0, inventario.getSolicitudesDeDescuento());
        assertTrue(controlador.listarVentas().isEmpty());
    }

    @Test
    void conservaCapturaEHistorialSiFallaElLote() {
        ProductoDePrueba productoA = new ProductoDePrueba(
                1, "Aceite", new BigDecimal("30.00"), TipoMedida.UNIDAD);
        ProductoDePrueba productoB = new ProductoDePrueba(
                2, "Sal", new BigDecimal("20.00"), TipoMedida.UNIDAD);
        inventario.configurarProducto(productoA, new BigDecimal("5"));
        inventario.configurarProducto(productoB, new BigDecimal("2"));

        Venta ventaPrevia = controlador.confirmarVenta(
                cajero, Map.of(1, new BigDecimal("1")), new BigDecimal("50.00"));
        assertEquals(1, ventaPrevia.getId());

        Map<Integer, BigDecimal> captura = new LinkedHashMap<>();
        captura.put(1, new BigDecimal("2"));
        captura.put(2, new BigDecimal("1"));
        inventario.rechazarProximoLote(2);

        StockInsuficienteException error = assertThrows(
                StockInsuficienteException.class,
                () -> controlador.confirmarVenta(cajero, captura, new BigDecimal("100.00")));

        assertEquals(2, error.getSku());
        assertEquals(0, new BigDecimal("4").compareTo(inventario.consultarExistencias(1)));
        assertEquals(0, new BigDecimal("2").compareTo(inventario.consultarExistencias(2)));
        assertEquals(1, controlador.listarVentas().size());
        assertEquals(Map.of(1, new BigDecimal("2"), 2, new BigDecimal("1")), captura);

        Venta ventaReintentada = controlador.confirmarVenta(cajero, captura, new BigDecimal("100.00"));
        assertEquals(2, ventaReintentada.getId());
        assertEquals(2, controlador.listarVentas().size());
    }
}
