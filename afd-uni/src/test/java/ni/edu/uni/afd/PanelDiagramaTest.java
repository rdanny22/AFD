package ni.edu.uni.afd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ni.edu.uni.afd.modelo.Automata;
import ni.edu.uni.afd.modelo.ResultadoValidacion;
import ni.edu.uni.afd.vista.PanelDiagrama;

/**
 * Pruebas del dibujo del AFD: el diagrama debe poder renderizarse sin entorno gráfico, con todas sus
 * transiciones etiquetadas, y aceptar el resaltado de trazas y el zoom sin romperse.
 */
class PanelDiagramaTest {

    @Test
    @DisplayName("El diagrama se dibuja en una imagen, sin necesidad de pantalla")
    void dibujaEnUnaImagen() {
        PanelDiagrama panel = new PanelDiagrama();
        BufferedImage imagen = panel.renderizar();

        assertEquals(PanelDiagrama.ANCHO_BASE, imagen.getWidth());
        assertEquals(PanelDiagrama.ALTO_BASE, imagen.getHeight());
        assertTrue(pixelesPintados(imagen) > 3_000, "El diagrama debe contener trazos y texto");
    }

    @Test
    @DisplayName("Se dibujan las 22 transiciones con sus etiquetas")
    void dibujaTodasLasTransicionesEtiquetadas() {
        Map<String, String> transiciones = new PanelDiagrama().transicionesDibujadas();

        assertEquals(22, transiciones.size());
        assertEquals("a–z", transiciones.get("q0->q1"));
        assertEquals("0–9", transiciones.get("q2->q5"));
        assertEquals("0–9", transiciones.get("q3->q5"));
        assertEquals("@", transiciones.get("q6->q8"));
        assertEquals("u", transiciones.get("q8->q9"));
        assertEquals(".", transiciones.get("q11->q12"));
        assertEquals("i", transiciones.get("q17->q18"));
        assertEquals("Σ", transiciones.get("q18->q19"));
    }

    @Test
    @DisplayName("El resaltado de una traza aceptada y las transiciones de rechazo se dibujan")
    void dibujaConResaltadoYRechazos() {
        PanelDiagrama panel = new PanelDiagrama();
        panel.setTraza(new Automata().validar("js25@uni.edu.ni"));
        panel.setMostrarRechazos(true);
        panel.setZoom(1.5);

        BufferedImage imagen = panel.renderizar();

        assertEquals(Math.round(PanelDiagrama.ANCHO_BASE * 1.5), imagen.getWidth());
        assertTrue(pixelesPintados(imagen) > 3_000);
        assertNotNull(panel.getTraza());
        assertTrue(panel.isMostrarRechazos());
    }

    @Test
    @DisplayName("Un rechazo también se resalta y el zoom queda acotado")
    void resaltaRechazosYAcotaElZoom() {
        PanelDiagrama panel = new PanelDiagrama();
        ResultadoValidacion rechazo = new Automata().validar("Js25@unicomp.edu");

        panel.setTraza(rechazo);
        assertTrue(pixelesPintados(panel.renderizar()) > 3_000);

        panel.setZoom(50.0);
        assertEquals(2.5, panel.getZoom(), 0.0001);

        panel.setZoom(0.01);
        assertEquals(0.6, panel.getZoom(), 0.0001);
        assertEquals(new Dimension(
                (int) Math.round(PanelDiagrama.ANCHO_BASE * 0.6),
                (int) Math.round(PanelDiagrama.ALTO_BASE * 0.6)), panel.getPreferredSize());
    }

    @Test
    @DisplayName("Sin traza no se resalta nada y el diagrama sigue siendo válido")
    void sinTrazaNoResalta() {
        PanelDiagrama panel = new PanelDiagrama();
        panel.setTraza(null);

        assertNull(panel.getTraza());
        assertFalse(panel.isMostrarRechazos());
        assertTrue(pixelesPintados(panel.renderizar()) > 3_000);
    }

    private static long pixelesPintados(BufferedImage imagen) {
        long total = 0;
        for (int x = 0; x < imagen.getWidth(); x += 3) {
            for (int y = 0; y < imagen.getHeight(); y += 3) {
                if ((imagen.getRGB(x, y) & 0xFFFFFF) != 0xFFFFFF) {
                    total++;
                }
            }
        }
        return total;
    }
}
