package ar.edu.utn.frsf.capitalhumano.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.frsf.capitalhumano.dto.ArchivoDTO;
import ar.edu.utn.frsf.capitalhumano.dto.EvaluacionDTO;
import ar.edu.utn.frsf.capitalhumano.dto.OrdenMeritoDTO;
import ar.edu.utn.frsf.capitalhumano.dto.PuestoDTO;
import ar.edu.utn.frsf.capitalhumano.gestor.GestorReportes;

/** CU028. Solo traduce HTTP a llamadas a GestorReportes. */
@RestController
@RequestMapping("/reportes/orden-merito")
public class ReporteController {

    /** Cuerpo del paso 7: el puesto y las evaluaciones elegidas. */
    public record SolicitudOrdenMeritoDTO(String codigoPuesto, List<Integer> idsEvaluaciones,
            String nombrePuesto) {
    }

    private final GestorReportes gestorReportes;

    public ReporteController(GestorReportes gestorReportes) {
        this.gestorReportes = gestorReportes;
    }

    @GetMapping("/puestos")
    public List<PuestoDTO> buscarPuestos(
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String empresa) {
        return gestorReportes.buscarPuestos(codigo, nombre, empresa);
    }

    @GetMapping("/puestos/{codigo}/evaluaciones")
    public List<EvaluacionDTO> obtenerEvaluaciones(@PathVariable String codigo) {
        return gestorReportes.obtenerEvaluaciones(codigo);
    }

    @PostMapping
    public List<OrdenMeritoDTO> generarOrdenMerito(@RequestBody SolicitudOrdenMeritoDTO datos) {
        return gestorReportes.generarOrdenMerito(datos.codigoPuesto(), datos.idsEvaluaciones());
    }

    @PostMapping("/impresion")
    public ResponseEntity<byte[]> imprimirOrdenMerito(@RequestBody SolicitudOrdenMeritoDTO datos) {
        List<OrdenMeritoDTO> ordenMerito =
                gestorReportes.generarOrdenMerito(datos.codigoPuesto(), datos.idsEvaluaciones());
        ArchivoDTO archivo = gestorReportes.imprimirOrdenMerito(ordenMerito, datos.nombrePuesto());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + archivo.nombre() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(archivo.contenido());
    }
}
