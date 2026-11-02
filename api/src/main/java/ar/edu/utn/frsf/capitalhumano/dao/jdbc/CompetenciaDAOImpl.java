package ar.edu.utn.frsf.capitalhumano.dao.jdbc;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import ar.edu.utn.frsf.capitalhumano.dao.CompetenciaDAO;
import ar.edu.utn.frsf.capitalhumano.dominio.Competencia;
import ar.edu.utn.frsf.capitalhumano.dominio.TipoCompetencia;

@Repository
public class CompetenciaDAOImpl implements CompetenciaDAO {

    private static final RowMapper<Competencia> MAPEADOR = (rs, fila) -> {
        Competencia c = new Competencia();
        c.setCodigo(rs.getString("codigo"));
        c.setNombre(rs.getString("nombre"));
        c.setDescripcion(rs.getString("descripcion"));
        c.setTipo(TipoCompetencia.valueOf(rs.getString("tipo")));
        c.setEliminado(rs.getBoolean("eliminado"));
        return c;
    };

    private static final String SELECT = """
            SELECT codigo, nombre, descripcion, tipo, eliminado FROM competencia
            """;

    private final JdbcTemplate jdbc;

    public CompetenciaDAOImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void guardar(Competencia competencia) {
        jdbc.update("""
                INSERT INTO competencia (codigo, nombre, descripcion, tipo, eliminado)
                VALUES (?, ?, ?, ?, FALSE)
                """,
                competencia.getCodigo(), competencia.getNombre(), competencia.getDescripcion(),
                competencia.getTipo().name());
    }

    @Override
    public void actualizar(Competencia competencia) {
        jdbc.update("""
                UPDATE competencia SET nombre = ?, descripcion = ?, tipo = ? WHERE codigo = ?
                """,
                competencia.getNombre(), competencia.getDescripcion(), competencia.getTipo().name(),
                competencia.getCodigo());
    }

    @Override
    public void eliminar(String codigo) {
        jdbc.update("UPDATE competencia SET eliminado = TRUE WHERE codigo = ?", codigo);
    }

    @Override
    public Competencia buscarPorCodigo(String codigo) {
        List<Competencia> encontradas = jdbc.query(SELECT + " WHERE codigo = ?", MAPEADOR, codigo);
        return encontradas.isEmpty() ? null : encontradas.get(0);
    }

    @Override
    public List<Competencia> buscarPorNombre(String nombre) {
        return jdbc.query(SELECT + """
                 WHERE NOT eliminado
                   AND nombre ILIKE '%' || COALESCE(CAST(? AS TEXT), '') || '%'
                 ORDER BY nombre
                """, MAPEADOR, nombre);
    }

    @Override
    public List<Competencia> buscarTodas() {
        return jdbc.query(SELECT + " WHERE NOT eliminado ORDER BY nombre", MAPEADOR);
    }

    @Override
    public boolean estaEnUso(String codigo) {
        Integer cantidad = jdbc.queryForObject("""
                SELECT count(*) FROM competencia_requerida WHERE codigo_competencia = ?
                """, Integer.class, codigo);
        return cantidad != null && cantidad > 0;
    }
}
