package ar.edu.utn.frsf.capitalhumano.gestor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.utn.frsf.capitalhumano.config.ErroresValidacionException;
import ar.edu.utn.frsf.capitalhumano.config.NoEncontradoException;
import ar.edu.utn.frsf.capitalhumano.config.ReglaNegocioException;
import ar.edu.utn.frsf.capitalhumano.dao.CuestionarioDAO;
import ar.edu.utn.frsf.capitalhumano.dao.IConfiguracionDAO;
import ar.edu.utn.frsf.capitalhumano.dominio.BloquePreguntas;
import ar.edu.utn.frsf.capitalhumano.dominio.Cuestionario;
import ar.edu.utn.frsf.capitalhumano.dominio.EstadoCuestionario;
import ar.edu.utn.frsf.capitalhumano.dominio.PreguntaCuestionario;
import ar.edu.utn.frsf.capitalhumano.dto.BloqueRespondidoDTO;
import ar.edu.utn.frsf.capitalhumano.dto.CandidatoCuestionarioDTO;
import ar.edu.utn.frsf.capitalhumano.dto.CuestionarioWizardDTO;
import ar.edu.utn.frsf.capitalhumano.dto.ItemOpcionRespuestaDTO;
import ar.edu.utn.frsf.capitalhumano.dto.PreguntaCuestionarioDTO;
import ar.edu.utn.frsf.capitalhumano.dto.RespuestaSeleccionadaDTO;

/** CU026: el candidato completa su cuestionario. */
@Service
public class GestorCuestionarios {

    private static final String ERROR_VENCIDO = "El cuestionario venció";
    private static final String ERROR_ACCESOS = "Superó la cantidad máxima de accesos";

    private final CuestionarioDAO cuestionarioDAO;
    private final IConfiguracionDAO configuracionDAO;

    public GestorCuestionarios(CuestionarioDAO cuestionarioDAO, IConfiguracionDAO configuracionDAO) {
        this.cuestionarioDAO = cuestionarioDAO;
        this.configuracionDAO = configuracionDAO;
    }

    /**
     * Recupera el cuestionario y controla si venció: el plazo del estado activo
     * (1.B) o el tiempo máximo desde el primer ingreso (1.A). Si venció, guarda
     * el cambio de estado con la fecha y hora.
     */
    @Transactional
    public CandidatoCuestionarioDTO recuperarCuestionario(int idCuestionario) {
        Cuestionario cuestionario = buscar(idCuestionario);
        cuestionario.verificarVencimiento();
        String estado = cuestionario.obtenerEstado();

        if (EstadoCuestionario.INCOMPLETO.name().equals(estado)
                || EstadoCuestionario.SIN_CONTESTAR.name().equals(estado)) {
            cuestionarioDAO.actualizar(cuestionario);
        }

        return new CandidatoCuestionarioDTO(
                cuestionario.getCandidato().getApellido() + ", " + cuestionario.getCandidato().getNombre(),
                cuestionario.getIdCuestionario(),
                estado,
                minutosRestantes(cuestionario),
                configuracionDAO.obtenerInstrucciones());
    }

    /**
     * Primer ingreso: arma los bloques con la copia del modelo. Reingreso:
     * registra el acceso y devuelve el bloque que falta, en el mismo orden.
     * Si el estado queda Incompleto por exceso de accesos, el cambio igual se
     * guarda (por eso la excepción no hace rollback).
     */
    @Transactional(noRollbackFor = ReglaNegocioException.class)
    public CuestionarioWizardDTO comenzar(int idCuestionario) {
        Cuestionario cuestionario = buscar(idCuestionario);
        if (!cuestionario.estaActivo()) {
            throw new ReglaNegocioException(ERROR_VENCIDO);
        }

        cuestionario.ingresar(configuracionDAO.obtenerCantidadPreguntasPorBloque());
        cuestionarioDAO.actualizar(cuestionario);

        if (EstadoCuestionario.INCOMPLETO.name().equals(cuestionario.obtenerEstado())) {
            throw new ReglaNegocioException(ERROR_ACCESOS);
        }
        return armarWizard(cuestionario);
    }

    /**
     * Guarda las respuestas del bloque. Si es el último, el cuestionario calcula
     * sus resultados y pasa a Completo.
     */
    @Transactional
    public CuestionarioWizardDTO responderBloque(BloqueRespondidoDTO bloqueRespondido) {
        Cuestionario cuestionario = buscar(bloqueRespondido.idCuestionario());
        if (!cuestionario.estaActivo()) {
            throw new ReglaNegocioException(ERROR_VENCIDO);
        }

        BloquePreguntas bloque = cuestionario.buscarBloque(bloqueRespondido.nroBloque());
        if (bloque == null) {
            throw new NoEncontradoException("No existe el bloque " + bloqueRespondido.nroBloque());
        }

        List<String> sinResponder = preguntasSinResponder(bloque, bloqueRespondido.respuestas());
        if (!sinResponder.isEmpty()) {
            // 6.A: se mantiene el bloque y lo que ya estaba respondido
            List<String> errores = new ArrayList<>();
            errores.add("Debe responder todas las preguntas del bloque");
            errores.addAll(sinResponder);
            throw new ErroresValidacionException(errores);
        }

        cuestionario.responderBloque(bloqueRespondido.nroBloque(), aMapa(bloqueRespondido.respuestas()));
        if (EstadoCuestionario.COMPLETO.name().equals(cuestionario.obtenerEstado())) {
            calcularResultados(cuestionario);
        }
        cuestionarioDAO.actualizar(cuestionario);
        return armarWizard(cuestionario);
    }

    /** El bloque está completo si todas sus preguntas tienen una opción elegida. */
    public boolean validarBloqueCompleto(BloqueRespondidoDTO bloqueRespondido) {
        Cuestionario cuestionario = buscar(bloqueRespondido.idCuestionario());
        BloquePreguntas bloque = cuestionario.buscarBloque(bloqueRespondido.nroBloque());
        return bloque != null && preguntasSinResponder(bloque, bloqueRespondido.respuestas()).isEmpty();
    }

    /**
     * El gestor no calcula nada por su cuenta: dispara el cálculo en el
     * cuestionario, que es quien tiene los datos.
     */
    public void calcularResultados(Cuestionario cuestionario) {
        cuestionario.calcularPuntaje();
    }

    /** Pasó el tiempo máximo contado desde el primer ingreso (CU026, obs. 2). */
    public boolean verificarTiempoMaximo(Cuestionario cuestionario) {
        if (cuestionario.getFechaHoraComienzo() == null) {
            return false;
        }
        return LocalDateTime.now()
                .isAfter(cuestionario.getFechaHoraComienzo().plusMinutes(cuestionario.getTiempoMaximo()));
    }

    // ---------- Apoyo ----------

    private Cuestionario buscar(int idCuestionario) {
        Cuestionario cuestionario = cuestionarioDAO.buscarPorId(idCuestionario);
        if (cuestionario == null) {
            throw new NoEncontradoException("No existe el cuestionario " + idCuestionario);
        }
        return cuestionario;
    }

    private List<String> preguntasSinResponder(BloquePreguntas bloque,
            List<RespuestaSeleccionadaDTO> respuestas) {
        Map<Integer, Integer> elegidas = aMapa(respuestas);
        List<String> faltantes = new ArrayList<>();
        int numero = 1;
        for (PreguntaCuestionario pregunta : bloque.getPreguntas()) {
            if (!elegidas.containsKey(pregunta.getIdPreguntaCuestionario())) {
                faltantes.add("Pregunta " + numero + ": " + pregunta.getPreguntaEvaluacion().getEnunciado());
            }
            numero++;
        }
        return faltantes;
    }

    private Map<Integer, Integer> aMapa(List<RespuestaSeleccionadaDTO> respuestas) {
        Map<Integer, Integer> elegidas = new HashMap<>();
        for (RespuestaSeleccionadaDTO respuesta : respuestas) {
            elegidas.put(respuesta.idPreguntaCuestionario(), respuesta.idItemOpcionElegido());
        }
        return elegidas;
    }

    private int minutosRestantes(Cuestionario cuestionario) {
        if (cuestionario.getFechaHoraComienzo() == null) {
            return cuestionario.getTiempoMaximo();
        }
        if (verificarTiempoMaximo(cuestionario)) {
            return 0;
        }
        long usados = Duration.between(cuestionario.getFechaHoraComienzo(), LocalDateTime.now()).toMinutes();
        return (int) Math.max(0, cuestionario.getTiempoMaximo() - usados);
    }

    /** Arma el paso del asistente con el primer bloque que falta responder. */
    private CuestionarioWizardDTO armarWizard(Cuestionario cuestionario) {
        int total = cuestionario.getBloques().size();
        BloquePreguntas bloque = cuestionario.primerBloqueSinResponder();
        if (bloque == null) {
            return new CuestionarioWizardDTO(cuestionario.getIdCuestionario(), 0, total, List.of());
        }
        List<PreguntaCuestionarioDTO> preguntas = bloque.getPreguntas().stream()
                .map(pregunta -> new PreguntaCuestionarioDTO(
                        pregunta.getIdPreguntaCuestionario(),
                        pregunta.getPreguntaEvaluacion().getEnunciado(),
                        pregunta.getPreguntaEvaluacion().getOpciones().stream()
                                .map(opcion -> new ItemOpcionRespuestaDTO(opcion.getIdOpcionEvaluacion(),
                                        opcion.getOrdenVisualizacion(), opcion.getDescripcion()))
                                .toList()))
                .toList();
        return new CuestionarioWizardDTO(cuestionario.getIdCuestionario(), bloque.getNroBloque(),
                total, preguntas);
    }
}
