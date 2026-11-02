package ar.edu.utn.frsf.capitalhumano.dominio;

import java.util.ArrayList;
import java.util.List;

/**
 * Plantilla que solo sirve para precargar la grilla de opciones de CU021.
 * Al dar de alta la pregunta las opciones se copian: la pregunta no queda
 * vinculada a la plantilla.
 */
public class PlantillaRespuesta {

    private int idPlantilla;
    private String nombre;
    private String descripcion;
    private boolean eliminado;
    private List<ItemPlantilla> items = new ArrayList<>();

    public PlantillaRespuesta() {
    }

    public PlantillaRespuesta(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public void agregarItem(int orden, String descripcion) {
        items.add(new ItemPlantilla(orden, descripcion));
    }

    public List<ItemPlantilla> obtenerItems() {
        return items;
    }

    public int getIdPlantilla() {
        return idPlantilla;
    }

    public void setIdPlantilla(int idPlantilla) {
        this.idPlantilla = idPlantilla;
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

    public boolean isEliminado() {
        return eliminado;
    }

    public void setEliminado(boolean eliminado) {
        this.eliminado = eliminado;
    }
}
