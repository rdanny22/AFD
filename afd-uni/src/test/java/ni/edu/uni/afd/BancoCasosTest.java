package ni.edu.uni.afd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ni.edu.uni.afd.modelo.Automata;
import ni.edu.uni.afd.modelo.BancoCasos;
import ni.edu.uni.afd.modelo.CasoPrueba;
import ni.edu.uni.afd.modelo.ResultadoValidacion;
import ni.edu.uni.afd.vista.PanelTraza;

/**
 * Pruebas del banco de casos de la aplicación.
 *
 * <p>Cubren el respaldo que usa el botón «Ejecutar pruebas» cuando el recurso
 * {@code /casos-prueba.csv} no viaja dentro del paquete (por ejemplo, un jar exportado sin la
 * carpeta de recursos): el banco integrado debe contener exactamente los mismos casos que el
 * archivo y la batería debe llenar la tabla de traza sin necesidad de entorno gráfico.</p>
 */
class BancoCasosTest {

    @Test
    @DisplayName("El banco integrado coincide caso por caso con casos-prueba.csv")
    void bancoIntegradoCoincideConElArchivo() throws IOException {
        BancoCasos archivo = BancoCasos.desdeRecurso();
        BancoCasos integrado = BancoCasos.incluido();

        assertEquals(22, archivo.total(), "El archivo debe contener los 22 casos documentados");
        assertEquals(archivo.total(), integrado.total(), "Ambas fuentes deben tener la misma cantidad");

        for (int i = 0; i < archivo.total(); i++) {
            assertEquals(archivo.casos().get(i), integrado.casos().get(i),
                    "El caso " + (i + 1) + " difiere entre el archivo y el banco integrado");
        }
    }

    @Test
    @DisplayName("El banco integrado produce los veredictos esperados en los 22 casos")
    void bancoIntegradoAciertaTodosLosCasos() {
        Automata automata = new Automata();
        int correctos = 0;

        for (CasoPrueba caso : BancoCasos.incluido().casos()) {
            ResultadoValidacion resultado = automata.validar(caso.cadena());
            assertEquals(caso.esperado(), resultado.aceptado(),
                    "Veredicto inesperado para «" + caso.cadena() + "»: " + resultado.tipo());
            correctos++;
        }

        assertEquals(22, correctos, "Deben evaluarse los 22 casos del banco integrado");
    }

    @Test
    @DisplayName("La batería de pruebas llena la tabla de traza sin necesidad de pantalla")
    void laBateriaLlenaLaTablaSinPantalla() {
        PanelTraza traza = new PanelTraza();
        traza.limpiar();

        Automata automata = new Automata();
        BancoCasos banco = BancoCasos.incluido();
        for (CasoPrueba caso : banco.casos()) {
            traza.agregar(automata.validar(caso.cadena()), Boolean.valueOf(caso.esperado()));
        }

        assertTrue(traza.totalFilas() > 0, "La tabla debe registrar filas tras ejecutar la batería");
        assertEquals(banco.total(), traza.totalFilas(),
                "Cada caso del banco debe ocupar una fila de la traza");
    }
}
