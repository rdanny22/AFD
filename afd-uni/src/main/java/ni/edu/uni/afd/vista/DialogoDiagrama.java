package ni.edu.uni.afd.vista;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Diálogo que muestra el diagrama de estados del AFD incluido en los recursos.
 */
public final class DialogoDiagrama {

    /** Ruta del diagrama dentro de los recursos de la aplicación. */
    public static final String RECURSO = "/recursos/diagrama-afd.png";

    private DialogoDiagrama() {
        // Clase de utilidades: no se instancia.
    }

    /**
     * Abre el diálogo con el diagrama del autómata.
     *
     * @param padre ventana propietaria
     */
    public static void mostrar(Window padre) {
        URL recurso = DialogoDiagrama.class.getResource(RECURSO);
        if (recurso == null) {
            JOptionPane.showMessageDialog(padre,
                    "No se encontró el diagrama " + RECURSO
                            + ".\nGenere la imagen con: dot -Tpng docs/diagrama-afd.dot -o docs/diagrama-afd.png",
                    "Diagrama no disponible", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialogo = new JDialog(padre, "Diagrama de estados del AFD", Dialog.ModalityType.APPLICATION_MODAL);
        JLabel imagen = new JLabel(new ImageIcon(recurso), SwingConstants.CENTER);
        JScrollPane scroll = new JScrollPane(imagen);
        scroll.setBorder(new EmptyBorder(0, 0, 0, 0));

        JLabel nota = new JLabel("q18 es el único estado final (doble círculo) y q19 el sumidero de rechazo.");
        nota.setBorder(new EmptyBorder(6, 10, 8, 10));

        JPanel contenido = new JPanel(new BorderLayout());
        contenido.add(scroll, BorderLayout.CENTER);
        contenido.add(nota, BorderLayout.SOUTH);

        dialogo.setContentPane(contenido);
        dialogo.setPreferredSize(new Dimension(900, 620));
        dialogo.pack();
        dialogo.setLocationRelativeTo(padre);
        dialogo.setVisible(true);
    }
}
