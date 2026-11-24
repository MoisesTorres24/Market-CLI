package org.marketcli.modelo.inventario;

import org.junit.jupiter.api.Test;
import org.marketcli.util.excepcion.StockInsuficienteException;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventarioTest {

    @Test
    void altaYConsultaDeProductoAgotado() {
        Inventario inventario = new Inventario();

        Producto jabon = inventario.registrarProducto("Jabón", TipoMedida.UNIDAD, new BigDecimal("18.50"));
        Producto frijol = inventario.registrarProducto("Frijol", TipoMedida.KILOGRAMO, new BigDecimal("32.00"));

        assertNotEquals(jabon.getSku(), frijol.getSku());
        assertInstanceOf(ProductoUnidad.class, jabon);
        assertInstanceOf(ProductoGranel.class, frijol);
        assertSame(jabon, inventario.consultarProductoActivo(jabon.getSku()));
        assertSame(frijol, inventario.consultarProducto(frijol.getSku()));
        assertEquals(0, inventario.consultarExistencias(jabon.getSku()).compareTo(BigDecimal.ZERO));
        assertEquals(0, inventario.consultarExistencias(frijol.getSku()).compareTo(BigDecimal.ZERO));
        assertEquals(2, inventario.listarProductosActivos().size());
        assertTrue(inventario.listarProductosDisponibles().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> inventario.consultarExistencias(99));
        assertThrows(UnsupportedOperationException.class, () -> inventario.listarProductos().clear());
    }

    @Test
    void bajaProductoSoloCuandoNoHayStock() {
        Inventario inventario = new Inventario();
        Producto frijol = inventario.registrarProducto("Frijol", TipoMedida.KILOGRAMO, new BigDecimal("32.00"));
        int sku = frijol.getSku();
        Proveedor proveedor = inventario.registrarProveedor("Granos del Valle");
        inventario.registrarEntrada(sku, proveedor.getId(), new BigDecimal("2.500"));

        inventario.modificarProducto(sku, "Frijol negro", new BigDecimal("35.00"));
        Producto modificado = inventario.consultarProducto(sku);
        assertEquals("Frijol negro", modificado.getNombre());
        assertEquals(sku, modificado.getSku());
        assertEquals(TipoMedida.KILOGRAMO, modificado.getMedida());
        assertEquals(0, inventario.consultarExistencias(sku).compareTo(new BigDecimal("2.500")));

        assertThrows(IllegalStateException.class, () -> inventario.darDeBajaProducto(sku));
        assertTrue(modificado.isActivo());

        inventario.descontarLote(Map.of(sku, new BigDecimal("2.500")));
        assertEquals(0, inventario.consultarExistencias(sku).compareTo(BigDecimal.ZERO));

        inventario.darDeBajaProducto(sku);
        Producto dadoDeBaja = inventario.consultarProducto(sku);
        assertEquals(sku, dadoDeBaja.getSku());
        assertFalse(dadoDeBaja.isActivo());
        assertEquals(1, inventario.listarEntradas().size());
        assertEquals(sku, inventario.listarEntradas().getFirst().getSku());
        assertFalse(inventario.listarProductosActivos().contains(dadoDeBaja));
        assertFalse(inventario.listarProductosDisponibles().contains(dadoDeBaja));
        assertThrows(IllegalArgumentException.class, () -> inventario.consultarProductoActivo(sku));
    }

    @Test
    void registraEntradaYConservaProveedorHistorico() {
        Inventario inventario = new Inventario();
        Producto frijol = inventario.registrarProducto("Frijol", TipoMedida.KILOGRAMO, new BigDecimal("32.00"));
        int sku = frijol.getSku();
        Proveedor proveedor = inventario.registrarProveedor("Granos del Valle");
        int proveedorId = proveedor.getId();

        EntradaMercancia entrada = inventario.registrarEntrada(sku, proveedorId, new BigDecimal("2.750"));
        assertEquals(sku, entrada.getSku());
        assertEquals(TipoMedida.KILOGRAMO, entrada.getMedida());
        assertEquals(0, entrada.getCantidad().compareTo(new BigDecimal("2.750")));
        assertEquals(proveedorId, entrada.getProveedorId());
        assertEquals("Granos del Valle", entrada.getNombreProveedor());
        assertEquals(0, inventario.consultarExistencias(sku).compareTo(new BigDecimal("2.750")));

        inventario.modificarProveedor(proveedorId, "Granos del Norte");
        inventario.darDeBajaProveedor(proveedorId);
        assertFalse(inventario.listarProveedoresActivos().contains(proveedor));

        EntradaMercancia historica = inventario.listarEntradas().getFirst();
        assertEquals(proveedorId, historica.getProveedorId());
        assertEquals("Granos del Valle", historica.getNombreProveedor());

        assertThrows(IllegalArgumentException.class,
                () -> inventario.registrarEntrada(sku, proveedorId, new BigDecimal("1.000")));
        assertEquals(0, inventario.consultarExistencias(sku).compareTo(new BigDecimal("2.750")));
        assertEquals(1, inventario.listarEntradas().size());
    }

    @Test
    void registraProveedoresConIdUnicoYExcluyeInactivos() {
        Inventario inventario = new Inventario();
        Proveedor primero = inventario.registrarProveedor("Granos del Valle");
        Proveedor segundo = inventario.registrarProveedor("Lácteos La Vaquita");
        int idPrimero = primero.getId();

        assertNotEquals(idPrimero, segundo.getId());

        inventario.modificarProveedor(idPrimero, "Granos del Norte");
        assertEquals(idPrimero, primero.getId());
        assertEquals("Granos del Norte", primero.getNombre());

        inventario.darDeBajaProveedor(idPrimero);
        assertEquals(List.of(segundo), inventario.listarProveedoresActivos());
    }

    @Test
    void registraEntradasYSumaCadaCantidadAlSku() {
        Inventario inventario = new Inventario();
        int proveedorId = inventario.registrarProveedor("Abarrotes del Sur").getId();
        int skuA = inventario.registrarProducto("Jabón", TipoMedida.UNIDAD, new BigDecimal("18.50")).getSku();
        int skuB = inventario.registrarProducto("Frijol", TipoMedida.KILOGRAMO, new BigDecimal("32.00")).getSku();
        inventario.registrarEntrada(skuA, proveedorId, new BigDecimal("4"));
        int registrosAntes = inventario.listarEntradas().size();

        inventario.registrarEntrada(skuA, proveedorId, new BigDecimal("10"));
        inventario.registrarEntrada(skuB, proveedorId, new BigDecimal("2.750"));

        assertEquals(0, inventario.consultarExistencias(skuA).compareTo(new BigDecimal("14")));
        assertEquals(0, inventario.consultarExistencias(skuB).compareTo(new BigDecimal("2.750")));
        assertEquals(registrosAntes + 2, inventario.listarEntradas().size());
    }

    @Test
    void rechazaEntradaInvalidaSinCambiarSaldoNiHistorial() {
        Inventario inventario = new Inventario();
        int proveedorId = inventario.registrarProveedor("Abarrotes del Sur").getId();
        int inactivoId = inventario.registrarProveedor("Distribuidora Cerrada").getId();
        int skuA = inventario.registrarProducto("Jabón", TipoMedida.UNIDAD, new BigDecimal("18.50")).getSku();
        int skuB = inventario.registrarProducto("Frijol", TipoMedida.KILOGRAMO, new BigDecimal("32.00")).getSku();
        inventario.registrarEntrada(skuA, proveedorId, new BigDecimal("4"));
        inventario.registrarEntrada(skuB, proveedorId, new BigDecimal("1.500"));
        inventario.darDeBajaProveedor(inactivoId);
        BigDecimal saldoA = inventario.consultarExistencias(skuA);
        BigDecimal saldoB = inventario.consultarExistencias(skuB);
        int registros = inventario.listarEntradas().size();

        assertThrows(IllegalArgumentException.class,
                () -> inventario.registrarEntrada(skuA, proveedorId, new BigDecimal("1.5")));
        assertThrows(IllegalArgumentException.class,
                () -> inventario.registrarEntrada(skuB, proveedorId, new BigDecimal("0.1234")));
        assertThrows(IllegalArgumentException.class,
                () -> inventario.registrarEntrada(skuA, proveedorId, BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class,
                () -> inventario.registrarEntrada(skuA, inactivoId, new BigDecimal("1")));

        assertEquals(0, inventario.consultarExistencias(skuA).compareTo(saldoA));
        assertEquals(0, inventario.consultarExistencias(skuB).compareTo(saldoB));
        assertEquals(registros, inventario.listarEntradas().size());
    }

    @Test
    void rechazaLoteCompletoSiFaltaElUltimoSku() {
        Inventario inventario = new Inventario();
        int proveedorId = inventario.registrarProveedor("Abarrotes del Sur").getId();
        int skuA = inventario.registrarProducto("Jabón", TipoMedida.UNIDAD, new BigDecimal("18.50")).getSku();
        int skuB = inventario.registrarProducto("Cloro", TipoMedida.UNIDAD, new BigDecimal("25.00")).getSku();
        inventario.registrarEntrada(skuA, proveedorId, new BigDecimal("5"));
        inventario.registrarEntrada(skuB, proveedorId, new BigDecimal("1"));

        Map<Integer, BigDecimal> lote = new LinkedHashMap<>();
        lote.put(skuA, new BigDecimal("2"));
        lote.put(skuB, new BigDecimal("2"));

        StockInsuficienteException excepcion =
                assertThrows(StockInsuficienteException.class, () -> inventario.descontarLote(lote));
        assertEquals(skuB, excepcion.getSku());
        assertEquals(0, excepcion.getDisponible().compareTo(new BigDecimal("1")));
        assertEquals(0, inventario.consultarExistencias(skuA).compareTo(new BigDecimal("5")));
        assertEquals(0, inventario.consultarExistencias(skuB).compareTo(new BigDecimal("1")));
    }

    @Test
    void descuentaLoteYExcluyeSkuAgotado() {
        Inventario inventario = new Inventario();
        int proveedorId = inventario.registrarProveedor("Abarrotes del Sur").getId();
        Producto a = inventario.registrarProducto("Jabón", TipoMedida.UNIDAD, new BigDecimal("18.50"));
        Producto b = inventario.registrarProducto("Cloro", TipoMedida.UNIDAD, new BigDecimal("25.00"));
        Producto c = inventario.registrarProducto("Vela", TipoMedida.UNIDAD, new BigDecimal("12.00"));
        inventario.registrarEntrada(a.getSku(), proveedorId, new BigDecimal("5"));
        inventario.registrarEntrada(b.getSku(), proveedorId, new BigDecimal("1"));
        inventario.darDeBajaProducto(c.getSku());

        Map<Integer, BigDecimal> lote = new LinkedHashMap<>();
        lote.put(a.getSku(), new BigDecimal("2"));
        lote.put(b.getSku(), new BigDecimal("1"));
        inventario.descontarLote(lote);

        assertEquals(0, inventario.consultarExistencias(a.getSku()).compareTo(new BigDecimal("3")));
        assertEquals(0, inventario.consultarExistencias(b.getSku()).compareTo(BigDecimal.ZERO));
        List<Producto> disponibles = inventario.listarProductosDisponibles();
        assertTrue(disponibles.contains(a));
        assertFalse(disponibles.contains(b));
        assertFalse(disponibles.contains(c));
    }
}
