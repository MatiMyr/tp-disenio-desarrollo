package ar.edu.utn.frsf.capitalhumano.dominio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ar.edu.utn.frsf.capitalhumano.dominio.estado.IEstadoCuestionario;

/**
 * Cuestionario que completa un candidato dentro de una evaluacion.
 * No decide con if/else sobre el estado: delega en su estado actual
 * (patron State, ver dominio.estado).
 */
public class Cuestionario {

    private int idCuestionario;
    private String clave;
    private LocalDateTime fechaHoraCreacion;
    private LocalDateTime fechaHoraComienzo;
    private LocalDateTime fechaHoraFin;
    private LocalDateTime ultimoIngreso;
    private int cantidadAccesos;
    private int tiempoMaximo;
    private int plazoActivo;
    private int maxAccesos;

    private Candidato candidato;
    private Evaluacion evaluacion;
    private IEstadoCuestionario estado;
    private List<BloquePreguntas> bloques = new ArrayList<>();
    private List<ResultadoCompetencia> resultados = new ArrayList<>();

    public Cuestionario() {
    }

    public Cuestionario(Candidato candidato, String clave, IEstadoCuestionario estado) {
        this.candidato = candidato;
        this.clave = clave;
        this.estado = estado;
        this.fechaHoraCreacion = LocalDateTime.now();
    }

    // ---------- Operaciones que delegan en el estado ----------

    public void ingresar(int tamBloque) {
        estado.ingresar(this, tamBloque);
    }

    public void responderBloque(int nroBloque, Map<Integer, Integer> respuestas) {
        estado.responderBloque(this, nroBloque, respuestas);
    }

    public void finalizar() {
        estado.finalizar(this);
    }

    public void verificarVencimiento() {
        estado.verificarVencimiento(this);
    }

    public String obtenerEstado() {
        return estado.obtenerEstado();
    }

    public void setEstado(IEstadoCuestionario nuevo) {
        this.estado = nuevo;
    }

    public IEstadoCuestionario getEstado() {
        return estado;
    }

    // ---------- Comportamiento propio ----------

    public void registrarAcceso() {
        cantidadAccesos++;
        ultimoIngreso = LocalDateTime.now();
    }

    /** Le queda al menos un acceso de los permitidos. */
    public boolean verificarAccesos() {
        return cantidadAccesos < maxAccesos;
    }

    public boolean estaActivo() {
        String actual = obtenerEstado();
        return EstadoCuestionario.ACTIVO.name().equals(actual)
                || EstadoCuestionario.EN_PROCESO.name().equals(actual);
    }

    /**
     * Elige 2 preguntas al azar por factor de la copia del modelo, mezcla todas
     * las preguntas entre si y las reparte en bloques, guardando el orden en que
     * se van a mostrar.
     */
    public void armarBloques(List<PreguntaEvaluacion> modelo, int tamBloque) {
        Map<String, List<PreguntaEvaluacion>> porFactor = new LinkedHashMap<>();
        for (PreguntaEvaluacion pe : modelo) {
            Factor f = pe.getFactor();
            if (f != null) {
                porFactor.computeIfAbsent(f.getCodigo(), k -> new ArrayList<>()).add(pe);
            }
        }

        List<PreguntaEvaluacion> elegidas = new ArrayList<>();
        for (List<PreguntaEvaluacion> delFactor : porFactor.values()) {
            if (delFactor.size() < Factor.MINIMO_PREGUNTAS) {
                continue;
            }
            List<PreguntaEvaluacion> copia = new ArrayList<>(delFactor);
            Collections.shuffle(copia);
            elegidas.addAll(copia.subList(0, Factor.MINIMO_PREGUNTAS));
        }
        Collections.shuffle(elegidas);

        bloques.clear();
        int orden = 1;
        BloquePreguntas bloque = null;
        for (PreguntaEvaluacion pe : elegidas) {
            if (bloque == null || bloque.getPreguntas().size() == tamBloque) {
                bloque = new BloquePreguntas(bloques.size() + 1);
                bloques.add(bloque);
            }
            bloque.agregarPregunta(new PreguntaCuestionario(pe, orden++));
        }
    }

    public BloquePreguntas buscarBloque(int nroBloque) {
        return bloques.stream().filter(b -> b.getNroBloque() == nroBloque).findFirst().orElse(null);
    }

    /** Primer bloque que todavia no fue respondido, o null si no queda ninguno. */
    public BloquePreguntas primerBloqueSinResponder() {
        return bloques.stream().filter(b -> !b.isRespondido()).findFirst().orElse(null);
    }

    public boolean todosLosBloquesRespondidos() {
        return !bloques.isEmpty() && bloques.stream().allMatch(BloquePreguntas::isRespondido);
    }

    /**
     * Calcula el total de cada factor (suma de las ponderaciones obtenidas
     * dividido 2) y el de cada competencia (promedio de sus factores),
     * segun CU026 observacion 3.
     */
    public void calcularPuntaje() {
        Map<String, Double> sumaPorFactor = new LinkedHashMap<>();
        Map<String, Factor> factores = new LinkedHashMap<>();
        for (BloquePreguntas b : bloques) {
            for (PreguntaCuestionario pc : b.getPreguntas()) {
                Factor f = pc.getFactor();
                if (f == null) {
                    continue;
                }
                factores.putIfAbsent(f.getCodigo(), f);
                double obtenida = pc.getPonderacionObtenida() == null ? 0 : pc.getPonderacionObtenida();
                sumaPorFactor.merge(f.getCodigo(), obtenida, Double::sum);
            }
        }

        Map<String, ResultadoCompetencia> porCompetencia = new LinkedHashMap<>();
        for (Map.Entry<String, Factor> entrada : factores.entrySet()) {
            Factor f = entrada.getValue();
            Competencia c = f.getCompetencia();
            if (c == null) {
                continue;
            }
            ResultadoCompetencia rc = porCompetencia.computeIfAbsent(c.getCodigo(),
                    k -> new ResultadoCompetencia(c));
            double totalFactor = sumaPorFactor.get(entrada.getKey()) / Factor.MINIMO_PREGUNTAS;
            rc.agregarResultadoFactor(new ResultadoFactor(f, totalFactor));
        }

        resultados.clear();
        for (ResultadoCompetencia rc : porCompetencia.values()) {
            double promedio = rc.getResultadosFactor().stream()
                    .mapToDouble(ResultadoFactor::getTotalFactor)
                    .average()
                    .orElse(0);
            rc.setTotalCompetencia(promedio);
            resultados.add(rc);
        }
    }

    /** Puntaje del candidato: promedio de los totales de sus competencias. */
    public float obtenerPuntajeTotal() {
        return (float) resultados.stream()
                .mapToDouble(ResultadoCompetencia::getTotalCompetencia)
                .average()
                .orElse(0);
    }

    /** Apto: alcanza la ponderacion minima en cada competencia requerida del puesto. */
    public boolean cumpleMinimos(List<CompetenciaRequerida> competenciasRequeridas) {
        for (CompetenciaRequerida cr : competenciasRequeridas) {
            ResultadoCompetencia rc = buscarResultado(cr.getCompetencia().getCodigo());
            if (rc == null || rc.getTotalCompetencia() < cr.getPonderacionMinima()) {
                return false;
            }
        }
        return true;
    }

    private ResultadoCompetencia buscarResultado(String codigoCompetencia) {
        return resultados.stream()
                .filter(r -> r.getCompetencia() != null
                        && codigoCompetencia.equals(r.getCompetencia().getCodigo()))
                .findFirst()
                .orElse(null);
    }

    // ---------- Accesores ----------

    public int getIdCuestionario() {
        return idCuestionario;
    }

    public void setIdCuestionario(int idCuestionario) {
        this.idCuestionario = idCuestionario;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public LocalDateTime getFechaHoraCreacion() {
        return fechaHoraCreacion;
    }

    public void setFechaHoraCreacion(LocalDateTime fechaHoraCreacion) {
        this.fechaHoraCreacion = fechaHoraCreacion;
    }

    public LocalDateTime getFechaHoraComienzo() {
        return fechaHoraComienzo;
    }

    public void setFechaHoraComienzo(LocalDateTime fechaHoraComienzo) {
        this.fechaHoraComienzo = fechaHoraComienzo;
    }

    public LocalDateTime getFechaHoraFin() {
        return fechaHoraFin;
    }

    public void setFechaHoraFin(LocalDateTime fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }

    public LocalDateTime getUltimoIngreso() {
        return ultimoIngreso;
    }

    public void setUltimoIngreso(LocalDateTime ultimoIngreso) {
        this.ultimoIngreso = ultimoIngreso;
    }

    public int getCantidadAccesos() {
        return cantidadAccesos;
    }

    public void setCantidadAccesos(int cantidadAccesos) {
        this.cantidadAccesos = cantidadAccesos;
    }

    public int getTiempoMaximo() {
        return tiempoMaximo;
    }

    public void setTiempoMaximo(int tiempoMaximo) {
        this.tiempoMaximo = tiempoMaximo;
    }

    public int getPlazoActivo() {
        return plazoActivo;
    }

    public void setPlazoActivo(int plazoActivo) {
        this.plazoActivo = plazoActivo;
    }

    public int getMaxAccesos() {
        return maxAccesos;
    }

    public void setMaxAccesos(int maxAccesos) {
        this.maxAccesos = maxAccesos;
    }

    public Candidato getCandidato() {
        return candidato;
    }

    public void setCandidato(Candidato candidato) {
        this.candidato = candidato;
    }

    public Evaluacion getEvaluacion() {
        return evaluacion;
    }

    public void setEvaluacion(Evaluacion evaluacion) {
        this.evaluacion = evaluacion;
    }

    public List<BloquePreguntas> getBloques() {
        return bloques;
    }

    public void setBloques(List<BloquePreguntas> bloques) {
        this.bloques = bloques;
    }

    public List<ResultadoCompetencia> getResultados() {
        return resultados;
    }

    public void setResultados(List<ResultadoCompetencia> resultados) {
        this.resultados = resultados;
    }
}
