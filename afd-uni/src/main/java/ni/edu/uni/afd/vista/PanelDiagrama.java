package ni.edu.uni.afd.vista;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;

import ni.edu.uni.afd.modelo.Estado;
import ni.edu.uni.afd.modelo.ResultadoValidacion;

/**
 * Dibuja el diagrama de estados del AFD directamente sobre la pantalla con Java2D.
 *
 * <p>El dibujo es vectorial: no depende de imágenes externas ni de librerías de graficación. Los 20
 * estados se distribuyen en dos filas en serpentina (q0…q9 arriba y q10…q19 abajo) y cada transición
 * se dibuja con su etiqueta. Opcionalmente resalta el camino recorrido en la última validación y
 * dibuja las transiciones de rechazo hacia {@code q19}.</p>
 *
 * <p>La geometría se define en coordenadas lógicas de {@value #ANCHO_BASE} × {@value #ALTO_BASE}
 * píxeles y se escala con {@link #setZoom(double)}, de modo que el tamaño preferido del componente
 * crece o disminuye sin deformar el diagrama.</p>
 */
public final class PanelDiagrama extends JComponent {

    private static final long serialVersionUID = 1L;

    /** Ancho lógico del diagrama. */
    public static final int ANCHO_BASE = 1060;

    /** Alto lógico del diagrama. */
    public static final int ALTO_BASE = 450;

    private static final double ZOOM_MINIMO = 0.6;
    private static final double ZOOM_MAXIMO = 2.5;

    private static final Color FONDO = Color.WHITE;
    private static final Color BORDE_ESTADO = new Color(0x2F, 0x55, 0x97);
    private static final Color RELLENO_ESTADO = new Color(0xEA, 0xF2, 0xFB);
    private static final Color RELLENO_FINAL = new Color(0xD9, 0xEA, 0xD3);
    private static final Color BORDE_FINAL = new Color(0x1B, 0x8A, 0x3A);
    private static final Color BORDE_SUMIDERO = new Color(0xC6, 0x28, 0x28);
    private static final Color RELLENO_SUMIDERO = new Color(0xFC, 0xE4, 0xE4);
    private static final Color RELLENO_RESALTADO = new Color(0xFF, 0xF3, 0xC4);
    private static final Color BORDE_RESALTADO = new Color(0xE0, 0x7B, 0x00);
    private static final Color TEXTO = new Color(0x1F, 0x1F, 0x1F);
    private static final Color TEXTO_SUAVE = new Color(0x5A, 0x5A, 0x5A);

    private static final double RADIO = 21.0;
    private static final double RADIO_FINAL = 25.0;
    private static final double MARGEN_LATERAL = 58.0;
    private static final double SEPARACION = (ANCHO_BASE - 2 * MARGEN_LATERAL) / 9.0;
    private static final double Y_SUPERIOR = 122.0;
    private static final double Y_INFERIOR = 330.0;
    private static final int Y_LEYENDA = 402;

    private static final Font FUENTE_ESTADO = new Font(Font.SANS_SERIF, Font.BOLD, 11);
    private static final Font FUENTE_ETIQUETA = new Font(Font.SANS_SERIF, Font.BOLD, 9);
    private static final Font FUENTE_NOTAS = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
    private static final Font FUENTE_TITULO = new Font(Font.SANS_SERIF, Font.BOLD, 12);

    private final Map<Estado, Point2D.Double> posiciones = new EnumMap<>(Estado.class);
    private final List<Arista> aristas = new ArrayList<>();

    private double zoom = 1.0;
    private boolean mostrarRechazos;
    private transient ResultadoValidacion traza;

    /**
     * Construye el panel con la geometría del autómata.
     */
    public PanelDiagrama() {
        calcularPosiciones();
        calcularAristas();
        setOpaque(true);
        setBackground(FONDO);
        setPreferredSize(new Dimension(ANCHO_BASE, ALTO_BASE));
        setToolTipText("Diagrama de estados del AFD: q18 es el estado final y q19 el sumidero de rechazo.");
    }

    // ------------------------------------------------------------------ geometría

    private void calcularPosiciones() {
        for (int i = 0; i <= 9; i++) {
            posiciones.put(Estado.values()[i],
                    new Point2D.Double(MARGEN_LATERAL + i * SEPARACION, Y_SUPERIOR));
        }
        for (int j = 0; j <= 8; j++) {
            posiciones.put(Estado.values()[10 + j],
                    new Point2D.Double(MARGEN_LATERAL + (9 - j) * SEPARACION, Y_INFERIOR));
        }
        posiciones.put(Estado.q19, new Point2D.Double(MARGEN_LATERAL, Y_INFERIOR));
    }

    private void calcularAristas() {
        // Cadena principal: el único camino posible hacia q18.
        aristas.add(new Arista(Estado.q0, Estado.q1, "a–z", Clase.CADENA));
        aristas.add(new Arista(Estado.q1, Estado.q2, "a–z", Clase.CADENA));
        aristas.add(new Arista(Estado.q2, Estado.q3, "a–z", Clase.CADENA));
        aristas.add(new Arista(Estado.q3, Estado.q4, "a–z", Clase.CADENA));
        aristas.add(new Arista(Estado.q4, Estado.q5, "0–9", Clase.CADENA));
        aristas.add(new Arista(Estado.q5, Estado.q6, "0–9", Clase.CADENA));
        aristas.add(new Arista(Estado.q6, Estado.q7, "0–9", Clase.CADENA));
        aristas.add(new Arista(Estado.q7, Estado.q8, "@", Clase.CADENA));
        aristas.add(new Arista(Estado.q8, Estado.q9, "u", Clase.CADENA));
        aristas.add(new Arista(Estado.q9, Estado.q10, "n", Clase.CADENA));
        aristas.add(new Arista(Estado.q10, Estado.q11, "i", Clase.CADENA));
        aristas.add(new Arista(Estado.q11, Estado.q12, ".", Clase.CADENA));
        aristas.add(new Arista(Estado.q12, Estado.q13, "e", Clase.CADENA));
        aristas.add(new Arista(Estado.q13, Estado.q14, "d", Clase.CADENA));
        aristas.add(new Arista(Estado.q14, Estado.q15, "u", Clase.CADENA));
        aristas.add(new Arista(Estado.q15, Estado.q16, ".", Clase.CADENA));
        aristas.add(new Arista(Estado.q16, Estado.q17, "n", Clase.CADENA));
        aristas.add(new Arista(Estado.q17, Estado.q18, "i", Clase.CADENA));

        // Atajos de la parte local (permiten 2–4 iniciales y 2–3 dígitos).
        aristas.add(new Arista(Estado.q2, Estado.q5, "0–9", Clase.ATAJO));
        aristas.add(new Arista(Estado.q3, Estado.q5, "0–9", Clase.ATAJO));
        aristas.add(new Arista(Estado.q6, Estado.q8, "@", Clase.ATAJO));

        // Estados absorbentes.
        aristas.add(new Arista(Estado.q18, Estado.q19, "Σ", Clase.RECHAZO));
    }

    // ------------------------------------------------------------------ API

    /**
     * Establece el resultado cuya traza se resaltará sobre el diagrama.
     *
     * @param resultado último resultado validado, o {@code null} para no resaltar nada
     */
    public void setTraza(ResultadoValidacion resultado) {
        this.traza = resultado;
        repaint();
    }

    /** @return el resultado cuya traza se está resaltando, o {@code null} */
    public ResultadoValidacion getTraza() {
        return traza;
    }

    /**
     * Indica si se dibujan las transiciones de rechazo que no forman parte de la cadena principal.
     *
     * @param mostrar {@code true} para dibujar las transiciones hacia {@code q19}
     */
    public void setMostrarRechazos(boolean mostrar) {
        this.mostrarRechazos = mostrar;
        repaint();
    }

    /** @return {@code true} si se dibujan las transiciones de rechazo */
    public boolean isMostrarRechazos() {
        return mostrarRechazos;
    }

    /**
     * Cambia el factor de escala del diagrama.
     *
     * @param factor escala entre {@value #ZOOM_MINIMO} y {@value #ZOOM_MAXIMO}
     */
    public void setZoom(double factor) {
        this.zoom = Math.max(ZOOM_MINIMO, Math.min(ZOOM_MAXIMO, factor));
        int ancho = (int) Math.round(ANCHO_BASE * zoom);
        int alto = (int) Math.round(ALTO_BASE * zoom);
        setPreferredSize(new Dimension(ancho, alto));
        setMinimumSize(new Dimension(ancho, alto));
        revalidate();
        repaint();
    }

    /** @return el factor de escala actual */
    public double getZoom() {
        return zoom;
    }

    /**
     * Dibuja el diagrama en una imagen, sin necesidad de que el componente esté visible.
     *
     * @return imagen RGB con el diagrama al tamaño actual
     */
    public BufferedImage renderizar() {
        int ancho = Math.max(1, (int) Math.round(ANCHO_BASE * zoom));
        int alto = Math.max(1, (int) Math.round(ALTO_BASE * zoom));
        BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        Graphics2D grafico = imagen.createGraphics();
        grafico.setColor(FONDO);
        grafico.fillRect(0, 0, ancho, alto);
        grafico.scale(zoom, zoom);
        dibujar(grafico);
        grafico.dispose();
        return imagen;
    }

    // ------------------------------------------------------------------ pintado

    @Override
    protected void paintComponent(Graphics grafico) {
        Graphics2D g = (Graphics2D) grafico.create();
        g.setColor(FONDO);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.scale(zoom, zoom);
        dibujar(g);
        g.dispose();
    }

    private void dibujar(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        Set<Estado> visitados = new LinkedHashSet<>();
        Set<String> camino = new LinkedHashSet<>();
        prepararResaltado(visitados, camino);

        dibujarTitulo(g);
        dibujarFlechaInicial(g);
        dibujarRechazosOpcionales(g, camino);
        dibujarAristas(g, camino);
        dibujarEstados(g, visitados);
        dibujarEtiquetaDeFallo(g);
        dibujarLeyenda(g);
    }

    private void prepararResaltado(Set<Estado> visitados, Set<String> camino) {
        if (traza == null) {
            return;
        }
        visitados.addAll(traza.secuencia());
        List<Estado> secuencia = traza.secuencia();
        for (int i = 1; i < secuencia.size(); i++) {
            camino.add(clave(secuencia.get(i - 1), secuencia.get(i)));
        }
    }

    private static String clave(Estado desde, Estado hasta) {
        return desde.etiqueta() + "->" + hasta.etiqueta();
    }

    private void dibujarTitulo(Graphics2D g) {
        g.setFont(FUENTE_TITULO);
        g.setColor(TEXTO);
        g.drawString("AFD — correo institucional UNI:  <iniciales 2–4 a–z><2–3 dígitos>@uni.edu.ni", (float) MARGEN_LATERAL, 26);
        g.setFont(FUENTE_NOTAS);
        g.setColor(TEXTO_SUAVE);
        g.drawString("Q = {q0 … q19}  ·  Σ = {a–z, 0–9, @, .}  (38 símbolos)  ·  F = {q18}", (float) MARGEN_LATERAL, 42);
    }

    private void dibujarFlechaInicial(Graphics2D g) {
        Point2D origen = posiciones.get(Estado.q0);
        Point2D desde = new Point2D.Double(origen.getX() - RADIO - 26, origen.getY());
        Point2D hasta = new Point2D.Double(origen.getX() - RADIO, origen.getY());
        g.setStroke(new BasicStroke(1.4f));
        g.setColor(TEXTO);
        g.draw(new Line2D.Double(desde, hasta));
        dibujarPunta(g, desde, hasta, TEXTO, 7.0);
        g.setFont(FUENTE_NOTAS);
        g.setColor(TEXTO_SUAVE);
        g.drawString("inicio", (float) desde.getX() - 4, (float) desde.getY() + 30);
    }

    private void dibujarEstados(Graphics2D g, Set<Estado> visitados) {
        g.setFont(FUENTE_ESTADO);
        for (Map.Entry<Estado, Point2D.Double> entrada : posiciones.entrySet()) {
            Estado estado = entrada.getKey();
            Point2D.Double centro = entrada.getValue();
            boolean resaltado = visitados.contains(estado);

            Color relleno;
            Color borde;
            if (estado.esFinal()) {
                relleno = RELLENO_FINAL;
                borde = BORDE_FINAL;
            } else if (estado.esSumidero()) {
                relleno = RELLENO_SUMIDERO;
                borde = BORDE_SUMIDERO;
            } else {
                relleno = resaltado ? RELLENO_RESALTADO : RELLENO_ESTADO;
                borde = resaltado ? BORDE_RESALTADO : BORDE_ESTADO;
            }

            double radio = estado.esFinal() ? RADIO_FINAL : RADIO;
            Ellipse2D circulo = new Ellipse2D.Double(
                    centro.getX() - radio, centro.getY() - radio, radio * 2, radio * 2);

            g.setColor(relleno);
            g.fill(circulo);
            g.setColor(borde);
            g.setStroke(estado.esSumidero()
                    ? new BasicStroke(1.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] {4f, 3f}, 0f)
                    : new BasicStroke(resaltado ? 2.4f : 1.6f));
            g.draw(circulo);
            if (estado.esFinal()) {
                double interior = radio - 3.5;
                g.draw(new Ellipse2D.Double(
                        centro.getX() - interior, centro.getY() - interior, interior * 2, interior * 2));
            }

            g.setColor(estado.esSumidero() ? BORDE_SUMIDERO : TEXTO);
            String nombre = estado.etiqueta();
            FontMetrics medidas = g.getFontMetrics();
            g.drawString(nombre,
                    (float) (centro.getX() - medidas.stringWidth(nombre) / 2.0),
                    (float) (centro.getY() + medidas.getAscent() / 2.0 - 1));

            if (estado.esFinal()) {
                g.setFont(FUENTE_NOTAS);
                g.setColor(BORDE_FINAL);
                g.drawString("estado final", (float) (centro.getX() - 26), (float) (centro.getY() + radio + 14));
                g.setFont(FUENTE_ESTADO);
            } else if (estado.esSumidero()) {
                g.setFont(FUENTE_NOTAS);
                g.setColor(BORDE_SUMIDERO);
                g.drawString("sumidero", (float) (centro.getX() - 22), (float) (centro.getY() + radio + 14));
                g.setFont(FUENTE_ESTADO);
            }
        }
    }

    private void dibujarAristas(Graphics2D g, Set<String> camino) {
        for (Arista arista : aristas) {
            boolean resaltada = camino.contains(clave(arista.desde(), arista.hasta()));
            dibujarArista(g, arista, resaltada);
        }
    }

    private void dibujarArista(Graphics2D g, Arista arista, boolean resaltada) {
        Estado desde = arista.desde();
        Estado hasta = arista.hasta();
        Point2D.Double origen = posiciones.get(desde);
        Point2D.Double destino = posiciones.get(hasta);
        double radioDestino = hasta.esFinal() ? RADIO_FINAL : RADIO;
        if (hasta.esSumidero()) {
            radioDestino = RADIO;
        }

        switch (arista.clase()) {
            case ATAJO:
                dibujarCurva(g, origen, destino, altoDelArco(desde, hasta), arista.etiqueta(), resaltada,
                        false, radioDestino);
                break;
            case RECHAZO:
                dibujarCurva(g, origen, destino, 34.0, arista.etiqueta(), resaltada, true, radioDestino);
                break;
            case CADENA:
            default:
                dibujarRecta(g, origen, destino, arista.etiqueta(), resaltada, radioDestino);
                break;
        }
    }

    private double altoDelArco(Estado desde, Estado hasta) {
        int columnas = Math.abs(indiceColumna(hasta) - indiceColumna(desde));
        return 52.0 + columnas * 16.0;
    }

    private int indiceColumna(Estado estado) {
        Point2D punto = posiciones.get(estado);
        return (int) Math.round((punto.getX() - MARGEN_LATERAL) / SEPARACION);
    }

    private void dibujarRecta(Graphics2D g, Point2D origen, Point2D destino, String etiqueta,
            boolean resaltada, double radioDestino) {
        boolean vertical = Math.abs(origen.getX() - destino.getX()) < 1.0;
        Point2D desde;
        Point2D hasta;
        if (vertical) {
            desde = new Point2D.Double(origen.getX(), origen.getY() + RADIO);
            hasta = new Point2D.Double(destino.getX(), destino.getY() - radioDestino);
        } else {
            desde = borde(origen, destino, RADIO);
            hasta = borde(destino, origen, radioDestino);
        }

        Color color = resaltada ? BORDE_RESALTADO : BORDE_ESTADO;
        g.setColor(color);
        g.setStroke(new BasicStroke(resaltada ? 2.6f : 1.3f));
        g.draw(new Line2D.Double(desde, hasta));
        dibujarPunta(g, desde, hasta, color, resaltada ? 9.0 : 7.0);

        double xEtiqueta = (desde.getX() + hasta.getX()) / 2.0;
        double yEtiqueta = vertical
                ? (desde.getY() + hasta.getY()) / 2.0
                : Math.min(desde.getY(), hasta.getY()) - 5.0;
        if (vertical) {
            xEtiqueta += 22.0;
        }
        dibujarEtiqueta(g, etiqueta, xEtiqueta, yEtiqueta, resaltada);
    }

    private void dibujarCurva(Graphics2D g, Point2D origen, Point2D destino, double altura,
            String etiqueta, boolean resaltada, boolean rechazo, double radioDestino) {
        double medioX = (origen.getX() + destino.getX()) / 2.0;
        double medioY = (origen.getY() + destino.getY()) / 2.0;
        boolean mismaFila = Math.abs(origen.getY() - destino.getY()) < 1.0;
        Point2D control = new Point2D.Double(medioX, medioY - (mismaFila ? altura : altura + 18.0));

        Point2D desde = borde(origen, control, RADIO);
        Point2D hasta = borde(destino, control, radioDestino);

        QuadCurve2D curva = new QuadCurve2D.Double(
                desde.getX(), desde.getY(), control.getX(), control.getY(), hasta.getX(), hasta.getY());

        Color color = rechazo ? BORDE_SUMIDERO : BORDE_ESTADO;
        if (resaltada) {
            color = BORDE_RESALTADO;
            if (rechazo) {
                color = BORDE_SUMIDERO;
            }
        }
        g.setColor(color);
        g.setStroke(rechazo
                ? new BasicStroke(resaltada ? 2.6f : 1.3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f,
                        new float[] {5f, 3f}, 0f)
                : new BasicStroke(resaltada ? 2.6f : 1.3f));
        g.draw(curva);
        dibujarPunta(g, control, hasta, color, resaltada ? 9.0 : 7.0);

        Point2D puntoEtiqueta = puntoDeCurva(desde, control, hasta, 0.5);
        dibujarEtiqueta(g, etiqueta, puntoEtiqueta.getX(), puntoEtiqueta.getY() - 8.0, resaltada);
    }

    private static Point2D puntoDeCurva(Point2D p0, Point2D control, Point2D p1, double t) {
        double u = 1 - t;
        double x = u * u * p0.getX() + 2 * u * t * control.getX() + t * t * p1.getX();
        double y = u * u * p0.getY() + 2 * u * t * control.getY() + t * t * p1.getY();
        return new Point2D.Double(x, y);
    }

    private void dibujarRechazosOpcionales(Graphics2D g, Set<String> camino) {
        if (!mostrarRechazos) {
            return;
        }
        Point2D sumidero = posiciones.get(Estado.q19);
        g.setColor(new Color(0xC6, 0x28, 0x28, 70));
        g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f,
                new float[] {3f, 4f}, 0f));

        for (Estado estado : Estado.values()) {
            if (estado.esFinal() || estado.esSumidero()) {
                continue;
            }
            boolean resaltada = camino.contains(clave(estado, Estado.q19));
            Point2D origen = posiciones.get(estado);
            Point2D desde = borde(origen, sumidero, RADIO);
            Point2D hasta = borde(sumidero, origen, RADIO);
            double controlY = origen.getY() < 220
                    ? Math.min(desde.getY(), hasta.getY()) - 70.0
                    : Math.max(desde.getY(), hasta.getY()) + 40.0;
            QuadCurve2D curva = new QuadCurve2D.Double(
                    desde.getX(), desde.getY(),
                    (desde.getX() + hasta.getX()) / 2.0, controlY,
                    hasta.getX(), hasta.getY());
            if (resaltada) {
                g.setColor(BORDE_SUMIDERO);
                g.setStroke(new BasicStroke(1.6f));
            }
            g.draw(curva);
        }
    }

    private void dibujarEtiquetaDeFallo(Graphics2D g) {
        if (traza == null || traza.consumioTodo() || traza.caracterFallo() == null) {
            return;
        }
        List<Estado> secuencia = traza.secuencia();
        Estado origen = secuencia.get(secuencia.size() - 2);
        Point2D puntoOrigen = posiciones.get(origen);
        Point2D sumidero = posiciones.get(Estado.q19);
        double x = (puntoOrigen.getX() + sumidero.getX()) / 2.0;
        double y = (puntoOrigen.getY() + sumidero.getY()) / 2.0 - 14.0;

        String texto = "'" + traza.caracterFallo() + "' rechazado";
        g.setFont(FUENTE_ETIQUETA);
        FontMetrics medidas = g.getFontMetrics();
        double ancho = medidas.stringWidth(texto) + 10.0;
        g.setColor(new Color(198, 40, 40, 235));
        g.fill(new RoundRectangle2D.Double(x - ancho / 2, y - 10, ancho, 15, 6, 6));
        g.setColor(Color.WHITE);
        g.drawString(texto, (float) (x - medidas.stringWidth(texto) / 2.0), (float) (y + 1.5));
    }

    private void dibujarLeyenda(Graphics2D g) {
        g.setFont(FUENTE_NOTAS);
        g.setColor(TEXTO_SUAVE);
        g.drawString("q18 (doble círculo) es el único estado final; q19 es el sumidero: cualquier símbolo "
                + "no contemplado en la etiqueta de salida conduce a q19.", (float) MARGEN_LATERAL, Y_LEYENDA);
        String nota = mostrarRechazos
                ? "Líneas rojas punteadas: transiciones de rechazo hacia q19 (activadas)."
                : "Active «transiciones de rechazo» para ver todas las aristas que llevan a q19.";
        g.drawString(nota, (float) MARGEN_LATERAL, Y_LEYENDA + 15);
        if (traza != null) {
            g.setColor(BORDE_RESALTADO);
            g.drawString("Naranja: camino recorrido por la última validación (" + traza.totalPasos()
                    + " transiciones).", (float) MARGEN_LATERAL, Y_LEYENDA + 30);
        }
    }

    // ------------------------------------------------------------------ utilidades de dibujo

    private static Point2D borde(Point2D centro, Point2D hacia, double radio) {
        double dx = hacia.getX() - centro.getX();
        double dy = hacia.getY() - centro.getY();
        double distancia = Math.hypot(dx, dy);
        if (distancia < 0.001) {
            return centro;
        }
        return new Point2D.Double(centro.getX() + dx / distancia * radio, centro.getY() + dy / distancia * radio);
    }

    private void dibujarPunta(Graphics2D g, Point2D desde, Point2D hasta, Color color, double tamano) {
        double angulo = Math.atan2(hasta.getY() - desde.getY(), hasta.getX() - desde.getX());
        double apertura = Math.toRadians(24);
        Path2D punta = new Path2D.Double();
        punta.moveTo(hasta.getX(), hasta.getY());
        punta.lineTo(hasta.getX() - tamano * Math.cos(angulo - apertura),
                hasta.getY() - tamano * Math.sin(angulo - apertura));
        punta.lineTo(hasta.getX() - tamano * Math.cos(angulo + apertura),
                hasta.getY() - tamano * Math.sin(angulo + apertura));
        punta.closePath();
        g.setColor(color);
        g.fill(punta);
    }

    private void dibujarEtiqueta(Graphics2D g, String texto, double x, double y, boolean resaltada) {
        if (texto == null || texto.isEmpty()) {
            return;
        }
        g.setFont(FUENTE_ETIQUETA);
        FontMetrics medidas = g.getFontMetrics();
        double ancho = medidas.stringWidth(texto) + 8.0;
        double alto = 13.0;

        g.setColor(new Color(255, 255, 255, 225));
        g.fill(new RoundRectangle2D.Double(x - ancho / 2.0, y - alto + 3.5, ancho, alto, 5, 5));
        g.setColor(resaltada ? BORDE_RESALTADO : BORDE_ESTADO);
        g.drawString(texto, (float) (x - medidas.stringWidth(texto) / 2.0), (float) (y + 1.0));
    }

    /** Clasificación visual de una arista. */
    private enum Clase {
        /** Transición de la cadena principal hacia q18. */
        CADENA,
        /** Atajo de la parte local (2–4 iniciales, 2–3 dígitos). */
        ATAJO,
        /** Transición hacia el sumidero. */
        RECHAZO
    }

    /**
     * Una transición del diagrama.
     *
     * @param desde    estado de origen
     * @param hasta    estado de destino
     * @param etiqueta texto de la transición
     * @param clase    clasificación visual
     */
    private record Arista(Estado desde, Estado hasta, String etiqueta, Clase clase) {
    }

    /**
     * Devuelve las transiciones dibujadas, útil para pruebas y documentación.
     *
     * @return pares {@code origen->destino} con su etiqueta
     */
    public Map<String, String> transicionesDibujadas() {
        Map<String, String> mapa = new LinkedHashMap<>();
        for (Arista arista : aristas) {
            mapa.put(clave(arista.desde(), arista.hasta()), arista.etiqueta());
        }
        return mapa;
    }
}
