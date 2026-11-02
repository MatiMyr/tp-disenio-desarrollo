package ar.edu.utn.frsf.capitalhumano.dominio;

import java.util.ArrayList;
import java.util.List;

public class ResultadoCompetencia {

    private int idResultadoCompetencia;
    private Competencia competencia;
    private double totalCompetencia;
    private List<ResultadoFactor> resultadosFactor = new ArrayList<>();

    public ResultadoCompetencia() {
    }

    public ResultadoCompetencia(Competencia competencia) {
        this.competencia = competencia;
    }

    public void agregarResultadoFactor(ResultadoFactor resultado) {
        resultadosFactor.add(resultado);
    }

    public int getIdResultadoCompetencia() {
        return idResultadoCompetencia;
    }

    public void setIdResultadoCompetencia(int idResultadoCompetencia) {
        this.idResultadoCompetencia = idResultadoCompetencia;
    }

    public Competencia getCompetencia() {
        return competencia;
    }

    public void setCompetencia(Competencia competencia) {
        this.competencia = competencia;
    }

    public double getTotalCompetencia() {
        return totalCompetencia;
    }

    public void setTotalCompetencia(double totalCompetencia) {
        this.totalCompetencia = totalCompetencia;
    }

    public List<ResultadoFactor> getResultadosFactor() {
        return resultadosFactor;
    }

    public void setResultadosFactor(List<ResultadoFactor> resultadosFactor) {
        this.resultadosFactor = resultadosFactor;
    }
}
