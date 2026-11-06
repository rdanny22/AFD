package ni.edu.uni.afd;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import ni.edu.uni.afd.vista.VentanaPrincipal;

/**
 * Punto de entrada de la aplicación.
 *
 * <p>Configura el aspecto del sistema (Nimbus como respaldo) y construye la ventana principal dentro
 * del hilo de despacho de eventos de Swing.</p>
 */
public final class Main {

    private static final Logger REGISTRO = Logger.getLogger(Main.class.getName());

    private Main() {
        // Clase de entrada: no se instancia.
    }

    /**
     * Arranca la aplicación.
     *
     * @param argumentos argumentos de línea de comandos (no se usan)
     */
    public static void main(String[] argumentos) {
        configurarAspecto();
        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
        });
    }

    private static void configurarAspecto() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException excepcion) {
            REGISTRO.log(Level.FINE, "No se pudo aplicar el aspecto del sistema; se intenta Nimbus", excepcion);
            aplicarNimbus();
        }
    }

    private static void aplicarNimbus() {
        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                try {
                    UIManager.setLookAndFeel(info.getClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                        | UnsupportedLookAndFeelException excepcion) {
                    REGISTRO.log(Level.FINE, "Nimbus no está disponible; se usa el aspecto por defecto", excepcion);
                }
                return;
            }
        }
    }
}
