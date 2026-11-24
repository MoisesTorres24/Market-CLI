package org.marketcli.util.excepcion;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StockInsuficienteExceptionTest {

    @Test
    void conservaSkuYExistenciaDisponible() {
        BigDecimal disponible = new BigDecimal("1.250");
        StockInsuficienteException excepcion = new StockInsuficienteException(12, disponible);

        assertEquals(12, excepcion.getSku());
        assertEquals(disponible, excepcion.getDisponible());
    }
}
