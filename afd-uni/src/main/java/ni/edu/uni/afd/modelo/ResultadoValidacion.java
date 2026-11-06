package ni.edu.uni.afd.modelo;

import java.util.List;

/**
 * Resultado completo de ejecutar el autómata sobre una cadena.
 *
 * @param cadena       cadena evaluada tal como se ingresó ({@code null} si la entrada fue nula)
 * @param aceptado     {@code true} si y solo si la cadena se consumió por completo y terminó en {@code q18}
 * @param tipo         clasificación del resultado
 * @param secuencia    camino de estados recorrido, empezando siempre por {@code q0}
 * @param pasos        detalle por carácter de cada transición aplicada
 * @param indiceFallo  posición del símbolo que produjo el rechazo, o {@code -1} si la cadena se consumió completa
 * @param caracterFallo símbolo que produjo el rechazo, o {@code null}
 * @param motivo       explicación legible en español
 */
public record ResultadoValidacion(
        String cadena,
        boolean aceptado,
        TipoResultado tipo,
        List<Estado> secuencia,
        List<Paso> pasos,
        int indiceFallo,
        Character caracterFallo,
        String motivo) {

    /**
     * Estado alcanzado al final de la ejecución.
     *
     * @return el último estado de la secuencia
     */
    public Estado estadoFinal() {
        return secuencia.get(secuencia.size() - 1);
    }

    /**
     * Indica si el autómata llegó a leer todos los símbolos de la cadena.
     *
     * @return {@code true} si no se abortó en un índice intermedio
     */
    public boolean consumioTodo() {
        return indiceFallo < 0;
    }

    /**
     * Secuencia de estados en forma de camino con flechas.
     *
     * @return texto como {@code q0 → q1 → q2 → … → q18}
     */
    public String caminoConFlechas() {
        StringBuilder texto = new StringBuilder();
        for (Estado estado : secuencia) {
            if (texto.length() > 0) {
                texto.append(" → ");
            }
            texto.append(estado.etiqueta());
        }
        return texto.toString();
    }

    /**
     * Secuencia de estados en formato compacto, para la columna de la tabla de traza.
     *
     * @return texto como {@code q0→q1→q2→…→q18}
     */
    public String caminoCompacto() {
        return caminoConFlechas().replace(" → ", "→");
    }

    /**
     * Cadena preparada para mostrarse en la interfaz.
     *
     * @return la cadena, {@code «vacía»} o {@code «nula»}
     */
    public String cadenaParaMostrar() {
        if (cadena == null) {
            return "«nula»";
        }
        return cadena.isEmpty() ? "«vacía»" : cadena;
    }

    /**
     * Número de transiciones aplicadas.
     *
     * @return cantidad de pasos registrados
     */
    public int totalPasos() {
        return pasos.size();
    }
}
