package cl.duoc.huertoapi.controller;

import cl.duoc.huertoapi.model.Cultivo;
import cl.duoc.huertoapi.service.CultivoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cultivos")
public class CultivoController {

    @Autowired
    private CultivoService cultivoService;

    // GET /cultivos  o  /cultivos?sectorId=1
    @GetMapping
    public ResponseEntity<List<Cultivo>> listar(@RequestParam(required = false) Long sectorId) {
        return ResponseEntity.ok(cultivoService.findAll(sectorId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cultivo> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(cultivoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Cultivo> registrar(@RequestBody Cultivo cultivo) {
        return new ResponseEntity<>(cultivoService.registrar(cultivo), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cultivo> actualizar(@PathVariable Long id, @RequestBody Cultivo cultivo) {
        return ResponseEntity.ok(cultivoService.actualizar(id, cultivo));
    }

    // 204 si se borró, 409 si tiene tareas
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrar(@PathVariable Long id) {
        cultivoService.borrar(id);
        return ResponseEntity.noContent().build();
    }
}
