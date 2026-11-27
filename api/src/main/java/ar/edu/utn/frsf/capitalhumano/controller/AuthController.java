package ar.edu.utn.frsf.capitalhumano.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.frsf.capitalhumano.config.ErroresValidacionException;
import ar.edu.utn.frsf.capitalhumano.dominio.Consultor;
import ar.edu.utn.frsf.capitalhumano.dominio.TipoDocumento;
import ar.edu.utn.frsf.capitalhumano.dto.ConsultorAutenticadoDTO;
import ar.edu.utn.frsf.capitalhumano.dto.CredencialesCandidatoDTO;
import ar.edu.utn.frsf.capitalhumano.dto.CredencialesConsultorDTO;
import ar.edu.utn.frsf.capitalhumano.dto.CuestionarioAsignadoDTO;
import ar.edu.utn.frsf.capitalhumano.gestor.GestorAutenticacion;

/** CU001. Solo traduce HTTP a llamadas al gestor. */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final GestorAutenticacion gestorAutenticacion;

    public AuthController(GestorAutenticacion gestorAutenticacion) {
        this.gestorAutenticacion = gestorAutenticacion;
    }

    @PostMapping("/consultor")
    public ConsultorAutenticadoDTO autenticarConsultor(@RequestBody CredencialesConsultorDTO credenciales) {
        List<String> errores = new ArrayList<>();
        if (esVacio(credenciales.usuario())) {
            errores.add("Debe ingresar el usuario");
        }
        if (esVacio(credenciales.contrasena())) {
            errores.add("Debe ingresar la contraseña");
        }
        if (!errores.isEmpty()) {
            throw new ErroresValidacionException(errores);
        }
        Consultor consultor = gestorAutenticacion.autenticarConsultor(
                credenciales.usuario(), credenciales.contrasena());
        return new ConsultorAutenticadoDTO(consultor.getNombreUsuario());
    }

    @PostMapping("/candidato")
    public CuestionarioAsignadoDTO autenticarCandidato(@RequestBody CredencialesCandidatoDTO credenciales) {
        List<String> errores = new ArrayList<>();
        TipoDocumento tipo = null;
        try {
            tipo = TipoDocumento.valueOf(credenciales.tipoDocumento());
        } catch (IllegalArgumentException | NullPointerException e) {
            errores.add("Debe seleccionar el tipo de documento");
        }
        if (esVacio(credenciales.nroDocumento())) {
            errores.add("Debe ingresar el número de documento");
        }
        if (esVacio(credenciales.clave())) {
            errores.add("Debe ingresar la clave de ingreso");
        }
        if (!errores.isEmpty()) {
            throw new ErroresValidacionException(errores);
        }
        int idCuestionario = gestorAutenticacion.autenticarCandidato(
                tipo, credenciales.nroDocumento().trim(), credenciales.clave());
        return new CuestionarioAsignadoDTO(idCuestionario);
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
