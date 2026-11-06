package ni.edu.uni.afd.modelo;

/**
 * Un paso de la ejecución del autómata: el consumo de un símbolo y la transición aplicada.
 *
 * @param indice  posición del símbolo dentro de la cadena (base 0)
 * @param simbolo símbolo leído
 * @param desde   estado de origen
 * @param hasta   estado de destino
 */
public record Paso(int indice, char simbolo, Estado desde, Estado hasta) {

    /**
     * Representación compacta del paso, útil en mensajes y registro.
     *
     * @return texto con la forma {@code δ(q0, 'j') = q1}
     */
    public String descripcion() {
        return "δ(" + desde.etiqueta() + ", '" + simbolo + "') = " + hasta.etiqueta();
    }
}
