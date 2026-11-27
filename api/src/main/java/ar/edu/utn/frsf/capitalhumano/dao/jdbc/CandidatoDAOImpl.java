package ar.edu.utn.frsf.capitalhumano.dao.jdbc;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import ar.edu.utn.frsf.capitalhumano.dao.CandidatoDAO;
import ar.edu.utn.frsf.capitalhumano.dao.ResultadoImportacion;
import ar.edu.utn.frsf.capitalhumano.dominio.Candidato;
import ar.edu.utn.frsf.capitalhumano.dominio.TipoDocumento;

@Repository
public class CandidatoDAOImpl implements CandidatoDAO {

    static final RowMapper<Candidato> MAPEADOR = (rs, fila) -> {
        Candidato c = new Candidato();
        c.setNroCandidato(rs.getInt("nro_candidato"));
        c.setApellido(rs.getString("apellido"));
        c.setNombre(rs.getString("nombre"));
        c.setTipoDocumento(TipoDocumento.valueOf(rs.getString("tipo_documento")));
        c.setNroDocumento(rs.getString("nro_documento"));
        c.setFechaNacimiento(rs.getObject("fecha_nacimiento", java.time.LocalDate.class));
        String genero = rs.getString("genero");
        c.setGenero(genero == null || genero.isEmpty() ? ' ' : genero.charAt(0));
        c.setNacionalidad(rs.getString("nacionalidad"));
        c.setEmail(rs.getString("email"));
        c.setEscolaridad(rs.getString("escolaridad"));
        c.setEliminado(rs.getBoolean("eliminado"));
        return c;
    };

    static final String SELECT = """
            SELECT nro_candidato, apellido, nombre, tipo_documento, nro_documento, fecha_nacimiento,
                   genero, nacionalidad, email, escolaridad, eliminado
            FROM candidato
            """;

    private final JdbcTemplate jdbc;

    public CandidatoDAOImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public ResultadoImportacion importar(List<Candidato> candidatos) {
        ResultadoImportacion resultado = new ResultadoImportacion();
        for (Candidato c : candidatos) {
            boolean existe = buscarPorNroCandidato(c.getNroCandidato()) != null;
            guardar(c);
            if (existe) {
                resultado.setCandidatosActualizados(resultado.getCandidatosActualizados() + 1);
            } else {
                resultado.setCandidatosCreados(resultado.getCandidatosCreados() + 1);
            }
        }
        return resultado;
    }

    @Override
    public Candidato buscarPorNroCandidato(int nroCandidato) {
        List<Candidato> encontrados = jdbc.query(SELECT + " WHERE nro_candidato = ?",
                MAPEADOR, nroCandidato);
        return encontrados.isEmpty() ? null : encontrados.get(0);
    }

    @Override
    public Candidato buscarPorTipoYNroDocumento(TipoDocumento tipo, String nroDocumento) {
        List<Candidato> encontrados = jdbc.query(SELECT + """
                 WHERE NOT eliminado AND tipo_documento = ? AND nro_documento = ?
                """, MAPEADOR, tipo.name(), nroDocumento);
        return encontrados.isEmpty() ? null : encontrados.get(0);
    }

    @Override
    public List<Candidato> buscarPorCriterios(String apellido, String nombre, Integer nroCandidato) {
        return jdbc.query(SELECT + """
                 WHERE NOT eliminado
                   AND apellido ILIKE '%' || COALESCE(CAST(? AS TEXT), '') || '%'
                   AND nombre   ILIKE '%' || COALESCE(CAST(? AS TEXT), '') || '%'
                   AND (CAST(? AS INTEGER) IS NULL OR nro_candidato = CAST(? AS INTEGER))
                 ORDER BY apellido, nombre
                """, MAPEADOR, apellido, nombre, nroCandidato, nroCandidato);
    }

    @Override
    public void guardar(Candidato candidato) {
        jdbc.update("""
                INSERT INTO candidato (nro_candidato, apellido, nombre, tipo_documento, nro_documento,
                                       fecha_nacimiento, genero, nacionalidad, email, escolaridad, eliminado)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (nro_candidato) DO UPDATE SET
                    apellido = EXCLUDED.apellido, nombre = EXCLUDED.nombre,
                    tipo_documento = EXCLUDED.tipo_documento, nro_documento = EXCLUDED.nro_documento,
                    fecha_nacimiento = EXCLUDED.fecha_nacimiento, genero = EXCLUDED.genero,
                    nacionalidad = EXCLUDED.nacionalidad, email = EXCLUDED.email,
                    escolaridad = EXCLUDED.escolaridad, eliminado = EXCLUDED.eliminado
                """,
                candidato.getNroCandidato(), candidato.getApellido(), candidato.getNombre(),
                candidato.getTipoDocumento().name(), candidato.getNroDocumento(),
                candidato.getFechaNacimiento(), String.valueOf(candidato.getGenero()),
                candidato.getNacionalidad(), candidato.getEmail(), candidato.getEscolaridad(),
                candidato.isEliminado());
    }
}
