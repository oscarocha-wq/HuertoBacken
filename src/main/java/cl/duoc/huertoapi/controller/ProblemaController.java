package cl.duoc.huertoapi.controller;

import cl.duoc.huertoapi.dto.AtencionProblemaRequest;
import cl.duoc.huertoapi.model.Problema;
import cl.duoc.huertoapi.service.ProblemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/problemas")
public class ProblemaController {

    @Autowired
    private ProblemaService problemaService;

    // GET /problemas  o  /problemas?reportadoPorId=EST-01
    @GetMapping
    public ResponseEntity<List<Problema>> listar(@RequestParam(required = false) String reportadoPorId) {
        return ResponseEntity.ok(problemaService.findAll(reportadoPorId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Problema> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(problemaService.buscarPorId(id));
    }

    // POST /problemas: un estudiante reporta un problema
    @PostMapping
    public ResponseEntity<Problema> registrar(@RequestBody Problema problema) {
        return new ResponseEntity<>(problemaService.registrar(problema), HttpStatus.CREATED);
    }

    // PATCH /problemas/1/estado: el docente lo atiende
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Problema> atender(@PathVariable Long id, @RequestBody AtencionProblemaRequest request) {
        return ResponseEntity.ok(problemaService.atender(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrar(@PathVariable Long id) {
        problemaService.borrar(id);
        return ResponseEntity.noContent().build();
    }
}
