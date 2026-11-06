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
