package ar.edu.utn.frsf.capitalhumano.externo;

/** Servicio de directorio de la organizacion contra el que se valida el consultor. */
public interface ILDAP {

    boolean validarCredenciales(String usuario, String contrasena);
}
