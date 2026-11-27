package ar.edu.utn.frsf.capitalhumano.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.frsf.capitalhumano.dto.PuestoDTO;
import ar.edu.utn.frsf.capitalhumano.gestor.GestorPuestos;

/** Búsqueda de puestos: la usa el paso 7 de CU025. */
@RestController
@RequestMapping("/puestos")
public class PuestoController {

    private final GestorPuestos gestorPuestos;

    public PuestoController(GestorPuestos gestorPuestos) {
        this.gestorPuestos = gestorPuestos;
    }

    @GetMapping
    public List<PuestoDTO> buscarPuestos(
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String empresa) {
        return gestorPuestos.buscarPuestos(codigo, nombre, empresa);
    }
}
