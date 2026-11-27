package ar.edu.utn.frsf.capitalhumano.dao.jdbc;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import ar.edu.utn.frsf.capitalhumano.dao.IConfiguracionDAO;

@Repository
public class ConfiguracionDAOImpl implements IConfiguracionDAO {

    private static final String CLAVE_INSTRUCCIONES = "instrucciones";
    private static final String CLAVE_PREGUNTAS_POR_BLOQUE = "preguntas_por_bloque";

    private final JdbcTemplate jdbc;

    public ConfiguracionDAOImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String obtenerInstrucciones() {
        return (String) obtenerParametro(CLAVE_INSTRUCCIONES);
    }

    @Override
    public Object obtenerParametro(String clave) {
        List<String> valores = jdbc.queryForList(
                "SELECT valor FROM parametro WHERE clave = ?", String.class, clave);
        return valores.isEmpty() ? null : valores.get(0);
    }

    @Override
    public int obtenerCantidadPreguntasPorBloque() {
        Object valor = obtenerParametro(CLAVE_PREGUNTAS_POR_BLOQUE);
        if (valor == null) {
            throw new IllegalStateException("Falta el parametro " + CLAVE_PREGUNTAS_POR_BLOQUE);
        }
        return Integer.parseInt(valor.toString());
    }
}
