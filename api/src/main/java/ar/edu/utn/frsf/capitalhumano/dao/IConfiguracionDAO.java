package ar.edu.utn.frsf.capitalhumano.dao;

/** Repositorio de configuracion del sistema (tabla parametro). */
public interface IConfiguracionDAO {

    String obtenerInstrucciones();

    Object obtenerParametro(String clave);

    int obtenerCantidadPreguntasPorBloque();
}
