package eess.ui;

import eess.datos.BaseDemostracion;
import eess.datos.Conexion;
import eess.datos.RepositorioJDBC;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

/** El usuario elige explícitamente entre datos de práctica y su base de Supabase. */
public final class PantallaInicio extends JFrame {
    private static final long serialVersionUID = 1L;
    private final JButton demo = Tema.boton("Abrir demostración", true);
    private final JButton conectar = Tema.boton("Conectar a Supabase", true);
    private final JTextField servidor = new JTextField("aws-0-us-west-2.pooler.supabase.com");
    private final JTextField usuario = new JTextField("postgres.vddiylgtyoqeyllescoo");
    private final JPasswordField clave = new JPasswordField();
    private final JLabel estado = new JLabel("Elige cómo quieres comenzar.");
    private final JProgressBar progreso = new JProgressBar();

    public PantallaInicio() {
        super("San Javier · Bienvenido");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(960, 680);
        setMinimumSize(new Dimension(870, 640));
        setLocationRelativeTo(null);
        JPanel raiz = new JPanel(new BorderLayout(0, 30));
        raiz.setBackground(Tema.FONDO);
        raiz.setBorder(BorderFactory.createEmptyBorder(35, 38, 25, 38));
        JPanel marca = new JPanel(new GridLayout(0, 1, 0, 10)); marca.setOpaque(false);
        JLabel etiqueta = new JLabel("SAN JAVIER / ESTACIÓN DE SERVICIO"); etiqueta.setForeground(Tema.VERDE);
        etiqueta.setFont(etiqueta.getFont().deriveFont(java.awt.Font.BOLD, 12f)); marca.add(etiqueta);
        marca.add(Tema.titulo("Todo listo para tu próximo turno.", 30));
        JLabel descripcion = new JLabel("Gestiona ventas, combustible y personal desde un solo lugar."); descripcion.setForeground(Tema.SUAVE); marca.add(descripcion);
        raiz.add(marca, BorderLayout.NORTH);
        JPanel opciones = new JPanel(new GridLayout(1, 2, 20, 0)); opciones.setOpaque(false);
        JPanel local = Tema.tarjeta(); local.setLayout(new BorderLayout(0, 22));
        local.add(Tema.titulo("Practicar sin conexión", 22), BorderLayout.NORTH);
        JLabel detalle = new JLabel("<html><body style='width:285px'>Explora el programa con datos ficticios.<br><br>Incluye tanques, clientes, personal y un turno abierto.<br><br>Los cambios se conservan en tu computadora y están separados de Supabase.<br><br><b>Recomendado para ensayar la demostración.</b></body></html>");
        detalle.setForeground(Tema.SUAVE); local.add(detalle, BorderLayout.CENTER); local.add(demo, BorderLayout.SOUTH); opciones.add(local);
        JPanel remoto = Tema.tarjeta(); remoto.setLayout(new BorderLayout(0, 16));
        remoto.add(Tema.titulo("Usar mi base de datos", 22), BorderLayout.NORTH);
        JPanel campos = new JPanel(new GridLayout(0, 1, 0, 5)); campos.setOpaque(false);
        agregarCampo(campos, "Servidor PostgreSQL", servidor); agregarCampo(campos, "Usuario de la base", usuario); agregarCampo(campos, "Contraseña de la base", clave);
        JLabel privacidad = new JLabel("<html>La contraseña se usa en memoria.<br>No se guarda en el proyecto.</html>"); privacidad.setForeground(Tema.SUAVE); privacidad.setFont(privacidad.getFont().deriveFont(11f)); campos.add(privacidad);
        remoto.add(campos, BorderLayout.CENTER); remoto.add(conectar, BorderLayout.SOUTH); opciones.add(remoto);
        raiz.add(opciones, BorderLayout.CENTER);
        JPanel pie = new JPanel(new BorderLayout(0, 10)); pie.setOpaque(false);
        estado.setForeground(Tema.SUAVE); pie.add(estado, BorderLayout.CENTER); progreso.setVisible(false); progreso.setPreferredSize(new Dimension(100, 6)); pie.add(progreso, BorderLayout.SOUTH);
        raiz.add(pie, BorderLayout.SOUTH); setContentPane(raiz);
        demo.addActionListener(e -> abrirDemostracion());
        conectar.addActionListener(e -> conectarSupabase());
        clave.addActionListener(e -> conectarSupabase());
        getRootPane().setDefaultButton(demo);
    }

    private void agregarCampo(JPanel panel, String texto, JTextField campo) {
        JLabel label = new JLabel(texto); label.setLabelFor(campo); panel.add(label); panel.add(campo);
    }

    public void abrirDemostracion() { iniciar(Conexion.demostracion()); }

    private void conectarSupabase() {
        if (!conectar.isEnabled()) { return; }
        char[] password = clave.getPassword();
        try { iniciar(Conexion.supabase(servidor.getText().trim(), usuario.getText().trim(), password)); }
        catch (RuntimeException exception) { mostrarError(exception); }
        finally { Arrays.fill(password, '\0'); clave.setText(""); }
    }

    private void iniciar(Conexion conexion) {
        if (!demo.isEnabled()) { return; }
        demo.setEnabled(false); conectar.setEnabled(false); servidor.setEnabled(false); usuario.setEnabled(false); clave.setEnabled(false);
        estado.setForeground(Tema.SUAVE); estado.setText("Preparando la conexión y comprobando las tablas…"); progreso.setVisible(true); progreso.setIndeterminate(true);
        new SwingWorker<RepositorioJDBC, Void>() {
            @Override protected RepositorioJDBC doInBackground() throws Exception {
                if (conexion.esDemostracion()) { BaseDemostracion.preparar(conexion); }
                RepositorioJDBC repositorio = new RepositorioJDBC(conexion); repositorio.verificarEstructura(); return repositorio;
            }
            @Override protected void done() {
                demo.setEnabled(true); conectar.setEnabled(true); servidor.setEnabled(true); usuario.setEnabled(true); clave.setEnabled(true); progreso.setVisible(false);
                try { new VentanaEstacion(get(), conexion.esDemostracion()).setVisible(true); dispose(); }
                catch (Exception exception) { mostrarError(VentanaEstacion.causa(exception)); }
            }
        }.execute();
    }

    private void mostrarError(Throwable exception) {
        estado.setForeground(new Color(162, 44, 44));
        estado.setText("<html><body style='width:640px'>" + Formulario.escapar(VentanaEstacion.mensaje(exception)) + "</body></html>");
    }
}
