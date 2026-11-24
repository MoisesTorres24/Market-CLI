package org.marketcli.modelo.inventario;

import org.marketcli.util.validacion.Validador;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Reúne los datos del catálogo y las reglas comunes de cantidad e importe.
 */
public abstract class Producto {

    protected final int sku;
    protected final TipoMedida medida;
    protected String nombre;
    protected BigDecimal precio;
    protected boolean activo;

    /**
     * Crea un producto activo con un SKU y una medida fijos.
     *
     * @param sku identificador asignado por el catálogo
     * @param nombre nombre del producto
     * @param precio precio de venta por la medida indicada
     * @param medida medida fija del producto
     * @throws IllegalArgumentException si el nombre está vacío o el precio no es positivo
     * @throws NullPointerException si la medida es nula
     */
    protected Producto(int sku, String nombre, BigDecimal precio, TipoMedida medida) {
        validarDatos(nombre, precio);
        this.sku = sku;
        this.medida = Objects.requireNonNull(medida, "medida");
        this.nombre = nombre;
        this.precio = precio;
        this.activo = true;
    }

    /**
     * Devuelve el SKU asignado por el catálogo.
     *
     * @return el SKU del producto
     */
    public int getSku() {
        return sku;
    }

    /**
     * Devuelve la medida fijada al crear el producto.
     *
     * @return la medida del producto
     */
    public TipoMedida getMedida() {
        return medida;
    }

    /**
     * Devuelve el nombre vigente en el catálogo.
     *
     * @return el nombre del producto
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Devuelve el precio de venta vigente.
     *
     * @return el precio por la medida del producto
     */
    public BigDecimal getPrecio() {
        return precio;
    }

    /**
     * Indica si el producto sigue activo en el catálogo.
     *
     * @return true si el producto está activo
     */
    public boolean isActivo() {
        return activo;
    }

    /**
     * Comprueba que una cantidad sea positiva y respete la precisión de la medida.
     *
     * @param cantidad cantidad que se quiere registrar o vender
     * @throws IllegalArgumentException si la cantidad no es positiva o su precisión es inválida
     */
    public final void validarCantidad(BigDecimal cantidad) {
        Validador.validarPositivo(cantidad, "cantidad");
        // POLIMORFISMO: cada variante valida la precisión de su medida.
        validarPrecision(cantidad);
    }

    /**
     * Calcula el importe de una partida y redondea al centavo, con medio centavo hacia arriba.
     *
     * @param cantidad cantidad acumulada en la partida
     * @return el importe redondeado a dos decimales
     * @throws IllegalArgumentException si la cantidad no es válida para el producto
     */
    public final BigDecimal calcularImporte(BigDecimal cantidad) {
        validarCantidad(cantidad);
        return precio.multiply(cantidad).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Valida la precisión de una cantidad positiva según la medida de la variante.
     *
     * @param cantidad cantidad positiva cuya precisión se debe comprobar
     * @throws IllegalArgumentException si la cantidad tiene una precisión incompatible
     */
    protected abstract void validarPrecision(BigDecimal cantidad);

    void actualizarDatos(String nombre, BigDecimal precio) {
        validarDatos(nombre, precio);
        this.nombre = nombre;
        this.precio = precio;
    }

    void darDeBaja() {
        activo = false;
    }

    private static void validarDatos(String nombre, BigDecimal precio) {
        Validador.validarTextoObligatorio(nombre, "nombre");
        Validador.validarPositivo(precio, "precio");
    }
}
