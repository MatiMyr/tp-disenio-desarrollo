package ar.edu.utn.frsf.capitalhumano.dominio;

/**
 * Valor que se persiste en la base. En memoria el estado del cuestionario se
 * representa con las clases de dominio.estado (patron State).
 */
public enum EstadoCuestionario {
    ACTIVO, EN_PROCESO, COMPLETO, INCOMPLETO, SIN_CONTESTAR
}
