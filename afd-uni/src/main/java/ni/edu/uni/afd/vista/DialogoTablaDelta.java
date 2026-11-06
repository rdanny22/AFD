package ni.edu.uni.afd.vista;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import ni.edu.uni.afd.modelo.Delta;

/**
 * Diálogo con la tabla de transiciones δ en su forma agrupada por clases de símbolos.
 */
public final class DialogoTablaDelta {

    private static final String[] COLUMNAS = {"Estado", "a–z (26)", "0–9 (10)", "@", ".", "otro"};

    private DialogoTablaDelta() {
        // Clase de utilidades: no se instancia.
    }

    /**
     * Abre el diálogo con la matriz de transición agrupada.
     *
     * @param padre ventana propietaria
     */
    public static void mostrar(Window padre) {
        JDialog dialogo = new JDialog(padre, "Tabla de transiciones δ", Dialog.ModalityType.APPLICATION_MODAL);

        ModeloSoloLectura modelo = new ModeloSoloLectura(Delta.tablaAgrupada(), COLUMNAS);
        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(22);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setFillsViewportHeight(true);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JLabel nota = new JLabel("<html><b>Nota:</b> la tabla agrupa los 38 símbolos de Σ en cinco columnas."
                + " Las celdas con paréntesis indican destinos particulares dentro de la clase"
                + " (en q8…q17 la transición depende del carácter exacto)."
                + " Los símbolos fuera de Σ también conducen a q19, pero no forman parte de la matriz.</html>");
        nota.setBorder(new EmptyBorder(8, 10, 10, 10));

        JPanel contenido = new JPanel(new BorderLayout());
        contenido.add(new JScrollPane(tabla), BorderLayout.CENTER);
        contenido.add(nota, BorderLayout.SOUTH);

        dialogo.setContentPane(contenido);
        dialogo.setPreferredSize(new Dimension(840, 620));
        dialogo.pack();
        dialogo.setLocationRelativeTo(padre);
        dialogo.setVisible(true);
    }

    /**
     * Modelo de solo lectura para la matriz.
     */
    private static final class ModeloSoloLectura extends DefaultTableModel {

        private static final long serialVersionUID = 1L;

        ModeloSoloLectura(Object[][] filas, Object[] columnas) {
            super(filas, columnas);
        }

        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    }
}
