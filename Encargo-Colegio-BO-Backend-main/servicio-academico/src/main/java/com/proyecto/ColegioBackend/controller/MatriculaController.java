package com.proyecto.ColegioBackend.controller;

import com.proyecto.ColegioBackend.model.Matricula;
import com.proyecto.ColegioBackend.service.AcademicoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/academico/matriculas")
public class MatriculaController {

    private final AcademicoService academicoService;

    private static final String ERROR_KEY = "error";

    public MatriculaController(AcademicoService academicoService) {
        this.academicoService = academicoService;
    }

    @PostMapping("/registrar")
    public ResponseEntity<Object> registrarMatricula(@RequestBody Map<String, Object> body) {
        try {
            Object usuarioIdObj = body.get("usuarioId");
            Object cursoIdObj = body.get("cursoId");
            
            if (usuarioIdObj == null || cursoIdObj == null) {
                return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "Los campos usuarioId y cursoId son requeridos"));
            }
            
            Long usuarioId = Long.valueOf(usuarioIdObj.toString());
            Long cursoId = Long.valueOf(cursoIdObj.toString());
            
            Matricula matricula = academicoService.matricularAlumno(usuarioId, cursoId);
            return new ResponseEntity<>(matricula, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(ERROR_KEY, e.getMessage()));
        }
    }

    @GetMapping("/curso/{cursoId}")
    public ResponseEntity<List<Matricula>> listarPorCurso(@PathVariable Long cursoId) {
        return ResponseEntity.ok(academicoService.listarMatriculasPorCurso(cursoId));
    }

    @GetMapping("/alumno/{usuarioId}")
    public ResponseEntity<List<Matricula>> listarPorAlumno(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(academicoService.listarMatriculasPorAlumno(usuarioId));
    }

    @DeleteMapping("/alumno/{usuarioId}")
    public ResponseEntity<Void> eliminarMatriculasPorAlumno(@PathVariable Long usuarioId) {
        academicoService.eliminarMatriculasPorAlumno(usuarioId);
        return ResponseEntity.noContent().build();
    }
}
