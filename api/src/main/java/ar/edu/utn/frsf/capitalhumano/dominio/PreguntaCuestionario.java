package ar.edu.utn.frsf.capitalhumano.dominio;

import java.time.LocalDateTime;

/**
 * Pregunta del modelo asignada a un bloque. Los datos de la respuesta quedan
 * vacios hasta que el candidato responde.
 */
public class PreguntaCuestionario {

    private int idPreguntaCuestionario;
    private PreguntaEvaluacion preguntaEvaluacion;
    private int ordenPresentacion;
    private OpcionEvaluacion opcionElegida;
    private LocalDateTime fechaHoraRespuesta;
    private String descripcionOpcionElegida;
    private Double ponderacionObtenida;

    public PreguntaCuestionario() {
    }

    public PreguntaCuestionario(PreguntaEvaluacion preguntaEvaluacion, int ordenPresentacion) {
        this.preguntaEvaluacion = preguntaEvaluacion;
        this.ordenPresentacion = ordenPresentacion;
    }

    /** Guarda la opcion elegida junto con su descripcion y su ponderacion. */
    public void responder(OpcionEvaluacion opcion) {
        this.opcionElegida = opcion;
        this.descripcionOpcionElegida = opcion.getDescripcion();
        this.ponderacionObtenida = opcion.getPonderacion();
        this.fechaHoraRespuesta = LocalDateTime.now();
    }

    public boolean estaRespondida() {
        return opcionElegida != null;
    }

    public Factor getFactor() {
        return preguntaEvaluacion == null ? null : preguntaEvaluacion.getFactor();
    }

    public int getIdPreguntaCuestionario() {
        return idPreguntaCuestionario;
    }

    public void setIdPreguntaCuestionario(int idPreguntaCuestionario) {
        this.idPreguntaCuestionario = idPreguntaCuestionario;
    }

    public PreguntaEvaluacion getPreguntaEvaluacion() {
        return preguntaEvaluacion;
    }

    public void setPreguntaEvaluacion(PreguntaEvaluacion preguntaEvaluacion) {
        this.preguntaEvaluacion = preguntaEvaluacion;
    }

    public int getOrdenPresentacion() {
        return ordenPresentacion;
    }

    public void setOrdenPresentacion(int ordenPresentacion) {
        this.ordenPresentacion = ordenPresentacion;
    }

    public OpcionEvaluacion getOpcionElegida() {
        return opcionElegida;
    }

    public void setOpcionElegida(OpcionEvaluacion opcionElegida) {
        this.opcionElegida = opcionElegida;
    }

    public LocalDateTime getFechaHoraRespuesta() {
        return fechaHoraRespuesta;
    }

    public void setFechaHoraRespuesta(LocalDateTime fechaHoraRespuesta) {
        this.fechaHoraRespuesta = fechaHoraRespuesta;
    }

    public String getDescripcionOpcionElegida() {
        return descripcionOpcionElegida;
    }

    public void setDescripcionOpcionElegida(String descripcionOpcionElegida) {
        this.descripcionOpcionElegida = descripcionOpcionElegida;
    }

    public Double getPonderacionObtenida() {
        return ponderacionObtenida;
    }

    public void setPonderacionObtenida(Double ponderacionObtenida) {
        this.ponderacionObtenida = ponderacionObtenida;
    }
}
