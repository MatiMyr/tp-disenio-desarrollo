package ar.edu.utn.frsf.capitalhumano.dao;

import java.util.List;

import ar.edu.utn.frsf.capitalhumano.dominio.Candidato;
import ar.edu.utn.frsf.capitalhumano.dominio.TipoDocumento;

public interface CandidatoDAO {

    ResultadoImportacion importar(List<Candidato> candidatos);

    Candidato buscarPorNroCandidato(int nroCandidato);

    Candidato buscarPorTipoYNroDocumento(TipoDocumento tipo, String nroDocumento);

    List<Candidato> buscarPorCriterios(String apellido, String nombre, Integer nroCandidato);

    void guardar(Candidato candidato);
}
