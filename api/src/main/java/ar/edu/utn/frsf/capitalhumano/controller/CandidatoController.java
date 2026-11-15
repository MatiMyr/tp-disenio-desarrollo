package ar.edu.utn.frsf.capitalhumano.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.frsf.capitalhumano.dto.CandidatoGestionDTO;
import ar.edu.utn.frsf.capitalhumano.gestor.GestorCandidatos;

/** Búsqueda de candidatos: la usa el paso 3 de CU025. */
@RestController
@RequestMapping("/candidatos")
public class CandidatoController {

    private final GestorCandidatos gestorCandidatos;

    public CandidatoController(GestorCandidatos gestorCandidatos) {
        this.gestorCandidatos = gestorCandidatos;
    }

    @GetMapping
    public List<CandidatoGestionDTO> buscarCandidatos(
            @RequestParam(required = false) String apellido,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Integer nroCandidato) {
        return gestorCandidatos.buscarCandidatos(apellido, nombre, nroCandidato);
    }
}
