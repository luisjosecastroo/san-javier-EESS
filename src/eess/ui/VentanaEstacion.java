package eess.ui;

import eess.datos.Catalogo;
import eess.datos.EstacionRepositorio;
import eess.datos.Opcion;
import eess.datos.Tabla;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

@SuppressWarnings("serial") // JFrame es serializable; esta aplicación no serializa sus ventanas.
public final class VentanaEstacion extends JFrame {
    private static final long serialVersionUID = 1L;
    private final EstacionRepositorio repositorio;
    private final Acciones acciones;
    private final JPanel centro = new JPanel(new BorderLayout(0, 20));
    private final JLabel estado = new JLabel("Listo");
    private final JProgressBar progreso = new JProgressBar();
    private final List<AbstractButton> botones = new ArrayList<>();
    private final Map<String, JButton> navegacion = new LinkedHashMap<>();
    private Catalogo actual;
    private JTable tabla;
    private boolean ocupado;
    private final boolean demostracion;

    public VentanaEstacion(EstacionRepositorio repositorio, boolean demostracion) {
        super("San Javier · Gestión de estación");
        this.repositorio = repositorio;
        this.demostracion = demostracion;
        acciones = new Acciones(this, repositorio);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1260, 810);
        setMinimumSize(new Dimension(1020, 700));
        setLocationRelativeTo(null);
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Tema.FONDO);
        raiz.add(crearMenu(), BorderLayout.WEST);
        centro.setOpaque(false);
        centro.setBorder(BorderFactory.createEmptyBorder(30, 30, 22, 30));
        raiz.add(centro, BorderLayout.CENTER);
        JPanel pie = new JPanel(new BorderLayout(18, 0));
        pie.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));
        estado.setForeground(Tema.SUAVE);
        pie.add(estado, BorderLayout.CENTER);
        progreso.setPreferredSize(new Dimension(130, 5));
        progreso.setVisible(false);
        pie.add(progreso, BorderLayout.EAST);
        raiz.add(pie, BorderLayout.SOUTH);
        setContentPane(raiz);
        mostrar(null);
    }

    private JPanel crearMenu() {
        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBackground(Tema.OSCURO);
        menu.setBorder(BorderFactory.createEmptyBorder(30, 18, 24, 18));
        menu.setPreferredSize(new Dimension(228, 0));
        JLabel marca = new JLabel("SAN JAVIER");
        marca.setForeground(Color.WHITE);
        marca.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        menu.add(marca);
        JLabel detalle = new JLabel("ESTACIÓN DE SERVICIO");
        detalle.setForeground(new Color(154, 190, 184));
        detalle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        menu.add(Box.createVerticalStrut(6));
        menu.add(detalle);
        menu.add(Box.createVerticalStrut(38));
        agregarMenu(menu, "Resumen", null);
        agregarMenu(menu, "Ventas", Catalogo.VENTAS);
        agregarMenu(menu, "Tanques y cisternas", Catalogo.TANQUES);
        agregarMenu(menu, "Clientes", Catalogo.CLIENTES);
        agregarMenu(menu, "Turnos", Catalogo.TURNOS);
        agregarMenu(menu, "Personal", Catalogo.PERSONAL);
        agregarMenu(menu, "Catálogos", Catalogo.COMBUSTIBLES);
        menu.add(Box.createVerticalGlue());
        JLabel modo = new JLabel(demostracion ? "●  DEMOSTRACIÓN LOCAL" : "●  CONECTADO A SUPABASE");
        modo.setForeground(new Color(140, 221, 191));
        modo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        menu.add(modo);
        menu.add(Box.createVerticalStrut(12));
        JButton cambiar = Tema.boton("Cambiar conexión", false);
        cambiar.setAlignmentX(LEFT_ALIGNMENT);
        cambiar.addActionListener(e -> { dispose(); new PantallaInicio().setVisible(true); });
        botones.add(cambiar);
        menu.add(cambiar);
        return menu;
    }

    private void agregarMenu(JPanel menu, String nombre, Catalogo catalogo) {
        JButton boton = new JButton(nombre);
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setMargin(new Insets(14, 15, 14, 10));
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        boton.setAlignmentX(LEFT_ALIGNMENT);
        boton.setBackground(Tema.OSCURO);
        boton.setForeground(Color.WHITE);
        boton.setBorder(BorderFactory.createEmptyBorder(13, 12, 13, 8));
        boton.addActionListener(e -> mostrar(catalogo));
        navegacion.put(nombre, boton);
        botones.add(boton);
        menu.add(boton);
        menu.add(Box.createVerticalStrut(7));
    }

    private String grupo(Catalogo catalogo) {
        if (catalogo == null) { return "Resumen"; }
        switch (catalogo) {
            case VENTAS: return "Ventas";
            case TANQUES: case RECEPCIONES: return "Tanques y cisternas";
            case CLIENTES: return "Clientes";
            case TURNOS: case ASIGNACIONES: return "Turnos";
            case PERSONAL: return "Personal";
            default: return "Catálogos";
        }
    }

    public void mostrar(Catalogo catalogo) {
        if (ocupado) { return; }
        actual = catalogo;
        for (Map.Entry<String, JButton> item : navegacion.entrySet()) {
            item.getValue().setBackground(item.getKey().equals(grupo(catalogo)) ? Tema.VERDE : Tema.OSCURO);
        }
        centro.removeAll();
        JPanel cabecera = new JPanel(new BorderLayout(8, 14));
        cabecera.setOpaque(false);
        JPanel titulos = new JPanel(new GridLayout(0, 1, 0, 6));
        titulos.setOpaque(false);
        titulos.add(Tema.titulo(catalogo == null ? "Tu estación, de un vistazo" : catalogo.toString(), 28));
        JLabel descripcion = new JLabel(catalogo == null ? "Operaciones de hoy y disponibilidad de combustible" : "Consulta, busca y registra las operaciones de tu estación");
        descripcion.setForeground(Tema.SUAVE);
        titulos.add(descripcion);
        cabecera.add(titulos, BorderLayout.CENTER);
        JButton recargar = boton("Actualizar", false, () -> mostrar(actual));
        cabecera.add(recargar, BorderLayout.EAST);
        centro.add(cabecera, BorderLayout.NORTH);
        JLabel cargando = new JLabel("Cargando información…", SwingConstants.CENTER);
        cargando.setForeground(Tema.SUAVE);
        centro.add(cargando, BorderLayout.CENTER);
        centro.revalidate(); centro.repaint();
        if (catalogo == null) {
            ejecutar(() -> new Tabla[]{repositorio.resumen(), repositorio.consultar(Catalogo.TANQUES)}, datos -> {
                centro.remove(cargando);
                centro.add(crearResumen(datos), BorderLayout.CENTER);
                centro.revalidate(); centro.repaint();
            });
        } else {
            ejecutar(() -> repositorio.consultar(catalogo), datos -> {
                centro.remove(cargando);
                centro.add(crearListado(catalogo, datos), BorderLayout.CENTER);
                centro.revalidate(); centro.repaint();
            });
        }
    }

    private JPanel crearResumen(Tabla[] datos) {
        JPanel panel = new JPanel(new BorderLayout(0, 24)); panel.setOpaque(false);
        Object[] valores = datos[0].getFilas()[0];
        String[] etiquetas = {"VENTAS DE HOY · Bs", "LITROS VENDIDOS HOY", "TURNOS ABIERTOS", "TANQUES BAJO 20%"};
        JPanel indicadores = new JPanel(new GridLayout(1, 4, 14, 0)); indicadores.setOpaque(false);
        for (int i = 0; i < valores.length; i++) {
            JPanel tarjeta = Tema.tarjeta(); tarjeta.setLayout(new BorderLayout(0, 12));
            JLabel etiqueta = new JLabel(etiquetas[i]); etiqueta.setForeground(Tema.SUAVE); etiqueta.setFont(etiqueta.getFont().deriveFont(10f));
            tarjeta.add(etiqueta, BorderLayout.NORTH);
            String valor = valores[i] instanceof Number ? new java.text.DecimalFormat(i < 2 ? "#,##0.00" : "0").format(valores[i]) : valores[i].toString();
            tarjeta.add(Tema.titulo(valor, 29), BorderLayout.CENTER);
            indicadores.add(tarjeta);
        }
        panel.add(indicadores, BorderLayout.NORTH);
        JPanel existencias = Tema.tarjeta(); existencias.setLayout(new BorderLayout(0, 22));
        existencias.add(Tema.titulo("Combustible disponible", 20), BorderLayout.NORTH);
        JPanel tanques = new JPanel(); tanques.setOpaque(false); tanques.setLayout(new BoxLayout(tanques, BoxLayout.Y_AXIS));
        for (Object[] fila : datos[1].getFilas()) {
            double stock = ((Number) fila[3]).doubleValue(); double capacidad = ((Number) fila[4]).doubleValue();
            JPanel tanque = new JPanel(new BorderLayout(12, 10)); tanque.setOpaque(false); tanque.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
            tanque.add(Tema.titulo(fila[1] + "  /  " + fila[2], 15), BorderLayout.NORTH);
            JProgressBar barra = new JProgressBar(0, 100); barra.setValue((int) (stock / capacidad * 100));
            barra.setForeground(stock < capacidad * 0.20 ? new Color(190, 125, 30) : Tema.VERDE);
            barra.setStringPainted(true); barra.setString(String.format(java.util.Locale.getDefault(), "%,.2f / %,.2f L", stock, capacidad));
            barra.setPreferredSize(new Dimension(400, 24)); tanque.add(barra, BorderLayout.CENTER);
            tanques.add(tanque); tanques.add(Box.createVerticalStrut(22));
        }
        if (datos[1].getFilas().length == 0) { tanques.add(new JLabel("Todavía no hay tanques. Agrégalos desde Tanques y cisternas.")); }
        JScrollPane scroll = new JScrollPane(tanques); scroll.setBorder(null); existencias.add(scroll, BorderLayout.CENTER);
        panel.add(existencias, BorderLayout.CENTER);
        JPanel accesos = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); accesos.setOpaque(false);
        accesos.add(boton("+ Registrar venta", true, acciones::venta));
        accesos.add(boton("+ Recibir cisterna", false, acciones::recepcion));
        accesos.add(boton("Ver turnos", false, () -> mostrar(Catalogo.TURNOS)));
        panel.add(accesos, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearListado(Catalogo catalogo, Tabla datos) {
        JPanel panel = Tema.tarjeta(); panel.setLayout(new BorderLayout(0, 18));
        JPanel herramientas = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); herramientas.setOpaque(false);
        switch (catalogo) {
            case VENTAS: herramientas.add(boton("+ Registrar venta", true, acciones::venta)); break;
            case TANQUES:
                herramientas.add(boton("+ Recibir cisterna", true, acciones::recepcion));
                herramientas.add(boton("+ Tanque", false, acciones::tanque));
                herramientas.add(boton("Ver recepciones", false, () -> mostrar(Catalogo.RECEPCIONES))); break;
            case RECEPCIONES:
                herramientas.add(boton("+ Recibir cisterna", true, acciones::recepcion));
                herramientas.add(boton("Ver tanques", false, () -> mostrar(Catalogo.TANQUES))); break;
            case CLIENTES:
                herramientas.add(boton("+ Cliente", true, () -> acciones.cliente(false)));
                herramientas.add(boton("Editar seleccionado", false, () -> acciones.cliente(true))); break;
            case PERSONAL:
                herramientas.add(boton("+ Personal", true, acciones::personal));
                herramientas.add(boton("Activar / desactivar", false, acciones::cambiarActivo)); break;
            case TURNOS:
                herramientas.add(boton("+ Abrir turno", true, acciones::abrirTurno));
                herramientas.add(boton("Asignar manguera", false, acciones::asignacion));
                herramientas.add(boton("Cerrar turno", false, acciones::cerrarTurno));
                herramientas.add(boton("Ver asignaciones", false, () -> mostrar(Catalogo.ASIGNACIONES))); break;
            case ASIGNACIONES:
                herramientas.add(boton("+ Asignación", true, acciones::asignacion));
                herramientas.add(boton("Cerrar asignación", false, acciones::cerrarAsignacion));
                herramientas.add(boton("Ver turnos", false, () -> mostrar(Catalogo.TURNOS))); break;
            default:
                JComboBox<Catalogo> selector = new JComboBox<>(new Catalogo[]{Catalogo.COMBUSTIBLES, Catalogo.PRECIOS, Catalogo.ISLAS, Catalogo.MANGUERAS});
                selector.setSelectedItem(catalogo); selector.addActionListener(e -> mostrar((Catalogo) selector.getSelectedItem()));
                herramientas.add(selector);
                herramientas.add(boton(catalogo == Catalogo.PRECIOS ? "+ Nuevo precio" : "+ Agregar", true, () -> acciones.catalogo(catalogo)));
        }
        panel.add(herramientas, BorderLayout.NORTH);
        DefaultTableModel modelo = new DefaultTableModel(datos.getFilas(), datos.getColumnas()) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int fila, int columna) { return false; }
            @Override public Class<?> getColumnClass(int columna) {
                for (int fila = 0; fila < getRowCount(); fila++) { if (getValueAt(fila, columna) != null) { return getValueAt(fila, columna).getClass(); } }
                return Object.class;
            }
        };
        tabla = new JTable(modelo); tabla.setRowHeight(35); tabla.setShowVerticalLines(false); tabla.setGridColor(Tema.BORDE);
        tabla.getTableHeader().setReorderingAllowed(false); tabla.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tabla.setSelectionBackground(new Color(216, 239, 232)); tabla.setSelectionForeground(Tema.TINTA);
        TableRowSorter<DefaultTableModel> orden = new TableRowSorter<>(modelo); tabla.setRowSorter(orden);
        JScrollPane scroll = new JScrollPane(tabla); scroll.setBorder(BorderFactory.createLineBorder(Tema.BORDE));
        panel.add(scroll, BorderLayout.CENTER);
        JPanel pie = new JPanel(new BorderLayout(12, 0)); pie.setOpaque(false);
        JTextField buscar = new JTextField(22); buscar.setToolTipText("Buscar en todas las columnas");
        buscar.getAccessibleContext().setAccessibleName("Buscar registros");
        buscar.getDocument().addDocumentListener(new DocumentListener() {
            private void filtrar() { orden.setRowFilter(buscar.getText().isBlank() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(buscar.getText()))); }
            @Override public void insertUpdate(DocumentEvent e) { filtrar(); }
            @Override public void removeUpdate(DocumentEvent e) { filtrar(); }
            @Override public void changedUpdate(DocumentEvent e) { filtrar(); }
        });
        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); busqueda.setOpaque(false);
        busqueda.add(new JLabel("Buscar")); busqueda.add(buscar);
        pie.add(busqueda, BorderLayout.WEST);
        pie.add(boton("Exportar CSV", false, this::exportar), BorderLayout.EAST);
        panel.add(pie, BorderLayout.SOUTH);
        estado.setText(datos.getFilas().length + " registros cargados · máximo 500 · " + (demostracion ? "Datos locales de práctica" : "Datos de Supabase"));
        return panel;
    }

    private JButton boton(String texto, boolean principal, Runnable accion) {
        JButton boton = Tema.boton(texto, principal);
        boton.addActionListener(e -> { try { accion.run(); } catch (RuntimeException exception) { error(exception); } });
        botones.add(boton);
        return boton;
    }

    public Object[] seleccion() {
        if (tabla == null || tabla.getSelectedRow() < 0) { throw new IllegalArgumentException("Selecciona primero una fila de la tabla."); }
        int fila = tabla.convertRowIndexToModel(tabla.getSelectedRow());
        Object[] valores = new Object[tabla.getColumnCount()];
        for (int i = 0; i < valores.length; i++) { valores[i] = tabla.getModel().getValueAt(fila, i); }
        return valores;
    }

    public void preparar(String[] grupos, Consumer<Map<String, List<Opcion>>> mostrarFormulario) {
        ejecutar(() -> {
            Map<String, List<Opcion>> opciones = new LinkedHashMap<>();
            for (String grupo : grupos) { opciones.put(grupo, repositorio.opciones(grupo)); }
            return opciones;
        }, mostrarFormulario);
    }

    @FunctionalInterface
    public interface Tarea { void ejecutar() throws Exception; }

    public void guardar(Formulario formulario, Tarea tarea) {
        formulario.ocupado(true);
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception { tarea.ejecutar(); return null; }
            @Override protected void done() {
                formulario.ocupado(false);
                try { get(); formulario.dispose(); mostrar(actual); }
                catch (Exception exception) { formulario.mostrarError(mensaje(causa(exception))); }
            }
        }.execute();
    }

    private <T> void ejecutar(Callable<T> tarea, Consumer<T> resultado) {
        establecerOcupado(true);
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() throws Exception { return tarea.call(); }
            @Override protected void done() {
                establecerOcupado(false);
                try { resultado.accept(get()); }
                catch (Exception exception) { error(causa(exception)); }
            }
        }.execute();
    }

    private void establecerOcupado(boolean valor) {
        ocupado = valor;
        botones.removeIf(boton -> !javax.swing.SwingUtilities.isDescendingFrom(boton, this));
        for (AbstractButton boton : botones) { boton.setEnabled(!valor); }
        progreso.setVisible(valor); progreso.setIndeterminate(valor);
        setCursor(Cursor.getPredefinedCursor(valor ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
    }

    public static Throwable causa(Throwable exception) {
        return exception instanceof ExecutionException && exception.getCause() != null ? exception.getCause() : exception;
    }

    public static String mensaje(Throwable exception) {
        if (exception instanceof SQLException) {
            String codigo = ((SQLException) exception).getSQLState();
            if ("23505".equals(codigo)) { return "Ese dato ya existe. Revisa el NIT/CI, código, factura o asignación duplicada."; }
            if ("23503".equals(codigo)) { return "Un registro relacionado ya no existe. Actualiza la información y vuelve a intentarlo."; }
            if ("23514".equals(codigo)) { return "La base rechazó un valor. Revisa cantidades, capacidad, fechas y estados."; }
            if ("28P01".equals(codigo)) { return "La contraseña de PostgreSQL no es correcta. Usa la contraseña de la base, no la de tu cuenta de Supabase."; }
            if (codigo != null && codigo.startsWith("08")) { return "No se pudo conectar. Revisa internet, servidor y que el proyecto de Supabase esté activo."; }
        }
        String mensaje = exception.getMessage();
        return mensaje == null ? "No se pudo completar la operación." : mensaje;
    }

    private void error(Throwable exception) { JOptionPane.showMessageDialog(this, mensaje(exception), "Revisa la operación", JOptionPane.WARNING_MESSAGE); }

    private void exportar() {
        if (tabla == null) { return; }
        JFileChooser selector = new JFileChooser(); selector.setSelectedFile(new File("san-javier-" + actual.name().toLowerCase(java.util.Locale.ROOT) + ".csv"));
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) { return; }
        File archivo = selector.getSelectedFile();
        if (archivo.exists() && JOptionPane.showConfirmDialog(this, "¿Reemplazar el archivo existente?", "Exportar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) { return; }
        try {
            StringBuilder csv = new StringBuilder("\ufeff");
            for (int i = -1; i < tabla.getRowCount(); i++) {
                for (int j = 0; j < tabla.getColumnCount(); j++) {
                    if (j > 0) { csv.append(','); }
                    Object valor = i == -1 ? tabla.getColumnName(j) : tabla.getValueAt(i, j);
                    String texto = valor == null ? "" : valor.toString();
                    if (!(valor instanceof Number) && texto.matches("^[=+@-].*")) { texto = "'" + texto; }
                    csv.append('"').append(texto.replace("\"", "\"\"")).append('"');
                }
                csv.append('\n');
            }
            Files.writeString(archivo.toPath(), csv.toString(), StandardCharsets.UTF_8);
            estado.setText("CSV exportado: " + archivo.getName() + " · " + tabla.getRowCount() + " filas visibles");
        } catch (Exception exception) { error(exception); }
    }
}
