package ni.edu.uni.afd.modelo;

/**
 * Alfabeto Σ del autómata y utilidades de clasificación de caracteres.
 *
 * <p>Σ = { a, b, …, z } ∪ { 0, 1, …, 9 } ∪ { '@', '.' }, es decir 38 símbolos. Cada símbolo
 * concreto ocupa una columna fija de la matriz de transición (véase {@link Delta}):</p>
 *
 * <pre>
 *   'a'..'z' → 0..25
 *   '0'..'9' → 26..35
 *   '@'      → 36
 *   '.'      → 37
 * </pre>
 *
 * <p>Cualquier otro carácter (mayúsculas, espacios, acentos, '_', '-', saltos de línea, …) no
 * pertenece a Σ: {@link #indice(char)} devuelve {@code -1} y el autómata lo envía al estado
 * sumidero {@code q19}.</p>
 */
public final class Alfabeto {

    /** Cardinalidad de Σ: 26 letras + 10 dígitos + '@' + '.'. */
    public static final int TAMANO = 38;

    /** Longitud mínima de las iniciales. */
    public static final int MIN_LETRAS = 2;

    /** Longitud máxima de las iniciales. */
    public static final int MAX_LETRAS = 4;

    /** Cantidad mínima de dígitos del número. */
    public static final int MIN_DIGITOS = 2;

    /** Cantidad máxima de dígitos del número. */
    public static final int MAX_DIGITOS = 3;

    /** Símbolo arroba. */
    public static final char ARROBA = '@';

    /** Símbolo punto. */
    public static final char PUNTO = '.';

    private static final int PRIMERA_COLUMNA_DIGITO = 26;
    private static final int COLUMNA_ARROBA = 36;
    private static final int COLUMNA_PUNTO = 37;
    private static final char ULTIMA_LETRA_MINUSCULA = 'z';
    private static final char PRIMERA_LETRA_MINUSCULA = 'a';
    private static final char ULTIMO_DIGITO = '9';
    private static final char PRIMER_DIGITO = '0';

    private Alfabeto() {
        // Clase de utilidades: no se instancia.
    }

    /**
     * Devuelve la columna de Σ asociada a un carácter.
     *
     * @param simbolo carácter a clasificar
     * @return índice entre 0 y 37, o {@code -1} si el carácter no pertenece a Σ
     */
    public static int indice(char simbolo) {
        if (simbolo >= PRIMERA_LETRA_MINUSCULA && simbolo <= ULTIMA_LETRA_MINUSCULA) {
            return simbolo - PRIMERA_LETRA_MINUSCULA;
        }
        if (simbolo >= PRIMER_DIGITO && simbolo <= ULTIMO_DIGITO) {
            return PRIMERA_COLUMNA_DIGITO + (simbolo - PRIMER_DIGITO);
        }
        if (simbolo == ARROBA) {
            return COLUMNA_ARROBA;
        }
        if (simbolo == PUNTO) {
            return COLUMNA_PUNTO;
        }
        return -1;
    }

    /**
     * Indica si el carácter pertenece a Σ.
     *
     * @param simbolo carácter a comprobar
     * @return {@code true} si es una letra minúscula, un dígito, {@code '@'} o {@code '.'}
     */
    public static boolean pertenece(char simbolo) {
        return indice(simbolo) >= 0;
    }

    /**
     * Devuelve el símbolo de Σ que ocupa una columna de la matriz.
     *
     * @param columna índice entre 0 y 37
     * @return símbolo correspondiente
     * @throws IllegalArgumentException si el índice está fuera de rango
     */
    public static char simbolo(int columna) {
        if (columna < 0 || columna >= TAMANO) {
            throw new IllegalArgumentException("Columna fuera de Σ: " + columna);
        }
        if (columna < PRIMERA_COLUMNA_DIGITO) {
            return (char) (PRIMERA_LETRA_MINUSCULA + columna);
        }
        if (columna < COLUMNA_ARROBA) {
            return (char) (PRIMER_DIGITO + (columna - PRIMERA_COLUMNA_DIGITO));
        }
        return columna == COLUMNA_ARROBA ? ARROBA : PUNTO;
    }

    /** @param simbolo carácter a comprobar @return {@code true} si es una letra {@code a–z} */
    public static boolean esLetra(char simbolo) {
        return simbolo >= PRIMERA_LETRA_MINUSCULA && simbolo <= ULTIMA_LETRA_MINUSCULA;
    }

    /** @param simbolo carácter a comprobar @return {@code true} si es un dígito {@code 0–9} */
    public static boolean esDigito(char simbolo) {
        return simbolo >= PRIMER_DIGITO && simbolo <= ULTIMO_DIGITO;
    }

    /**
     * Describe un carácter para los mensajes de rechazo.
     *
     * @param simbolo carácter leído
     * @return descripción legible en español
     */
    public static String describir(char simbolo) {
        if (esLetra(simbolo)) {
            return "letra minúscula";
        }
        if (esDigito(simbolo)) {
            return "dígito";
        }
        if (simbolo >= 'A' && simbolo <= 'Z') {
            return "letra mayúscula, que no pertenece a Σ";
        }
        switch (simbolo) {
            case ' ':
                return "espacio en blanco, que no pertenece a Σ";
            case '\n':
                return "salto de línea, que no pertenece a Σ";
            case '\r':
                return "retorno de carro, que no pertenece a Σ";
            case '\t':
                return "tabulador, que no pertenece a Σ";
            case '-':
                return "guion, que no pertenece a Σ";
            case '_':
                return "guion bajo, que no pertenece a Σ";
            case '+':
                return "signo más, que no pertenece a Σ";
            default:
                return "carácter no perteneciente a Σ";
        }
    }
}
