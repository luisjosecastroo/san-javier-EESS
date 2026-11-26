package eess;

import eess.datos.BaseDemostracion;
import eess.datos.Catalogo;
import eess.datos.Conexion;
import eess.datos.RepositorioJDBC;
import eess.ui.PantallaInicio;
import eess.ui.Tema;
import java.util.Arrays;
import javax.swing.SwingUtilities;

public final class Main {
    private Main() { }

    public static void main(String[] args) throws Exception {
        java.nio.file.Path ubicacion = java.nio.file.Path.of(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        java.nio.file.Path raiz = ubicacion.getParent();
        if (raiz.getFileName().toString().equals("app") && raiz.getParent().getFileName().toString().equals("Contents")) {
            raiz = raiz.getParent().getParent().getParent();
        }
        System.setProperty("eess.directorio", raiz.toString());
        if (Arrays.asList(args).contains("--verificar")) {
            Conexion conexion = Conexion.demostracion();
            BaseDemostracion.preparar(conexion);
            RepositorioJDBC repositorio = new RepositorioJDBC(conexion);
            repositorio.verificarEstructura();
            for (Catalogo catalogo : Catalogo.values()) {
                System.out.println(catalogo + ": " + repositorio.consultar(catalogo).getFilas().length + " registros");
            }
            System.out.println("Verificación local correcta. Supabase no fue modificado.");
            return;
        }
        SwingUtilities.invokeLater(() -> {
            Tema.aplicar();
            PantallaInicio inicio = new PantallaInicio();
            inicio.setVisible(true);
            if (Arrays.asList(args).contains("--demo")) { inicio.abrirDemostracion(); }
        });
    }
}
