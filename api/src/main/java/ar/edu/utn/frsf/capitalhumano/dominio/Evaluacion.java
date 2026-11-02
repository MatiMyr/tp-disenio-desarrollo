package ar.edu.utn.frsf.capitalhumano.dominio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Evaluacion {

    private int idEvaluacion;
    private LocalDateTime fechaCreacion;
    private Puesto puesto;
    private Consultor consultor;
    private List<PreguntaEvaluacion> modelo = new ArrayList<>();
    private List<Cuestionario> cuestionarios = new ArrayList<>();

    public Evaluacion() {
    }

    public Evaluacion(Puesto puesto, Consultor consultor) {
        this.puesto = puesto;
        this.consultor = consultor;
        this.fechaCreacion = LocalDateTime.now();
    }

    /**
     * Guarda una copia de cada pregunta con sus opciones: todos los
     * cuestionarios de esta evaluacion usan este mismo modelo.
     */
    public void definirModelo(List<Pregunta> preguntas) {
        modelo.clear();
        for (Pregunta p : preguntas) {
            modelo.add(new PreguntaEvaluacion(p));
        }
    }

    public List<PreguntaEvaluacion> obtenerModelo() {
        return modelo;
    }

    public void agregarCuestionario(Cuestionario c) {
        cuestionarios.add(c);
        c.setEvaluacion(this);
    }

    public int getIdEvaluacion() {
        return idEvaluacion;
    }

    public void setIdEvaluacion(int idEvaluacion) {
        this.idEvaluacion = idEvaluacion;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Puesto getPuesto() {
        return puesto;
    }

    public void setPuesto(Puesto puesto) {
        this.puesto = puesto;
    }

    public Consultor getConsultor() {
        return consultor;
    }

    public void setConsultor(Consultor consultor) {
        this.consultor = consultor;
    }

    public void setModelo(List<PreguntaEvaluacion> modelo) {
        this.modelo = modelo;
    }

    public List<Cuestionario> getCuestionarios() {
        return cuestionarios;
    }

    public void setCuestionarios(List<Cuestionario> cuestionarios) {
        this.cuestionarios = cuestionarios;
    }
}
