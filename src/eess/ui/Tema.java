package eess.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

public final class Tema {
    public static final Color FONDO = new Color(244, 247, 248);
    public static final Color TINTA = new Color(24, 49, 51);
    public static final Color SUAVE = new Color(96, 115, 120);
    public static final Color VERDE = new Color(0, 119, 102);
    public static final Color OSCURO = new Color(18, 43, 44);
    public static final Color BORDE = new Color(221, 230, 232);

    private Tema() { }

    public static void aplicar() {
        try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); }
        catch (Exception exception) { System.err.println("Se usará el aspecto predeterminado de Java."); }
        Font fuente = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
        for (Object clave : UIManager.getDefaults().keySet().toArray()) {
            if (clave.toString().endsWith(".font")) { UIManager.put(clave, fuente); }
        }
        UIManager.put("control", Color.WHITE);
        UIManager.put("text", TINTA);
        UIManager.put("nimbusBase", VERDE);
        UIManager.put("nimbusFocus", new Color(71, 164, 149));
        UIManager.put("Table.alternateRowColor", new Color(246, 249, 249));
    }

    public static JLabel titulo(String texto, int tamano) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, tamano));
        label.setForeground(TINTA);
        return label;
    }

    public static JButton boton(String texto, boolean principal) {
        JButton boton = new JButton(texto);
        boton.setFocusPainted(true);
        boton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        boton.setBackground(principal ? VERDE : Color.WHITE);
        boton.setForeground(principal ? Color.WHITE : TINTA);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(principal ? VERDE : BORDE),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        return boton;
    }

    public static JPanel tarjeta() { return new Tarjeta(); }

    private static final class Tarjeta extends JPanel {
        private static final long serialVersionUID = 1L;
        Tarjeta() { setOpaque(false); setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22)); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D dibujo = (Graphics2D) g.create();
            dibujo.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            dibujo.setColor(Color.WHITE);
            dibujo.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            dibujo.setColor(BORDE);
            dibujo.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            dibujo.dispose();
            super.paintComponent(g);
        }
    }
}
