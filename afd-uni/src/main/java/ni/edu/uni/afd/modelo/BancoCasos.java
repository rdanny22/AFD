package ni.edu.uni.afd.modelo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Banco de casos de prueba cargado desde {@code /casos-prueba.csv}.
 *
 * <p>Formato de cada línea: {@code cadena;esperado;descripcion}. La primera línea (cabecera) se
 * ignora y las líneas vacías o que empiezan por {@code #} se descartan. La cadena <em>no</em> se
 * recorta: la validación es estricta, de modo que un espacio final forma parte de la entrada.</p>
 */
public final class BancoCasos {

    /** Recurso con la batería mínima de casos. */
    public static final String RECURSO = "/casos-prueba.csv";

    /** Nombre con el que se identifica el banco de respaldo en los mensajes de la interfaz. */
    public static final String NOMBRE_INTEGRADO = "banco integrado";

    /**
     * Banco de respaldo definido en el propio código.
     *
     * <p>Se usa cuando {@link #RECURSO} no está disponible, por ejemplo en un jar empaquetado sin
     * la carpeta de recursos. Contiene exactamente los mismos 22 casos que el archivo, en el mismo
     * orden; la prueba {@code BancoCasosTest} verifica que ambas fuentes coinciden para que no se
     * desincronicen.</p>
     */
    private static final List<CasoPrueba> CASOS_INTEGRADOS = List.of(
            new CasoPrueba("js25@uni.edu.ni", true, "Caso canónico del enunciado: 2 iniciales y 2 dígitos"),
            new CasoPrueba("lmra123@uni.edu.ni", true, "Límites máximos: 4 iniciales y 3 dígitos"),
            new CasoPrueba("ab12@uni.edu.ni", true, "Límites mínimos: 2 iniciales y 2 dígitos"),
            new CasoPrueba("abcd999@uni.edu.ni", true, "Límites máximos en ambas partes"),
            new CasoPrueba("Js25@unicomp.edu", false, "La mayúscula J no pertenece a Sigma"),
            new CasoPrueba("j@unicomp.edu", false, "Falta el número y el dominio es incorrecto"),
            new CasoPrueba("js25@unicom.edu", false, "Dominio mal escrito"),
            new CasoPrueba("abc123@gmail.com", false, "Dominio incorrecto"),
            new CasoPrueba("JS25@uni.edu.ni", false, "Iniciales en mayúscula"),
            new CasoPrueba("js25@UNI.EDU.NI", false, "Dominio en mayúsculas"),
            new CasoPrueba("js2@uni.edu.ni", false, "Un solo dígito: se requieren 2 o 3"),
            new CasoPrueba("js2500@uni.edu.ni", false, "Cuatro dígitos: se requieren 2 o 3"),
            new CasoPrueba("a12@uni.edu.ni", false, "Una sola inicial: se requieren de 2 a 4"),
            new CasoPrueba("abcde12@uni.edu.ni", false, "Cinco iniciales: se requieren de 2 a 4"),
            new CasoPrueba("js25@uni.edu.ni.mx", false, "Caracteres extra después de alcanzar q18"),
            new CasoPrueba("js25uni.edu.ni", false, "Falta la arroba del dominio"),
            new CasoPrueba("js25@uni.edu.ni ", false, "Espacio final: carácter fuera de Sigma"),
            new CasoPrueba("js25@uni.edu", false, "Dominio truncado"),
            new CasoPrueba("js25@uni.edu.n", false, "Dominio truncado"),
            new CasoPrueba("js25@uni.edu.ni#", false, "Carácter fuera de Sigma después de q18"),
            new CasoPrueba("js25", false, "Falta el número y el dominio"),
            new CasoPrueba("", false, "Cadena vacía: q0 no es un estado final"));

    private final List<CasoPrueba> casos;
    private final Map<String, Boolean> esperadosPorCadena;

    private BancoCasos(List<CasoPrueba> casos) {
        this.casos = List.copyOf(casos);
        Map<String, Boolean> esperados = new LinkedHashMap<>();
        for (CasoPrueba caso : casos) {
            esperados.put(caso.cadena(), Boolean.valueOf(caso.esperado()));
        }
        this.esperadosPorCadena = Collections.unmodifiableMap(esperados);
    }

    /**
     * Banco vacío, usado cuando el recurso no puede leerse.
     *
     * @return un banco sin casos
     */
    public static BancoCasos vacio() {
        return new BancoCasos(List.of());
    }

    /**
     * Banco de respaldo incluido en el código, con los mismos casos que {@link #RECURSO}.
     *
     * @return el banco integrado
     */
    public static BancoCasos incluido() {
        return new BancoCasos(CASOS_INTEGRADOS);
    }

    /**
     * Carga el banco incluido en los recursos de la aplicación.
     *
     * @return el banco de casos
     * @throws IOException si el recurso no existe o no puede leerse
     */
    public static BancoCasos desdeRecurso() throws IOException {
        try (InputStream entrada = BancoCasos.class.getResourceAsStream(RECURSO)) {
            if (entrada == null) {
                throw new IOException("No se encontró el recurso " + RECURSO);
            }
            return cargar(entrada);
        }
    }

    /**
     * Carga un banco de casos desde un flujo de texto UTF-8.
     *
     * @param entrada flujo de entrada
     * @return el banco de casos
     * @throws IOException si ocurre un error de lectura
     */
    public static BancoCasos cargar(InputStream entrada) throws IOException {
        List<CasoPrueba> casos = new ArrayList<>();
        try (BufferedReader lector = new BufferedReader(new InputStreamReader(entrada, StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                String limpia = linea.strip();
                if (limpia.isEmpty() || limpia.startsWith("#")) {
                    continue;
                }
                String[] campos = linea.split(";", -1);
                if (campos.length < 3) {
                    continue;
                }
                if ("cadena".equals(campos[0].strip())) {
                    continue;
                }
                boolean esperado = "ACEPTADO".equalsIgnoreCase(campos[1].strip());
                casos.add(new CasoPrueba(campos[0], esperado, campos[2].strip()));
            }
        }
        return new BancoCasos(casos);
    }

    /**
     * Casos del banco, en el orden del archivo.
     *
     * @return lista inmutable de casos
     */
    public List<CasoPrueba> casos() {
        return casos;
    }

    /**
     * Veredicto esperado para una cadena concreta.
     *
     * @param cadena cadena a buscar
     * @return {@code TRUE}/{@code FALSE} si está en el banco, {@code null} si no está
     */
    public Boolean esperadoPara(String cadena) {
        if (cadena == null) {
            return null;
        }
        return esperadosPorCadena.get(cadena);
    }

    /**
     * Cantidad de casos cargados.
     *
     * @return número de casos
     */
    public int total() {
        return casos.size();
    }
}
