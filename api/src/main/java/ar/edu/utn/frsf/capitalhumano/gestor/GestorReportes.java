package ar.edu.utn.frsf.capitalhumano.gestor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import ar.edu.utn.frsf.capitalhumano.config.NoEncontradoException;
import ar.edu.utn.frsf.capitalhumano.config.ReglaNegocioException;
import ar.edu.utn.frsf.capitalhumano.dao.CuestionarioDAO;
import ar.edu.utn.frsf.capitalhumano.dao.EvaluacionDAO;
import ar.edu.utn.frsf.capitalhumano.dao.PuestoDAO;
import ar.edu.utn.frsf.capitalhumano.dominio.Candidato;
import ar.edu.utn.frsf.capitalhumano.dominio.Cuestionario;
import ar.edu.utn.frsf.capitalhumano.dominio.EstadoCuestionario;
import ar.edu.utn.frsf.capitalhumano.dominio.Evaluacion;
import ar.edu.utn.frsf.capitalhumano.dominio.Puesto;
import ar.edu.utn.frsf.capitalhumano.dto.ArchivoDTO;
import ar.edu.utn.frsf.capitalhumano.dto.EvaluacionDTO;
import ar.edu.utn.frsf.capitalhumano.dto.OrdenMeritoDTO;
import ar.edu.utn.frsf.capitalhumano.dto.PuestoDTO;
import ar.edu.utn.frsf.capitalhumano.externo.IImpresora;

/** CU028: emitir el orden de mérito de un puesto. */
@Service
public class GestorReportes {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_ARCHIVO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");

    private final PuestoDAO puestoDAO;
    private final EvaluacionDAO evaluacionDAO;
    private final CuestionarioDAO cuestionarioDAO;
    private final IImpresora impresora;

    public GestorReportes(PuestoDAO puestoDAO, EvaluacionDAO evaluacionDAO,
            CuestionarioDAO cuestionarioDAO, IImpresora impresora) {
        this.puestoDAO = puestoDAO;
        this.evaluacionDAO = evaluacionDAO;
        this.cuestionarioDAO = cuestionarioDAO;
        this.impresora = impresora;
    }

    public List<PuestoDTO> buscarPuestos(String codigo, String nombre, String empresa) {
        return puestoDAO.buscarPorFiltros(codigo, nombre, empresa).stream()
                .map(p -> new PuestoDTO(p.getCodigo(), p.getNombre(), p.getEmpresa()))
                .toList();
    }

    /** Paso 6.A: si el puesto no tiene evaluaciones, la lista vuelve vacía. */
    public List<EvaluacionDTO> obtenerEvaluaciones(String codigoPuesto) {
        Puesto puesto = buscarPuesto(codigoPuesto);
        return evaluacionDAO.buscarPorPuesto(puesto).stream()
                .map(e -> new EvaluacionDTO(e.getIdEvaluacion(), e.getFechaCreacion()))
                .toList();
    }

    /**
     * Arma el listado: primero los aptos ordenados por puntaje y después el
     * resto, agrupado por estado del cuestionario. Apto es quien completó el
     * cuestionario y alcanzó la ponderación mínima de CADA competencia
     * requerida del puesto.
     */
    public List<OrdenMeritoDTO> generarOrdenMerito(String codigoPuesto, List<Integer> evaluaciones) {
        Puesto puesto = buscarPuesto(codigoPuesto);
        if (evaluaciones == null || evaluaciones.isEmpty()) {
            throw new ReglaNegocioException("Debe seleccionar al menos una evaluación");
        }

        List<OrdenMeritoDTO> aptos = new ArrayList<>();
        List<OrdenMeritoDTO> noAptos = new ArrayList<>();

        for (Evaluacion evaluacion : evaluacionDAO.buscarPorPuesto(puesto)) {
            if (!evaluaciones.contains(evaluacion.getIdEvaluacion())) {
                continue;
            }
            for (Cuestionario cuestionario : cuestionarioDAO.buscarPorEvaluacion(evaluacion)) {
                String estado = cuestionario.obtenerEstado();
                if (EstadoCuestionario.COMPLETO.name().equals(estado)) {
                    float puntaje = cuestionario.obtenerPuntajeTotal();
                    boolean apto = cuestionario.cumpleMinimos(puesto.getCompetenciasRequeridas());
                    OrdenMeritoDTO fila = aFila(cuestionario, estado, puntaje, apto);
                    if (apto) {
                        aptos.add(fila);
                    } else {
                        noAptos.add(fila);
                    }
                } else {
                    noAptos.add(aFila(cuestionario, estado, null, false));
                }
            }
        }

        aptos.sort(Comparator.comparing(OrdenMeritoDTO::puntajeObtenido).reversed());
        noAptos.sort(Comparator.comparing(OrdenMeritoDTO::estadoCuestionario)
                .thenComparing(f -> f.puntajeObtenido() == null ? -1f : f.puntajeObtenido(),
                        Comparator.reverseOrder()));

        List<OrdenMeritoDTO> ordenMerito = new ArrayList<>(aptos);
        ordenMerito.addAll(noAptos);
        return ordenMerito;
    }

    private OrdenMeritoDTO aFila(Cuestionario cuestionario, String estado, Float puntaje,
            boolean apto) {
        Candidato candidato = cuestionario.getCandidato();
        return new OrdenMeritoDTO(
                candidato.getTipoDocumento().name(),
                candidato.getNroDocumento(),
                candidato.getApellido(),
                candidato.getNombre(),
                puntaje,
                estado,
                cuestionario.getFechaHoraComienzo(),
                cuestionario.getFechaHoraFin(),
                cuestionario.getUltimoIngreso(),
                cuestionario.getCantidadAccesos(),
                apto);
    }

    /**
     * Manda el listado a la impresora. En la web eso es generar el PDF que el
     * navegador descarga o imprime.
     */
    public ArchivoDTO imprimirOrdenMerito(List<OrdenMeritoDTO> ordenMerito, String nombrePuesto) {
        List<List<String>> reporte = new ArrayList<>();
        reporte.add(List.of("Pos.", "Tipo Doc.", "Nro Documento", "Apellido", "Nombre", "Puntaje",
                "Estado", "Fecha Inicio", "Fecha Fin", "Último ingreso", "Accesos"));

        int posicion = 0;
        for (OrdenMeritoDTO fila : ordenMerito) {
            posicion = fila.apto() ? posicion + 1 : posicion;
            reporte.add(List.of(
                    fila.apto() ? String.valueOf(posicion) : "-",
                    fila.tipoDocumento(),
                    fila.nroDocumento(),
                    fila.apellido(),
                    fila.nombre(),
                    fila.puntajeObtenido() == null ? "-" : String.format("%.2f", fila.puntajeObtenido()),
                    fila.estadoCuestionario().replace('_', ' '),
                    formatear(fila.fechaInicio()),
                    formatear(fila.fechaFin()),
                    formatear(fila.ultimoIngreso()),
                    String.valueOf(fila.cantidadAccesos())));
        }

        String titulo = "Orden de Mérito - " + nombrePuesto;
        String nombreArchivo = LocalDateTime.now().format(FORMATO_ARCHIVO) + "-Orden-de-Merito.pdf";
        try {
            Path destino = Files.createTempDirectory("capital-humano").resolve(nombreArchivo);
            impresora.imprimir(reporte, titulo, destino.toString());
            byte[] contenido = Files.readAllBytes(destino);
            Files.deleteIfExists(destino);
            return new ArchivoDTO(nombreArchivo, contenido);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar el listado", e);
        }
    }

    private String formatear(LocalDateTime fecha) {
        return fecha == null ? "-" : fecha.format(FORMATO_FECHA);
    }

    private Puesto buscarPuesto(String codigoPuesto) {
        Puesto puesto = puestoDAO.buscarPorCodigo(codigoPuesto);
        if (puesto == null) {
            throw new NoEncontradoException("No existe el puesto " + codigoPuesto);
        }
        return puesto;
    }
}
