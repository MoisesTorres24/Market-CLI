package org.marketcli.modelo.ventas;

import org.marketcli.util.validacion.Validador;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Venta confirmada en caja con sus partidas, el total, el efectivo recibido y el cambio.
 */
public final class Venta {

    protected final int id;
    protected final LocalDateTime fechaHora;
    protected final String nombreCajero;
    // COMPOSICIÓN: la venta es dueña de sus partidas y conserva una copia no modificable.
    protected final List<PartidaVenta> partidas;
    protected final BigDecimal total;
    protected final BigDecimal efectivo;
    protected final BigDecimal cambio;

    /**
     * Crea una venta y calcula su total y su cambio.
     *
     * @param id identificador de la venta
     * @param fechaHora fecha y hora en que se confirmó la venta
     * @param nombreCajero nombre del cajero que la registró
     * @param partidas partidas vendidas; se guarda una copia no modificable
     * @param efectivo efectivo recibido del cliente
     * @throws NullPointerException si la fecha, las partidas o alguna partida son nulas
     * @throws IllegalArgumentException si el nombre del cajero está vacío, no hay partidas,
     *                                  el efectivo es nulo o el efectivo es menor que el total
     */
    public Venta(int id, LocalDateTime fechaHora, String nombreCajero,
                 List<PartidaVenta> partidas, BigDecimal efectivo) {
        Objects.requireNonNull(fechaHora, "fechaHora");
        Validador.validarTextoObligatorio(nombreCajero, "nombre del cajero");
        Objects.requireNonNull(partidas, "partidas");
        if (efectivo == null) {
            throw new IllegalArgumentException("El campo efectivo es obligatorio.");
        }
        List<PartidaVenta> copia = List.copyOf(partidas);
        if (copia.isEmpty()) {
            throw new IllegalArgumentException("La venta debe tener al menos una partida.");
        }
        BigDecimal sumaImportes = BigDecimal.ZERO;
        for (PartidaVenta partida : copia) {
            sumaImportes = sumaImportes.add(partida.getImporte());
        }
        if (efectivo.compareTo(sumaImportes) < 0) {
            throw new IllegalArgumentException("El efectivo es menor que el total de la venta.");
        }
        this.id = id;
        this.fechaHora = fechaHora;
        this.nombreCajero = nombreCajero;
        this.partidas = copia;
        this.total = sumaImportes;
        this.efectivo = efectivo;
        this.cambio = efectivo.subtract(sumaImportes);
    }

    /**
     * Devuelve el identificador de la venta.
     *
     * @return el id de la venta
     */
    public int getId() {
        return id;
    }

    /**
     * Devuelve la fecha y hora de la venta.
     *
     * @return la fecha y hora registradas
     */
    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    /**
     * Devuelve el nombre del cajero que registró la venta.
     *
     * @return el nombre del cajero
     */
    public String getNombreCajero() {
        return nombreCajero;
    }

    /**
     * Devuelve las partidas vendidas.
     *
     * @return una lista no modificable de partidas
     */
    public List<PartidaVenta> getPartidas() {
        return partidas;
    }

    /**
     * Devuelve el total de la venta, suma de los importes ya redondeados de sus partidas.
     *
     * @return el total de la venta
     */
    public BigDecimal getTotal() {
        return total;
    }

    /**
     * Devuelve el efectivo recibido.
     *
     * @return el efectivo recibido del cliente
     */
    public BigDecimal getEfectivo() {
        return efectivo;
    }

    /**
     * Devuelve el cambio que se entrega al cliente.
     *
     * @return la diferencia entre el efectivo y el total
     */
    public BigDecimal getCambio() {
        return cambio;
    }
}
