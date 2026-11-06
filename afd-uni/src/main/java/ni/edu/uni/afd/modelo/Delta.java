package ni.edu.uni.afd.modelo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Función de transición δ : Q × Σ → Q implementada como una matriz explícita de
 * {@code 20 × 38 = 760} transiciones.
 *
 * <p>La matriz se construye una sola vez, de forma explícita: todas las celdas se inicializan en
 * el estado sumidero {@code q19} y después se sobrescriben únicamente las transiciones definidas.
 * Al ser una función total, nunca hay celdas nulas ni transiciones indefinidas.</p>
 *
 * <p><strong>Advertencia de implementación:</strong> las transiciones de la parte local
 * ({@code q0}–{@code q7}) dependen de la <em>clase</em> del símbolo (letra o dígito), mientras que
 * las del dominio ({@code q8}–{@code q17}) dependen del <em>carácter exacto</em>. Como la matriz
 * tiene una columna por símbolo concreto, esa distinción queda resuelta en la propia matriz.</p>
 */
public final class Delta {

    /** Número de columnas de la tabla agrupada que muestra la interfaz (Estado + 5 clases). */
    public static final int COLUMNAS_AGRUPADAS = 6;

    private static final int INICIO_LETRAS = 0;
    private static final int FIN_LETRAS = 26;
    private static final int INICIO_DIGITOS = 26;
    private static final int FIN_DIGITOS = 36;
    private static final int COLUMNA_ARROBA = 36;
    private static final int COLUMNA_PUNTO = 37;

    private static final Estado[][] MATRIZ = new Estado[Estado.values().length][Alfabeto.TAMANO];

    static {
        for (Estado[] fila : MATRIZ) {
            Arrays.fill(fila, Estado.q19);
        }

        // Parte A: iniciales (2 a 4 letras minúsculas). Una quinta letra cae en q19 por defecto.
        for (int columna = INICIO_LETRAS; columna < FIN_LETRAS; columna++) {
            MATRIZ[Estado.q0.ordinal()][columna] = Estado.q1;
            MATRIZ[Estado.q1.ordinal()][columna] = Estado.q2;
            MATRIZ[Estado.q2.ordinal()][columna] = Estado.q3;
            MATRIZ[Estado.q3.ordinal()][columna] = Estado.q4;
        }

        // Parte A: número (2 o 3 dígitos). Un cuarto dígito cae en q19 por defecto.
        for (int columna = INICIO_DIGITOS; columna < FIN_DIGITOS; columna++) {
            MATRIZ[Estado.q2.ordinal()][columna] = Estado.q5;
            MATRIZ[Estado.q3.ordinal()][columna] = Estado.q5;
            MATRIZ[Estado.q4.ordinal()][columna] = Estado.q5;
            MATRIZ[Estado.q5.ordinal()][columna] = Estado.q6;
            MATRIZ[Estado.q6.ordinal()][columna] = Estado.q7;
        }

        // Cierre de la parte local: solo '@' con 2 o 3 dígitos ya leídos.
        conectar(Estado.q6, Alfabeto.ARROBA, Estado.q8);
        conectar(Estado.q7, Alfabeto.ARROBA, Estado.q8);

        // Parte B: dominio @uni.edu.ni, transición por carácter exacto.
        conectar(Estado.q8, 'u', Estado.q9);
        conectar(Estado.q9, 'n', Estado.q10);
        conectar(Estado.q10, 'i', Estado.q11);
        conectar(Estado.q11, Alfabeto.PUNTO, Estado.q12);
        conectar(Estado.q12, 'e', Estado.q13);
        conectar(Estado.q13, 'd', Estado.q14);
        conectar(Estado.q14, 'u', Estado.q15);
        conectar(Estado.q15, Alfabeto.PUNTO, Estado.q16);
        conectar(Estado.q16, 'n', Estado.q17);
        conectar(Estado.q17, 'i', Estado.q18);

        // Parte C: q18 (no se admiten símbolos extra) y q19 (sumidero) permanecen en q19.
    }

    private Delta() {
        // Clase de utilidades: no se instancia.
    }

    private static void conectar(Estado desde, char simbolo, Estado hasta) {
        int columna = Alfabeto.indice(simbolo);
        if (columna < 0) {
            throw new IllegalStateException("Transición fuera de Σ: '" + simbolo + "'");
        }
        MATRIZ[desde.ordinal()][columna] = hasta;
    }

    /**
     * Aplica δ a un estado y un símbolo.
     *
     * @param actual  estado de origen
     * @param simbolo símbolo leído; si no pertenece a Σ el resultado es {@code q19}
     * @return el estado de destino
     */
    public static Estado transicion(Estado actual, char simbolo) {
        int columna = Alfabeto.indice(simbolo);
        if (columna < 0) {
            return Estado.q19;
        }
        return MATRIZ[actual.ordinal()][columna];
    }

    /**
     * Devuelve el carácter que el autómata espera leer en un estado del dominio.
     *
     * @param estado estado de origen
     * @return el carácter esperado, o {@code null} si el estado no forma parte del dominio
     */
    public static Character caracterEsperado(Estado estado) {
        switch (estado) {
            case q8:
                return Character.valueOf('u');
            case q9:
                return Character.valueOf('n');
            case q10:
                return Character.valueOf('i');
            case q11:
                return Character.valueOf(Alfabeto.PUNTO);
            case q12:
                return Character.valueOf('e');
            case q13:
                return Character.valueOf('d');
            case q14:
                return Character.valueOf('u');
            case q15:
                return Character.valueOf(Alfabeto.PUNTO);
            case q16:
                return Character.valueOf('n');
            case q17:
                return Character.valueOf('i');
            default:
                return null;
        }
    }

    /**
     * Copia defensiva de la matriz completa, útil para inspección y pruebas.
     *
     * @return matriz de {@code [estado][símbolo]}
     */
    public static Estado[][] copia() {
        Estado[][] copia = new Estado[MATRIZ.length][];
        for (int i = 0; i < MATRIZ.length; i++) {
            copia[i] = Arrays.copyOf(MATRIZ[i], MATRIZ[i].length);
        }
        return copia;
    }

    /**
     * Construye la tabla agrupada de δ que muestra la interfaz: una fila por estado y las columnas
     * {@code Estado | a–z | 0–9 | @ | . | otro}.
     *
     * <p>Cuando una clase de símbolos tiene varios destinos (por ejemplo en {@code q8}, donde solo
     * {@code 'u'} avanza), la celda indica el destino mayoritario y los destinos particulares entre
     * paréntesis, de modo que la tabla nunca oculta información.</p>
     *
     * @return matriz de textos de {@code 20 × 6}
     */
    public static String[][] tablaAgrupada() {
        Estado[] estados = Estado.values();
        String[][] filas = new String[estados.length][COLUMNAS_AGRUPADAS];
        for (Estado estado : estados) {
            int fila = estado.ordinal();
            filas[fila][0] = estado.etiqueta();
            filas[fila][1] = resumen(estado, INICIO_LETRAS, FIN_LETRAS);
            filas[fila][2] = resumen(estado, INICIO_DIGITOS, FIN_DIGITOS);
            filas[fila][3] = resumen(estado, COLUMNA_ARROBA, COLUMNA_ARROBA + 1);
            filas[fila][4] = resumen(estado, COLUMNA_PUNTO, COLUMNA_PUNTO + 1);
            filas[fila][5] = "—";
        }
        return filas;
    }

    private static String resumen(Estado estado, int desde, int hasta) {
        Map<Estado, List<Character>> grupos = new LinkedHashMap<>();
        for (int columna = desde; columna < hasta; columna++) {
            Estado destino = MATRIZ[estado.ordinal()][columna];
            grupos.computeIfAbsent(destino, clave -> new ArrayList<>()).add(Character.valueOf(Alfabeto.simbolo(columna)));
        }
        List<String> partes = new ArrayList<>();
        for (Map.Entry<Estado, List<Character>> grupo : grupos.entrySet()) {
            if (grupos.size() == 1) {
                partes.add(grupo.getKey().etiqueta());
            } else {
                partes.add(grupo.getKey().etiqueta() + " (" + rango(grupo.getValue()) + ")");
            }
        }
        return String.join(" / ", partes);
    }

    private static String rango(List<Character> simbolos) {
        if (simbolos.size() == 1) {
            return String.valueOf(simbolos.get(0));
        }
        boolean consecutivos = true;
        for (int i = 1; i < simbolos.size(); i++) {
            if (simbolos.get(i).charValue() != simbolos.get(i - 1).charValue() + 1) {
                consecutivos = false;
                break;
            }
        }
        if (consecutivos) {
            return simbolos.get(0) + "–" + simbolos.get(simbolos.size() - 1);
        }
        if (simbolos.size() > 6) {
            return "… " + simbolos.size() + " símbolos";
        }
        StringBuilder texto = new StringBuilder(simbolos.size());
        for (Character simbolo : simbolos) {
            texto.append(simbolo.charValue());
        }
        return texto.toString();
    }
}
