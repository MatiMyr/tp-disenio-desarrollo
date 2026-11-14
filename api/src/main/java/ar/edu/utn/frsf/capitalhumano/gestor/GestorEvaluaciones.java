package ar.edu.utn.frsf.capitalhumano.gestor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.utn.frsf.capitalhumano.config.NoEncontradoException;
import ar.edu.utn.frsf.capitalhumano.config.ReglaNegocioException;
import ar.edu.utn.frsf.capitalhumano.dao.CandidatoDAO;
import ar.edu.utn.frsf.capitalhumano.dao.CuestionarioDAO;
import ar.edu.utn.frsf.capitalhumano.dao.EvaluacionDAO;
import ar.edu.utn.frsf.capitalhumano.dao.FactorDAO;
import ar.edu.utn.frsf.capitalhumano.dao.IConfiguracionDAO;
import ar.edu.utn.frsf.capitalhumano.dao.PuestoDAO;
import ar.edu.utn.frsf.capitalhumano.dominio.Candidato;
import ar.edu.utn.frsf.capitalhumano.dominio.CompetenciaRequerida;
import ar.edu.utn.frsf.capitalhumano.dominio.Consultor;
import ar.edu.utn.frsf.capitalhumano.dominio.Cuestionario;
import ar.edu.utn.frsf.capitalhumano.dominio.Evaluacion;
import ar.edu.utn.frsf.capitalhumano.dominio.Factor;
import ar.edu.utn.frsf.capitalhumano.dominio.Pregunta;
import ar.edu.utn.frsf.capitalhumano.dominio.Puesto;
import ar.edu.utn.frsf.capitalhumano.dominio.estado.EstadoActivo;
import ar.edu.utn.frsf.capitalhumano.dto.ArchivoDTO;
import ar.edu.utn.frsf.capitalhumano.dto.CandidatoClaveDTO;
import ar.edu.utn.frsf.capitalhumano.dto.CandidatoGestionDTO;
import ar.edu.utn.frsf.capitalhumano.dto.CompetenciaEvaluableDTO;
import ar.edu.utn.frsf.capitalhumano.externo.IExportadorExcel;

/** CU025: evaluar candidatos para un puesto. */
@Service
public class GestorEvaluaciones {

    /** Las claves de ingreso son de 8 caracteres, con letras y dígitos. */
    private static final int LARGO_CLAVE = 8;
    private static final String CARACTERES_CLAVE =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final SecureRandom AZAR = new SecureRandom();

    private static final String CLAVE_TIEMPO_MAXIMO = "tiempo_maximo_minutos";
    private static final String CLAVE_PLAZO_ACTIVO = "plazo_activo_dias";
    private static final String CLAVE_MAX_ACCESOS = "max_accesos";

    private static final DateTimeFormatter FORMATO_ARCHIVO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");

    private final CandidatoDAO candidatoDAO;
    private final PuestoDAO puestoDAO;
    private final EvaluacionDAO evaluacionDAO;
    private final CuestionarioDAO cuestionarioDAO;
    private final FactorDAO factorDAO;
    private final IConfiguracionDAO configuracionDAO;
    private final IExportadorExcel exportadorExcel;

    public GestorEvaluaciones(CandidatoDAO candidatoDAO, PuestoDAO puestoDAO,
            EvaluacionDAO evaluacionDAO, CuestionarioDAO cuestionarioDAO, FactorDAO factorDAO,
            IConfiguracionDAO configuracionDAO, IExportadorExcel exportadorExcel) {
        this.candidatoDAO = candidatoDAO;
        this.puestoDAO = puestoDAO;
        this.evaluacionDAO = evaluacionDAO;
        this.cuestionarioDAO = cuestionarioDAO;
        this.factorDAO = factorDAO;
        this.configuracionDAO = configuracionDAO;
        this.exportadorExcel = exportadorExcel;
    }

    // ---------- Paso 6.A: candidatos que ya tienen cuestionario ----------

    /** Un candidato no puede evaluarse de nuevo si ya tiene uno activo o en proceso. */
    public boolean tieneCuestionarioActivo(int nroCandidato) {
        Candidato candidato = candidatoDAO.buscarPorNroCandidato(nroCandidato);
        if (candidato == null) {
            return false;
        }
        return cuestionarioDAO.buscarActivoOEnProceso(candidato) != null;
    }

    // ---------- Paso 9: competencias del puesto ----------

    /**
     * Una competencia se puede evaluar si tiene al menos un factor con 2 o más
     * preguntas activas.
     */
    public List<CompetenciaEvaluableDTO> validarCompetenciasEvaluables(String codigoPuesto) {
        Puesto puesto = buscarPuesto(codigoPuesto);
        List<CompetenciaEvaluableDTO> competencias = new ArrayList<>();
        for (CompetenciaRequerida requerida : puesto.getCompetenciasRequeridas()) {
            boolean evaluable = factorDAO.buscarPorCompetencia(requerida.getCompetencia().getCodigo())
                    .stream()
                    .anyMatch(Factor::tieneMinimoPreguntas);
            competencias.add(new CompetenciaEvaluableDTO(requerida.getCompetencia().getNombre(),
                    requerida.getPonderacionMinima(), evaluable));
        }
        return competencias;
    }

    // ---------- Paso 11: claves de ingreso ----------

    /** Genera una clave por candidato. Todavía no guarda nada. */
    public List<CandidatoClaveDTO> generarClaves(List<CandidatoGestionDTO> candidatos) {
        return candidatos.stream()
                .map(c -> new CandidatoClaveDTO(c.nroCandidato(), c.apellido(), c.nombre(),
                        c.tipoDocumento(), c.nroDocumento(), generarClaveAleatoria()))
                .toList();
    }

    public String generarClaveAleatoria() {
        StringBuilder clave = new StringBuilder(LARGO_CLAVE);
        for (int i = 0; i < LARGO_CLAVE; i++) {
            clave.append(CARACTERES_CLAVE.charAt(AZAR.nextInt(CARACTERES_CLAVE.length())));
        }
        return clave.toString();
    }

    // ---------- Paso 13: generar la evaluación ----------

    /**
     * Crea la evaluación con su copia del modelo y un cuestionario activo por
     * candidato. Todo en una sola transacción: si algo falla, no se guarda nada.
     */
    @Transactional
    public void evaluarCandidatos(List<CandidatoClaveDTO> claves, String codigoPuesto,
            Consultor consultor) {
        if (claves == null || claves.isEmpty()) {
            throw new ReglaNegocioException("Debe seleccionar uno o más candidatos");
        }
        Puesto puesto = buscarPuesto(codigoPuesto);

        List<String> yaEvaluados = claves.stream()
                .filter(c -> tieneCuestionarioActivo(c.nroCandidato()))
                .map(c -> c.apellido() + ", " + c.nombre())
                .toList();
        if (!yaEvaluados.isEmpty()) {
            throw new ReglaNegocioException("Los candidatos " + String.join("; ", yaEvaluados)
                    + " no pueden ser evaluados nuevamente");
        }

        List<String> noEvaluables = validarCompetenciasEvaluables(codigoPuesto).stream()
                .filter(c -> !c.evaluable())
                .map(CompetenciaEvaluableDTO::nombreCompetencia)
                .toList();
        if (!noEvaluables.isEmpty()) {
            throw new ReglaNegocioException("Las siguientes competencias no pueden ser evaluadas: "
                    + String.join(", ", noEvaluables));
        }

        Evaluacion evaluacion = new Evaluacion(puesto, consultor);
        evaluacion.definirModelo(preguntasDelModelo(puesto));
        evaluacionDAO.guardar(evaluacion);

        generarCuestionarios(claves, evaluacion);
    }

    /** Las preguntas activas de los factores evaluables de cada competencia requerida. */
    private List<Pregunta> preguntasDelModelo(Puesto puesto) {
        List<Pregunta> preguntas = new ArrayList<>();
        for (CompetenciaRequerida requerida : puesto.getCompetenciasRequeridas()) {
            for (Factor factor : factorDAO.buscarPorCompetencia(requerida.getCompetencia().getCodigo())) {
                if (factor.tieneMinimoPreguntas()) {
                    preguntas.addAll(factor.obtenerPreguntasActivas());
                }
            }
        }
        return preguntas;
    }

    /**
     * Un cuestionario activo por candidato, con su clave y con el tiempo máximo,
     * el plazo activo y la cantidad de accesos que salen de la configuración.
     */
    public void generarCuestionarios(List<CandidatoClaveDTO> claves, Evaluacion evaluacion) {
        int tiempoMaximo = parametroEntero(CLAVE_TIEMPO_MAXIMO);
        int plazoActivo = parametroEntero(CLAVE_PLAZO_ACTIVO);
        int maxAccesos = parametroEntero(CLAVE_MAX_ACCESOS);

        for (CandidatoClaveDTO clave : claves) {
            Candidato candidato = candidatoDAO.buscarPorNroCandidato(clave.nroCandidato());
            if (candidato == null) {
                throw new NoEncontradoException("No existe el candidato " + clave.nroCandidato());
            }
            Cuestionario cuestionario = new Cuestionario(candidato, clave.clave(),
                    EstadoActivo.getInstancia());
            cuestionario.setTiempoMaximo(tiempoMaximo);
            cuestionario.setPlazoActivo(plazoActivo);
            cuestionario.setMaxAccesos(maxAccesos);
            evaluacion.agregarCuestionario(cuestionario);
            cuestionarioDAO.guardar(cuestionario);
        }
    }

    // ---------- Paso 17: exportar a Excel ----------

    /**
     * Arma la planilla con los candidatos y sus claves. En la web "elegir la
     * carpeta" es la descarga del navegador, así que el archivo se genera en una
     * carpeta temporal y se devuelve para descargarlo.
     */
    public ArchivoDTO exportarClavesExcel(List<CandidatoClaveDTO> claves, String carpeta,
            String nombrePuesto) {
        if (claves == null || claves.isEmpty()) {
            throw new ReglaNegocioException("No hay candidatos para exportar");
        }
        List<List<String>> datos = new ArrayList<>();
        datos.add(List.of("Apellido", "Nombre", "Tipo Documento", "Nro Documento", "Clave de Ingreso"));
        for (CandidatoClaveDTO clave : claves) {
            datos.add(List.of(clave.apellido(), clave.nombre(), clave.tipoDocumento(),
                    clave.nroDocumento(), clave.clave()));
        }

        String nombreArchivo = LocalDateTime.now().format(FORMATO_ARCHIVO) + "-"
                + normalizar(nombrePuesto) + ".xls";
        try {
            Path destino = (carpeta == null || carpeta.isBlank())
                    ? Files.createTempDirectory("capital-humano").resolve(nombreArchivo)
                    : Path.of(carpeta, nombreArchivo);
            exportadorExcel.exportar(datos, destino.toString());
            byte[] contenido = Files.readAllBytes(destino);
            Files.deleteIfExists(destino);
            return new ArchivoDTO(nombreArchivo, contenido);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar el archivo de claves", e);
        }
    }

    /** El nombre del archivo es aaaa-mm-dd-hh-mm-NombreFunción.xls. */
    private String normalizar(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "Evaluacion";
        }
        return java.text.Normalizer.normalize(nombre, java.text.Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .trim()
                .replaceAll("\\s+", "-");
    }

    private Puesto buscarPuesto(String codigoPuesto) {
        Puesto puesto = puestoDAO.buscarPorCodigo(codigoPuesto);
        if (puesto == null) {
            throw new NoEncontradoException("No existe el puesto " + codigoPuesto);
        }
        return puesto;
    }

    private int parametroEntero(String clave) {
        Object valor = configuracionDAO.obtenerParametro(clave);
        if (valor == null) {
            throw new IllegalStateException("Falta el parámetro de configuración " + clave);
        }
        return Integer.parseInt(valor.toString().trim());
    }
}
