package ar.edu.utn.frsf.capitalhumano.dominio;

import java.util.ArrayList;
import java.util.List;

public class Pregunta {

    private String codigo;
    private String nombre;
    private String enunciado;
    private String descripcion;
    private boolean eliminado;
    private Factor factor;
    private List<OpcionRespuesta> opciones = new ArrayList<>();

    public Pregunta() {
    }

    public Pregunta(String nombre, String enunciado, String descripcion, Factor factor) {
        this.nombre = nombre;
        this.enunciado = enunciado;
        this.descripcion = descripcion;
        this.factor = factor;
    }

    /** Cada pregunta tiene sus propias opciones de respuesta. */
    public void agregarOpcion(int orden, String descripcion, double ponderacion) {
        opciones.add(new OpcionRespuesta(orden, descripcion, ponderacion));
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

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isEliminado() {
        return eliminado;
    }

    public void setEliminado(boolean eliminado) {
        this.eliminado = eliminado;
    }

    public Factor getFactor() {
        return factor;
    }

    public void setFactor(Factor factor) {
        this.factor = factor;
    }

    public List<OpcionRespuesta> getOpciones() {
        return opciones;
    }

    public void setOpciones(List<OpcionRespuesta> opciones) {
        this.opciones = opciones;
    }
}
