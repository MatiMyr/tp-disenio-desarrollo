package ar.edu.utn.frsf.capitalhumano.dao;

import java.util.List;

import ar.edu.utn.frsf.capitalhumano.dominio.Candidato;
import ar.edu.utn.frsf.capitalhumano.dominio.Cuestionario;
import ar.edu.utn.frsf.capitalhumano.dominio.Evaluacion;

public interface CuestionarioDAO {

    void guardar(Cuestionario cuestionario);

    void actualizar(Cuestionario cuestionario);

    Cuestionario buscarPorId(int idCuestionario);

    List<Cuestionario> buscarPorCandidato(Candidato candidato);

    Cuestionario buscarActivoOEnProceso(Candidato candidato);

    List<Cuestionario> buscarPorEvaluacion(Evaluacion evaluacion);

    List<Cuestionario> buscarVencidos();
}
