package ar.edu.utn.frsf.capitalhumano.dao.jdbc;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import ar.edu.utn.frsf.capitalhumano.dao.ConsultorDAO;
import ar.edu.utn.frsf.capitalhumano.dominio.Consultor;

@Repository
public class ConsultorDAOImpl implements ConsultorDAO {

    private final JdbcTemplate jdbc;

    public ConsultorDAOImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Consultor buscarPorNombreUsuario(String nombreUsuario) {
        List<Consultor> encontrados = jdbc.query(
                "SELECT nombre_usuario FROM consultor WHERE nombre_usuario = ?",
                (rs, fila) -> new Consultor(rs.getString("nombre_usuario")),
                nombreUsuario);
        return encontrados.isEmpty() ? null : encontrados.get(0);
    }

    @Override
    public Consultor buscarOCrear(String nombreUsuario) {
        Consultor existente = buscarPorNombreUsuario(nombreUsuario);
        if (existente != null) {
            return existente;
        }
        jdbc.update("INSERT INTO consultor (nombre_usuario) VALUES (?) ON CONFLICT DO NOTHING",
                nombreUsuario);
        return new Consultor(nombreUsuario);
    }
}
