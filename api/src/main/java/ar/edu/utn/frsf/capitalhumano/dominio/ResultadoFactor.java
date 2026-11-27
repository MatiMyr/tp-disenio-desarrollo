package ar.edu.utn.frsf.capitalhumano.dominio;

public class ResultadoFactor {

    private int idResultadoFactor;
    private Factor factor;
    private double totalFactor;

    public ResultadoFactor() {
    }

    public ResultadoFactor(Factor factor, double totalFactor) {
        this.factor = factor;
        this.totalFactor = totalFactor;
    }

    public int getIdResultadoFactor() {
        return idResultadoFactor;
    }

    public void setIdResultadoFactor(int idResultadoFactor) {
        this.idResultadoFactor = idResultadoFactor;
    }

    public Factor getFactor() {
        return factor;
    }

    public void setFactor(Factor factor) {
        this.factor = factor;
    }

    public double getTotalFactor() {
        return totalFactor;
    }

    public void setTotalFactor(double totalFactor) {
        this.totalFactor = totalFactor;
    }
}
