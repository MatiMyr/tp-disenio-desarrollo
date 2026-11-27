package ar.edu.utn.frsf.capitalhumano.externo;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Adaptador de LDAP para esta entrega: en lugar de conectarse a un servidor
 * real, valida contra los usuarios configurados en application.properties.
 * Se reemplaza por el LDAP de la organizacion sin tocar GestorAutenticacion.
 */
@Component
public class AdaptadorLDAP implements ILDAP {

    private final Map<String, String> usuarios = new HashMap<>();

    public AdaptadorLDAP(@Value("${ldap.usuarios:}") String configuracion) {
        for (String entrada : configuracion.split(",")) {
            String[] partes = entrada.trim().split(":", 2);
            if (partes.length == 2 && !partes[0].isBlank()) {
                usuarios.put(partes[0].trim().toLowerCase(), partes[1]);
            }
        }
    }

    @Override
    public boolean validarCredenciales(String usuario, String contrasena) {
        conectar();
        if (usuario == null || contrasena == null) {
            return false;
        }
        String esperada = usuarios.get(usuario.trim().toLowerCase());
        return esperada != null && esperada.equals(contrasena);
    }

    /** El adaptador de prueba no abre ninguna conexion. */
    private void conectar() {
        // Sin servidor de directorio en esta entrega
    }
}
