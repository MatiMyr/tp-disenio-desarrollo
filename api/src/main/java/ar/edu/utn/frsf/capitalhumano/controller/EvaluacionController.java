package ar.edu.utn.frsf.capitalhumano.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.frsf.capitalhumano.dominio.Consultor;
import ar.edu.utn.frsf.capitalhumano.dto.ArchivoDTO;
import ar.edu.utn.frsf.capitalhumano.dto.CandidatoClaveDTO;
import ar.edu.utn.frsf.capitalhumano.dto.CandidatoGestionDTO;
import ar.edu.utn.frsf.capitalhumano.dto.CompetenciaEvaluableDTO;
import ar.edu.utn.frsf.capitalhumano.gestor.GestorEvaluaciones;

/** CU025. Solo traduce HTTP a llamadas a GestorEvaluaciones. */
@RestController
@RequestMapping("/evaluaciones")
public class EvaluacionController {

    /** Cuerpo del paso 6.A: los candidatos que se quieren evaluar. */
    public record VerificacionDTO(List<Integer> nrosCandidato) {
    }

    /** Cuerpo del paso 11: los candidatos elegidos. */
    public record GeneracionClavesDTO(List<CandidatoGestionDTO> candidatos) {
    }

    /** Cuerpo del paso 13: el puesto y los candidatos con su clave. */
    public record EvaluacionNuevaDTO(String codigoPuesto, List<CandidatoClaveDTO> claves) {
    }

    /** Cuerpo del paso 17: lo que se exporta a Excel. */
    public record ExportacionClavesDTO(String nombrePuesto, List<CandidatoClaveDTO> claves) {
    }

    private final GestorEvaluaciones gestorEvaluaciones;
    private final UsuarioActual usuarioActual;

    public EvaluacionController(GestorEvaluaciones gestorEvaluaciones, UsuarioActual usuarioActual) {
        this.gestorEvaluaciones = gestorEvaluaciones;
        this.usuarioActual = usuarioActual;
    }

    /** Devuelve los candidatos que ya tienen un cuestionario activo o en proceso. */
    @PostMapping("/verificar-candidatos")
    public List<Integer> verificarCandidatos(@RequestBody VerificacionDTO datos) {
        return datos.nrosCandidato().stream()
                .filter(gestorEvaluaciones::tieneCuestionarioActivo)
                .toList();
    }

    @GetMapping("/puestos/{codigo}/competencias")
    public List<CompetenciaEvaluableDTO> competenciasDelPuesto(@PathVariable String codigo) {
        return gestorEvaluaciones.validarCompetenciasEvaluables(codigo);
    }

    @PostMapping("/claves")
    public List<CandidatoClaveDTO> generarClaves(@RequestBody GeneracionClavesDTO datos) {
        return gestorEvaluaciones.generarClaves(datos.candidatos());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void evaluarCandidatos(@RequestBody EvaluacionNuevaDTO datos,
            @RequestHeader(value = "X-Usuario", required = false) String nombreUsuario) {
        Consultor consultor = usuarioActual.resolver(nombreUsuario);
        gestorEvaluaciones.evaluarCandidatos(datos.claves(), datos.codigoPuesto(), consultor);
    }

    @PostMapping("/claves/excel")
    public ResponseEntity<byte[]> exportarClavesExcel(@RequestBody ExportacionClavesDTO datos) {
        ArchivoDTO archivo = gestorEvaluaciones.exportarClavesExcel(datos.claves(), null,
                datos.nombrePuesto());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + archivo.nombre() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(archivo.contenido());
    }
}
