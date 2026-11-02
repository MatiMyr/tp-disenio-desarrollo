package ar.edu.utn.frsf.capitalhumano.dao.jdbc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import ar.edu.utn.frsf.capitalhumano.dao.FactorDAO;
import ar.edu.utn.frsf.capitalhumano.dominio.Competencia;
import ar.edu.utn.frsf.capitalhumano.dominio.Factor;
import ar.edu.utn.frsf.capitalhumano.dominio.Pregunta;
import ar.edu.utn.frsf.capitalhumano.dominio.TipoCompetencia;

@Repository
public class FactorDAOImpl implements FactorDAO {

    /** Trae el factor con su competencia: ambos los necesita el alta de pregunta. */
    private static final RowMapper<Factor> MAPEADOR = (rs, fila) -> {
        Factor f = new Factor();
        f.setCodigo(rs.getString("f_codigo"));
        f.setNombre(rs.getString("f_nombre"));
        f.setDescripcion(rs.getString("f_descripcion"));
        f.setNroOrden(rs.getInt("f_nro_orden"));
        f.setEliminado(rs.getBoolean("f_eliminado"));
        Competencia c = new Competencia();
        c.setCodigo(rs.getString("c_codigo"));
        c.setNombre(rs.getString("c_nombre"));
        c.setDescripcion(rs.getString("c_descripcion"));
        c.setTipo(TipoCompetencia.valueOf(rs.getString("c_tipo")));
        c.setEliminado(rs.getBoolean("c_eliminado"));
        f.setCompetencia(c);
        return f;
    };

    private static final String SELECT = """
            SELECT f.codigo AS f_codigo, f.nombre AS f_nombre, f.descripcion AS f_descripcion,
                   f.nro_orden AS f_nro_orden, f.eliminado AS f_eliminado,
                   c.codigo AS c_codigo, c.nombre AS c_nombre, c.descripcion AS c_descripcion,
                   c.tipo AS c_tipo, c.eliminado AS c_eliminado
            FROM factor f
            JOIN competencia c ON c.codigo = f.codigo_competencia
            """;

    private final JdbcTemplate jdbc;

    public FactorDAOImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void guardar(Factor factor) {
        jdbc.update("""
                INSERT INTO factor (codigo, codigo_competencia, nombre, descripcion, nro_orden, eliminado)
                VALUES (?, ?, ?, ?, ?, FALSE)
                """,
                factor.getCodigo(), factor.getCompetencia().getCodigo(), factor.getNombre(),
                factor.getDescripcion(), factor.getNroOrden());
    }

    @Override
    public void actualizar(Factor factor) {
        jdbc.update("""
                UPDATE factor SET nombre = ?, descripcion = ?, nro_orden = ? WHERE codigo = ?
                """,
                factor.getNombre(), factor.getDescripcion(), factor.getNroOrden(), factor.getCodigo());
    }

    @Override
    public void eliminar(String codigo) {
        jdbc.update("UPDATE factor SET eliminado = TRUE WHERE codigo = ?", codigo);
    }

    @Override
    public Factor buscarPorCodigo(String codigo) {
        List<Factor> encontrados = jdbc.query(SELECT + " WHERE f.codigo = ?", MAPEADOR, codigo);
        return encontrados.isEmpty() ? null : encontrados.get(0);
    }

    @Override
    public List<Factor> buscarPorFiltros(String codigoCompetencia, String nombre) {
        return jdbc.query(SELECT + """
                 WHERE NOT f.eliminado
                   AND f.codigo_competencia ILIKE '%' || COALESCE(CAST(? AS TEXT), '') || '%'
                   AND f.nombre             ILIKE '%' || COALESCE(CAST(? AS TEXT), '') || '%'
                 ORDER BY c.nombre, f.nro_orden
                """, MAPEADOR, codigoCompetencia, nombre);
    }

    /**
     * Los factores de la competencia con sus preguntas activas y las opciones de
     * cada pregunta: asi el factor puede responder tieneMinimoPreguntas() y
     * obtenerPreguntasActivas() sin volver a la base (CU025).
     */
    @Override
    public List<Factor> buscarPorCompetencia(String codigoCompetencia) {
        List<Factor> factores = jdbc.query(SELECT + """
                 WHERE NOT f.eliminado AND f.codigo_competencia = ?
                 ORDER BY f.nro_orden
                """, MAPEADOR, codigoCompetencia);
        cargarPreguntasActivas(factores);
        return factores;
    }

    private void cargarPreguntasActivas(List<Factor> factores) {
        if (factores.isEmpty()) {
            return;
        }
        Map<String, Factor> porCodigo = new HashMap<>();
        factores.forEach(f -> porCodigo.put(f.getCodigo(), f));
        String comodines = String.join(",", factores.stream().map(f -> "?").toList());
        Object[] codigos = factores.stream().map(Factor::getCodigo).toArray();

        Map<String, Pregunta> preguntas = new HashMap<>();
        jdbc.query("""
                SELECT codigo, codigo_factor, nombre, enunciado, descripcion, eliminado
                FROM pregunta
                WHERE NOT eliminado AND codigo_factor IN (%s)
                ORDER BY codigo
                """.formatted(comodines), rs -> {
            Pregunta p = new Pregunta();
            p.setCodigo(rs.getString("codigo"));
            p.setNombre(rs.getString("nombre"));
            p.setEnunciado(rs.getString("enunciado"));
            p.setDescripcion(rs.getString("descripcion"));
            p.setEliminado(rs.getBoolean("eliminado"));
            porCodigo.get(rs.getString("codigo_factor")).agregarPregunta(p);
            preguntas.put(p.getCodigo(), p);
        }, codigos);

        if (preguntas.isEmpty()) {
            return;
        }
        List<String> codigosPregunta = new ArrayList<>(preguntas.keySet());
        String comodinesPregunta = String.join(",", codigosPregunta.stream().map(c -> "?").toList());
        jdbc.query("""
                SELECT codigo_pregunta, orden_visualizacion, descripcion, ponderacion
                FROM opcion_respuesta
                WHERE codigo_pregunta IN (%s)
                ORDER BY codigo_pregunta, orden_visualizacion
                """.formatted(comodinesPregunta), rs -> {
            preguntas.get(rs.getString("codigo_pregunta")).agregarOpcion(
                    rs.getInt("orden_visualizacion"),
                    rs.getString("descripcion"),
                    rs.getDouble("ponderacion"));
        }, codigosPregunta.toArray());
    }

    @Override
    public boolean existeOrdenEnCompetencia(String codigoCompetencia, int orden) {
        Integer cantidad = jdbc.queryForObject("""
                SELECT count(*) FROM factor
                WHERE NOT eliminado AND codigo_competencia = ? AND nro_orden = ?
                """, Integer.class, codigoCompetencia, orden);
        return cantidad != null && cantidad > 0;
    }
}
