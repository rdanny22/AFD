package ni.edu.uni.afd.vista;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import ni.edu.uni.afd.modelo.ResultadoValidacion;

/**
 * Ventana que <strong>dibuja el AFD en pantalla</strong> con {@link PanelDiagrama}.
 *
 * <p>El diagrama es vectorial y se repinta en vivo: cada validación de la ventana principal resalta
 * sobre el dibujo el camino de estados recorrido. Incluye control de zoom, un interruptor para ver las
 * transiciones de rechazo hacia {@code q19} y exportación a PNG.</p>
 */
public final class DialogoDiagrama extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final Logger REGISTRO = Logger.getLogger(DialogoDiagrama.class.getName());

    private static final double PASO_ZOOM = 1.25;
    private static final int ANCHO_VENTANA = 1080;
    private static final int ALTO_VENTANA = 660;

    private final transient PanelDiagrama panel = new PanelDiagrama();
    private final JCheckBox casillaResaltar = new JCheckBox("Resaltar última validación", true);
    private final JCheckBox casillaRechazos = new JCheckBox("Transiciones de rechazo", false);
    private final JLabel etiquetaZoom = new JLabel("100 %");
    private final JLabel etiquetaTraza = new JLabel("Sin validaciones todavía.");
    private final JButton botonAcercar = new JButton("+");
    private final JButton botonAlejar = new JButton("−");
    private final JButton botonCien = new JButton("100 %");
    private final JButton botonExportar = new JButton("Exportar PNG");
    private final JButton botonCerrar = new JButton("Cerrar");

    private transient ResultadoValidacion ultima;

    /**
     * Construye la ventana del diagrama.
     *
     * @param padre ventana propietaria
     */
    public DialogoDiagrama(Window padre) {
        super(padre, "Diagrama de estados del AFD", Dialog.ModalityType.MODELESS);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(construirContenido());
        setPreferredSize(new Dimension(ANCHO_VENTANA, ALTO_VENTANA));
        pack();
        setLocationRelativeTo(padre);
        conectarAcciones();
    }

    private JPanel construirContenido() {
        JPanel contenido = new JPanel(new BorderLayout());
        contenido.add(barraHerramientas(), BorderLayout.NORTH);
        contenido.add(new JScrollPane(panel), BorderLayout.CENTER);
        contenido.add(pie(), BorderLayout.SOUTH);
        return contenido;
    }

    private JPanel barraHerramientas() {
        botonAcercar.setToolTipText("Acercar el diagrama");
        botonAlejar.setToolTipText("Alejar el diagrama");
        botonCien.setToolTipText("Volver al 100 %");
        botonExportar.setToolTipText("Guardar el diagrama como imagen PNG");
        casillaResaltar.setToolTipText("Resalta el camino de estados de la última cadena validada");
        casillaRechazos.setToolTipText("Dibuja también las transiciones que conducen a q19");
        etiquetaZoom.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        etiquetaZoom.setBorder(new EmptyBorder(0, 6, 0, 6));

        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        izquierda.add(botonAlejar);
        izquierda.add(etiquetaZoom);
        izquierda.add(botonAcercar);
        izquierda.add(botonCien);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        derecha.add(casillaResaltar);
        derecha.add(casillaRechazos);
        derecha.add(botonExportar);

        JPanel barra = new JPanel(new BorderLayout());
        barra.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(0xD0, 0xD0, 0xD0)));
        barra.add(izquierda, BorderLayout.WEST);
        barra.add(derecha, BorderLayout.EAST);
        return barra;
    }

    private JPanel pie() {
        etiquetaTraza.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        etiquetaTraza.setForeground(PanelValidacion.GRIS);
        etiquetaTraza.setBorder(new EmptyBorder(6, 10, 8, 10));

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        acciones.add(botonCerrar);

        JPanel panelPie = new JPanel(new BorderLayout());
        panelPie.add(etiquetaTraza, BorderLayout.CENTER);
        panelPie.add(acciones, BorderLayout.EAST);
        return panelPie;
    }

    private void conectarAcciones() {
        botonAcercar.addActionListener(evento -> aplicarZoom(panel.getZoom() * PASO_ZOOM));
        botonAlejar.addActionListener(evento -> aplicarZoom(panel.getZoom() / PASO_ZOOM));
        botonCien.addActionListener(evento -> aplicarZoom(1.0));
        botonCerrar.addActionListener(evento -> dispose());
        casillaRechazos.addActionListener(evento -> panel.setMostrarRechazos(casillaRechazos.isSelected()));
        casillaResaltar.addActionListener(evento -> panel.setTraza(
                casillaResaltar.isSelected() ? ultima : null));
        botonExportar.addActionListener(evento -> exportar());

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ESCAPE"), "cerrar-diagrama");
        getRootPane().getActionMap().put("cerrar-diagrama", new AccionCerrar(this));
    }

    private void aplicarZoom(double factor) {
        panel.setZoom(factor);
        etiquetaZoom.setText(String.format(Locale.ROOT, "%d %%", Math.round(panel.getZoom() * 100)));
    }

    /**
     * Resalta en el dibujo el camino de estados de un resultado.
     *
     * @param resultado último resultado validado, o {@code null} para limpiar el resaltado
     */
    public void actualizarTraza(ResultadoValidacion resultado) {
        this.ultima = resultado;
        panel.setTraza(casillaResaltar.isSelected() ? resultado : null);
        etiquetaTraza.setText(textoDeTraza(resultado));
    }

    private static String textoDeTraza(ResultadoValidacion resultado) {
        if (resultado == null) {
            return "Sin validaciones todavía.";
        }
        String veredicto = resultado.aceptado() ? "ACEPTADO" : "RECHAZADO";
        String detalle = resultado.consumioTodo()
                ? "cadena consumida por completo"
                : "detenido en la posición " + resultado.indiceFallo();
        return "Última validación: «" + resultado.cadenaParaMostrar() + "» · " + veredicto
                + " · " + detalle + " · camino: " + resultado.caminoConFlechas();
    }

    /** @return el panel que dibuja el autómata */
    public PanelDiagrama getPanelDiagrama() {
        return panel;
    }

    private void exportar() {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Guardar el diagrama del AFD");
        selector.setSelectedFile(new File("diagrama-afd.png"));
        selector.setFileFilter(new FileNameExtensionFilter("Imagen PNG (*.png)", "png"));
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File destino = selector.getSelectedFile();
        if (!destino.getName().toLowerCase(Locale.ROOT).endsWith(".png")) {
            destino = new File(destino.getParentFile(), destino.getName() + ".png");
        }
        try {
            ImageIO.write(panel.renderizar(), "png", destino);
            JOptionPane.showMessageDialog(this, "Diagrama guardado en:\n" + destino.getAbsolutePath(),
                    "Exportar PNG", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException excepcion) {
            REGISTRO.log(Level.WARNING, "No se pudo exportar el diagrama", excepcion);
            JOptionPane.showMessageDialog(this, "No se pudo guardar la imagen:\n" + excepcion.getMessage(),
                    "Exportar PNG", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Acción que cierra la ventana con la tecla Escape.
     */
    private static final class AccionCerrar extends AbstractAction {

        private static final long serialVersionUID = 1L;

        private final transient DialogoDiagrama dialogo;

        AccionCerrar(DialogoDiagrama dialogo) {
            super("Cerrar diagrama");
            this.dialogo = dialogo;
        }

        @Override
        public void actionPerformed(ActionEvent evento) {
            dialogo.dispose();
        }
    }
}
