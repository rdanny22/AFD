package ni.edu.uni.afd.vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Locale;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import ni.edu.uni.afd.modelo.ResultadoValidacion;

/**
 * Panel superior: encabezado, campo de entrada, botón de validación y veredicto.
 *
 * <p>No contiene lógica de validación: solo muestra lo que le indica el controlador.</p>
 */
public final class PanelValidacion extends JPanel {

    private static final long serialVersionUID = 1L;

    /** Color del veredicto de aceptación. */
    public static final Color VERDE = new Color(0x1B, 0x8A, 0x3A);

    /** Color del veredicto de rechazo. */
    public static final Color ROJO = new Color(0xC6, 0x28, 0x28);

    /** Color del estado neutral. */
    public static final Color GRIS = new Color(0x60, 0x60, 0x60);

    /** Texto mostrado antes de la primera validación. */
    public static final String TEXTO_NEUTRAL = "Esperando validación…";

    private static final int LIMITE_CARACTERES = 64;

    private final JTextField campo = new JTextField(28);
    private final JButton botonValidar = new JButton("VALIDAR");
    private final JLabel veredicto = new JLabel(TEXTO_NEUTRAL, SwingConstants.CENTER);
    private final JLabel detalle = new JLabel("Estado: q0", SwingConstants.CENTER);

    /**
     * Construye el panel con todos sus componentes.
     */
    public PanelValidacion() {
        super(new BorderLayout(0, 10));
        setBorder(new EmptyBorder(14, 18, 10, 18));
        configurarVeredicto();
        add(encabezado(), BorderLayout.NORTH);
        add(cuerpo(), BorderLayout.CENTER);
    }

    private void configurarVeredicto() {
        veredicto.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        veredicto.setForeground(GRIS);
        veredicto.setBorder(new CompoundBorder(
                new LineBorder(new Color(0xD0, 0xD0, 0xD0)),
                new EmptyBorder(8, 26, 8, 26)));

        detalle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        detalle.setForeground(GRIS);
        detalle.setBorder(new EmptyBorder(4, 0, 8, 0));
    }

    private JPanel encabezado() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("UNIVERSIDAD NACIONAL DE INGENIERÍA");
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitulo = new JLabel(
                "Validación de correos institucionales mediante Autómata Finito Determinista");
        subtitulo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        subtitulo.setForeground(GRIS);
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(titulo);
        panel.add(Box.createVerticalStrut(2));
        panel.add(subtitulo);
        return panel;
    }

    private JPanel cuerpo() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.add(entrada(), BorderLayout.NORTH);
        panel.add(centrado(veredicto), BorderLayout.CENTER);
        panel.add(centrado(detalle), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel entrada() {
        JLabel etiqueta = new JLabel("Correo institucional:");
        etiqueta.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));

        campo.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        campo.setToolTipText("Ejemplo: js25@uni.edu.ni");
        campo.setPreferredSize(new Dimension(420, 32));
        if (campo.getDocument() instanceof AbstractDocument documento) {
            documento.setDocumentFilter(new LimiteCaracteres(LIMITE_CARACTERES));
        }
        campo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent evento) {
                campo.selectAll();
            }
        });

        botonValidar.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        botonValidar.setMnemonic('V');
        botonValidar.setToolTipText("Valida la cadena con el AFD (Alt+V o Enter)");

        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        fila.add(etiqueta);
        fila.add(campo);
        fila.add(botonValidar);

        JLabel formato = new JLabel(
                "Formato: <iniciales 2–4 minúsculas><2–3 dígitos>@uni.edu.ni");
        formato.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        formato.setForeground(GRIS);
        formato.setBorder(new EmptyBorder(0, 12, 0, 0));

        JPanel columna = new JPanel(new BorderLayout());
        columna.add(fila, BorderLayout.NORTH);
        columna.add(formato, BorderLayout.CENTER);

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.add(columna, BorderLayout.WEST);
        return contenedor;
    }

    private static JPanel centrado(JLabel etiqueta) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(etiqueta);
        return panel;
    }

    /** @return el campo de texto donde se ingresa el correo */
    public JTextField getCampo() {
        return campo;
    }

    /** @return el botón que ejecuta la validación */
    public JButton getBotonValidar() {
        return botonValidar;
    }

    /** @return el texto ingresado, sin recortar ni normalizar */
    public String getTextoIngresado() {
        return campo.getText();
    }

    /**
     * Muestra el veredicto y el detalle del resultado.
     *
     * @param resultado resultado devuelto por el autómata
     */
    public void mostrarResultado(ResultadoValidacion resultado) {
        if (resultado.aceptado()) {
            veredicto.setText("ACEPTADO");
            veredicto.setForeground(VERDE);
            detalle.setText("Estado final: q18 · cadena consumida en "
                    + resultado.totalPasos() + " transiciones");
        } else {
            veredicto.setText("RECHAZADO");
            veredicto.setForeground(ROJO);
            detalle.setText(textoDetalle(resultado));
        }
    }

    private static String textoDetalle(ResultadoValidacion resultado) {
        String tipo = resultado.tipo().etiqueta().toLowerCase(Locale.ROOT);
        if (!resultado.consumioTodo() && resultado.caracterFallo() != null) {
            return "Fallo en la posición " + resultado.indiceFallo()
                    + " ('" + resultado.caracterFallo() + "') — " + tipo
                    + " · estado " + resultado.estadoFinal().etiqueta();
        }
        return "Estado final: " + resultado.estadoFinal().etiqueta()
                + " — " + tipo + " · la cadena se consumió sin alcanzar q18";
    }

    /**
     * Sustituye el detalle por un texto informativo (por ejemplo, el resumen de la batería).
     *
     * @param texto texto a mostrar
     */
    public void mostrarDetalle(String texto) {
        detalle.setText(texto);
    }

    /**
     * Deja el panel en su estado inicial.
     */
    public void reiniciar() {
        campo.setText("");
        campo.requestFocusInWindow();
        veredicto.setText(TEXTO_NEUTRAL);
        veredicto.setForeground(GRIS);
        detalle.setText("Estado: q0");
    }

    /**
     * Filtro que limita la longitud del campo sin alterar el contenido.
     */
    private static final class LimiteCaracteres extends DocumentFilter {

        private final int maximo;

        LimiteCaracteres(int maximo) {
            this.maximo = maximo;
        }

        @Override
        public void insertString(FilterBypass fb, int desplazamiento, String texto, AttributeSet atributos)
                throws BadLocationException {
            if (texto != null && fb.getDocument().getLength() + texto.length() <= maximo) {
                super.insertString(fb, desplazamiento, texto, atributos);
            }
        }

        @Override
        public void replace(FilterBypass fb, int desplazamiento, int longitud, String texto, AttributeSet atributos)
                throws BadLocationException {
            int nuevos = texto == null ? 0 : texto.length();
            if (fb.getDocument().getLength() - longitud + nuevos <= maximo) {
                super.replace(fb, desplazamiento, longitud, texto, atributos);
            }
        }
    }
}
