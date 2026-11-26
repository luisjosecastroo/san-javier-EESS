package eess.ui;

import eess.datos.Opcion;
import eess.modelo.Validacion;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** Formulario reutilizable con errores visibles y conservación de los datos escritos. */
public final class Formulario extends JDialog {
    private static final long serialVersionUID = 1L;
    private final JPanel campos = new JPanel(new GridBagLayout());
    private final JLabel error = new JLabel(" ");
    private final JButton guardar = Tema.boton("Guardar", true);
    private final JButton cancelar = Tema.boton("Cancelar", false);
    private int fila;

    public Formulario(Window padre, String titulo) {
        super(padre, titulo, Dialog.ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel contenido = new JPanel(new BorderLayout(12, 20));
        contenido.setBorder(BorderFactory.createEmptyBorder(24, 26, 20, 26));
        contenido.add(Tema.titulo(titulo, 23), BorderLayout.NORTH);
        contenido.add(campos, BorderLayout.CENTER);
        JPanel pie = new JPanel(new BorderLayout(0, 12));
        error.setForeground(new java.awt.Color(162, 44, 44));
        pie.add(error, BorderLayout.NORTH);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(cancelar);
        botones.add(guardar);
        pie.add(botones, BorderLayout.SOUTH);
        contenido.add(pie, BorderLayout.SOUTH);
        setContentPane(contenido);
        cancelar.addActionListener(evento -> dispose());
        getRootPane().setDefaultButton(guardar);
    }

    public JTextField texto(String etiqueta, String valor) {
        JTextField campo = new JTextField(valor, 24);
        agregar(etiqueta, campo);
        return campo;
    }

    public JComboBox<String> seleccion(String etiqueta, String... valores) {
        JComboBox<String> combo = new JComboBox<>(valores);
        agregar(etiqueta, combo);
        return combo;
    }

    public JComboBox<Opcion> opciones(String etiqueta, List<Opcion> valores) {
        if (valores.isEmpty()) { throw new IllegalArgumentException("No hay registros para «" + etiqueta + "». Agrégalos primero en su sección."); }
        JComboBox<Opcion> combo = new JComboBox<>(valores.toArray(new Opcion[0]));
        agregar(etiqueta, combo);
        return combo;
    }

    public void agregar(String etiqueta, JComponent componente) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = fila++; g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(7, 0, 7, 18);
        JLabel label = new JLabel(etiqueta);
        label.setLabelFor(componente);
        campos.add(label, g);
        g.gridx = 1; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(7, 0, 7, 0);
        campos.add(componente, g);
    }

    public void mostrar(Runnable accionGuardar) {
        guardar.addActionListener(evento -> {
            try { error.setText(" "); accionGuardar.run(); }
            catch (RuntimeException exception) { mostrarError(exception.getMessage()); }
        });
        pack();
        setMinimumSize(new java.awt.Dimension(560, getHeight()));
        setLocationRelativeTo(getOwner());
        setVisible(true);
    }

    public void ocupado(boolean ocupado) {
        guardar.setEnabled(!ocupado);
        cancelar.setEnabled(!ocupado);
        setDefaultCloseOperation(ocupado ? DO_NOTHING_ON_CLOSE : DISPOSE_ON_CLOSE);
        for (Component componente : campos.getComponents()) { componente.setEnabled(!ocupado); }
        guardar.setText(ocupado ? "Guardando…" : "Guardar");
    }

    public void mostrarError(String mensaje) {
        error.setText("<html><body style='width:440px'>" + escapar(mensaje) + "</body></html>");
        pack();
    }

    public static String escapar(String texto) {
        return texto == null ? "Error inesperado." : texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public static Opcion elegida(JComboBox<Opcion> combo) {
        Opcion opcion = (Opcion) combo.getSelectedItem();
        if (opcion == null) { throw new IllegalArgumentException("Selecciona una opción."); }
        return opcion;
    }

    public static BigDecimal numero(JTextField campo, String nombre) {
        return Validacion.numero(campo.getText(), nombre);
    }
}
