package ni.edu.uni.afd.modelo;

/**
 * Un caso del banco de pruebas que la aplicación ejecuta desde {@code casos-prueba.csv}.
 *
 * @param cadena      cadena de entrada; el caso de cadena vacía se representa con {@code ""}
 * @param esperado    veredicto esperado
 * @param descripcion justificación legible del caso
 */
public record CasoPrueba(String cadena, boolean esperado, String descripcion) {

    /**
     * Texto del veredicto esperado.
     *
     * @return {@code "ACEPTADO"} o {@code "RECHAZADO"}
     */
    public String esperadoComoTexto() {
        return esperado ? "ACEPTADO" : "RECHAZADO";
    }
}
