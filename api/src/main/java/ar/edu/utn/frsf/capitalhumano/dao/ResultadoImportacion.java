package ar.edu.utn.frsf.capitalhumano.dao;

/**
 * Tipo de retorno de CandidatoDAO.importar(). Pertenece al lenguaje interno
 * entre el DAO y el gestor: no cruza hacia la UI.
 */
public class ResultadoImportacion {

    private int candidatosCreados;
    private int candidatosActualizados;
    private int candidatosEliminados;

    public int getCandidatosCreados() {
        return candidatosCreados;
    }

    public void setCandidatosCreados(int candidatosCreados) {
        this.candidatosCreados = candidatosCreados;
    }

    public int getCandidatosActualizados() {
        return candidatosActualizados;
    }

    public void setCandidatosActualizados(int candidatosActualizados) {
        this.candidatosActualizados = candidatosActualizados;
    }

    public int getCandidatosEliminados() {
        return candidatosEliminados;
    }

    public void setCandidatosEliminados(int candidatosEliminados) {
        this.candidatosEliminados = candidatosEliminados;
    }
}
