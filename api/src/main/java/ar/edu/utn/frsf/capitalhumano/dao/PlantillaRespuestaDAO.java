package ar.edu.utn.frsf.capitalhumano.dao;

import java.util.List;

import ar.edu.utn.frsf.capitalhumano.dominio.PlantillaRespuesta;

public interface PlantillaRespuestaDAO {

    void guardar(PlantillaRespuesta plantilla);

    void actualizar(PlantillaRespuesta plantilla);

    PlantillaRespuesta buscarPorId(int idPlantilla);

    List<PlantillaRespuesta> buscarPorFiltro(String nombre);

    boolean existeNombre(String nombre, int idPlantilla);
}
