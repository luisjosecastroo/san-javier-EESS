package eess.modelo;

import java.math.BigDecimal;

/** Validaciones compartidas por los objetos del negocio. */
public final class Validacion {
    private Validacion() { }

    public static String texto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " es obligatorio.");
        }
        return valor.trim();
    }

    public static BigDecimal positivo(BigDecimal valor, String campo) {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException(campo + " debe ser mayor que cero.");
        }
        return valor;
    }

    public static BigDecimal numero(String texto, String campo) {
        try {
            return new BigDecimal(texto.trim().replace(',', '.'));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(campo + " debe ser un número válido.");
        }
    }
}
