package ar.edu.utn.frsf.capitalhumano.gestor;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import ar.edu.utn.frsf.capitalhumano.dao.RegistroAuditoriaDAO;
import ar.edu.utn.frsf.capitalhumano.dominio.Consultor;
import ar.edu.utn.frsf.capitalhumano.dominio.RegistroAuditoria;

/** Registra quien hizo cada baja logica y cuando. */
@Service
public class GestorAuditoria {

    private final RegistroAuditoriaDAO registroAuditoriaDAO;

    public GestorAuditoria(RegistroAuditoriaDAO registroAuditoriaDAO) {
        this.registroAuditoriaDAO = registroAuditoriaDAO;
    }

    public void registrarBaja(Consultor consultor, String entidadAfectada, LocalDateTime fechaHora) {
        registroAuditoriaDAO.guardar(new RegistroAuditoria("BAJA", fechaHora, entidadAfectada, consultor));
    }

    /** Los registros del proceso batch no tienen consultor: los genera el sistema. */
    public void registrarEjecucionProceso(LocalDateTime fechaHora, String accion) {
        registroAuditoriaDAO.guardar(new RegistroAuditoria(accion, fechaHora, "Proceso batch", null));
    }
}
