package eess.modelo;

import java.math.BigDecimal;

public final class Recepcion extends MovimientoCombustible {
    private final String guiaRemision;
    private final int tanqueId;
    private final int administradorId;

    public Recepcion(String guiaRemision, int tanqueId, int administradorId, BigDecimal litros) {
        super(litros);
        this.guiaRemision = Validacion.texto(guiaRemision, "Guía de remisión");
        if (tanqueId <= 0 || administradorId <= 0) {
            throw new IllegalArgumentException("Selecciona un tanque y un administrador.");
        }
        this.tanqueId = tanqueId;
        this.administradorId = administradorId;
    }

    public String getGuiaRemision() { return guiaRemision; }
    public int getTanqueId() { return tanqueId; }
    public int getAdministradorId() { return administradorId; }

    @Override
    public BigDecimal variacionStock() { return getLitros(); }

    @Override
    public String getTipo() { return "Recepción de cisterna"; }
}
