package ar.edu.utn.frsf.capitalhumano.dominio;

public class ItemPlantilla {

    private int ordenVisualizacion;
    private String descripcion;

    public ItemPlantilla() {
    }

    public ItemPlantilla(int ordenVisualizacion, String descripcion) {
        this.ordenVisualizacion = ordenVisualizacion;
        this.descripcion = descripcion;
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
}
