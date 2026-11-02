package ar.edu.utn.frsf.capitalhumano.dao.jdbc;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import ar.edu.utn.frsf.capitalhumano.dao.PlantillaRespuestaDAO;
import ar.edu.utn.frsf.capitalhumano.dominio.PlantillaRespuesta;

@Repository
public class PlantillaRespuestaDAOImpl implements PlantillaRespuestaDAO {

    private static final RowMapper<PlantillaRespuesta> MAPEADOR = (rs, fila) -> {
        PlantillaRespuesta p = new PlantillaRespuesta();
        p.setIdPlantilla(rs.getInt("id_plantilla"));
        p.setNombre(rs.getString("nombre"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setEliminado(rs.getBoolean("eliminado"));
        return p;
    };

    private static final String SELECT = """
            SELECT id_plantilla, nombre, descripcion, eliminado FROM plantilla_respuesta
            """;

    private final JdbcTemplate jdbc;

    public PlantillaRespuestaDAOImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void guardar(PlantillaRespuesta plantilla) {
        Integer id = jdbc.queryForObject("""
                INSERT INTO plantilla_respuesta (nombre, descripcion, eliminado)
                VALUES (?, ?, FALSE)
                RETURNING id_plantilla
                """, Integer.class, plantilla.getNombre(), plantilla.getDescripcion());
        plantilla.setIdPlantilla(id == null ? 0 : id);
        guardarItems(plantilla);
    }

    @Override
    public void actualizar(PlantillaRespuesta plantilla) {
        jdbc.update("""
                UPDATE plantilla_respuesta SET nombre = ?, descripcion = ? WHERE id_plantilla = ?
                """, plantilla.getNombre(), plantilla.getDescripcion(), plantilla.getIdPlantilla());
        jdbc.update("DELETE FROM item_plantilla WHERE id_plantilla = ?", plantilla.getIdPlantilla());
        guardarItems(plantilla);
    }

    private void guardarItems(PlantillaRespuesta plantilla) {
        plantilla.obtenerItems().forEach(i -> jdbc.update("""
                INSERT INTO item_plantilla (id_plantilla, orden_visualizacion, descripcion)
                VALUES (?, ?, ?)
                """, plantilla.getIdPlantilla(), i.getOrdenVisualizacion(), i.getDescripcion()));
    }

    @Override
    public PlantillaRespuesta buscarPorId(int idPlantilla) {
        List<PlantillaRespuesta> encontradas = jdbc.query(SELECT + " WHERE id_plantilla = ?",
                MAPEADOR, idPlantilla);
        if (encontradas.isEmpty()) {
            return null;
        }
        PlantillaRespuesta plantilla = encontradas.get(0);
        jdbc.query("""
                SELECT orden_visualizacion, descripcion FROM item_plantilla
                WHERE id_plantilla = ? ORDER BY orden_visualizacion
                """, rs -> {
            plantilla.agregarItem(rs.getInt("orden_visualizacion"), rs.getString("descripcion"));
        }, idPlantilla);
        return plantilla;
    }

    @Override
    public List<PlantillaRespuesta> buscarPorFiltro(String nombre) {
        return jdbc.query(SELECT + """
                 WHERE NOT eliminado
                   AND nombre ILIKE '%' || COALESCE(CAST(? AS TEXT), '') || '%'
                 ORDER BY nombre
                """, MAPEADOR, nombre);
    }

    @Override
    public boolean existeNombre(String nombre, int idPlantilla) {
        Integer cantidad = jdbc.queryForObject("""
                SELECT count(*) FROM plantilla_respuesta
                WHERE NOT eliminado AND upper(nombre) = upper(?) AND id_plantilla <> ?
                """, Integer.class, nombre, idPlantilla);
        return cantidad != null && cantidad > 0;
    }
}
