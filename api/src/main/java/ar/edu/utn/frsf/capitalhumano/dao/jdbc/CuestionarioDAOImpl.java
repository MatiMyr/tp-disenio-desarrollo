package ar.edu.utn.frsf.capitalhumano.dao.jdbc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import ar.edu.utn.frsf.capitalhumano.dao.CuestionarioDAO;
import ar.edu.utn.frsf.capitalhumano.dominio.BloquePreguntas;
import ar.edu.utn.frsf.capitalhumano.dominio.Candidato;
import ar.edu.utn.frsf.capitalhumano.dominio.Competencia;
import ar.edu.utn.frsf.capitalhumano.dominio.Cuestionario;
import ar.edu.utn.frsf.capitalhumano.dominio.EstadoCuestionario;
import ar.edu.utn.frsf.capitalhumano.dominio.Evaluacion;
import ar.edu.utn.frsf.capitalhumano.dominio.Factor;
import ar.edu.utn.frsf.capitalhumano.dominio.PreguntaCuestionario;
import ar.edu.utn.frsf.capitalhumano.dominio.PreguntaEvaluacion;
import ar.edu.utn.frsf.capitalhumano.dominio.ResultadoCompetencia;
import ar.edu.utn.frsf.capitalhumano.dominio.ResultadoFactor;
import ar.edu.utn.frsf.capitalhumano.dominio.TipoCompetencia;
import ar.edu.utn.frsf.capitalhumano.dominio.estado.EstadoActivo;
import ar.edu.utn.frsf.capitalhumano.dominio.estado.EstadoCompleto;
import ar.edu.utn.frsf.capitalhumano.dominio.estado.EstadoEnProceso;
import ar.edu.utn.frsf.capitalhumano.dominio.estado.EstadoIncompleto;
import ar.edu.utn.frsf.capitalhumano.dominio.estado.EstadoSinContestar;
import ar.edu.utn.frsf.capitalhumano.dominio.estado.IEstadoCuestionario;

/**
 * En la base el estado del cuestionario es el enum EstadoCuestionario; en
 * memoria es una clase del paquete dominio.estado. Esta implementacion traduce
 * entre los dos.
 */
@Repository
public class CuestionarioDAOImpl implements CuestionarioDAO {

    private static final String SELECT = """
            SELECT q.id_cuestionario, q.id_evaluacion, q.clave, q.estado, q.fecha_hora_creacion,
                   q.fecha_hora_comienzo, q.fecha_hora_fin, q.ultimo_ingreso, q.cantidad_accesos,
                   q.tiempo_maximo, q.plazo_activo, q.max_accesos,
                   c.nro_candidato, c.apellido, c.nombre, c.tipo_documento, c.nro_documento,
                   c.fecha_nacimiento, c.genero, c.nacionalidad, c.email, c.escolaridad, c.eliminado
            FROM cuestionario q
            JOIN candidato c ON c.nro_candidato = q.nro_candidato
            """;

    private final JdbcTemplate jdbc;
    private final ModeloEvaluacionJdbc modelo;

    CuestionarioDAOImpl(JdbcTemplate jdbc, ModeloEvaluacionJdbc modelo) {
        this.jdbc = jdbc;
        this.modelo = modelo;
    }

    // ---------- Traduccion entre el enum de la base y la clase de estado ----------

    private EstadoCuestionario aEnum(IEstadoCuestionario estado) {
        return EstadoCuestionario.valueOf(estado.obtenerEstado());
    }

    private IEstadoCuestionario aEstado(EstadoCuestionario valor) {
        return switch (valor) {
            case ACTIVO -> EstadoActivo.getInstancia();
            case EN_PROCESO -> EstadoEnProceso.getInstancia();
            case COMPLETO -> EstadoCompleto.getInstancia();
            case INCOMPLETO -> EstadoIncompleto.getInstancia();
            case SIN_CONTESTAR -> EstadoSinContestar.getInstancia();
        };
    }

    private final RowMapper<Cuestionario> mapeador = (rs, fila) -> {
        Cuestionario q = new Cuestionario();
        q.setIdCuestionario(rs.getInt("id_cuestionario"));
        q.setClave(rs.getString("clave"));
        q.setEstado(aEstado(EstadoCuestionario.valueOf(rs.getString("estado"))));
        q.setFechaHoraCreacion(rs.getObject("fecha_hora_creacion", LocalDateTime.class));
        q.setFechaHoraComienzo(rs.getObject("fecha_hora_comienzo", LocalDateTime.class));
        q.setFechaHoraFin(rs.getObject("fecha_hora_fin", LocalDateTime.class));
        q.setUltimoIngreso(rs.getObject("ultimo_ingreso", LocalDateTime.class));
        q.setCantidadAccesos(rs.getInt("cantidad_accesos"));
        q.setTiempoMaximo(rs.getInt("tiempo_maximo"));
        q.setPlazoActivo(rs.getInt("plazo_activo"));
        q.setMaxAccesos(rs.getInt("max_accesos"));
        q.setCandidato(CandidatoDAOImpl.MAPEADOR.mapRow(rs, fila));
        return q;
    };

    // ---------- Escritura ----------

    @Override
    public void guardar(Cuestionario cuestionario) {
        Integer id = jdbc.queryForObject("""
                INSERT INTO cuestionario (id_evaluacion, nro_candidato, clave, estado, fecha_hora_creacion,
                        fecha_hora_comienzo, fecha_hora_fin, ultimo_ingreso, cantidad_accesos,
                        tiempo_maximo, plazo_activo, max_accesos)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id_cuestionario
                """, Integer.class,
                cuestionario.getEvaluacion().getIdEvaluacion(),
                cuestionario.getCandidato().getNroCandidato(),
                cuestionario.getClave(),
                aEnum(cuestionario.getEstado()).name(),
                cuestionario.getFechaHoraCreacion(),
                cuestionario.getFechaHoraComienzo(),
                cuestionario.getFechaHoraFin(),
                cuestionario.getUltimoIngreso(),
                cuestionario.getCantidadAccesos(),
                cuestionario.getTiempoMaximo(),
                cuestionario.getPlazoActivo(),
                cuestionario.getMaxAccesos());
        cuestionario.setIdCuestionario(id == null ? 0 : id);
        guardarBloques(cuestionario);
        guardarResultados(cuestionario);
    }

    @Override
    public void actualizar(Cuestionario cuestionario) {
        jdbc.update("""
                UPDATE cuestionario SET estado = ?, fecha_hora_comienzo = ?, fecha_hora_fin = ?,
                       ultimo_ingreso = ?, cantidad_accesos = ?
                WHERE id_cuestionario = ?
                """,
                aEnum(cuestionario.getEstado()).name(),
                cuestionario.getFechaHoraComienzo(),
                cuestionario.getFechaHoraFin(),
                cuestionario.getUltimoIngreso(),
                cuestionario.getCantidadAccesos(),
                cuestionario.getIdCuestionario());
        guardarBloques(cuestionario);
        guardarResultados(cuestionario);
    }

    /**
     * Inserta los bloques y las preguntas nuevas y actualiza las respuestas de
     * las que ya estaban, para no cambiarles el id entre un bloque y el otro.
     */
    private void guardarBloques(Cuestionario cuestionario) {
        for (BloquePreguntas bloque : cuestionario.getBloques()) {
            jdbc.update("""
                    INSERT INTO bloque_preguntas (id_cuestionario, nro_bloque, respondido)
                    VALUES (?, ?, ?)
                    ON CONFLICT (id_cuestionario, nro_bloque)
                    DO UPDATE SET respondido = EXCLUDED.respondido
                    """,
                    cuestionario.getIdCuestionario(), bloque.getNroBloque(), bloque.isRespondido());

            for (PreguntaCuestionario pc : bloque.getPreguntas()) {
                if (pc.getIdPreguntaCuestionario() == 0) {
                    Integer id = jdbc.queryForObject("""
                            INSERT INTO pregunta_cuestionario (id_cuestionario, nro_bloque,
                                    id_pregunta_evaluacion, orden_presentacion, id_opcion_elegida,
                                    fecha_hora_respuesta, descripcion_opcion_elegida, ponderacion_obtenida)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                            RETURNING id_pregunta_cuestionario
                            """, Integer.class,
                            cuestionario.getIdCuestionario(), bloque.getNroBloque(),
                            pc.getPreguntaEvaluacion().getIdPreguntaEvaluacion(),
                            pc.getOrdenPresentacion(),
                            pc.getOpcionElegida() == null ? null : pc.getOpcionElegida().getIdOpcionEvaluacion(),
                            pc.getFechaHoraRespuesta(), pc.getDescripcionOpcionElegida(),
                            pc.getPonderacionObtenida());
                    pc.setIdPreguntaCuestionario(id == null ? 0 : id);
                } else {
                    jdbc.update("""
                            UPDATE pregunta_cuestionario SET id_opcion_elegida = ?,
                                   fecha_hora_respuesta = ?, descripcion_opcion_elegida = ?,
                                   ponderacion_obtenida = ?
                            WHERE id_pregunta_cuestionario = ?
                            """,
                            pc.getOpcionElegida() == null ? null : pc.getOpcionElegida().getIdOpcionEvaluacion(),
                            pc.getFechaHoraRespuesta(), pc.getDescripcionOpcionElegida(),
                            pc.getPonderacionObtenida(), pc.getIdPreguntaCuestionario());
                }
            }
        }
    }

    /** Los resultados se calculan una sola vez, al completar el cuestionario. */
    private void guardarResultados(Cuestionario cuestionario) {
        if (cuestionario.getResultados().isEmpty()) {
            return;
        }
        jdbc.update("DELETE FROM resultado_competencia WHERE id_cuestionario = ?",
                cuestionario.getIdCuestionario());
        for (ResultadoCompetencia rc : cuestionario.getResultados()) {
            Integer idResultado = jdbc.queryForObject("""
                    INSERT INTO resultado_competencia (id_cuestionario, codigo_competencia, total_competencia)
                    VALUES (?, ?, ?)
                    RETURNING id_resultado_competencia
                    """, Integer.class,
                    cuestionario.getIdCuestionario(), rc.getCompetencia().getCodigo(),
                    rc.getTotalCompetencia());
            rc.setIdResultadoCompetencia(idResultado == null ? 0 : idResultado);
            for (ResultadoFactor rf : rc.getResultadosFactor()) {
                jdbc.update("""
                        INSERT INTO resultado_factor (id_resultado_competencia, codigo_factor, total_factor)
                        VALUES (?, ?, ?)
                        """, rc.getIdResultadoCompetencia(), rf.getFactor().getCodigo(), rf.getTotalFactor());
            }
        }
    }

    // ---------- Lectura ----------

    /** Trae el cuestionario completo: modelo de la evaluacion, bloques y resultados. */
    @Override
    public Cuestionario buscarPorId(int idCuestionario) {
        List<Cuestionario> encontrados = jdbc.query(SELECT + " WHERE q.id_cuestionario = ?",
                mapeador, idCuestionario);
        if (encontrados.isEmpty()) {
            return null;
        }
        Cuestionario cuestionario = encontrados.get(0);
        Integer idEvaluacion = jdbc.queryForObject(
                "SELECT id_evaluacion FROM cuestionario WHERE id_cuestionario = ?",
                Integer.class, idCuestionario);
        Evaluacion evaluacion = modelo.cargar(idEvaluacion == null ? 0 : idEvaluacion);
        if (evaluacion != null) {
            evaluacion.agregarCuestionario(cuestionario);
        }
        cargarBloques(cuestionario);
        cargarResultados(cuestionario);
        return cuestionario;
    }

    @Override
    public List<Cuestionario> buscarPorCandidato(Candidato candidato) {
        List<Cuestionario> cuestionarios = jdbc.query(SELECT + """
                 WHERE q.nro_candidato = ? ORDER BY q.fecha_hora_creacion DESC
                """, mapeador, candidato.getNroCandidato());
        cuestionarios.forEach(this::cargarResultados);
        return cuestionarios;
    }

    @Override
    public Cuestionario buscarActivoOEnProceso(Candidato candidato) {
        List<Cuestionario> encontrados = jdbc.query(SELECT + """
                 WHERE q.nro_candidato = ? AND q.estado IN ('ACTIVO', 'EN_PROCESO')
                """, mapeador, candidato.getNroCandidato());
        return encontrados.isEmpty() ? null : encontrados.get(0);
    }

    /** Para CU028: alcanza con el estado, las fechas y los resultados de cada cuestionario. */
    @Override
    public List<Cuestionario> buscarPorEvaluacion(Evaluacion evaluacion) {
        List<Cuestionario> cuestionarios = jdbc.query(SELECT + """
                 WHERE q.id_evaluacion = ? ORDER BY c.apellido, c.nombre
                """, mapeador, evaluacion.getIdEvaluacion());
        cuestionarios.forEach(q -> {
            q.setEvaluacion(evaluacion);
            cargarResultados(q);
        });
        return cuestionarios;
    }

    @Override
    public List<Cuestionario> buscarVencidos() {
        return jdbc.query(SELECT + """
                 WHERE (q.estado = 'ACTIVO'
                        AND now() > q.fecha_hora_creacion + (q.plazo_activo * INTERVAL '1 day'))
                    OR (q.estado = 'EN_PROCESO'
                        AND now() > q.fecha_hora_comienzo + (q.tiempo_maximo * INTERVAL '1 minute'))
                """, mapeador);
    }

    private void cargarBloques(Cuestionario cuestionario) {
        Map<Integer, PreguntaEvaluacion> porId = new HashMap<>();
        if (cuestionario.getEvaluacion() != null) {
            cuestionario.getEvaluacion().obtenerModelo()
                    .forEach(pe -> porId.put(pe.getIdPreguntaEvaluacion(), pe));
        }

        Map<Integer, BloquePreguntas> bloques = new HashMap<>();
        List<BloquePreguntas> ordenados = new ArrayList<>();
        jdbc.query("""
                SELECT nro_bloque, respondido FROM bloque_preguntas
                WHERE id_cuestionario = ? ORDER BY nro_bloque
                """, rs -> {
            BloquePreguntas b = new BloquePreguntas(rs.getInt("nro_bloque"));
            b.setRespondido(rs.getBoolean("respondido"));
            bloques.put(b.getNroBloque(), b);
            ordenados.add(b);
        }, cuestionario.getIdCuestionario());

        jdbc.query("""
                SELECT id_pregunta_cuestionario, nro_bloque, id_pregunta_evaluacion, orden_presentacion,
                       id_opcion_elegida, fecha_hora_respuesta, descripcion_opcion_elegida,
                       ponderacion_obtenida
                FROM pregunta_cuestionario
                WHERE id_cuestionario = ? ORDER BY orden_presentacion
                """, rs -> {
            PreguntaEvaluacion pe = porId.get(rs.getInt("id_pregunta_evaluacion"));
            PreguntaCuestionario pc = new PreguntaCuestionario(pe, rs.getInt("orden_presentacion"));
            pc.setIdPreguntaCuestionario(rs.getInt("id_pregunta_cuestionario"));
            pc.setFechaHoraRespuesta(rs.getObject("fecha_hora_respuesta", LocalDateTime.class));
            pc.setDescripcionOpcionElegida(rs.getString("descripcion_opcion_elegida"));
            double ponderacion = rs.getDouble("ponderacion_obtenida");
            pc.setPonderacionObtenida(rs.wasNull() ? null : ponderacion);
            int idOpcion = rs.getInt("id_opcion_elegida");
            if (!rs.wasNull() && pe != null) {
                pe.getOpciones().stream()
                        .filter(o -> o.getIdOpcionEvaluacion() == idOpcion)
                        .findFirst()
                        .ifPresent(pc::setOpcionElegida);
            }
            BloquePreguntas bloque = bloques.get(rs.getInt("nro_bloque"));
            if (bloque != null) {
                bloque.agregarPregunta(pc);
            }
        }, cuestionario.getIdCuestionario());

        cuestionario.setBloques(ordenados);
    }

    private void cargarResultados(Cuestionario cuestionario) {
        Map<Integer, ResultadoCompetencia> porId = new HashMap<>();
        List<ResultadoCompetencia> resultados = new ArrayList<>();
        jdbc.query("""
                SELECT rc.id_resultado_competencia, rc.total_competencia,
                       c.codigo, c.nombre, c.tipo
                FROM resultado_competencia rc
                JOIN competencia c ON c.codigo = rc.codigo_competencia
                WHERE rc.id_cuestionario = ?
                ORDER BY c.nombre
                """, rs -> {
            Competencia c = new Competencia();
            c.setCodigo(rs.getString("codigo"));
            c.setNombre(rs.getString("nombre"));
            c.setTipo(TipoCompetencia.valueOf(rs.getString("tipo")));
            ResultadoCompetencia rc = new ResultadoCompetencia(c);
            rc.setIdResultadoCompetencia(rs.getInt("id_resultado_competencia"));
            rc.setTotalCompetencia(rs.getDouble("total_competencia"));
            porId.put(rc.getIdResultadoCompetencia(), rc);
            resultados.add(rc);
        }, cuestionario.getIdCuestionario());

        if (!resultados.isEmpty()) {
            jdbc.query("""
                    SELECT rf.id_resultado_factor, rf.id_resultado_competencia, rf.total_factor,
                           f.codigo, f.nombre, f.nro_orden
                    FROM resultado_factor rf
                    JOIN factor f ON f.codigo = rf.codigo_factor
                    JOIN resultado_competencia rc
                      ON rc.id_resultado_competencia = rf.id_resultado_competencia
                    WHERE rc.id_cuestionario = ?
                    ORDER BY f.nro_orden
                    """, rs -> {
                Factor f = new Factor();
                f.setCodigo(rs.getString("codigo"));
                f.setNombre(rs.getString("nombre"));
                f.setNroOrden(rs.getInt("nro_orden"));
                ResultadoCompetencia rc = porId.get(rs.getInt("id_resultado_competencia"));
                f.setCompetencia(rc.getCompetencia());
                ResultadoFactor rf = new ResultadoFactor(f, rs.getDouble("total_factor"));
                rf.setIdResultadoFactor(rs.getInt("id_resultado_factor"));
                rc.agregarResultadoFactor(rf);
            }, cuestionario.getIdCuestionario());
        }

        cuestionario.setResultados(resultados);
    }
}
