package eess.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Venta extends MovimientoCombustible {
    private final String factura;
    private final String placa;
    private final String metodoPago;
    private final int clienteId;
    private final int asignacionId;
    private final BigDecimal precio;

    public Venta(String factura, String placa, String metodoPago, int clienteId,
            int asignacionId, BigDecimal litros, BigDecimal precio) {
        super(litros);
        this.factura = Validacion.texto(factura, "Número de factura");
        this.placa = Validacion.texto(placa, "Placa").toUpperCase(java.util.Locale.ROOT);
        this.metodoPago = Validacion.texto(metodoPago, "Método de pago");
        if (clienteId <= 0 || asignacionId <= 0) {
            throw new IllegalArgumentException("Selecciona un cliente y una asignación de turno.");
        }
        this.clienteId = clienteId;
        this.asignacionId = asignacionId;
        this.precio = Validacion.positivo(precio, "Precio").setScale(2, RoundingMode.HALF_UP);
        Validacion.positivo(this.precio, "Precio");
    }

    public String getFactura() { return factura; }
    public String getPlaca() { return placa; }
    public String getMetodoPago() { return metodoPago; }
    public int getClienteId() { return clienteId; }
    public int getAsignacionId() { return asignacionId; }
    public BigDecimal getPrecio() { return precio; }
    public BigDecimal getTotal() { return getLitros().multiply(precio).setScale(2, RoundingMode.HALF_UP); }

    @Override
    public BigDecimal variacionStock() { return getLitros().negate(); }

    @Override
    public String getTipo() { return "Venta"; }
}
