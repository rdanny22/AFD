package ni.edu.uni.afd.vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import ni.edu.uni.afd.modelo.ResultadoValidacion;

/**
 * Panel inferior con la tabla de traza de ejecución del AFD y sus acciones.
 */
public final class PanelTraza extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int ANCHO_NUMERO = 44;
    private static final int ANCHO_CADENA = 160;
    private static final int ANCHO_TIPO = 150;
    private static final int ANCHO_SECUENCIA = 470;
    private static final int ANCHO_RESULTADO = 90;
    private static final int ANCHO_ESPERADO = 72;

    private final TrazaTableModel modelo = new TrazaTableModel();
    private final JTable tabla = new TablaConTooltips(modelo);
    private final JButton botonPruebas = new JButton("Ejecutar pruebas");
    private final JButton botonLimpiar = new JButton("Limpiar");

    /**
     * Construye el panel de traza.
     */
    public PanelTraza() {
        super(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(4, 18, 12, 18));
        add(cabecera(), BorderLayout.NORTH);
        add(construirTabla(), BorderLayout.CENTER);
    }

    private JPanel cabecera() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel titulo = new JLabel("Traza de ejecución del AFD");
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));

        botonPruebas.setToolTipText("Ejecuta todos los casos de casos-prueba.csv");
        botonLimpiar.setToolTipText("Limpia la tabla, el campo y el veredicto");

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.add(botonPruebas);
        acciones.add(botonLimpiar);

        panel.add(titulo, BorderLayout.WEST);
        panel.add(acciones, BorderLayout.EAST);
        return panel;
    }

    private JScrollPane construirTabla() {
        tabla.setRowHeight(22);
        tabla.setAutoCreateRowSorter(true);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setFillsViewportHeight(true);
        tabla.setShowGrid(true);
        tabla.getTableHeader().setReorderingAllowed(false);

        TableColumnModel columnas = tabla.getColumnModel();
        ajustar(columnas.getColumn(TrazaTableModel.COLUMNA_NUMERO), ANCHO_NUMERO, 38);
        ajustar(columnas.getColumn(TrazaTableModel.COLUMNA_CADENA), ANCHO_CADENA, 90);
        ajustar(columnas.getColumn(TrazaTableModel.COLUMNA_TIPO), ANCHO_TIPO, 110);
        ajustar(columnas.getColumn(TrazaTableModel.COLUMNA_SECUENCIA), ANCHO_SECUENCIA, 200);
        ajustar(columnas.getColumn(TrazaTableModel.COLUMNA_RESULTADO), ANCHO_RESULTADO, 90);
        ajustar(columnas.getColumn(TrazaTableModel.COLUMNA_ESPERADO), ANCHO_ESPERADO, 80);

        columnas.getColumn(TrazaTableModel.COLUMNA_NUMERO).setCellRenderer(new RendererCentrado());
        columnas.getColumn(TrazaTableModel.COLUMNA_SECUENCIA).setCellRenderer(new RendererMonoespaciado());
        columnas.getColumn(TrazaTableModel.COLUMNA_RESULTADO).setCellRenderer(new RendererResultado());
        columnas.getColumn(TrazaTableModel.COLUMNA_ESPERADO).setCellRenderer(new RendererCentrado());

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setPreferredSize(new Dimension(920, 250));
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xC8, 0xC8, 0xC8)));
        return scroll;
    }

    private static void ajustar(TableColumn columna, int ancho, int minimo) {
        columna.setPreferredWidth(ancho);
        columna.setMinWidth(minimo);
    }

    /**
     * Añade una fila con el resultado de una validación y se desplaza hasta ella.
     *
     * @param resultado resultado del autómata
     * @param esperado  veredicto esperado si la cadena pertenece al banco de casos, o {@code null}
     */
    public void agregar(ResultadoValidacion resultado, Boolean esperado) {
        modelo.agregar(new TrazaTableModel.Fila(
                modelo.getRowCount() + 1,
                resultado.cadenaParaMostrar(),
                resultado.tipo().etiqueta(),
                secuencia(resultado),
                resultado.aceptado(),
                esperado,
                resultado.motivo()));
        desplazarAlFinal();
    }

    private static String secuencia(ResultadoValidacion resultado) {
        return resultado.consumioTodo()
                ? resultado.caminoCompacto()
                : resultado.caminoCompacto() + " ⛔";
    }

    private void desplazarAlFinal() {
        int ultima = tabla.getRowCount() - 1;
        if (ultima >= 0) {
            tabla.scrollRectToVisible(tabla.getCellRect(ultima, 0, true));
        }
    }

    /**
     * Vacía la tabla de traza.
     */
    public void limpiar() {
        modelo.limpiar();
    }

    /** @return cantidad de filas registradas */
    public int totalFilas() {
        return modelo.getRowCount();
    }

    /** @return el botón que ejecuta la batería de casos */
    public JButton getBotonPruebas() {
        return botonPruebas;
    }

    /** @return el botón que limpia la tabla */
    public JButton getBotonLimpiar() {
        return botonLimpiar;
    }

    /**
     * Tabla que muestra el motivo del resultado como ayuda emergente de cada fila.
     */
    private static final class TablaConTooltips extends JTable {

        private static final long serialVersionUID = 1L;

        TablaConTooltips(TrazaTableModel modelo) {
            super(modelo);
        }

        @Override
        public String getToolTipText(MouseEvent evento) {
            int filaVista = rowAtPoint(evento.getPoint());
            if (filaVista < 0) {
                return null;
            }
            int filaModelo = convertRowIndexToModel(filaVista);
            TrazaTableModel.Fila fila = ((TrazaTableModel) getModel()).fila(filaModelo);
            return "<html>" + fila.motivo()
                    + "<br><br><b>Secuencia:</b> " + fila.secuencia() + "</html>";
        }
    }

    /**
     * Renderer monoespaciado para la secuencia de estados.
     */
    private static final class RendererMonoespaciado extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 1L;
        private static final Font FUENTE = new Font(Font.MONOSPACED, Font.PLAIN, 11);

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionado,
                boolean conFoco, int fila, int columna) {
            Component componente = super.getTableCellRendererComponent(
                    tabla, valor, seleccionado, conFoco, fila, columna);
            componente.setFont(FUENTE);
            return componente;
        }
    }

    /**
     * Renderer que pinta el veredicto en verde o rojo.
     */
    private static final class RendererResultado extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionado,
                boolean conFoco, int fila, int columna) {
            Component componente = super.getTableCellRendererComponent(
                    tabla, valor, seleccionado, conFoco, fila, columna);
            if (!seleccionado) {
                componente.setForeground("Aceptado".equals(valor) ? PanelValidacion.VERDE : PanelValidacion.ROJO);
            }
            return componente;
        }
    }

    /**
     * Renderer centrado para las columnas numéricas y de veredicto esperado.
     */
    private static final class RendererCentrado extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 1L;

        RendererCentrado() {
            setHorizontalAlignment(SwingConstants.CENTER);
        }
    }
}
