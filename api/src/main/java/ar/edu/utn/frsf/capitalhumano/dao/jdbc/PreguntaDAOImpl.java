package ar.edu.utn.frsf.capitalhumano.dao.jdbc;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import ar.edu.utn.frsf.capitalhumano.dao.PreguntaDAO;
import ar.edu.utn.frsf.capitalhumano.dominio.Competencia;
import ar.edu.utn.frsf.capitalhumano.dominio.Factor;
import ar.edu.utn.frsf.capitalhumano.dominio.Pregunta;
import ar.edu.utn.frsf.capitalhumano.dominio.TipoCompetencia;

@Repository
public class PreguntaDAOImpl implements PreguntaDAO {

    /** La pregunta viaja con su factor y su competencia: la grilla de CU020 los muestra. */
    private static final RowMapper<Pregunta> MAPEADOR = (rs, fila) -> {
        Pregunta p = new Pregunta();
        p.setCodigo(rs.getString("p_codigo"));
        p.setNombre(rs.getString("p_nombre"));
        p.setEnunciado(rs.getString("p_enunciado"));
        p.setDescripcion(rs.getString("p_descripcion"));
        p.setEliminado(rs.getBoolean("p_eliminado"));
        Competencia c = new Competencia();
        c.setCodigo(rs.getString("c_codigo"));
        c.setNombre(rs.getString("c_nombre"));
        c.setTipo(TipoCompetencia.valueOf(rs.getString("c_tipo")));
        Factor f = new Factor();
        f.setCodigo(rs.getString("f_codigo"));
        f.setNombre(rs.getString("f_nombre"));
        f.setNroOrden(rs.getInt("f_nro_orden"));
        f.setCompetencia(c);
        p.setFactor(f);
        return p;
    };

    private static final String SELECT = """
            SELECT p.codigo AS p_codigo, p.nombre AS p_nombre, p.enunciado AS p_enunciado,
                   p.descripcion AS p_descripcion, p.eliminado AS p_eliminado,
                   f.codigo AS f_codigo, f.nombre AS f_nombre, f.nro_orden AS f_nro_orden,
                   c.codigo AS c_codigo, c.nombre AS c_nombre, c.tipo AS c_tipo
            FROM pregunta p
            JOIN factor f ON f.codigo = p.codigo_factor
            JOIN competencia c ON c.codigo = f.codigo_competencia
            """;

    private final JdbcTemplate jdbc;

    public PreguntaDAOImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** El codigo lo genera la base (secuencia seq_pregunta) y vuelve en el RETURNING. */
    @Override
    public void guardar(Pregunta pregunta) {
        String codigo = jdbc.queryForObject("""
                INSERT INTO pregunta (codigo_factor, nombre, enunciado, descripcion, eliminado)
                VALUES (?, ?, ?, ?, FALSE)
                RETURNING codigo
                """, String.class,
                pregunta.getFactor().getCodigo(), pregunta.getNombre(), pregunta.getEnunciado(),
                pregunta.getDescripcion());
        pregunta.setCodigo(codigo);
        guardarOpciones(pregunta);
    }

    @Override
    public void actualizar(Pregunta pregunta) {
        jdbc.update("""
                UPDATE pregunta SET codigo_factor = ?, nombre = ?, enunciado = ?, descripcion = ?
                WHERE codigo = ?
                """,
                pregunta.getFactor().getCodigo(), pregunta.getNombre(), pregunta.getEnunciado(),
                pregunta.getDescripcion(), pregunta.getCodigo());
        jdbc.update("DELETE FROM opcion_respuesta WHERE codigo_pregunta = ?", pregunta.getCodigo());
        guardarOpciones(pregunta);
    }

    private void guardarOpciones(Pregunta pregunta) {
        pregunta.getOpciones().forEach(o -> jdbc.update("""
                INSERT INTO opcion_respuesta (codigo_pregunta, orden_visualizacion, descripcion, ponderacion)
                VALUES (?, ?, ?, ?)
                """,
                pregunta.getCodigo(), o.getOrdenVisualizacion(), o.getDescripcion(), o.getPonderacion()));
    }

    /** La baja es logica: la pregunta se marca como eliminada. */
    @Override
    public void eliminar(String codigo) {
        jdbc.update("UPDATE pregunta SET eliminado = TRUE WHERE codigo = ?", codigo);
    }

    @Override
    public Pregunta buscarPorCodigo(String codigo) {
        List<Pregunta> encontradas = jdbc.query(SELECT + " WHERE p.codigo = ?", MAPEADOR, codigo);
        if (encontradas.isEmpty()) {
            return null;
        }
        Pregunta pregunta = encontradas.get(0);
        jdbc.query("""
                SELECT orden_visualizacion, descripcion, ponderacion
                FROM opcion_respuesta WHERE codigo_pregunta = ?
                ORDER BY orden_visualizacion
                """, rs -> {
            pregunta.agregarOpcion(rs.getInt("orden_visualizacion"), rs.getString("descripcion"),
                    rs.getDouble("ponderacion"));
        }, codigo);
        return pregunta;
    }

    /** Busqueda de CU020: el criterio es "contiene". */
    @Override
    public List<Pregunta> buscarPorFiltros(String codigoCompetencia, String codigoFactor, String nombre) {
        return jdbc.query(SELECT + """
                 WHERE NOT p.eliminado
                   AND c.codigo ILIKE '%' || COALESCE(CAST(? AS TEXT), '') || '%'
                   AND f.codigo ILIKE '%' || COALESCE(CAST(? AS TEXT), '') || '%'
                   AND (p.nombre    ILIKE '%' || COALESCE(CAST(? AS TEXT), '') || '%'
                     OR p.enunciado ILIKE '%' || COALESCE(CAST(? AS TEXT), '') || '%')
                 ORDER BY c.nombre, f.nro_orden, p.nombre
                """, MAPEADOR, codigoCompetencia, codigoFactor, nombre, nombre);
    }

    /**
     * CU021, observacion 1: la pregunta no se puede repetir en el factor, ni por
     * nombre ni por enunciado.
     */
    @Override
    public boolean existePregunta(String codigoCompetencia, String codigoFactor, String nombre, String enunciado) {
        Integer cantidad = jdbc.queryForObject("""
                SELECT count(*)
                FROM pregunta p
                JOIN factor f ON f.codigo = p.codigo_factor
                WHERE NOT p.eliminado
                  AND p.codigo_factor = ?
                  AND f.codigo_competencia = ?
                  AND (upper(p.nombre) = upper(?) OR upper(p.enunciado) = upper(?))
                """, Integer.class, codigoFactor, codigoCompetencia, nombre, enunciado);
        return cantidad != null && cantidad > 0;
    }
}
