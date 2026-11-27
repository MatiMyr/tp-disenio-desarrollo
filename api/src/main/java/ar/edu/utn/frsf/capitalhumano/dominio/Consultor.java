package ar.edu.utn.frsf.capitalhumano.dominio;

public class Consultor {

    private String nombreUsuario;

    public Consultor() {
    }

    public Consultor(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }
}
