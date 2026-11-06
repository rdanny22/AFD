package ni.edu.uni.afd.modelo;

/**
 * Conjunto de estados Q del autómata: {@code Q = { q0, q1, …, q19 }} (20 estados).
 *
 * <table border="1">
 *   <caption>Significado de cada estado</caption>
 *   <tr><th>Estado</th><th>Significado</th></tr>
 *   <tr><td>q0</td><td>Sin leer caracteres (estado inicial)</td></tr>
 *   <tr><td>q1</td><td>1 letra leída</td></tr>
 *   <tr><td>q2</td><td>2 letras leídas (iniciales ya válidas)</td></tr>
 *   <tr><td>q3</td><td>3 letras leídas</td></tr>
 *   <tr><td>q4</td><td>4 letras leídas (límite máximo)</td></tr>
 *   <tr><td>q5</td><td>1 dígito leído</td></tr>
 *   <tr><td>q6</td><td>2 dígitos leídos (número ya válido)</td></tr>
 *   <tr><td>q7</td><td>3 dígitos leídos (límite máximo)</td></tr>
 *   <tr><td>q8..q17</td><td>Consumo parcial del dominio {@code @uni.edu.ni}</td></tr>
 *   <tr><td>q18</td><td>Dominio completo: único estado final</td></tr>
 *   <tr><td>q19</td><td>Estado trampa / sumidero de rechazo</td></tr>
 * </table>
 */
public enum Estado {

    /** Estado inicial: aún no se ha leído ningún carácter. */
    q0,
    /** Una letra leída. */
    q1,
    /** Dos letras leídas. */
    q2,
    /** Tres letras leídas. */
    q3,
    /** Cuatro letras leídas. */
    q4,
    /** Un dígito leído. */
    q5,
    /** Dos dígitos leídos. */
    q6,
    /** Tres dígitos leídos. */
    q7,
    /** Se leyó '@'. */
    q8,
    /** Se leyó '@u'. */
    q9,
    /** Se leyó '@un'. */
    q10,
    /** Se leyó '@uni'. */
    q11,
    /** Se leyó '@uni.'. */
    q12,
    /** Se leyó '@uni.e'. */
    q13,
    /** Se leyó '@uni.ed'. */
    q14,
    /** Se leyó '@uni.edu'. */
    q15,
    /** Se leyó '@uni.edu.'. */
    q16,
    /** Se leyó '@uni.edu.n'. */
    q17,
    /** Se leyó '@uni.edu.ni' completo: estado de aceptación. */
    q18,
    /** Estado trampa: una vez alcanzado, ningún símbolo permite salir. */
    q19;

    /**
     * Indica si el estado pertenece al conjunto de estados finales {@code F = {q18}}.
     *
     * @return {@code true} solo para {@code q18}
     */
    public boolean esFinal() {
        return this == q18;
    }

    /**
     * Indica si el estado es la trampa (sumidero) del autómata.
     *
     * @return {@code true} solo para {@code q19}
     */
    public boolean esSumidero() {
        return this == q19;
    }

    /**
     * Etiqueta usada en la interfaz y en la traza.
     *
     * @return el nombre del estado, por ejemplo {@code "q7"}
     */
    public String etiqueta() {
        return name();
    }
}
