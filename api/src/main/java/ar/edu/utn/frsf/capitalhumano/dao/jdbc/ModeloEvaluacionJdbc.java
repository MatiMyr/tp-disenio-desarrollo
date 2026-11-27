package ar.edu.utn.frsf.capitalhumano.dao.jdbc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import ar.edu.utn.frsf.capitalhumano.dominio.Competencia;
import ar.edu.utn.frsf.capitalhumano.dominio.Consultor;
import ar.edu.utn.frsf.capitalhumano.dominio.Evaluacion;
import ar.edu.utn.frsf.capitalhumano.dominio.Factor;
import ar.edu.utn.frsf.capitalhumano.dominio.OpcionEvaluacion;
import ar.edu.utn.frsf.capitalhumano.dominio.Pregunta;
import ar.edu.utn.frsf.capitalhumano.dominio.PreguntaEvaluacion;
import ar.edu.utn.frsf.capitalhumano.dominio.Puesto;
import ar.edu.utn.frsf.capitalhumano.dominio.TipoCompetencia;

/**
 * Lectura y escritura del modelo de la evaluacion (la copia de preguntas y
 * opciones). La usan EvaluacionDAOImpl y CuestionarioDAOImpl, que necesitan el
 * modelo para armar y recuperar los cuestionarios.
 */
@Component
class ModeloEvaluacionJdbc {

    private final JdbcTemplate jdbc;

    ModeloEvaluacionJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Trae la evaluacion con su puesto, su consultor y su modelo completo. */
    Evaluacion cargar(int idEvaluacion) {
        List<Evaluacion> encontradas = jdbc.query("""
                SELECT e.id_evaluacion, e.fecha_creacion, e.nombre_usuario,
                       p.codigo AS p_codigo, p.nombre AS p_nombre, p.descripcion AS p_descripcion,
                       p.empresa AS p_empresa, p.eliminado AS p_eliminado
                FROM evaluacion e
                JOIN puesto p ON p.codigo = e.codigo_puesto
                WHERE e.id_evaluacion = ?
                """, (rs, fila) -> {
            Evaluacion e = new Evaluacion();
            e.setIdEvaluacion(rs.getInt("id_evaluacion"));
            e.setFechaCreacion(rs.getObject("fecha_creacion", java.time.LocalDateTime.class));
            e.setConsultor(new Consultor(rs.getString("nombre_usuario")));
            Puesto p = new Puesto();
            p.setCodigo(rs.getString("p_codigo"));
            p.setNombre(rs.getString("p_nombre"));
            p.setDescripcion(rs.getString("p_descripcion"));
            p.setEmpresa(rs.getString("p_empresa"));
            p.setEliminado(rs.getBoolean("p_eliminado"));
            e.setPuesto(p);
            return e;
        }, idEvaluacion);

        if (encontradas.isEmpty()) {
            return null;
        }
        Evaluacion evaluacion = encontradas.get(0);
        evaluacion.setModelo(cargarPreguntas(idEvaluacion));
        return evaluacion;
    }

    private List<PreguntaEvaluacion> cargarPreguntas(int idEvaluacion) {
        Map<String, Competencia> competencias = new HashMap<>();
        Map<String, Factor> factores = new HashMap<>();
        Map<Integer, PreguntaEvaluacion> porId = new HashMap<>();
        List<PreguntaEvaluacion> modelo = new ArrayList<>();

        jdbc.query("""
                SELECT pe.id_pregunta_evaluacion, pe.enunciado,
                       p.codigo AS p_codigo, p.nombre AS p_nombre, p.descripcion AS p_descripcion,
                       p.eliminado AS p_eliminado,
                       f.codigo AS f_codigo, f.nombre AS f_nombre, f.nro_orden AS f_nro_orden,
                       c.codigo AS c_codigo, c.nombre AS c_nombre, c.tipo AS c_tipo
                FROM pregunta_evaluacion pe
                JOIN pregunta p ON p.codigo = pe.codigo_pregunta
                JOIN factor f ON f.codigo = p.codigo_factor
                JOIN competencia c ON c.codigo = f.codigo_competencia
                WHERE pe.id_evaluacion = ?
                ORDER BY pe.id_pregunta_evaluacion
                """, rs -> {
            Competencia competencia = competencias.computeIfAbsent(rs.getString("c_codigo"), codigo -> {
                Competencia c = new Competencia();
                c.setCodigo(codigo);
                return c;
            });
            competencia.setNombre(rs.getString("c_nombre"));
            competencia.setTipo(TipoCompetencia.valueOf(rs.getString("c_tipo")));

            String codigoFactor = rs.getString("f_codigo");
            Factor factor = factores.get(codigoFactor);
            if (factor == null) {
                factor = new Factor();
                factor.setCodigo(codigoFactor);
                factor.setNombre(rs.getString("f_nombre"));
                factor.setNroOrden(rs.getInt("f_nro_orden"));
                factor.setCompetencia(competencia);
                factores.put(codigoFactor, factor);
            }

            Pregunta original = new Pregunta();
            original.setCodigo(rs.getString("p_codigo"));
            original.setNombre(rs.getString("p_nombre"));
            original.setDescripcion(rs.getString("p_descripcion"));
            original.setEnunciado(rs.getString("enunciado"));
            original.setEliminado(rs.getBoolean("p_eliminado"));
            original.setFactor(factor);

            PreguntaEvaluacion pe = new PreguntaEvaluacion();
            pe.setIdPreguntaEvaluacion(rs.getInt("id_pregunta_evaluacion"));
            pe.setEnunciado(rs.getString("enunciado"));
            pe.setOriginal(original);
            modelo.add(pe);
            porId.put(pe.getIdPreguntaEvaluacion(), pe);
        }, idEvaluacion);

        if (modelo.isEmpty()) {
            return modelo;
        }
        jdbc.query("""
                SELECT oe.id_opcion_evaluacion, oe.id_pregunta_evaluacion, oe.orden_visualizacion,
                       oe.descripcion, oe.ponderacion
                FROM opcion_evaluacion oe
                JOIN pregunta_evaluacion pe ON pe.id_pregunta_evaluacion = oe.id_pregunta_evaluacion
                WHERE pe.id_evaluacion = ?
                ORDER BY oe.id_pregunta_evaluacion, oe.orden_visualizacion
                """, rs -> {
            OpcionEvaluacion o = new OpcionEvaluacion(rs.getInt("orden_visualizacion"),
                    rs.getString("descripcion"), rs.getDouble("ponderacion"));
            o.setIdOpcionEvaluacion(rs.getInt("id_opcion_evaluacion"));
            porId.get(rs.getInt("id_pregunta_evaluacion")).getOpciones().add(o);
        }, idEvaluacion);
        return modelo;
    }

    /** Guarda la copia del modelo y deja en los objetos los id que genero la base. */
    void guardarModelo(Evaluacion evaluacion) {
        for (PreguntaEvaluacion pe : evaluacion.obtenerModelo()) {
            Integer idPregunta = jdbc.queryForObject("""
                    INSERT INTO pregunta_evaluacion (id_evaluacion, codigo_pregunta, enunciado)
                    VALUES (?, ?, ?)
                    RETURNING id_pregunta_evaluacion
                    """, Integer.class,
                    evaluacion.getIdEvaluacion(), pe.getOriginal().getCodigo(), pe.getEnunciado());
            pe.setIdPreguntaEvaluacion(idPregunta == null ? 0 : idPregunta);

            for (OpcionEvaluacion o : pe.getOpciones()) {
                Integer idOpcion = jdbc.queryForObject("""
                        INSERT INTO opcion_evaluacion (id_pregunta_evaluacion, orden_visualizacion,
                                                       descripcion, ponderacion)
                        VALUES (?, ?, ?, ?)
                        RETURNING id_opcion_evaluacion
                        """, Integer.class,
                        pe.getIdPreguntaEvaluacion(), o.getOrdenVisualizacion(), o.getDescripcion(),
                        o.getPonderacion());
                o.setIdOpcionEvaluacion(idOpcion == null ? 0 : idOpcion);
            }
        }
    }
}
