package ar.edu.utn.frsf.capitalhumano.dao;

import java.util.List;

import ar.edu.utn.frsf.capitalhumano.dominio.Factor;

public interface FactorDAO {

    void guardar(Factor factor);

    void actualizar(Factor factor);

    void eliminar(String codigo);

    Factor buscarPorCodigo(String codigo);

    List<Factor> buscarPorFiltros(String codigoCompetencia, String nombre);

    /** Devuelve los factores de la competencia con sus preguntas activas y sus opciones. */
    List<Factor> buscarPorCompetencia(String codigoCompetencia);

    boolean existeOrdenEnCompetencia(String codigoCompetencia, int orden);
}
