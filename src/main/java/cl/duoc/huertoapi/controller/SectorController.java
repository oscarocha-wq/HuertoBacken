package cl.duoc.huertoapi.controller;

import cl.duoc.huertoapi.model.Cultivo;
import cl.duoc.huertoapi.model.Sector;
import cl.duoc.huertoapi.service.CultivoService;
import cl.duoc.huertoapi.service.SectorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sectores")
public class SectorController {

    @Autowired
    private SectorService sectorService;
    @Autowired
    private CultivoService cultivoService;

    @GetMapping
    public ResponseEntity<List<Sector>> listar() {
        return ResponseEntity.ok(sectorService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sector> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(sectorService.buscarPorId(id));
    }

    // GET /sectores/1/cultivos: cultivos de un sector
    @GetMapping("/{id}/cultivos")
    public ResponseEntity<List<Cultivo>> cultivosDelSector(@PathVariable Long id) {
        sectorService.buscarPorId(id); // 404 si el sector no existe
        return ResponseEntity.ok(cultivoService.findAll(id));
    }

    @PostMapping
    public ResponseEntity<Sector> registrar(@RequestBody Sector sector) {
        return new ResponseEntity<>(sectorService.registrar(sector), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Sector> actualizar(@PathVariable Long id, @RequestBody Sector sector) {
        return ResponseEntity.ok(sectorService.actualizar(id, sector));
    }

    // 204 si se borró, 409 si tiene cultivos, tareas o problemas
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrar(@PathVariable Long id) {
        sectorService.borrar(id);
        return ResponseEntity.noContent().build();
    }
}
