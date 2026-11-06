package ni.edu.uni.afd.controlador;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import ni.edu.uni.afd.modelo.Automata;
import ni.edu.uni.afd.modelo.BancoCasos;
import ni.edu.uni.afd.modelo.CasoPrueba;
import ni.edu.uni.afd.modelo.ResultadoValidacion;
import ni.edu.uni.afd.vista.DialogoDiagrama;
import ni.edu.uni.afd.vista.DialogoTablaDelta;
import ni.edu.uni.afd.vista.PanelTraza;
import ni.edu.uni.afd.vista.PanelValidacion;
import ni.edu.uni.afd.vista.VentanaPrincipal;

/**
 * Controlador de la aplicación: conecta la vista con el autómata.
 *
 * <p>Toda la lógica de validación vive en {@link Automata}; este controlador solo recoge la entrada,
 * actualiza el veredicto, registra la fila de traza y muestra la alerta modal correspondiente.</p>
 */
public final class ControladorValidador {

    private static final Logger REGISTRO = Logger.getLogger(ControladorValidador.class.getName());

    private final VentanaPrincipal ventana;
    private final Automata automata = new Automata();
    private final BancoCasos banco;
    private final String origenBanco;

    private DialogoDiagrama diagrama;
    private ResultadoValidacion ultimaValidacion;

    /**
     * Crea el controlador y carga el banco de casos de prueba.
     *
     * @param ventana ventana principal de la aplicación
     */
    public ControladorValidador(VentanaPrincipal ventana) {
        this.ventana = ventana;
        String origen = BancoCasos.RECURSO;
        BancoCasos cargado;
        try {
            cargado = BancoCasos.desdeRecurso();
        } catch (IOException excepcion) {
            REGISTRO.log(Level.WARNING,
                    "No se pudo leer " + BancoCasos.RECURSO + "; se usa el " + BancoCasos.NOMBRE_INTEGRADO,
                    excepcion);
            cargado = BancoCasos.incluido();
            origen = BancoCasos.NOMBRE_INTEGRADO;
        }
        this.banco = cargado;
        this.origenBanco = origen;
    }

    /**
     * Conecta los componentes de la vista con las acciones del controlador.
     */
    public void conectar() {
        PanelValidacion validacion = ventana.getPanelValidacion();
        validacion.getBotonValidar().addActionListener(evento -> validarCampo());
        validacion.getCampo().addActionListener(evento -> validarCampo());
        ventana.getRootPane().setDefaultButton(validacion.getBotonValidar());

        PanelTraza traza = ventana.getPanelTraza();
        traza.getBotonPruebas().addActionListener(evento -> ejecutarBateria());
        traza.getBotonLimpiar().addActionListener(evento -> limpiar());

        ventana.getBotonDiagrama().addActionListener(evento -> mostrarDiagrama());
        ventana.getBotonTablaDelta().addActionListener(evento -> DialogoTablaDelta.mostrar(ventana));
    }

    /**
     * Valida la cadena que está en el campo de texto y actualiza la interfaz.
     */
    public void validarCampo() {
        String cadena = ventana.getPanelValidacion().getTextoIngresado();
        ResultadoValidacion resultado = automata.validar(cadena);

        ventana.getPanelValidacion().mostrarResultado(resultado);
        ventana.getPanelTraza().agregar(resultado, banco.esperadoPara(cadena));
        ultimaValidacion = resultado;
        sincronizarDiagrama();

        REGISTRO.log(Level.FINE, "Validación de «{0}»: {1} ({2})",
                new Object[] {cadena, resultado.tipo().etiqueta(), resultado.motivo()});

        mostrarAlerta(resultado);
    }

    private void mostrarAlerta(ResultadoValidacion resultado) {
        boolean aceptado = resultado.aceptado();
        String titulo = aceptado ? "ACEPTADO" : "RECHAZADO";
        int tipoMensaje = aceptado ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE;
        JOptionPane.showMessageDialog(ventana, textoAlerta(resultado), titulo, tipoMensaje);
    }

    private static String textoAlerta(ResultadoValidacion resultado) {
        String secuencia = resultado.caminoConFlechas();
        if (resultado.aceptado()) {
            return "ACEPTADO\n\n«" + resultado.cadenaParaMostrar() + "»"
                    + "\nEl correo cumple el formato institucional de la UNI."
                    + "\n\nSecuencia: " + secuencia;
        }
        return "RECHAZADO\n\n«" + resultado.cadenaParaMostrar() + "»"
                + "\n" + resultado.motivo()
                + "\n\nSecuencia: " + secuencia;
    }

    /**
     * Ejecuta la batería completa de casos de prueba y llena la tabla de traza.
     *
     * <p>Cualquier fallo inesperado se muestra en un diálogo con su causa: al ejecutar desde un
     * jar con {@code javaw} no hay consola donde leer la traza, de modo que un error silencioso se
     * confundiría con un botón que no responde.</p>
     */
    public void ejecutarBateria() {
        try {
            ejecutarBateriaInterna();
        } catch (RuntimeException excepcion) {
            REGISTRO.log(Level.SEVERE, "Falló la ejecución de la batería de pruebas", excepcion);
            JOptionPane.showMessageDialog(ventana,
                    "No se pudo ejecutar la batería de pruebas.\n\n"
                            + excepcion.getClass().getSimpleName() + ": " + excepcion.getMessage()
                            + "\n\nBanco de casos: " + origenBanco,
                    "Ejecutar pruebas", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ejecutarBateriaInterna() {
        PanelTraza traza = ventana.getPanelTraza();
        traza.limpiar();

        int total = banco.total();
        if (total == 0) {
            JOptionPane.showMessageDialog(ventana,
                    "El banco de casos está vacío (" + origenBanco + ").\n"
                            + "No hay casos que ejecutar.",
                    "Ejecutar pruebas", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int correctos = 0;
        for (CasoPrueba caso : banco.casos()) {
            ResultadoValidacion resultado = automata.validar(caso.cadena());
            traza.agregar(resultado, Boolean.valueOf(caso.esperado()));
            ultimaValidacion = resultado;
            if (resultado.aceptado() == caso.esperado()) {
                correctos++;
            }
        }
        sincronizarDiagrama();

        String resumen = correctos + "/" + total + " casos esperados correctos";
        ventana.getPanelValidacion().mostrarDetalle("Batería de pruebas: " + resumen);
        JOptionPane.showMessageDialog(ventana,
                "Se validaron " + total + " casos de " + origenBanco + ".\n" + resumen,
                "Ejecutar pruebas", JOptionPane.INFORMATION_MESSAGE);
        REGISTRO.log(Level.FINE, "Batería ejecutada: {0}", resumen);
    }

    /**
     * Abre —o trae al frente— la ventana que dibuja el AFD en pantalla.
     */
    public void mostrarDiagrama() {
        if (diagrama == null || !diagrama.isDisplayable()) {
            diagrama = new DialogoDiagrama(ventana);
            diagrama.actualizarTraza(ultimaValidacion);
        }
        diagrama.setVisible(true);
        diagrama.toFront();
    }

    private void sincronizarDiagrama() {
        if (diagrama != null && diagrama.isDisplayable()) {
            diagrama.actualizarTraza(ultimaValidacion);
        }
    }

    /**
     * Limpia la tabla de traza, el campo y el veredicto.
     */
    public void limpiar() {
        ventana.getPanelTraza().limpiar();
        ventana.getPanelValidacion().reiniciar();
        ultimaValidacion = null;
        sincronizarDiagrama();
    }

    /**
     * Origen de los casos que ejecuta el botón «Ejecutar pruebas».
     *
     * @return {@code /casos-prueba.csv} o {@link BancoCasos#NOMBRE_INTEGRADO}
     */
    public String getOrigenBanco() {
        return origenBanco;
    }

    /** @return el banco de casos cargado */
    public BancoCasos getBanco() {
        return banco;
    }
}
