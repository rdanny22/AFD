package ni.edu.uni.afd.modelo;

/**
 * Clasificación del resultado de una validación. Se usa en la tabla de traza y en los mensajes.
 *
 * <p>El tipo se deriva siempre del <em>estado de origen del fallo</em> (o del estado final
 * alcanzado), nunca de heurísticas sobre la cadena completa.</p>
 */
public enum TipoResultado {

    /** La cadena se consumió por completo y terminó en {@code q18}. */
    VALIDO("Válido"),

    /** Cadena nula o vacía: el autómata permanece en {@code q0}. */
    CADENA_VACIA("Cadena vacía"),

    /** Apareció un carácter que no pertenece a Σ. */
    SIMBOLO_INVALIDO("Símbolo no permitido"),

    /** Menos de 2 letras minúsculas antes del número. */
    INICIALES_INVALIDAS("Iniciales inválidas"),

    /** La parte numérica quedó incompleta, tiene dígitos de más o un símbolo no numérico. */
    DIGITOS_INVALIDOS("Dígitos inválidos"),

    /** El sufijo no coincide exactamente con {@code @uni.edu.ni}. */
    DOMINIO_INVALIDO("Dominio inválido"),

    /** Se alcanzó {@code q18} y aún quedaban símbolos de Σ por leer. */
    CARACTERES_EXTRA("Caracteres extra");

    private final String etiqueta;

    TipoResultado(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /**
     * Etiqueta legible que se muestra en la interfaz.
     *
     * @return texto en español
     */
    public String etiqueta() {
        return etiqueta;
    }

    /**
     * Clasifica un rechazo producido por una transición hacia {@code q19}.
     *
     * @param origen  estado en el que se encontraba el autómata
     * @param simbolo símbolo que provocó el rechazo
     * @return el tipo de resultado correspondiente
     */
    public static TipoResultado desdeFallo(Estado origen, char simbolo) {
        if (!Alfabeto.pertenece(simbolo)) {
            return SIMBOLO_INVALIDO;
        }
        if (origen.esFinal()) {
            return CARACTERES_EXTRA;
        }
        if (origen.ordinal() <= Estado.q4.ordinal()) {
            return INICIALES_INVALIDAS;
        }
        if (origen.ordinal() <= Estado.q7.ordinal()) {
            return DIGITOS_INVALIDOS;
        }
        return DOMINIO_INVALIDO;
    }

    /**
     * Clasifica un rechazo porque la cadena terminó en un estado que no es final.
     *
     * @param terminal estado alcanzado al consumir toda la cadena
     * @return el tipo de resultado correspondiente
     */
    public static TipoResultado porEstadoTerminal(Estado terminal) {
        if (terminal.esFinal()) {
            return VALIDO;
        }
        if (terminal.esSumidero()) {
            return DOMINIO_INVALIDO;
        }
        if (terminal == Estado.q0) {
            return CADENA_VACIA;
        }
        if (terminal == Estado.q1) {
            return INICIALES_INVALIDAS;
        }
        if (terminal.ordinal() <= Estado.q7.ordinal()) {
            return DIGITOS_INVALIDOS;
        }
        return DOMINIO_INVALIDO;
    }
}
