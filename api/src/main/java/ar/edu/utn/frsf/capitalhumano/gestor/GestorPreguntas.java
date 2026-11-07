package ar.edu.utn.frsf.capitalhumano.gestor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.utn.frsf.capitalhumano.dao.CompetenciaDAO;
import ar.edu.utn.frsf.capitalhumano.dao.FactorDAO;
import ar.edu.utn.frsf.capitalhumano.dao.PreguntaDAO;
import ar.edu.utn.frsf.capitalhumano.dominio.Factor;
import ar.edu.utn.frsf.capitalhumano.dominio.Pregunta;
import ar.edu.utn.frsf.capitalhumano.dto.CompetenciaDTO;
import ar.edu.utn.frsf.capitalhumano.dto.FactorDTO;
import ar.edu.utn.frsf.capitalhumano.dto.FiltroPreguntaDTO;
import ar.edu.utn.frsf.capitalhumano.dto.PonderacionOpcionDTO;
import ar.edu.utn.frsf.capitalhumano.dto.PreguntaDetalleDTO;
import ar.edu.utn.frsf.capitalhumano.dto.PreguntaDTO;

/** CU020 (gestionar preguntas) y CU021 (dar de alta pregunta). */
@Service
public class GestorPreguntas {

    /** Las ponderaciones de las opciones tienen que sumar 10 puntos. */
    private static final double SUMA_PONDERACIONES = 10;
    private static final int MINIMO_OPCIONES = 2;

    private static final int LARGO_NOMBRE = 40;
    private static final int LARGO_ENUNCIADO = 80;
    private static final int LARGO_DESCRIPCION = 100;
    private static final int LARGO_DESCRIPCION_OPCION = 50;

    private final PreguntaDAO preguntaDAO;
    private final FactorDAO factorDAO;
    private final CompetenciaDAO competenciaDAO;

    public GestorPreguntas(PreguntaDAO preguntaDAO, FactorDAO factorDAO, CompetenciaDAO competenciaDAO) {
        this.preguntaDAO = preguntaDAO;
        this.factorDAO = factorDAO;
        this.competenciaDAO = competenciaDAO;
    }

    // ---------- CU020 ----------

    public List<PreguntaDTO> buscarPreguntas(FiltroPreguntaDTO filtros) {
        return preguntaDAO
                .buscarPorFiltros(filtros.codigoCompetencia(), filtros.codigoFactor(), filtros.nombre())
                .stream()
                .map(p -> new PreguntaDTO(p.getCodigo(),
                        p.getFactor().getCompetencia().getNombre(),
                        p.getFactor().getNombre(),
                        p.getNombre()))
                .toList();
    }

    // ---------- CU021 ----------

    public List<CompetenciaDTO> obtenerCompetencias() {
        return competenciaDAO.buscarTodas().stream()
                .map(c -> new CompetenciaDTO(c.getCodigo(), c.getNombre()))
                .toList();
    }

    /** Al elegir una competencia se cargan sus factores. */
    public List<FactorDTO> obtenerFactores(String codigoCompetencia) {
        return factorDAO.buscarPorCompetencia(codigoCompetencia).stream()
                .map(f -> new FactorDTO(f.getCodigo(), f.getNombre(), f.getCompetencia().getNombre()))
                .toList();
    }

    /**
     * Da de alta la pregunta con sus opciones. Devuelve TODOS los errores
     * juntos; si la lista vuelve vacía, la pregunta quedó guardada.
     */
    @Transactional
    public List<String> altaPregunta(PreguntaDetalleDTO datos) {
        List<String> errores = validarDatos(datos);

        Factor factor = null;
        if (datos.codigoFactor() != null && !datos.codigoFactor().isBlank()) {
            factor = factorDAO.buscarPorCodigo(datos.codigoFactor());
            if (factor == null) {
                errores.add("El factor seleccionado no existe");
            }
        }

        errores.addAll(erroresDeOpciones(datos.opciones()));

        if (factor != null && esTextoCargado(datos.nombrePregunta()) && esTextoCargado(datos.enunciado())
                && existePregunta(datos.codigoCompetencia(), datos.codigoFactor(),
                        datos.nombrePregunta().trim(), datos.enunciado().trim())) {
            errores.add("Ya existe una pregunta definida para esta competencia y factor");
        }

        if (!errores.isEmpty()) {
            return errores;
        }

        Pregunta pregunta = new Pregunta(datos.nombrePregunta().trim(), datos.enunciado().trim(),
                textoOpcional(datos.descripcion()), factor);
        for (PonderacionOpcionDTO opcion : datos.opciones()) {
            pregunta.agregarOpcion(opcion.orden(), opcion.descripcion().trim(), opcion.ponderacion());
        }
        preguntaDAO.guardar(pregunta);
        return errores;
    }

    public boolean existePregunta(String codigoCompetencia, String codigoFactor, String nombre,
            String enunciado) {
        return preguntaDAO.existePregunta(codigoCompetencia, codigoFactor, nombre, enunciado);
    }

    /** Obligatorios y longitud máxima de cada campo. */
    private List<String> validarDatos(PreguntaDetalleDTO datos) {
        List<String> errores = new ArrayList<>();

        List<String> faltantes = new ArrayList<>();
        if (!esTextoCargado(datos.codigoCompetencia())) {
            faltantes.add("Competencia");
        }
        if (!esTextoCargado(datos.codigoFactor())) {
            faltantes.add("Factor");
        }
        if (!esTextoCargado(datos.nombrePregunta())) {
            faltantes.add("Nombre de Pregunta");
        }
        if (!esTextoCargado(datos.enunciado())) {
            faltantes.add("Pregunta");
        }
        if (!faltantes.isEmpty()) {
            errores.add("Los siguientes campos son obligatorios: " + String.join(" · ", faltantes));
        }

        List<String> largos = new ArrayList<>();
        if (superaLargo(datos.nombrePregunta(), LARGO_NOMBRE)) {
            largos.add("Nombre de Pregunta (máximo " + LARGO_NOMBRE + ")");
        }
        if (superaLargo(datos.enunciado(), LARGO_ENUNCIADO)) {
            largos.add("Pregunta (máximo " + LARGO_ENUNCIADO + ")");
        }
        if (superaLargo(datos.descripcion(), LARGO_DESCRIPCION)) {
            largos.add("Descripción (máximo " + LARGO_DESCRIPCION + ")");
        }
        if (!largos.isEmpty()) {
            errores.add("Los siguientes campos superan la longitud máxima: " + String.join(" · ", largos));
        }
        return errores;
    }

    /** Las ponderaciones suman 10 y ninguna es negativa. */
    public boolean validarPonderaciones(List<PonderacionOpcionDTO> opciones) {
        if (opciones == null || opciones.size() < MINIMO_OPCIONES) {
            return false;
        }
        double suma = 0;
        for (PonderacionOpcionDTO opcion : opciones) {
            if (opcion.ponderacion() == null || opcion.ponderacion() < 0) {
                return false;
            }
            suma += opcion.ponderacion();
        }
        return Math.abs(suma - SUMA_PONDERACIONES) < 0.001;
    }

    /** Mensajes concretos de la grilla de opciones, todos juntos. */
    private List<String> erroresDeOpciones(List<PonderacionOpcionDTO> opciones) {
        List<String> errores = new ArrayList<>();
        if (opciones.size() < MINIMO_OPCIONES) {
            errores.add("Debe cargar al menos " + MINIMO_OPCIONES + " opciones de respuesta");
            return errores;
        }

        boolean hayDescripcionVacia = false;
        boolean hayLarga = false;
        boolean hayNegativaOVacia = false;
        double suma = 0;
        Set<String> descripciones = new HashSet<>();
        Set<Integer> ordenes = new HashSet<>();
        boolean hayDescripcionRepetida = false;
        boolean hayOrdenRepetido = false;

        for (PonderacionOpcionDTO opcion : opciones) {
            if (!esTextoCargado(opcion.descripcion())) {
                hayDescripcionVacia = true;
            } else {
                if (opcion.descripcion().trim().length() > LARGO_DESCRIPCION_OPCION) {
                    hayLarga = true;
                }
                if (!descripciones.add(opcion.descripcion().trim().toUpperCase())) {
                    hayDescripcionRepetida = true;
                }
            }
            if (!ordenes.add(opcion.orden())) {
                hayOrdenRepetido = true;
            }
            if (opcion.ponderacion() == null || opcion.ponderacion() < 0) {
                hayNegativaOVacia = true;
            } else {
                suma += opcion.ponderacion();
            }
        }

        if (hayDescripcionVacia) {
            errores.add("Todas las opciones de respuesta deben tener descripción");
        }
        if (hayLarga) {
            errores.add("La descripción de una opción no puede superar los "
                    + LARGO_DESCRIPCION_OPCION + " caracteres");
        }
        if (hayDescripcionRepetida) {
            errores.add("No puede repetir la descripción de una opción de respuesta");
        }
        if (hayOrdenRepetido) {
            errores.add("No puede repetir el orden de una opción de respuesta");
        }
        if (hayNegativaOVacia) {
            errores.add("Las ponderaciones deben ser números mayores o iguales a 0");
        } else if (!validarPonderaciones(opciones)) {
            errores.add("Las ponderaciones deben sumar exactamente " + (int) SUMA_PONDERACIONES
                    + " puntos. Suma actual: " + formatear(suma));
        }
        return errores;
    }

    private boolean esTextoCargado(String valor) {
        return valor != null && !valor.isBlank();
    }

    private boolean superaLargo(String valor, int maximo) {
        return valor != null && valor.trim().length() > maximo;
    }

    private String textoOpcional(String valor) {
        return esTextoCargado(valor) ? valor.trim() : null;
    }

    private String formatear(double valor) {
        return valor == Math.floor(valor) ? String.valueOf((long) valor) : String.valueOf(valor);
    }
}
