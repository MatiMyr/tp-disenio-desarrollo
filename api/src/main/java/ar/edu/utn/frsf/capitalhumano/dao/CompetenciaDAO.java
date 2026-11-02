package ar.edu.utn.frsf.capitalhumano.dao;

import java.util.List;

import ar.edu.utn.frsf.capitalhumano.dominio.Competencia;

public interface CompetenciaDAO {

    void guardar(Competencia competencia);

    void actualizar(Competencia competencia);

    void eliminar(String codigo);

    Competencia buscarPorCodigo(String codigo);

    List<Competencia> buscarPorNombre(String nombre);

    List<Competencia> buscarTodas();

    boolean estaEnUso(String codigo);
}
