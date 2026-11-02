package ar.edu.utn.frsf.capitalhumano.dao;

import ar.edu.utn.frsf.capitalhumano.dominio.Consultor;

public interface ConsultorDAO {

    Consultor buscarPorNombreUsuario(String nombreUsuario);

    /** El consultor se autentica contra el LDAP: si todavia no esta en la base, se crea. */
    Consultor buscarOCrear(String nombreUsuario);
}
