package eess.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Lo común a una venta y una recepción: ambas cambian el stock. */
public abstract class MovimientoCombustible {
    private final BigDecimal litros;

    protected MovimientoCombustible(BigDecimal litros) {
        this.litros = Validacion.positivo(litros, "Litros").setScale(3, RoundingMode.HALF_UP);
        Validacion.positivo(this.litros, "Litros");
    }

    public BigDecimal getLitros() { return litros; }

    public abstract BigDecimal variacionStock();
    public abstract String getTipo();
}
