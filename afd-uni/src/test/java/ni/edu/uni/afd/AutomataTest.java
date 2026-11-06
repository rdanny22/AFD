package ni.edu.uni.afd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import ni.edu.uni.afd.modelo.Alfabeto;
import ni.edu.uni.afd.modelo.Automata;
import ni.edu.uni.afd.modelo.BancoCasos;
import ni.edu.uni.afd.modelo.CasoPrueba;
import ni.edu.uni.afd.modelo.Delta;
import ni.edu.uni.afd.modelo.Estado;
import ni.edu.uni.afd.modelo.ResultadoValidacion;
import ni.edu.uni.afd.modelo.TipoResultado;

/**
 * Pruebas del autómata: casos válidos e inválidos, totalidad y determinismo de δ, sumidero,
 * aceptación estricta al final, longitud de la traza, clasificación y verificación cruzada contra una
 * expresión regular de referencia (usada solo en pruebas).
 */
class AutomataTest {

    private static final Pattern REFERENCIA = Pattern.compile("^[a-z]{2,4}[0-9]{2,3}@uni\\.edu\\.ni$");

    private static final String SIMBOLOS_ALEATORIOS =
            "abcdefghijklmnopqrstuvwxyz0123456789@.A- ";

    private final Automata automata = new Automata();

    // ------------------------------------------------------------------ válidos

    static Stream<String> correosValidos() {
        return Stream.of(
                "js25@uni.edu.ni",
                "lmra123@uni.edu.ni",
                "ab12@uni.edu.ni",
                "abcd999@uni.edu.ni");
    }

    @ParameterizedTest(name = "«{0}» debe ser aceptado")
    @MethodSource("correosValidos")
    @DisplayName("Acepta los correos con el formato institucional")
    void aceptaCorreosValidos(String cadena) {
        ResultadoValidacion resultado = automata.validar(cadena);

        assertTrue(resultado.aceptado(), "Se esperaba ACEPTADO para " + cadena);
        assertEquals(TipoResultado.VALIDO, resultado.tipo());
        assertEquals(Estado.q18, resultado.estadoFinal());
        assertEquals(-1, resultado.indiceFallo());
        assertNull(resultado.caracterFallo());
        assertEquals(cadena.length() + 1, resultado.secuencia().size());
        assertEquals(cadena.length(), resultado.pasos().size());
    }

    // ---------------------------------------------------------------- inválidos

    static Stream<Arguments> correosInvalidos() {
        return Stream.of(
                Arguments.of("Js25@unicomp.edu", TipoResultado.SIMBOLO_INVALIDO),
                Arguments.of("j@unicomp.edu", TipoResultado.INICIALES_INVALIDAS),
                Arguments.of("js25@unicom.edu", TipoResultado.DOMINIO_INVALIDO),
                Arguments.of("abc123@gmail.com", TipoResultado.DOMINIO_INVALIDO),
                Arguments.of("JS25@uni.edu.ni", TipoResultado.SIMBOLO_INVALIDO),
                Arguments.of("js25@UNI.EDU.NI", TipoResultado.SIMBOLO_INVALIDO),
                Arguments.of("js2@uni.edu.ni", TipoResultado.DIGITOS_INVALIDOS),
                Arguments.of("js2500@uni.edu.ni", TipoResultado.DIGITOS_INVALIDOS),
                Arguments.of("a12@uni.edu.ni", TipoResultado.INICIALES_INVALIDAS),
                Arguments.of("abcde12@uni.edu.ni", TipoResultado.INICIALES_INVALIDAS),
                Arguments.of("js25@uni.edu.ni.mx", TipoResultado.CARACTERES_EXTRA),
                Arguments.of("js25@uni.edu.ni1", TipoResultado.CARACTERES_EXTRA),
                Arguments.of("js25@uni.edu.ni ", TipoResultado.SIMBOLO_INVALIDO),
                Arguments.of(" js25@uni.edu.ni", TipoResultado.SIMBOLO_INVALIDO),
                Arguments.of("js25uni.edu.ni", TipoResultado.DIGITOS_INVALIDOS),
                Arguments.of("js25@uni.edu", TipoResultado.DOMINIO_INVALIDO),
                Arguments.of("js25@uni.edu.n", TipoResultado.DOMINIO_INVALIDO),
                Arguments.of("js25@uni.edu.ni#", TipoResultado.SIMBOLO_INVALIDO),
                Arguments.of("js2", TipoResultado.DIGITOS_INVALIDOS),
                Arguments.of("j", TipoResultado.INICIALES_INVALIDAS),
                Arguments.of("js25@uni.", TipoResultado.DOMINIO_INVALIDO));
    }

    @ParameterizedTest(name = "«{0}» debe ser rechazado como {1}")
    @MethodSource("correosInvalidos")
    @DisplayName("Rechaza los correos que no cumplen el formato y clasifica el fallo")
    void rechazaCorreosInvalidos(String cadena, TipoResultado tipoEsperado) {
        ResultadoValidacion resultado = automata.validar(cadena);

        assertFalse(resultado.aceptado(), "Se esperaba RECHAZADO para " + cadena);
        assertEquals(tipoEsperado, resultado.tipo(), "Tipo inesperado para " + cadena);
        assertEquals(tipoEsperado.etiqueta(), resultado.tipo().etiqueta());
        assertFalse(resultado.motivo().isEmpty(), "El rechazo debe explicarse");
    }

    // --------------------------------------------------------- cadena vacía y null

    @Test
    @DisplayName("La cadena vacía se rechaza en q0 sin excepciones")
    void rechazaCadenaVacia() {
        ResultadoValidacion resultado = automata.validar("");

        assertFalse(resultado.aceptado());
        assertEquals(TipoResultado.CADENA_VACIA, resultado.tipo());
        assertEquals(List.of(Estado.q0), resultado.secuencia());
        assertEquals(Estado.q0, resultado.estadoFinal());
    }

    @Test
    @DisplayName("La cadena nula se rechaza y no lanza excepciones")
    void rechazaCadenaNula() {
        ResultadoValidacion resultado = automata.validar(null);

        assertFalse(resultado.aceptado());
        assertEquals(TipoResultado.CADENA_VACIA, resultado.tipo());
        assertNull(resultado.cadena());
        assertEquals("«nula»", resultado.cadenaParaMostrar());
        assertEquals(List.of(Estado.q0), resultado.secuencia());
    }

    // ------------------------------------------------------------- δ y sumidero

    @Test
    @DisplayName("δ es total y determinista: 20 × 38 transiciones definidas")
    void deltaEsTotalYDeterminista() {
        Estado[][] matriz = Delta.copia();

        assertEquals(Estado.values().length, matriz.length);
        for (Estado estado : Estado.values()) {
            assertEquals(Alfabeto.TAMANO, matriz[estado.ordinal()].length,
                    "El estado " + estado.etiqueta() + " debe tener 38 transiciones");
            for (int columna = 0; columna < Alfabeto.TAMANO; columna++) {
                assertNotNull(matriz[estado.ordinal()][columna],
                        "Transición indefinida en " + estado.etiqueta() + " para el símbolo "
                                + Alfabeto.simbolo(columna));
            }
        }
    }

    @Test
    @DisplayName("q19 es absorbente y q18 no admite símbolos adicionales")
    void estadosAbsorbentes() {
        for (int columna = 0; columna < Alfabeto.TAMANO; columna++) {
            char simbolo = Alfabeto.simbolo(columna);
            assertEquals(Estado.q19, Delta.transicion(Estado.q19, simbolo),
                    "q19 debe ser sumidero para '" + simbolo + "'");
            assertEquals(Estado.q19, Delta.transicion(Estado.q18, simbolo),
                    "q18 no debe admitir '" + simbolo + "'");
        }
    }

    @Test
    @DisplayName("Todo símbolo fuera de Σ lleva a q19 en esa posición")
    void simbolosFueraDelAlfabetoVanAlSumidero() {
        String base = "js25";
        for (char ajeno : new char[] {'A', 'Z', '-', '_', '+', ' ', '\n', '\t', 'á', 'ñ', '#'}) {
            String cadena = base + ajeno + "@uni.edu.ni";
            ResultadoValidacion resultado = automata.validar(cadena);

            assertFalse(resultado.aceptado(), "Debe rechazarse: " + cadena);
            assertEquals(TipoResultado.SIMBOLO_INVALIDO, resultado.tipo());
            assertEquals(base.length(), resultado.indiceFallo(),
                    "El fallo debe ocurrir en la posición " + base.length());
            assertEquals(Estado.q19, resultado.estadoFinal());
            assertFalse(Alfabeto.pertenece(ajeno), "El símbolo debe estar fuera de Σ");
        }
    }

    @Test
    @DisplayName("q18 solo se alcanza tras leer el dominio completo")
    void aceptacionEstrictaAlFinal() {
        ResultadoValidacion aceptado = automata.validar("js25@uni.edu.ni");
        assertEquals(Estado.q18, aceptado.estadoFinal());

        ResultadoValidacion conExtra = automata.validar("js25@uni.edu.ni.");
        assertFalse(conExtra.aceptado());
        assertEquals(TipoResultado.CARACTERES_EXTRA, conExtra.tipo());

        ResultadoValidacion conMayuscula = automata.validar("js25@uni.edu.niX");
        assertFalse(conMayuscula.aceptado());
        assertEquals(TipoResultado.SIMBOLO_INVALIDO, conMayuscula.tipo());
    }

    // ------------------------------------------------------------------- trazas

    @Test
    @DisplayName("La traza de js25@uni.edu.ni es la secuencia esperada")
    void trazaExacta() {
        ResultadoValidacion resultado = automata.validar("js25@uni.edu.ni");

        assertEquals(List.of(
                Estado.q0, Estado.q1, Estado.q2, Estado.q5, Estado.q6, Estado.q8,
                Estado.q9, Estado.q10, Estado.q11, Estado.q12, Estado.q13,
                Estado.q14, Estado.q15, Estado.q16, Estado.q17, Estado.q18),
                resultado.secuencia());
        assertEquals("q0 → q1 → q2 → q5 → q6 → q8 → q9 → q10 → q11 → q12 → q13 → q14 → q15 → q16 → q17 → q18",
                resultado.caminoConFlechas());
        assertTrue(resultado.consumioTodo());
    }

    @Test
    @DisplayName("La secuencia crece un estado por símbolo consumido")
    void longitudDeLaSecuencia() {
        for (String cadena : new String[] {"js25@uni.edu.ni", "lmra123@uni.edu.ni", "js25", "js25@uni.edu"}) {
            ResultadoValidacion resultado = automata.validar(cadena);
            assertTrue(resultado.consumioTodo(), "Debe consumir toda la cadena: " + cadena);
            assertEquals(cadena.length() + 1, resultado.secuencia().size(), "Secuencia inválida para " + cadena);
        }
    }

    // ------------------------------------------------- verificación cruzada

    @Test
    @DisplayName("El AFD coincide con la expresión regular de referencia en 2000 cadenas aleatorias")
    void verificacionCruzadaConExpresionRegular() {
        Random aleatorio = new Random(20241105L);
        for (int intento = 0; intento < 2000; intento++) {
            int longitud = aleatorio.nextInt(21);
            StringBuilder cadena = new StringBuilder(longitud);
            for (int i = 0; i < longitud; i++) {
                cadena.append(SIMBOLOS_ALEATORIOS.charAt(aleatorio.nextInt(SIMBOLOS_ALEATORIOS.length())));
            }
            String muestra = cadena.toString();
            boolean esperado = REFERENCIA.matcher(muestra).matches();
            assertEquals(esperado, automata.validar(muestra).aceptado(),
                    "Discrepancia con la referencia en «" + muestra + "»");
        }
    }

    // ------------------------------------------------------- banco de casos CSV

    @Test
    @DisplayName("Todos los casos de casos-prueba.csv producen el veredicto esperado")
    void bancoDeCasosCoincide() throws IOException {
        BancoCasos banco = BancoCasos.desdeRecurso();

        assertTrue(banco.total() >= 15, "El banco debe tener al menos 15 casos");
        List<String> discrepancias = new ArrayList<>();
        for (CasoPrueba caso : banco.casos()) {
            boolean aceptado = automata.validar(caso.cadena()).aceptado();
            if (aceptado != caso.esperado()) {
                discrepancias.add(caso.cadena() + " → " + caso.descripcion());
            }
        }
        assertTrue(discrepancias.isEmpty(), "Casos con veredicto inesperado: " + discrepancias);
    }

    @Test
    @DisplayName("La tabla agrupada de δ tiene 20 filas y 6 columnas")
    void tablaAgrupadaTieneDimensionesCorrectas() {
        String[][] tabla = Delta.tablaAgrupada();

        assertEquals(Estado.values().length, tabla.length);
        for (String[] fila : tabla) {
            assertEquals(Delta.COLUMNAS_AGRUPADAS, fila.length);
            for (String celda : fila) {
                assertNotNull(celda);
            }
        }
        assertEquals("q0", tabla[0][0]);
    }

    // ------------------------------------------------ validación parametrizada

    @ParameterizedTest(name = "«{0}» produce siempre el mismo resultado")
    @ValueSource(strings = {"js25@uni.edu.ni", "Js25@unicomp.edu", "js2@uni.edu.ni"})
    @DisplayName("El autómata es determinista: dos ejecuciones dan el mismo resultado")
    void esDeterminista(String cadena) {
        ResultadoValidacion primera = automata.validar(cadena);
        ResultadoValidacion segunda = automata.validar(cadena);

        assertEquals(primera.tipo(), segunda.tipo());
        assertEquals(primera.secuencia(), segunda.secuencia());
        assertEquals(primera.aceptado(), segunda.aceptado());
    }
}
