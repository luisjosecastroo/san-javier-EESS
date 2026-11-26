package eess.modelo;

public final class Cliente {
    private final int id;
    private final String nitCi;
    private final String razonSocial;

    public Cliente(int id, String nitCi, String razonSocial) {
        this.id = id;
        this.nitCi = Validacion.texto(nitCi, "NIT / CI");
        this.razonSocial = Validacion.texto(razonSocial, "Razón social");
    }

    public int getId() { return id; }
    public String getNitCi() { return nitCi; }
    public String getRazonSocial() { return razonSocial; }

    @Override
    public String toString() { return razonSocial + " · " + nitCi; }
}
