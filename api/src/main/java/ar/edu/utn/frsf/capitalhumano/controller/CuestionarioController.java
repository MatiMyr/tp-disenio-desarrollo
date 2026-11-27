package ar.edu.utn.frsf.capitalhumano.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.frsf.capitalhumano.dto.BloqueRespondidoDTO;
import ar.edu.utn.frsf.capitalhumano.dto.CandidatoCuestionarioDTO;
import ar.edu.utn.frsf.capitalhumano.dto.CuestionarioWizardDTO;
import ar.edu.utn.frsf.capitalhumano.dto.RespuestaSeleccionadaDTO;
import ar.edu.utn.frsf.capitalhumano.gestor.GestorCuestionarios;

/** CU026. Solo traduce HTTP a llamadas a GestorCuestionarios. */
@RestController
@RequestMapping("/cuestionarios")
public class CuestionarioController {

    /** Cuerpo del envío de un bloque: el id y el número vienen en la ruta. */
    public record RespuestasBloqueDTO(List<RespuestaSeleccionadaDTO> respuestas) {
    }

    private final GestorCuestionarios gestorCuestionarios;

    public CuestionarioController(GestorCuestionarios gestorCuestionarios) {
        this.gestorCuestionarios = gestorCuestionarios;
    }

    @GetMapping("/{id}")
    public CandidatoCuestionarioDTO recuperarCuestionario(@PathVariable int id) {
        return gestorCuestionarios.recuperarCuestionario(id);
    }

    @PostMapping("/{id}/comenzar")
    public CuestionarioWizardDTO comenzar(@PathVariable int id) {
        return gestorCuestionarios.comenzar(id);
    }

    @PostMapping("/{id}/bloques/{nroBloque}")
    public CuestionarioWizardDTO responderBloque(@PathVariable int id, @PathVariable int nroBloque,
            @RequestBody RespuestasBloqueDTO datos) {
        return gestorCuestionarios.responderBloque(
                new BloqueRespondidoDTO(id, nroBloque, datos.respuestas()));
    }
}
