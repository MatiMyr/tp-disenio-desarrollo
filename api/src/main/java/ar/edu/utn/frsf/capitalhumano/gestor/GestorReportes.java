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
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import ar.edu.utn.frsf.capitalhumano.config.NoEncontradoException;
import ar.edu.utn.frsf.capitalhumano.config.ReglaNegocioException;
import ar.edu.utn.frsf.capitalhumano.dao.CuestionarioDAO;
import ar.edu.utn.frsf.capitalhumano.dao.EvaluacionDAO;
import ar.edu.utn.frsf.capitalhumano.dao.PuestoDAO;
import ar.edu.utn.frsf.capitalhumano.dominio.Candidato;
import ar.edu.utn.frsf.capitalhumano.dominio.Consultor;
import ar.edu.utn.frsf.capitalhumano.dominio.Cuestionario;
import ar.edu.utn.frsf.capitalhumano.dominio.EstadoCuestionario;
import ar.edu.utn.frsf.capitalhumano.dominio.Evaluacion;
import ar.edu.utn.frsf.capitalhumano.dominio.Puesto;
import ar.edu.utn.frsf.capitalhumano.dto.ArchivoDTO;
import ar.edu.utn.frsf.capitalhumano.dto.EvaluacionDTO;
import ar.edu.utn.frsf.capitalhumano.dto.InformeDTO;
import ar.edu.utn.frsf.capitalhumano.dto.OrdenMeritoDTO;
import ar.edu.utn.frsf.capitalhumano.dto.ParDTO;
import ar.edu.utn.frsf.capitalhumano.dto.PuestoDTO;
import ar.edu.utn.frsf.capitalhumano.dto.SeccionInformeDTO;
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
                cuestionario.getFechaHoraCreacion(),
                cuestionario.getFechaHoraComienzo(),
                cuestionario.getFechaHoraFin(),
                cuestionario.getUltimoIngreso(),
                cuestionario.getCantidadAccesos(),
                apto);
    }

    /**
     * Manda el listado a la impresora. En la web eso es generar el PDF que el
     * navegador descarga o imprime. Los aptos van ordenados por puntaje y el
     * resto agrupado por estado del cuestionario.
     */
    public ArchivoDTO imprimirOrdenMerito(List<OrdenMeritoDTO> ordenMerito, String codigoPuesto,
            List<Integer> evaluaciones, Consultor consultor) {
        Puesto puesto = buscarPuesto(codigoPuesto);
        String emitidoPor = consultor == null ? "-" : consultor.getNombreUsuario();
        String ahora = LocalDateTime.now().format(FORMATO_FECHA);

        List<OrdenMeritoDTO> aptos = ordenMerito.stream().filter(OrdenMeritoDTO::apto).toList();
        List<OrdenMeritoDTO> completos = porEstado(ordenMerito, EstadoCuestionario.COMPLETO, false);
        List<OrdenMeritoDTO> incompletos = porEstado(ordenMerito, EstadoCuestionario.INCOMPLETO, false);
        List<OrdenMeritoDTO> sinContestar = porEstado(ordenMerito, EstadoCuestionario.SIN_CONTESTAR, false);

        List<ParDTO> datos = List.of(
                new ParDTO("Puesto / función:", puesto.getNombre()),
                new ParDTO("Empresa:", puesto.getEmpresa()),
                new ParDTO("Código función:", puesto.getCodigo()),
                new ParDTO("Fecha de generación:", ahora),
                new ParDTO("Evaluaciones incluidas:", textoEvaluaciones(puesto, evaluaciones)),
                new ParDTO("Generado por:", emitidoPor));

        List<SeccionInformeDTO> secciones = new ArrayList<>();
        secciones.add(seccionAptos(aptos, puesto));
        secciones.add(seccionCompletosNoAptos(completos));
        secciones.add(seccionIncompletos(incompletos));
        secciones.add(seccionSinContestar(sinContestar));

        int total = ordenMerito.size();
        int cantidadAptos = aptos.size();
        List<ParDTO> resumen = List.of(
                new ParDTO("Total candidatos", String.valueOf(total)),
                new ParDTO("Aptos", String.valueOf(cantidadAptos)),
                new ParDTO("No aptos", String.valueOf(total - cantidadAptos)),
                new ParDTO("Tasa de aptitud",
                        total == 0 ? "-" : Math.round(cantidadAptos * 100.0 / total) + " %"));

        InformeDTO informe = new InformeDTO(
                "Orden de mérito",
                "Reporte de clasificación de candidatos evaluados para el puesto",
                "OM-" + puesto.getCodigo() + "-" + LocalDateTime.now().format(FORMATO_ARCHIVO),
                ahora, datos, secciones, resumen, emitidoPor,
                "EvalPro · Orden de mérito · CU028");

        String nombreArchivo = LocalDateTime.now().format(FORMATO_ARCHIVO) + "-Orden-de-Merito.pdf";
        try {
            Path destino = Files.createTempDirectory("capital-humano").resolve(nombreArchivo);
            impresora.imprimir(informe, destino.toString());
            byte[] contenido = Files.readAllBytes(destino);
            Files.deleteIfExists(destino);
            return new ArchivoDTO(nombreArchivo, contenido);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar el listado", e);
        }
    }

    private List<OrdenMeritoDTO> porEstado(List<OrdenMeritoDTO> filas, EstadoCuestionario estado,
            boolean apto) {
        return filas.stream()
                .filter(f -> f.apto() == apto && estado.name().equals(f.estadoCuestionario()))
                .toList();
    }

    private String textoEvaluaciones(Puesto puesto, List<Integer> evaluaciones) {
        boolean todas = evaluacionDAO.buscarPorPuesto(puesto).size() == evaluaciones.size();
        String listado = evaluaciones.stream().map(String::valueOf).collect(Collectors.joining(", "));
        return todas ? "TODAS (" + listado + ")" : listado;
    }

    private SeccionInformeDTO seccionAptos(List<OrdenMeritoDTO> aptos, Puesto puesto) {
        List<List<String>> filas = new ArrayList<>();
        int posicion = 1;
        for (OrdenMeritoDTO fila : aptos) {
            filas.add(List.of(String.valueOf(posicion++), fila.tipoDocumento(), fila.nroDocumento(),
                    fila.apellido(), fila.nombre(), puntaje(fila), formatear(fila.fechaFin()),
                    String.valueOf(fila.cantidadAccesos())));
        }
        return new SeccionInformeDTO("Candidatos aptos",
                "Alcanzan la ponderación mínima de cada competencia requerida ("
                        + minimosDelPuesto(puesto) + "). Ordenados por puntaje.",
                List.of("Pos.", "Tipo doc.", "Documento", "Apellido", "Nombre", "Puntaje",
                        "Fecha fin", "Accesos"),
                filas);
    }

    private SeccionInformeDTO seccionCompletosNoAptos(List<OrdenMeritoDTO> completos) {
        List<List<String>> filas = new ArrayList<>();
        for (OrdenMeritoDTO fila : completos) {
            filas.add(List.of(fila.tipoDocumento(), fila.nroDocumento(), fila.apellido(),
                    fila.nombre(), puntaje(fila), formatear(fila.fechaInicio()),
                    formatear(fila.fechaFin()), String.valueOf(fila.cantidadAccesos())));
        }
        return new SeccionInformeDTO("No aptos · cuestionario completo",
                "Completaron el cuestionario pero no alcanzan el mínimo en alguna competencia.",
                List.of("Tipo doc.", "Documento", "Apellido", "Nombre", "Puntaje", "Fecha inicio",
                        "Fecha fin", "Accesos"),
                filas);
    }

    private SeccionInformeDTO seccionIncompletos(List<OrdenMeritoDTO> incompletos) {
        List<List<String>> filas = new ArrayList<>();
        for (OrdenMeritoDTO fila : incompletos) {
            filas.add(List.of(fila.tipoDocumento(), fila.nroDocumento(), fila.apellido(),
                    fila.nombre(), formatear(fila.fechaInicio()), formatear(fila.ultimoIngreso()),
                    String.valueOf(fila.cantidadAccesos())));
        }
        return new SeccionInformeDTO("No aptos · cuestionario incompleto",
                "Comenzaron el cuestionario y no lo terminaron: sin puntaje.",
                List.of("Tipo doc.", "Documento", "Apellido", "Nombre", "Fecha inicio",
                        "Último ingreso", "Accesos"),
                filas);
    }

    private SeccionInformeDTO seccionSinContestar(List<OrdenMeritoDTO> sinContestar) {
        List<List<String>> filas = new ArrayList<>();
        for (OrdenMeritoDTO fila : sinContestar) {
            filas.add(List.of(fila.tipoDocumento(), fila.nroDocumento(), fila.apellido(),
                    fila.nombre(), formatear(fila.fechaCreacion()),
                    "Sin registro de acceso al sistema"));
        }
        return new SeccionInformeDTO("No aptos · sin contestar",
                "No ingresaron al cuestionario antes de que venciera el plazo.",
                List.of("Tipo doc.", "Documento", "Apellido", "Nombre", "Fecha de envío",
                        "Observación"),
                filas);
    }

    private String minimosDelPuesto(Puesto puesto) {
        return puesto.getCompetenciasRequeridas().stream()
                .map(cr -> cr.getCompetencia().getNombre() + " "
                        + String.format("%.1f", cr.getPonderacionMinima()))
                .collect(Collectors.joining(" · "));
    }

    private String puntaje(OrdenMeritoDTO fila) {
        return fila.puntajeObtenido() == null ? "-" : String.format("%.2f", fila.puntajeObtenido());
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
