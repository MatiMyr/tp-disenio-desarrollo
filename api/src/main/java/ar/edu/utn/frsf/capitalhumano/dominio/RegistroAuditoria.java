package ar.edu.utn.frsf.capitalhumano.dominio;

import java.time.LocalDateTime;

public class RegistroAuditoria {

    private int idRegistro;
    private String accion;
    private LocalDateTime fechaHora;
    private String entidadAfectada;
    private Consultor consultor;

    public RegistroAuditoria() {
    }

    public RegistroAuditoria(String accion, LocalDateTime fechaHora, String entidadAfectada, Consultor consultor) {
        this.accion = accion;
        this.fechaHora = fechaHora;
        this.entidadAfectada = entidadAfectada;
        this.consultor = consultor;
    }

    public int getIdRegistro() {
        return idRegistro;
    }

    public void setIdRegistro(int idRegistro) {
        this.idRegistro = idRegistro;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getEntidadAfectada() {
        return entidadAfectada;
    }

    public void setEntidadAfectada(String entidadAfectada) {
        this.entidadAfectada = entidadAfectada;
    }

    public Consultor getConsultor() {
        return consultor;
    }

    public void setConsultor(Consultor consultor) {
        this.consultor = consultor;
    }
}
