package ar.edu.utn.frsf.capitalhumano.dominio;

public class OpcionEvaluacion {

    private int idOpcionEvaluacion;
    private int ordenVisualizacion;
    private String descripcion;
    private double ponderacion;

    public OpcionEvaluacion() {
    }

    public OpcionEvaluacion(int ordenVisualizacion, String descripcion, double ponderacion) {
        this.ordenVisualizacion = ordenVisualizacion;
        this.descripcion = descripcion;
        this.ponderacion = ponderacion;
    }

    public int getIdOpcionEvaluacion() {
        return idOpcionEvaluacion;
    }

    public void setIdOpcionEvaluacion(int idOpcionEvaluacion) {
        this.idOpcionEvaluacion = idOpcionEvaluacion;
    }

    public int getOrdenVisualizacion() {
        return ordenVisualizacion;
    }

    public void setOrdenVisualizacion(int ordenVisualizacion) {
        this.ordenVisualizacion = ordenVisualizacion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPonderacion() {
        return ponderacion;
    }

    public void setPonderacion(double ponderacion) {
        this.ponderacion = ponderacion;
    }
}
