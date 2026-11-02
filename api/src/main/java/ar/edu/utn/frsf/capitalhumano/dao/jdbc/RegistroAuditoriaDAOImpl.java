package ar.edu.utn.frsf.capitalhumano.dao.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import ar.edu.utn.frsf.capitalhumano.dao.RegistroAuditoriaDAO;
import ar.edu.utn.frsf.capitalhumano.dominio.RegistroAuditoria;

@Repository
public class RegistroAuditoriaDAOImpl implements RegistroAuditoriaDAO {

    private final JdbcTemplate jdbc;

    public RegistroAuditoriaDAOImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void guardar(RegistroAuditoria registro) {
        String usuario = registro.getConsultor() == null ? null : registro.getConsultor().getNombreUsuario();
        jdbc.update("""
                INSERT INTO registro_auditoria (fecha_hora, accion, entidad_afectada, nombre_usuario)
                VALUES (?, ?, ?, ?)
                """,
                registro.getFechaHora(), registro.getAccion(), registro.getEntidadAfectada(), usuario);
    }
}
