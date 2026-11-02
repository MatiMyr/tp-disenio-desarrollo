package ar.edu.utn.frsf.capitalhumano.dominio;

import java.util.ArrayList;
import java.util.List;

/**
 * Copia de una pregunta dentro del modelo de la evaluacion. Guarda su enunciado
 * y sus opciones, asi los cuestionarios no cambian si despues se modifica o se
 * da de baja la pregunta original.
 */
public class PreguntaEvaluacion {

    private int idPreguntaEvaluacion;
    private String enunciado;
    private Pregunta original;
    private List<OpcionEvaluacion> opciones = new ArrayList<>();

    public PreguntaEvaluacion() {
    }

    public PreguntaEvaluacion(Pregunta original) {
        this.original = original;
        this.enunciado = original.getEnunciado();
        for (OpcionRespuesta o : original.getOpciones()) {
            opciones.add(new OpcionEvaluacion(o.getOrdenVisualizacion(), o.getDescripcion(), o.getPonderacion()));
        }
    }

    /** El factor sale de la pregunta original, que no cambia de factor. */
    public Factor getFactor() {
        return original == null ? null : original.getFactor();
    }

    public int getIdPreguntaEvaluacion() {
        return idPreguntaEvaluacion;
    }

    public void setIdPreguntaEvaluacion(int idPreguntaEvaluacion) {
        this.idPreguntaEvaluacion = idPreguntaEvaluacion;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    public Pregunta getOriginal() {
        return original;
    }

    public void setOriginal(Pregunta original) {
        this.original = original;
    }

    public List<OpcionEvaluacion> getOpciones() {
        return opciones;
    }

    public void setOpciones(List<OpcionEvaluacion> opciones) {
        this.opciones = opciones;
    }
}
