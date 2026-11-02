package ar.edu.utn.frsf.capitalhumano.dominio;

import java.util.ArrayList;
import java.util.List;

public class Factor {

    /** Minimo de preguntas activas para que el factor pueda evaluarse (CU026, obs. 1). */
    public static final int MINIMO_PREGUNTAS = 2;

    private String codigo;
    private String nombre;
    private String descripcion;
    private int nroOrden;
    private boolean eliminado;
    private Competencia competencia;
    private List<Pregunta> preguntas = new ArrayList<>();

    public Factor() {
    }

    public Factor(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }

    /** El factor se puede evaluar si tiene al menos 2 preguntas no eliminadas. */
    public boolean tieneMinimoPreguntas() {
        return obtenerPreguntasActivas().size() >= MINIMO_PREGUNTAS;
    }

    public List<Pregunta> obtenerPreguntasActivas() {
        return preguntas.stream().filter(p -> !p.isEliminado()).toList();
    }

    public void agregarPregunta(Pregunta pregunta) {
        preguntas.add(pregunta);
        pregunta.setFactor(this);
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getNroOrden() {
        return nroOrden;
    }

    public void setNroOrden(int nroOrden) {
        this.nroOrden = nroOrden;
    }

    public boolean isEliminado() {
        return eliminado;
    }

    public void setEliminado(boolean eliminado) {
        this.eliminado = eliminado;
    }

    public Competencia getCompetencia() {
        return competencia;
    }

    public void setCompetencia(Competencia competencia) {
        this.competencia = competencia;
    }

    public List<Pregunta> getPreguntas() {
        return preguntas;
    }

    public void setPreguntas(List<Pregunta> preguntas) {
        this.preguntas = preguntas;
    }
}
