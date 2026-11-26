package eess.ui;

import eess.datos.Catalogo;
import eess.datos.EstacionRepositorio;
import eess.datos.Opcion;
import eess.modelo.Cliente;
import eess.modelo.Recepcion;
import eess.modelo.Venta;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Traduce los formularios a objetos del negocio y llama al repositorio. */
public final class Acciones {
    private final VentanaEstacion ventana;
    private final EstacionRepositorio repositorio;

    public Acciones(VentanaEstacion ventana, EstacionRepositorio repositorio) {
        this.ventana = ventana;
        this.repositorio = repositorio;
    }

    public void venta() {
        ventana.preparar(new String[]{"clientes", "asignaciones"}, datos -> {
            Formulario f = new Formulario(ventana, "Registrar venta");
            JComboBox<Opcion> cliente = f.opciones("Cliente", datos.get("clientes"));
            JComboBox<Opcion> asignacion = f.opciones("Manguera / turno", datos.get("asignaciones"));
            JTextField factura = f.texto("Número de factura", "SJ-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd-HHmmss")));
            JTextField placa = f.texto("Placa del vehículo", "");
            JTextField litros = f.texto("Litros a vender", "");
            JComboBox<String> pago = f.seleccion("Método de pago", "Efectivo", "QR", "Tarjeta");
            JLabel precio = new JLabel(); f.agregar("Precio por litro", precio);
            JLabel total = Tema.titulo("Bs 0.00", 25); f.agregar("Total de la venta", total);
            Runnable calcular = () -> {
                Opcion opcion = Formulario.elegida(asignacion);
                BigDecimal p = opcion.getPrecio();
                precio.setText(p == null ? "Sin precio vigente: revisa Catálogos → Precios" : "Bs " + p.toPlainString());
                try { total.setText(p == null ? "—" : "Bs " + Formulario.numero(litros, "Litros").multiply(p).setScale(2, RoundingMode.HALF_UP)); }
                catch (IllegalArgumentException exception) { total.setText("Bs 0.00"); }
            };
            asignacion.addActionListener(e -> calcular.run());
            litros.getDocument().addDocumentListener(new DocumentListener() {
                @Override public void insertUpdate(DocumentEvent e) { calcular.run(); }
                @Override public void removeUpdate(DocumentEvent e) { calcular.run(); }
                @Override public void changedUpdate(DocumentEvent e) { calcular.run(); }
            });
            calcular.run();
            f.mostrar(() -> {
                Venta v = new Venta(factura.getText(), placa.getText(), (String) pago.getSelectedItem(),
                        Formulario.elegida(cliente).getId(), Formulario.elegida(asignacion).getId(),
                        Formulario.numero(litros, "Litros"), Formulario.elegida(asignacion).getPrecio());
                ventana.guardar(f, () -> repositorio.registrarVenta(v));
            });
        });
    }

    public void recepcion() {
        ventana.preparar(new String[]{"tanques", "administradores"}, datos -> {
            Formulario f = new Formulario(ventana, "Recibir combustible de cisterna");
            JComboBox<Opcion> tanque = f.opciones("Tanque de destino", datos.get("tanques"));
            JComboBox<Opcion> administrador = f.opciones("Administrador", datos.get("administradores"));
            JTextField guia = f.texto("Guía de remisión", "");
            JTextField litros = f.texto("Litros recibidos", "");
            f.mostrar(() -> {
                Recepcion r = new Recepcion(guia.getText(), Formulario.elegida(tanque).getId(),
                        Formulario.elegida(administrador).getId(), Formulario.numero(litros, "Litros recibidos"));
                ventana.guardar(f, () -> repositorio.registrarRecepcion(r));
            });
        });
    }

    public void cliente(boolean editar) {
        Object[] fila = editar ? ventana.seleccion() : null;
        int id = editar ? ((Number) fila[0]).intValue() : 0;
        Formulario f = new Formulario(ventana, editar ? "Editar cliente" : "Nuevo cliente");
        JTextField nit = f.texto("NIT / CI", editar ? fila[1].toString() : "");
        JTextField nombre = f.texto("Razón social / nombre", editar ? fila[2].toString() : "");
        f.mostrar(() -> {
            Cliente cliente = new Cliente(id, nit.getText(), nombre.getText());
            ventana.guardar(f, () -> repositorio.guardarCliente(cliente));
        });
    }

    public void personal() {
        Formulario f = new Formulario(ventana, "Registrar personal");
        JTextField ci = f.texto("CI", "");
        JTextField nombres = f.texto("Nombres", "");
        JTextField apellidos = f.texto("Apellidos", "");
        JComboBox<String> cargo = f.seleccion("Cargo", "Administrador", "Despachador");
        JTextField telefono = f.texto("Teléfono (opcional)", "");
        f.mostrar(() -> {
            String documento = ci.getText(); String nombre = nombres.getText(); String apellido = apellidos.getText();
            String rol = (String) cargo.getSelectedItem(); String contacto = telefono.getText();
            ventana.guardar(f, () -> repositorio.guardarPersonal(documento, nombre, apellido, rol, contacto));
        });
    }

    public void cambiarActivo() {
        Object[] fila = ventana.seleccion();
        int id = ((Number) fila[0]).intValue();
        boolean nuevoEstado = !Boolean.TRUE.equals(fila[6]);
        Formulario f = new Formulario(ventana, nuevoEstado ? "Activar personal" : "Desactivar personal");
        f.agregar("Personal", new JLabel(fila[2] + " " + fila[3]));
        f.agregar("Nuevo estado", new JLabel(nuevoEstado ? "Activo" : "Inactivo"));
        f.mostrar(() -> ventana.guardar(f, () -> repositorio.cambiarPersonalActivo(id, nuevoEstado)));
    }

    public void abrirTurno() {
        ventana.preparar(new String[]{"administradores"}, datos -> {
            Formulario f = new Formulario(ventana, "Abrir turno de hoy");
            JComboBox<String> franja = f.seleccion("Franja", "Mañana", "Tarde", "Noche");
            JComboBox<Opcion> admin = f.opciones("Administrador", datos.get("administradores"));
            f.mostrar(() -> {
                String periodo = (String) franja.getSelectedItem(); int id = Formulario.elegida(admin).getId();
                ventana.guardar(f, () -> repositorio.abrirTurno(periodo, id));
            });
        });
    }

    public void asignacion() {
        ventana.preparar(new String[]{"turnos", "despachadores", "mangueras"}, datos -> {
            Formulario f = new Formulario(ventana, "Asignar manguera a despachador");
            JComboBox<Opcion> turno = f.opciones("Turno abierto", datos.get("turnos"));
            JComboBox<Opcion> despachador = f.opciones("Despachador", datos.get("despachadores"));
            JComboBox<Opcion> manguera = f.opciones("Manguera", datos.get("mangueras"));
            JTextField lectura = f.texto("Lectura inicial (L)", "0");
            f.mostrar(() -> {
                int t = Formulario.elegida(turno).getId(); int d = Formulario.elegida(despachador).getId(); int m = Formulario.elegida(manguera).getId();
                BigDecimal inicial = Formulario.numero(lectura, "Lectura inicial");
                ventana.guardar(f, () -> repositorio.asignarManguera(t, d, m, inicial));
            });
        });
    }

    public void cerrarAsignacion() {
        Object[] fila = ventana.seleccion(); int id = ((Number) fila[0]).intValue();
        if (fila[6] != null) { throw new IllegalArgumentException("La asignación seleccionada ya está cerrada."); }
        BigDecimal esperada = new BigDecimal(fila[5].toString()).add(new BigDecimal(fila[8].toString()));
        Formulario f = new Formulario(ventana, "Cerrar asignación #" + id);
        f.agregar("Manguera", new JLabel(fila[3].toString()));
        f.agregar("Litros vendidos", new JLabel(fila[8].toString()));
        JTextField lectura = f.texto("Lectura final (L)", esperada.toPlainString());
        JTextField calibracion = f.texto("Calibración (L)", "0");
        f.agregar("Conciliación", new JLabel("Inicial + ventas + calibración = lectura final"));
        f.mostrar(() -> {
            BigDecimal fin = Formulario.numero(lectura, "Lectura final"); BigDecimal calibrado = Formulario.numero(calibracion, "Calibración");
            ventana.guardar(f, () -> repositorio.cerrarAsignacion(id, fin, calibrado));
        });
    }

    public void cerrarTurno() {
        Object[] fila = ventana.seleccion(); int id = ((Number) fila[0]).intValue();
        if (!"Abierto".equals(fila[3])) { throw new IllegalArgumentException("El turno seleccionado ya está cerrado."); }
        Formulario f = new Formulario(ventana, "Cerrar turno #" + id);
        f.agregar("Turno", new JLabel(fila[1] + " · " + fila[2]));
        f.agregar("Antes de guardar", new JLabel("Todas sus asignaciones deben estar cerradas."));
        f.agregar("Recaudación", new JLabel("Se calculará sumando las ventas de este turno."));
        f.mostrar(() -> ventana.guardar(f, () -> repositorio.cerrarTurno(id)));
    }

    public void tanque() {
        ventana.preparar(new String[]{"combustibles"}, datos -> {
            Formulario f = new Formulario(ventana, "Agregar tanque");
            JTextField codigo = f.texto("Código", ""); JTextField capacidad = f.texto("Capacidad máxima (L)", "");
            JComboBox<Opcion> combustible = f.opciones("Combustible", datos.get("combustibles"));
            f.agregar("Stock inicial", new JLabel("0 L · registra una recepción para cargar combustible"));
            f.mostrar(() -> {
                String identificador = codigo.getText(); BigDecimal maximo = Formulario.numero(capacidad, "Capacidad"); int id = Formulario.elegida(combustible).getId();
                ventana.guardar(f, () -> repositorio.agregarTanque(identificador, maximo, id));
            });
        });
    }

    public void catalogo(Catalogo catalogo) {
        switch (catalogo) {
            case COMBUSTIBLES:
                Formulario f = new Formulario(ventana, "Agregar combustible"); JTextField nombre = f.texto("Nombre", "");
                f.mostrar(() -> { String valor = nombre.getText(); ventana.guardar(f, () -> repositorio.agregarCombustible(valor)); }); break;
            case ISLAS:
                Formulario isla = new Formulario(ventana, "Agregar isla"); JTextField numero = isla.texto("Número de isla", ""); JTextField descripcion = isla.texto("Descripción", "");
                isla.mostrar(() -> {
                    int n;
                    try { n = Integer.parseInt(numero.getText().trim()); } catch (NumberFormatException e) { throw new IllegalArgumentException("El número de isla debe ser un entero."); }
                    String texto = descripcion.getText(); ventana.guardar(isla, () -> repositorio.agregarIsla(n, texto));
                }); break;
            case MANGUERAS: manguera(); break;
            case PRECIOS: precio(); break;
            default: throw new IllegalArgumentException("Catálogo no disponible.");
        }
    }

    private void manguera() {
        ventana.preparar(new String[]{"islas", "tanques"}, datos -> {
            Formulario f = new Formulario(ventana, "Agregar manguera"); JTextField codigo = f.texto("Código", "");
            JComboBox<String> lado = f.seleccion("Lado", "A", "B"); JComboBox<Opcion> isla = f.opciones("Isla", datos.get("islas"));
            JComboBox<Opcion> tanque = f.opciones("Tanque", datos.get("tanques"));
            f.mostrar(() -> {
                String cod = codigo.getText(); String l = (String) lado.getSelectedItem(); int i = Formulario.elegida(isla).getId(); int t = Formulario.elegida(tanque).getId();
                ventana.guardar(f, () -> repositorio.agregarManguera(cod, l, i, t));
            });
        });
    }

    private void precio() {
        ventana.preparar(new String[]{"combustibles"}, datos -> {
            Formulario f = new Formulario(ventana, "Registrar nuevo precio"); JComboBox<Opcion> combustible = f.opciones("Combustible", datos.get("combustibles"));
            JTextField precio = f.texto("Precio por litro (Bs)", ""); JTextField resolucion = f.texto("Resolución / referencia", "");
            f.agregar("Historial", new JLabel("Se conserva el precio anterior y se cierra su vigencia."));
            f.mostrar(() -> {
                int id = Formulario.elegida(combustible).getId(); BigDecimal p = Formulario.numero(precio, "Precio").setScale(2, RoundingMode.HALF_UP); String referencia = resolucion.getText();
                ventana.guardar(f, () -> repositorio.cambiarPrecio(id, p, referencia));
            });
        });
    }
}
