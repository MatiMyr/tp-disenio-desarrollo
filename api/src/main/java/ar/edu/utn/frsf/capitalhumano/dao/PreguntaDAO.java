package ar.edu.utn.frsf.capitalhumano.dao;

import java.util.List;

import ar.edu.utn.frsf.capitalhumano.dominio.Pregunta;

public interface PreguntaDAO {

    void guardar(Pregunta pregunta);

    void actualizar(Pregunta pregunta);

    void eliminar(String codigo);

    Pregunta buscarPorCodigo(String codigo);

    List<Pregunta> buscarPorFiltros(String codigoCompetencia, String codigoFactor, String nombre);

    boolean existePregunta(String codigoCompetencia, String codigoFactor, String nombre, String enunciado);
}
