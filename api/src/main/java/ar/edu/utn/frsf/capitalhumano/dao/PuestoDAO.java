package ar.edu.utn.frsf.capitalhumano.dao;

import java.util.List;

import ar.edu.utn.frsf.capitalhumano.dominio.Puesto;

public interface PuestoDAO {

    void guardar(Puesto puesto);

    void actualizar(Puesto puesto);

    void eliminar(String codigo);

    Puesto buscarPorCodigo(String codigo);

    List<Puesto> buscarPorFiltros(String codigo, String nombre, String empresa);

    boolean existeCodigoONombre(String codigo, String nombre);

    boolean estaEnUso(String codigo);
}
