package org.marketcli.util.lectura;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Scanner;

/**
 * Lee datos de la consola y vuelve a pedir una entrada cuando el formato es inválido.
 */
public final class LectorConsola {

    protected final Scanner scanner;

    /**
     * Crea un lector que usa el scanner compartido de la aplicación.
     *
     * @param scanner origen de las líneas de entrada
     * @throws NullPointerException si no se proporciona un scanner
     */
    public LectorConsola(Scanner scanner) {
        this.scanner = Objects.requireNonNull(scanner, "scanner");
    }

    /**
     * Pide texto y vuelve a intentarlo si solo contiene espacios.
     *
     * @param mensaje indicación que se muestra antes de leer
     * @return el texto ingresado sin espacios exteriores
     * @throws NoSuchElementException si se agota la entrada
     */
    public String pedirTexto(String mensaje) {
        while (true) {
            String texto = leerLinea(mensaje);
            if (!texto.isBlank()) {
                return texto;
            }
            System.out.println("Ingresa un texto no vacío.");
        }
    }

    /**
     * Pide un número entero mayor que cero.
     *
     * @param mensaje indicación que se muestra antes de leer
     * @return el entero positivo ingresado
     * @throws NoSuchElementException si se agota la entrada
     */
    public int pedirEnteroPositivo(String mensaje) {
        return pedirEnteroEnRango(mensaje, 1, Integer.MAX_VALUE, "Ingresa un número entero positivo.");
    }

    /**
     * Pide una opción dentro de un intervalo cerrado.
     *
     * @param mensaje indicación que se muestra antes de leer
     * @param minimo valor mínimo admitido
     * @param maximo valor máximo admitido
     * @return la opción elegida
     * @throws IllegalArgumentException si el mínimo es mayor que el máximo
     * @throws NoSuchElementException si se agota la entrada
     */
    public int pedirOpcion(String mensaje, int minimo, int maximo) {
        if (minimo > maximo) {
            throw new IllegalArgumentException("La opción mínima no puede superar la máxima.");
        }
        String aviso = "Ingresa una opción entre %d y %d.".formatted(minimo, maximo);
        return pedirEnteroEnRango(mensaje, minimo, maximo, aviso);
    }

    /**
     * Pide un decimal con punto o coma como separador decimal.
     *
     * @param mensaje indicación que se muestra antes de leer
     * @return el valor decimal leído
     * @throws NoSuchElementException si se agota la entrada
     */
    public BigDecimal pedirDecimal(String mensaje) {
        while (true) {
            String texto = leerLinea(mensaje).replace(',', '.');
            try {
                return new BigDecimal(texto);
            } catch (NumberFormatException error) {
                System.out.println("Ingresa un número decimal válido.");
            }
        }
    }

    private String leerLinea(String mensaje) {
        System.out.print(mensaje);
        return scanner.nextLine().strip();
    }

    private int pedirEnteroEnRango(String mensaje, int minimo, int maximo, String aviso) {
        while (true) {
            String texto = leerLinea(mensaje);
            try {
                int numero = Integer.parseInt(texto);
                if (numero >= minimo && numero <= maximo) {
                    return numero;
                }
            } catch (NumberFormatException error) {
                // Un dato que no cabe en un int recibe el mismo aviso que uno fuera de rango.
            }
            System.out.println(aviso);
        }
    }
}
