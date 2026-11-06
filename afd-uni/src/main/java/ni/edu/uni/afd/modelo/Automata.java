package ni.edu.uni.afd.modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Autómata Finito Determinista que reconoce correos institucionales de la UNI.
 *
 * <pre>
 *   M = (Q, Σ, δ, q0, F)
 *   Q  = { q0, q1, …, q19 }                       (20 estados, véase {@link Estado})
 *   Σ  = { a–z, 0–9, '@', '.' }                   (38 símbolos, véase {@link Alfabeto})
 *   q0 = q0                                       (estado inicial)
 *   F  = { q18 }                                  (único estado de aceptación)
 *   δ  : Q × Σ → Q                                (función total, véase {@link Delta})
 * </pre>
 *
 * <p>El lenguaje aceptado es
 * {@code L(M) = { w ∈ Σ* | w ∈ [a-z]{2,4} · [0-9]{2,3} · "@uni.edu.ni" }}.</p>
 *
 * <p>La validación se realiza símbolo por símbolo consultando la matriz δ: no se usan expresiones
 * regulares, comparaciones de subcadenas ni librerías externas. La clase no lanza excepciones para
 * entradas nulas, vacías o con símbolos ajenos a Σ, y no depende de Swing, por lo que puede probarse
 * en modo <em>headless</em>.</p>
 */
public final class Automata {

    /** Sufijo obligatorio del correo institucional (solo para mensajes y documentación). */
    public static final String DOMINIO = "@uni.edu.ni";

    /** Estado inicial del autómata. */
    public static final Estado ESTADO_INICIAL = Estado.q0;

    /** Conjunto de estados finales {@code F}. */
    public static final Set<Estado> ESTADOS_FINALES = Set.of(Estado.q18);

    /**
     * Ejecuta el autómata sobre una cadena.
     *
     * @param cadena entrada a validar; puede ser {@code null} o vacía
     * @return el resultado con el veredicto, la secuencia de estados y el motivo del rechazo
     */
    public ResultadoValidacion validar(String cadena) {
        if (cadena == null) {
            return new ResultadoValidacion(
                    null,
                    false,
                    TipoResultado.CADENA_VACIA,
                    List.of(ESTADO_INICIAL),
                    List.of(),
                    -1,
                    null,
                    "Cadena nula: no hay símbolos que leer y el estado inicial q0 no es final.");
        }
        if (cadena.isEmpty()) {
            return new ResultadoValidacion(
                    cadena,
                    false,
                    TipoResultado.CADENA_VACIA,
                    List.of(ESTADO_INICIAL),
                    List.of(),
                    -1,
                    null,
                    "Cadena vacía: el autómata permanece en q0, que no es un estado final.");
        }

        List<Estado> secuencia = new ArrayList<>(cadena.length() + 1);
        List<Paso> pasos = new ArrayList<>(cadena.length());
        Estado actual = ESTADO_INICIAL;
        secuencia.add(actual);

        for (int indice = 0; indice < cadena.length(); indice++) {
            char simbolo = cadena.charAt(indice);
            Estado siguiente = Delta.transicion(actual, simbolo);
            pasos.add(new Paso(indice, simbolo, actual, siguiente));
            secuencia.add(siguiente);

            if (siguiente.esSumidero()) {
                TipoResultado tipo = TipoResultado.desdeFallo(actual, simbolo);
                return new ResultadoValidacion(
                        cadena,
                        false,
                        tipo,
                        List.copyOf(secuencia),
                        List.copyOf(pasos),
                        indice,
                        Character.valueOf(simbolo),
                        motivoDeFallo(actual, simbolo, indice, tipo));
            }
            actual = siguiente;
        }

        if (ESTADOS_FINALES.contains(actual)) {
            return new ResultadoValidacion(
                    cadena,
                    true,
                    TipoResultado.VALIDO,
                    List.copyOf(secuencia),
                    List.copyOf(pasos),
                    -1,
                    null,
                    "Aceptada: la cadena se consumió por completo y terminó en q18.");
        }

        TipoResultado tipo = TipoResultado.porEstadoTerminal(actual);
        return new ResultadoValidacion(
                cadena,
                false,
                tipo,
                List.copyOf(secuencia),
                List.copyOf(pasos),
                -1,
                null,
                motivoDeTerminal(actual));
    }

    private static String motivoDeFallo(Estado origen, char simbolo, int indice, TipoResultado tipo) {
        String posicion = "Posición " + indice + " ('" + simbolo + "'): ";
        switch (tipo) {
            case SIMBOLO_INVALIDO:
                return posicion + "es una " + Alfabeto.describir(simbolo)
                        + ". Σ admite únicamente minúsculas a–z, dígitos 0–9, '@' y '.'.";
            case CARACTERES_EXTRA:
                return posicion + "el autómata ya había aceptado en q18 y aún quedan símbolos por leer;"
                        + " no se admite nada después de " + DOMINIO + ".";
            case DOMINIO_INVALIDO: {
                Character esperado = Delta.caracterEsperado(origen);
                if (esperado != null) {
                    return posicion + "se esperaba '" + esperado + "' para continuar con el dominio "
                            + DOMINIO + " y el autómata pasó de " + origen.etiqueta() + " a q19.";
                }
                return posicion + "el dominio debe ser exactamente " + DOMINIO
                        + "; desde " + origen.etiqueta() + " el autómata pasó a q19.";
            }
            case INICIALES_INVALIDAS:
                return posicion + "se esperaban entre " + Alfabeto.MIN_LETRAS + " y " + Alfabeto.MAX_LETRAS
                        + " letras minúsculas y el autómata pasó de " + origen.etiqueta() + " a q19.";
            case DIGITOS_INVALIDOS:
                return posicion + "se esperaban entre " + Alfabeto.MIN_DIGITOS + " y " + Alfabeto.MAX_DIGITOS
                        + " dígitos y el autómata pasó de " + origen.etiqueta() + " a q19.";
            default:
                return posicion + "transición a q19 desde " + origen.etiqueta() + ".";
        }
    }

    private static String motivoDeTerminal(Estado terminal) {
        switch (terminal) {
            case q1:
                return "La cadena terminó en q1: hacen falta al menos "
                        + Alfabeto.MIN_LETRAS + " letras minúsculas antes del número.";
            case q2:
            case q3:
            case q4:
                return "La cadena terminó en " + terminal.etiqueta() + ": faltan los "
                        + Alfabeto.MIN_DIGITOS + " o " + Alfabeto.MAX_DIGITOS + " dígitos del número.";
            case q5:
                return "La cadena terminó en q5: el número requiere al menos "
                        + Alfabeto.MIN_DIGITOS + " dígitos.";
            case q6:
            case q7:
                return "La cadena terminó en " + terminal.etiqueta() + ": falta el dominio "
                        + DOMINIO + ".";
            default:
                return "El dominio quedó incompleto en " + terminal.etiqueta()
                        + "; se requiere exactamente " + DOMINIO + ".";
        }
    }
}
