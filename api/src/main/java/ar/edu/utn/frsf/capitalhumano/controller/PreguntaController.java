package ar.edu.utn.frsf.capitalhumano.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.frsf.capitalhumano.config.ErroresValidacionException;
import ar.edu.utn.frsf.capitalhumano.dto.CompetenciaDTO;
import ar.edu.utn.frsf.capitalhumano.dto.FactorDTO;
import ar.edu.utn.frsf.capitalhumano.dto.FiltroPreguntaDTO;
import ar.edu.utn.frsf.capitalhumano.dto.PreguntaDetalleDTO;
import ar.edu.utn.frsf.capitalhumano.dto.PreguntaDTO;
import ar.edu.utn.frsf.capitalhumano.gestor.GestorPreguntas;

/** CU020 y CU021. Solo traduce HTTP a llamadas a GestorPreguntas. */
@RestController
@RequestMapping("/preguntas")
public class PreguntaController {

    private final GestorPreguntas gestorPreguntas;

    public PreguntaController(GestorPreguntas gestorPreguntas) {
        this.gestorPreguntas = gestorPreguntas;
    }

    @GetMapping
    public List<PreguntaDTO> buscarPreguntas(
            @RequestParam(required = false) String competencia,
            @RequestParam(required = false) String factor,
            @RequestParam(required = false) String nombre) {
        return gestorPreguntas.buscarPreguntas(new FiltroPreguntaDTO(competencia, factor, nombre));
    }

    @GetMapping("/competencias")
    public List<CompetenciaDTO> obtenerCompetencias() {
        return gestorPreguntas.obtenerCompetencias();
    }

    @GetMapping("/competencias/{codigo}/factores")
    public List<FactorDTO> obtenerFactores(@PathVariable String codigo) {
        return gestorPreguntas.obtenerFactores(codigo);
    }

    /** Si el gestor devuelve errores, la respuesta es 400 con todos juntos. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void altaPregunta(@RequestBody PreguntaDetalleDTO datos) {
        List<String> errores = gestorPreguntas.altaPregunta(datos);
        if (!errores.isEmpty()) {
            throw new ErroresValidacionException(errores);
        }
    }
}
