package ar.edu.utn.frsf.capitalhumano.gestor;

import org.springframework.stereotype.Service;

import ar.edu.utn.frsf.capitalhumano.config.ErroresValidacionException;
import ar.edu.utn.frsf.capitalhumano.dao.CandidatoDAO;
import ar.edu.utn.frsf.capitalhumano.dao.ConsultorDAO;
import ar.edu.utn.frsf.capitalhumano.dao.CuestionarioDAO;
import ar.edu.utn.frsf.capitalhumano.dominio.Candidato;
import ar.edu.utn.frsf.capitalhumano.dominio.Consultor;
import ar.edu.utn.frsf.capitalhumano.dominio.Cuestionario;
import ar.edu.utn.frsf.capitalhumano.dominio.TipoDocumento;
import ar.edu.utn.frsf.capitalhumano.externo.ILDAP;

/** CU001: ingreso del consultor (contra el LDAP) y del candidato (con su clave). */
@Service
public class GestorAutenticacion {

    private static final String ERROR_CONSULTOR = "El usuario o la contraseña no son válidos";
    private static final String ERROR_CANDIDATO =
            "Los datos ingresados no son válidos o no existe un cuestionario para el Candidato";

    private final ILDAP ldap;
    private final ConsultorDAO consultorDAO;
    private final CandidatoDAO candidatoDAO;
    private final CuestionarioDAO cuestionarioDAO;

    public GestorAutenticacion(ILDAP ldap, ConsultorDAO consultorDAO, CandidatoDAO candidatoDAO,
            CuestionarioDAO cuestionarioDAO) {
        this.ldap = ldap;
        this.consultorDAO = consultorDAO;
        this.candidatoDAO = candidatoDAO;
        this.cuestionarioDAO = cuestionarioDAO;
    }

    public Consultor autenticarConsultor(String usuario, String contrasena) {
        if (!ldap.validarCredenciales(usuario, contrasena)) {
            throw new ErroresValidacionException(ERROR_CONSULTOR);
        }
        return consultorDAO.buscarOCrear(usuario.trim());
    }

    /**
     * Comprueba que exista el candidato, que tenga un cuestionario activo o en
     * proceso y que la clave sea la de ese cuestionario. Devuelve el numero de
     * cuestionario, que es con lo que sigue CU026.
     */
    public int autenticarCandidato(TipoDocumento tipo, String nroDocumento, String clave) {
        Candidato candidato = candidatoDAO.buscarPorTipoYNroDocumento(tipo, nroDocumento);
        if (candidato == null) {
            throw new ErroresValidacionException(ERROR_CANDIDATO);
        }
        Cuestionario cuestionario = cuestionarioDAO.buscarActivoOEnProceso(candidato);
        if (cuestionario == null || clave == null
                || !cuestionario.getClave().trim().equals(clave.trim())) {
            throw new ErroresValidacionException(ERROR_CANDIDATO);
        }
        return cuestionario.getIdCuestionario();
    }
}
