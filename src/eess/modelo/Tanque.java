package eess.modelo;

import java.math.BigDecimal;

public final class Tanque {
    private final int id;
    private final String codigo;
    private final String combustible;
    private final BigDecimal capacidad;
    private BigDecimal stock;

    public Tanque(int id, String codigo, String combustible, BigDecimal capacidad, BigDecimal stock) {
        this.id = id;
        this.codigo = Validacion.texto(codigo, "Código de tanque");
        this.combustible = Validacion.texto(combustible, "Combustible");
        this.capacidad = Validacion.positivo(capacidad, "Capacidad");
        if (stock == null || stock.signum() < 0 || stock.compareTo(capacidad) > 0) {
            throw new IllegalArgumentException("El stock debe estar entre cero y la capacidad del tanque.");
        }
        this.stock = stock;
    }

    /** Polimorfismo: el tanque recibe el mismo tipo base para sumar o restar. */
    public void aplicarMovimiento(MovimientoCombustible movimiento) {
        if (movimiento == null) {
            throw new IllegalArgumentException("El movimiento es obligatorio.");
        }
        BigDecimal nuevoStock = stock.add(movimiento.variacionStock());
        if (nuevoStock.signum() < 0) {
            throw new IllegalArgumentException("Stock insuficiente: quedan " + stock + " litros.");
        }
        if (nuevoStock.compareTo(capacidad) > 0) {
            throw new IllegalArgumentException("La recepción supera la capacidad disponible del tanque.");
        }
        stock = nuevoStock;
    }

    public int getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getCombustible() { return combustible; }
    public BigDecimal getCapacidad() { return capacidad; }
    public BigDecimal getStock() { return stock; }

    @Override
    public String toString() { return codigo + " · " + combustible + " · " + stock + " L"; }
}
