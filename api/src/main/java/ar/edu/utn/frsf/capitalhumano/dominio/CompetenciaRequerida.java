package ar.edu.utn.frsf.capitalhumano.dominio;

/** Clase asociacion de Puesto-Competencia: la ponderacion minima que pide el puesto. */
public class CompetenciaRequerida {

    private Competencia competencia;
    private double ponderacionMinima;

    public CompetenciaRequerida() {
    }

    public CompetenciaRequerida(Competencia competencia, double ponderacionMinima) {
        this.competencia = competencia;
        this.ponderacionMinima = ponderacionMinima;
    }

    public Competencia getCompetencia() {
        return competencia;
    }

    public void setCompetencia(Competencia competencia) {
        this.competencia = competencia;
    }

    public double getPonderacionMinima() {
        return ponderacionMinima;
    }

    public void setPonderacionMinima(double ponderacionMinima) {
        this.ponderacionMinima = ponderacionMinima;
    }
}
