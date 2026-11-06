package ni.edu.uni.afd.vista;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * Modelo de la tabla de traza del AFD.
 *
 * <p>Columnas, en este orden: {@code #}, {@code Cadena evaluada}, {@code Tipo},
 * {@code Secuencia de estados}, {@code Resultado} y {@code ¿Esperado?}. Las filas no son editables y
 * conservan el motivo del rechazo para mostrarlo como ayuda emergente.</p>
 */
public final class TrazaTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    /** Índice de la columna {@code #}. */
    public static final int COLUMNA_NUMERO = 0;

    /** Índice de la columna {@code Cadena evaluada}. */
    public static final int COLUMNA_CADENA = 1;

    /** Índice de la columna {@code Tipo}. */
    public static final int COLUMNA_TIPO = 2;

    /** Índice de la columna {@code Secuencia de estados}. */
    public static final int COLUMNA_SECUENCIA = 3;

    /** Índice de la columna {@code Resultado}. */
    public static final int COLUMNA_RESULTADO = 4;

    /** Índice de la columna {@code ¿Esperado?}. */
    public static final int COLUMNA_ESPERADO = 5;

    private static final String[] COLUMNAS = {
        "#", "Cadena evaluada", "Tipo", "Secuencia de estados", "Resultado", "¿Esperado?"
    };

    private static final String SIN_EXPECTATIVA = "—";

    private final transient List<Fila> filas = new ArrayList<>();

    /**
     * Una fila de la tabla de traza.
     *
     * @param numero    consecutivo de la evaluación
     * @param cadena    cadena evaluada ya formateada para mostrar
     * @param tipo      etiqueta del tipo de resultado
     * @param secuencia secuencia de estados con flechas
     * @param aceptado  veredicto del autómata
     * @param esperado  veredicto esperado si la cadena está en el banco, o {@code null}
     * @param motivo    explicación del resultado, usada como ayuda emergente
     */
    public record Fila(
            int numero,
            String cadena,
            String tipo,
            String secuencia,
            boolean aceptado,
            Boolean esperado,
            String motivo) {
    }

    @Override
    public int getRowCount() {
        return filas.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNAS.length;
    }

    @Override
    public String getColumnName(int columna) {
        return COLUMNAS[columna];
    }

    @Override
    public Class<?> getColumnClass(int columna) {
        return columna == COLUMNA_NUMERO ? Integer.class : String.class;
    }

    @Override
    public boolean isCellEditable(int fila, int columna) {
        return false;
    }

    @Override
    public Object getValueAt(int fila, int columna) {
        return valor(filas.get(fila), columna);
    }

    private static Object valor(Fila datos, int columna) {
        switch (columna) {
            case COLUMNA_NUMERO:
                return Integer.valueOf(datos.numero());
            case COLUMNA_CADENA:
                return datos.cadena();
            case COLUMNA_TIPO:
                return datos.tipo();
            case COLUMNA_SECUENCIA:
                return datos.secuencia();
            case COLUMNA_RESULTADO:
                return datos.aceptado() ? "Aceptado" : "Rechazado";
            case COLUMNA_ESPERADO:
                return textoEsperado(datos.esperado());
            default:
                return "";
        }
    }

    private static String textoEsperado(Boolean esperado) {
        if (esperado == null) {
            return SIN_EXPECTATIVA;
        }
        return esperado.booleanValue() ? "Sí" : "No";
    }

    /**
     * Añade una fila al final del modelo.
     *
     * @param fila datos de la evaluación
     */
    public void agregar(Fila fila) {
        filas.add(fila);
        int ultima = filas.size() - 1;
        fireTableRowsInserted(ultima, ultima);
    }

    /**
     * Elimina todas las filas.
     */
    public void limpiar() {
        int total = filas.size();
        if (total == 0) {
            return;
        }
        filas.clear();
        fireTableRowsDeleted(0, total - 1);
    }

    /**
     * Devuelve la fila indicada.
     *
     * @param fila índice en el modelo
     * @return los datos de la fila
     */
    public Fila fila(int fila) {
        return filas.get(fila);
    }
}
