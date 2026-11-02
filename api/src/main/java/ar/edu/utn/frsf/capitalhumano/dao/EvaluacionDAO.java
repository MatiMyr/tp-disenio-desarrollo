package ar.edu.utn.frsf.capitalhumano.dao;

import java.util.List;

import ar.edu.utn.frsf.capitalhumano.dominio.Evaluacion;
import ar.edu.utn.frsf.capitalhumano.dominio.Puesto;

public interface EvaluacionDAO {

    void guardar(Evaluacion evaluacion);

    List<Evaluacion> buscarPorPuesto(Puesto puesto);
}
