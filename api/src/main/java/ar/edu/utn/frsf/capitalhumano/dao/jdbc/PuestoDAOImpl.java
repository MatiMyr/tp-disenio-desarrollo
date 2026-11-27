package ar.edu.utn.frsf.capitalhumano.dao.jdbc;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import ar.edu.utn.frsf.capitalhumano.dao.PuestoDAO;
import ar.edu.utn.frsf.capitalhumano.dominio.Competencia;
import ar.edu.utn.frsf.capitalhumano.dominio.Puesto;
import ar.edu.utn.frsf.capitalhumano.dominio.TipoCompetencia;

@Repository
public class PuestoDAOImpl implements PuestoDAO {

    private static final RowMapper<Puesto> MAPEADOR = (rs, fila) -> {
        Puesto p = new Puesto();
        p.setCodigo(rs.getString("codigo"));
        p.setNombre(rs.getString("nombre"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setEmpresa(rs.getString("empresa"));
        p.setEliminado(rs.getBoolean("eliminado"));
        return p;
    };

    private static final String SELECT = """
            SELECT codigo, nombre, descripcion, empresa, eliminado FROM puesto
            """;

    private final JdbcTemplate jdbc;

    public PuestoDAOImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void guardar(Puesto puesto) {
        jdbc.update("""
                INSERT INTO puesto (codigo, nombre, descripcion, empresa, eliminado)
                VALUES (?, ?, ?, ?, FALSE)
                """,
                puesto.getCodigo(), puesto.getNombre(), puesto.getDescripcion(), puesto.getEmpresa());
        guardarCompetenciasRequeridas(puesto);
    }

    @Override
    public void actualizar(Puesto puesto) {
        jdbc.update("""
                UPDATE puesto SET nombre = ?, descripcion = ?, empresa = ? WHERE codigo = ?
                """,
                puesto.getNombre(), puesto.getDescripcion(), puesto.getEmpresa(), puesto.getCodigo());
        jdbc.update("DELETE FROM competencia_requerida WHERE codigo_puesto = ?", puesto.getCodigo());
        guardarCompetenciasRequeridas(puesto);
    }

    private void guardarCompetenciasRequeridas(Puesto puesto) {
        puesto.getCompetenciasRequeridas().forEach(cr -> jdbc.update("""
                INSERT INTO competencia_requerida (codigo_puesto, codigo_competencia, ponderacion_minima)
                VALUES (?, ?, ?)
                """,
                puesto.getCodigo(), cr.getCompetencia().getCodigo(), cr.getPonderacionMinima()));
    }

    @Override
    public void eliminar(String codigo) {
        jdbc.update("UPDATE puesto SET eliminado = TRUE WHERE codigo = ?", codigo);
    }

    /** Trae el puesto con sus competencias requeridas (las necesitan CU025 y CU028). */
    @Override
    public Puesto buscarPorCodigo(String codigo) {
        List<Puesto> encontrados = jdbc.query(SELECT + " WHERE codigo = ?", MAPEADOR, codigo);
        if (encontrados.isEmpty()) {
            return null;
        }
        Puesto puesto = encontrados.get(0);
        jdbc.query("""
                SELECT cr.ponderacion_minima, c.codigo, c.nombre, c.descripcion, c.tipo, c.eliminado
                FROM competencia_requerida cr
                JOIN competencia c ON c.codigo = cr.codigo_competencia
                WHERE cr.codigo_puesto = ?
                ORDER BY c.nombre
                """, rs -> {
            Competencia c = new Competencia();
            c.setCodigo(rs.getString("codigo"));
            c.setNombre(rs.getString("nombre"));
            c.setDescripcion(rs.getString("descripcion"));
            c.setTipo(TipoCompetencia.valueOf(rs.getString("tipo")));
            c.setEliminado(rs.getBoolean("eliminado"));
            puesto.agregarCompetenciaRequerida(c, rs.getDouble("ponderacion_minima"));
        }, codigo);
        return puesto;
    }

    @Override
    public List<Puesto> buscarPorFiltros(String codigo, String nombre, String empresa) {
        return jdbc.query(SELECT + """
                 WHERE NOT eliminado
                   AND codigo  ILIKE '%' || COALESCE(CAST(? AS TEXT), '') || '%'
                   AND nombre  ILIKE '%' || COALESCE(CAST(? AS TEXT), '') || '%'
                   AND empresa ILIKE '%' || COALESCE(CAST(? AS TEXT), '') || '%'
                 ORDER BY nombre
                """, MAPEADOR, codigo, nombre, empresa);
    }

    @Override
    public boolean existeCodigoONombre(String codigo, String nombre) {
        Integer cantidad = jdbc.queryForObject("""
                SELECT count(*) FROM puesto WHERE NOT eliminado AND (codigo = ? OR upper(nombre) = upper(?))
                """, Integer.class, codigo, nombre);
        return cantidad != null && cantidad > 0;
    }

    @Override
    public boolean estaEnUso(String codigo) {
        Integer cantidad = jdbc.queryForObject("""
                SELECT count(*) FROM evaluacion WHERE codigo_puesto = ?
                """, Integer.class, codigo);
        return cantidad != null && cantidad > 0;
    }
}
