package ar.edu.utn.frsf.capitalhumano.dao.jdbc;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import ar.edu.utn.frsf.capitalhumano.dao.EvaluacionDAO;
import ar.edu.utn.frsf.capitalhumano.dominio.Evaluacion;
import ar.edu.utn.frsf.capitalhumano.dominio.Puesto;

@Repository
public class EvaluacionDAOImpl implements EvaluacionDAO {

    private final JdbcTemplate jdbc;
    private final ModeloEvaluacionJdbc modelo;

    EvaluacionDAOImpl(JdbcTemplate jdbc, ModeloEvaluacionJdbc modelo) {
        this.jdbc = jdbc;
        this.modelo = modelo;
    }

    /** Guarda la evaluacion y, con el id que genera la base, su copia del modelo. */
    @Override
    public void guardar(Evaluacion evaluacion) {
        Integer id = jdbc.queryForObject("""
                INSERT INTO evaluacion (fecha_creacion, codigo_puesto, nombre_usuario)
                VALUES (?, ?, ?)
                RETURNING id_evaluacion
                """, Integer.class,
                evaluacion.getFechaCreacion() == null ? LocalDateTime.now() : evaluacion.getFechaCreacion(),
                evaluacion.getPuesto().getCodigo(),
                evaluacion.getConsultor().getNombreUsuario());
        evaluacion.setIdEvaluacion(id == null ? 0 : id);
        modelo.guardarModelo(evaluacion);
    }

    @Override
    public List<Evaluacion> buscarPorPuesto(Puesto puesto) {
        return jdbc.query("""
                SELECT id_evaluacion, fecha_creacion, nombre_usuario
                FROM evaluacion
                WHERE codigo_puesto = ?
                ORDER BY fecha_creacion
                """, (rs, fila) -> {
            Evaluacion e = new Evaluacion();
            e.setIdEvaluacion(rs.getInt("id_evaluacion"));
            e.setFechaCreacion(rs.getObject("fecha_creacion", LocalDateTime.class));
            e.setPuesto(puesto);
            return e;
        }, puesto.getCodigo());
    }
}
