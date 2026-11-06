package ni.edu.uni.afd.vista;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import ni.edu.uni.afd.controlador.ControladorValidador;
import ni.edu.uni.afd.modelo.Alfabeto;
import ni.edu.uni.afd.modelo.Automata;
import ni.edu.uni.afd.modelo.Estado;

/**
 * Ventana principal de la aplicación: panel de validación arriba, traza del AFD abajo y pie con
 * acciones secundarias y barra de estado.
 */
public final class VentanaPrincipal extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final String TITULO = "Validador de Correo Institucional UNI — AFD";
    private static final int ANCHO_MINIMO = 980;
    private static final int ALTO_MINIMO = 700;
    private static final int ANCHO_INICIAL = 1060;
    private static final int ALTO_INICIAL = 720;
    private static final int DIVISOR_INICIAL = 300;

    private final PanelValidacion panelValidacion = new PanelValidacion();
    private final PanelTraza panelTraza = new PanelTraza();
    private final JButton botonDiagrama = new JButton("Ver diagrama del AFD");
    private final JButton botonTablaDelta = new JButton("Tabla de transiciones δ");
    private final JLabel barraEstado = new JLabel(textoEstado(), SwingConstants.RIGHT);
    private final ControladorValidador controlador;

    /**
     * Construye la ventana, conecta el controlador y deja la interfaz lista para mostrarse.
     */
    public VentanaPrincipal() {
        super(TITULO);
        this.controlador = new ControladorValidador(this);
        configurarVentana();
        controlador.conectar();
    }

    private void configurarVentana() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(ANCHO_MINIMO, ALTO_MINIMO));
        setSize(ANCHO_INICIAL, ALTO_INICIAL);

        JSplitPane division = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelValidacion, panelTraza);
        division.setResizeWeight(0.4);
        division.setDividerLocation(DIVISOR_INICIAL);
        division.setBorder(null);
        division.setContinuousLayout(true);

        JPanel contenido = new JPanel(new BorderLayout());
        contenido.add(division, BorderLayout.CENTER);
        contenido.add(pie(), BorderLayout.SOUTH);
        setContentPane(contenido);
        setLocationRelativeTo(null);
    }

    private JPanel pie() {
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        botonDiagrama.setToolTipText("Muestra docs/diagrama-afd.png desde los recursos");
        botonTablaDelta.setToolTipText("Muestra la matriz δ agrupada por clases de símbolos");
        acciones.add(botonDiagrama);
        acciones.add(botonTablaDelta);

        barraEstado.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        barraEstado.setForeground(PanelValidacion.GRIS);
        barraEstado.setBorder(new EmptyBorder(0, 0, 0, 18));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(acciones, BorderLayout.WEST);
        panel.add(barraEstado, BorderLayout.EAST);
        return panel;
    }

    private static String textoEstado() {
        return "Estado: " + Automata.ESTADO_INICIAL.etiqueta()
                + " · |Σ| = " + Alfabeto.TAMANO
                + " · Estados: " + Estado.values().length
                + " · Finales: " + Automata.ESTADOS_FINALES.size();
    }

    /** @return el panel de entrada y veredicto */
    public PanelValidacion getPanelValidacion() {
        return panelValidacion;
    }

    /** @return el panel de traza */
    public PanelTraza getPanelTraza() {
        return panelTraza;
    }

    /** @return el botón que abre el diagrama del autómata */
    public JButton getBotonDiagrama() {
        return botonDiagrama;
    }

    /** @return el botón que abre la tabla de transiciones δ */
    public JButton getBotonTablaDelta() {
        return botonTablaDelta;
    }

    /** @return la etiqueta de la barra de estado */
    public JLabel getBarraEstado() {
        return barraEstado;
    }

    /** @return el controlador asociado a esta ventana */
    public ControladorValidador getControlador() {
        return controlador;
    }
}
