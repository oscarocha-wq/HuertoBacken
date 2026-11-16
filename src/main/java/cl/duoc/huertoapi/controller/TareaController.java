package cl.duoc.huertoapi.controller;

import cl.duoc.huertoapi.dto.CambioEstadoTareaRequest;
import cl.duoc.huertoapi.model.Tarea;
import cl.duoc.huertoapi.model.enums.EstadoTarea;
import cl.duoc.huertoapi.service.TareaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tareas")
public class TareaController {

    @Autowired
    private TareaService tareaService;

    // GET /tareas
    // GET /tareas?asignadoId=EST-01&grupo=Taller A   (tareas de un estudiante)
    // GET /tareas?estado=PENDIENTE_DE_VALIDACION     (para el docente)
    @GetMapping
    public ResponseEntity<List<Tarea>> listar(
            @RequestParam(required = false) String asignadoId,
            @RequestParam(required = false) String grupo,
            @RequestParam(required = false) EstadoTarea estado) {
        return ResponseEntity.ok(tareaService.findAll(asignadoId, grupo, estado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tarea> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(tareaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Tarea> registrar(@RequestBody Tarea tarea) {
        return new ResponseEntity<>(tareaService.registrar(tarea), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tarea> actualizar(@PathVariable Long id, @RequestBody Tarea tarea) {
        return ResponseEntity.ok(tareaService.actualizar(id, tarea));
    }

    // PATCH /tareas/1/estado  { "estado": "VALIDADA" }
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Tarea> cambiarEstado(@PathVariable Long id, @RequestBody CambioEstadoTareaRequest request) {
        return ResponseEntity.ok(tareaService.cambiarEstado(id, request.estado()));
    }

    // 204 si se borró, 409 si ya fue iniciada
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrar(@PathVariable Long id) {
        tareaService.borrar(id);
        return ResponseEntity.noContent().build();
    }
}
