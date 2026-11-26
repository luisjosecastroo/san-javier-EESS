package eess;

import eess.datos.BaseDemostracion;
import eess.datos.Catalogo;
import eess.datos.Conexion;
import eess.datos.RepositorioJDBC;
import eess.modelo.Cliente;
import eess.modelo.Recepcion;
import eess.modelo.Tanque;
import eess.modelo.Venta;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Pruebas de negocio y de integración en H2 aislado, nunca en Supabase. */
public final class PruebasEstacion {
    private static int aprobadas;
    private PruebasEstacion() { }

    private static BigDecimal decimal(String valor) { return new BigDecimal(valor); }
    private static void comprobar(String nombre, boolean condicion) {
        if (!condicion) { throw new AssertionError(nombre); }
        aprobadas++; System.out.println("PASS " + nombre);
    }
    private static void igual(String nombre, BigDecimal esperado, BigDecimal actual) {
        comprobar(nombre, esperado.compareTo(actual) == 0);
    }
    @FunctionalInterface
    private interface Accion { void ejecutar() throws Exception; }
    private static void rechaza(String nombre, Accion accion) throws Exception {
        try { accion.ejecutar(); }
        catch (IllegalArgumentException | java.sql.SQLException exception) { comprobar(nombre, true); return; }
        throw new AssertionError(nombre + ": no rechazó la operación");
    }
    private static BigDecimal consultar(Conexion conexion, String sql) throws Exception {
        try (Connection c = conexion.abrir(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            r.next(); return r.getBigDecimal(1);
        }
    }
    private static Venta venta(String factura, int asignacion, String litros, String precio) {
        return new Venta(factura, "abc123", "Efectivo", 1, asignacion, decimal(litros), decimal(precio));
    }
    public static void main(String[] args) throws Exception {
        modelos(); integracion(); concurrencia();
        System.out.println("RESULTADO: " + aprobadas + " comprobaciones aprobadas.");
    }

    private static void modelos() throws Exception {
        rechaza("cliente sin NIT", () -> new Cliente(0, " ", "Nombre"));
        rechaza("cliente sin razón social", () -> new Cliente(0, "123", null));
        comprobar("el cliente normaliza espacios exteriores", new Cliente(0, " 123 ", " Nombre ").getNitCi().equals("123"));
        rechaza("venta con cero litros", () -> venta("A", 1, "0", "3.74"));
        rechaza("venta negativa", () -> venta("A", 1, "-1", "3.74"));
        rechaza("venta con precio cero", () -> venta("A", 1, "1", "0"));
        rechaza("venta sin placa", () -> new Venta("A", "", "QR", 1, 1, decimal("10"), decimal("3.74")));
        rechaza("litros que redondean a cero", () -> venta("A", 1, "0.00001", "3.74"));
        Venta v = venta("A", 1, "12.345", "3.74");
        igual("total decimal exacto redondeado a centavos", decimal("46.17"), v.getTotal());
        comprobar("normalización de placa", v.getPlaca().equals("ABC123"));
        Tanque t = new Tanque(1, "T", "Especial", decimal("100"), decimal("50"));
        t.aplicarMovimiento(venta("B", 1, "10", "3.74"));
        igual("polimorfismo: la venta resta stock", decimal("40"), t.getStock());
        t.aplicarMovimiento(new Recepcion("G", 1, 1, decimal("20")));
        igual("polimorfismo: la recepción suma stock", decimal("60"), t.getStock());
        rechaza("tanque no admite sobrecapacidad", () -> t.aplicarMovimiento(new Recepcion("G", 1, 1, decimal("50"))));
        igual("rechazo de recepción conserva stock", decimal("60"), t.getStock());
        rechaza("tanque no admite sobregiro", () -> t.aplicarMovimiento(venta("C", 1, "70", "3.74")));
        igual("rechazo de venta conserva stock", decimal("60"), t.getStock());
    }

    private static void integracion() throws Exception {
        Conexion conexion = Conexion.prueba("integracion");
        BaseDemostracion.preparar(conexion);
        BaseDemostracion.preparar(conexion);
        RepositorioJDBC r = new RepositorioJDBC(conexion); r.verificarEstructura();
        for (Catalogo catalogo : Catalogo.values()) { comprobar("consulta de " + catalogo, r.consultar(catalogo).getFilas().length > 0); }
        igual("inicialización idempotente", decimal("3"), consultar(conexion, "SELECT COUNT(*) FROM cliente"));
        for (String grupo : new String[]{"clientes","administradores","despachadores","tanques","combustibles","islas","mangueras","turnos","asignaciones"}) {
            comprobar("selector de " + grupo, !r.opciones(grupo).isEmpty());
        }
        comprobar("resumen tiene cuatro indicadores", r.resumen().getColumnas().length == 4);
        r.guardarCliente(new Cliente(0, "TEST", "O'Connor; DROP TABLE venta;"));
        igual("texto con SQL se almacena como dato", decimal("4"), consultar(conexion, "SELECT COUNT(*) FROM cliente"));
        r.guardarCliente(new Cliente(4, "TEST", "Cliente editado"));
        rechaza("NIT duplicado", () -> r.guardarCliente(new Cliente(0, "TEST", "Otro")));
        igual("duplicado no aumenta registros", decimal("4"), consultar(conexion, "SELECT COUNT(*) FROM cliente"));

        r.registrarVenta(venta("PRUEBA-1", 1, "10", "3.74"));
        igual("venta descuenta una sola vez", decimal("11960"), consultar(conexion, "SELECT stock_actual_litros FROM tanque WHERE id_tanque=1"));
        igual("venta guarda el total correcto", decimal("37.40"), consultar(conexion, "SELECT monto_total FROM venta WHERE nro_factura='PRUEBA-1'"));
        rechaza("factura duplicada", () -> r.registrarVenta(venta("PRUEBA-1", 1, "10", "3.74")));
        rechaza("cliente inexistente revierte la transacción", () -> r.registrarVenta(new Venta("FK", "ABC", "QR", 99999, 1, decimal("10"), decimal("3.74"))));
        rechaza("venta sobre el stock", () -> r.registrarVenta(venta("EXCESO", 1, "20000", "3.74")));
        igual("ventas rechazadas conservan stock", decimal("11960"), consultar(conexion, "SELECT stock_actual_litros FROM tanque WHERE id_tanque=1"));
        igual("ventas rechazadas no se insertan", decimal("4"), consultar(conexion, "SELECT COUNT(*) FROM venta"));
        r.registrarRecepcion(new Recepcion("PRUEBA-G", 1, 1, decimal("100")));
        igual("recepción suma una sola vez", decimal("12060"), consultar(conexion, "SELECT stock_actual_litros FROM tanque WHERE id_tanque=1"));
        rechaza("despachador no puede recibir como administrador", () -> r.registrarRecepcion(new Recepcion("ROL", 1, 2, decimal("10"))));
        rechaza("recepción supera capacidad", () -> r.registrarRecepcion(new Recepcion("EXCESO", 1, 1, decimal("50000"))));
        igual("recepciones rechazadas no se insertan", decimal("4"), consultar(conexion, "SELECT COUNT(*) FROM recepcion_cisterna"));
        igual("recepciones rechazadas conservan stock", decimal("12060"), consultar(conexion, "SELECT stock_actual_litros FROM tanque WHERE id_tanque=1"));
        r.cambiarPrecio(1, decimal("4.10"), "PRUEBA");
        rechaza("formulario con precio antiguo se rechaza", () -> r.registrarVenta(venta("ANTIGUO", 1, "5", "3.74")));
        igual("historial conserva el precio anterior", decimal("2"), consultar(conexion, "SELECT COUNT(*) FROM historial_precio WHERE id_combustible=1"));
        igual("solo un precio queda abierto", decimal("1"), consultar(conexion, "SELECT COUNT(*) FROM historial_precio WHERE id_combustible=1 AND fecha_fin IS NULL"));
        r.registrarVenta(venta("NUEVO", 1, "5", "4.10"));
        rechaza("turno con asignaciones abiertas no cierra", () -> r.cerrarTurno(1));
        rechaza("lectura que no concilia se rechaza", () -> r.cerrarAsignacion(1, decimal("1001"), BigDecimal.ZERO));
        igual("rechazo mantiene asignación abierta", decimal("1"), consultar(conexion, "SELECT COUNT(*) FROM asignacion_turno_manguera WHERE id_asignacion=1 AND lectura_final IS NULL"));
        r.cerrarAsignacion(1, decimal("1045"), BigDecimal.ZERO);
        rechaza("asignación cerrada no admite venta", () -> r.registrarVenta(venta("CERRADA", 1, "1", "4.10")));
        rechaza("no se cierra dos veces una asignación", () -> r.cerrarAsignacion(1, decimal("1045"), BigDecimal.ZERO));
        r.cerrarAsignacion(2, decimal("2080"), BigDecimal.ZERO);
        r.cerrarAsignacion(3, decimal("520"), BigDecimal.ZERO);
        r.cerrarTurno(1);
        igual("cierre calcula la recaudación desde ventas", consultar(conexion, "SELECT SUM(monto_total) FROM venta"), consultar(conexion, "SELECT total_recaudado_cierre FROM turno WHERE id_turno=1"));
        rechaza("no se cierra dos veces un turno", () -> r.cerrarTurno(1));
        r.abrirTurno("Tarde", 1);
        rechaza("lectura inicial no puede retroceder", () -> r.asignarManguera(2, 2, 1, decimal("1000")));
        r.asignarManguera(2, 2, 1, decimal("1045"));
        r.abrirTurno("Noche", 1);
        rechaza("manguera no se asigna simultáneamente a dos turnos", () -> r.asignarManguera(3, 2, 1, decimal("1045")));
        r.cambiarPersonalActivo(2, false);
        rechaza("despachador inactivo no vende", () -> r.registrarVenta(venta("INACTIVO", 4, "1", "4.10")));
        comprobar("despachador inactivo no aparece en selector", r.opciones("despachadores").isEmpty());
        r.cambiarPersonalActivo(2, true);
        r.registrarVenta(venta("REACTIVADO", 4, "1", "4.10"));
        r.guardarPersonal("OTRO", "Persona", "Prueba", "Despachador", "");
        r.agregarCombustible("Combustible de prueba");
        r.agregarTanque("T-PRUEBA", decimal("1000"), 4);
        r.agregarIsla(10, "Prueba");
        r.agregarManguera("M-PRUEBA", "A", 3, 4);
        igual("nuevo tanque nace vacío", BigDecimal.ZERO, consultar(conexion, "SELECT stock_actual_litros FROM tanque WHERE id_tanque=4"));
        igual("catálogos guardan relaciones", decimal("4"), consultar(conexion, "SELECT id_tanque FROM manguera WHERE codigo_manguera='M-PRUEBA'"));
        comprobar("demás catálogos siguen disponibles", r.consultar(Catalogo.PERSONAL).getFilas().length == 3);
    }

    private static void concurrencia() throws Exception {
        Conexion conexion = Conexion.prueba("concurrencia"); BaseDemostracion.preparar(conexion);
        RepositorioJDBC r = new RepositorioJDBC(conexion);
        ExecutorService ejecutor = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> primera = ejecutor.submit(() -> intentarVenta(r, "CONC-1"));
            Future<Boolean> segunda = ejecutor.submit(() -> intentarVenta(r, "CONC-2"));
            comprobar("ventas concurrentes no sobregiran stock", primera.get() ^ segunda.get());
            igual("stock consistente después de concurrencia", decimal("3970"), consultar(conexion, "SELECT stock_actual_litros FROM tanque WHERE id_tanque=1"));
            igual("solo una venta concurrente se guarda", decimal("4"), consultar(conexion, "SELECT COUNT(*) FROM venta"));
        } finally { ejecutor.shutdownNow(); }
    }
    private static boolean intentarVenta(RepositorioJDBC r, String factura) throws Exception {
        try { r.registrarVenta(venta(factura, 1, "8000", "3.74")); return true; }
        catch (IllegalArgumentException exception) { return false; }
    }
}
