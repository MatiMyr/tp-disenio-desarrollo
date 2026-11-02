package ar.edu.utn.frsf.capitalhumano.controller;

import org.springframework.stereotype.Component;

import ar.edu.utn.frsf.capitalhumano.config.ErroresValidacionException;
import ar.edu.utn.frsf.capitalhumano.dao.ConsultorDAO;
import ar.edu.utn.frsf.capitalhumano.dominio.Consultor;

/**
 * Resuelve el consultor que viene en el header X-Usuario. No hay seguridad real
 * en esta entrega: los controladores lo usan para pasarle el Consultor a los
 * gestores que lo registran (evaluaciones y auditoria de bajas).
 */
@Component
public class UsuarioActual {

    private final ConsultorDAO consultorDAO;

    public UsuarioActual(ConsultorDAO consultorDAO) {
        this.consultorDAO = consultorDAO;
    }

    public Consultor resolver(String nombreUsuario) {
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            throw new ErroresValidacionException("Debe iniciar sesión para realizar esta operación");
        }
        return consultorDAO.buscarOCrear(nombreUsuario.trim());
    }
}
