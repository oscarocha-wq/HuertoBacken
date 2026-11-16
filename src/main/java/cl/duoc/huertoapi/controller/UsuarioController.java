package cl.duoc.huertoapi.controller;

import cl.duoc.huertoapi.dto.LoginRequest;
import cl.duoc.huertoapi.model.Usuario;
import cl.duoc.huertoapi.model.enums.Perfil;
import cl.duoc.huertoapi.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // GET /usuarios  o  /usuarios?perfil=ESTUDIANTE
    @GetMapping
    public ResponseEntity<List<Usuario>> listar(@RequestParam(required = false) Perfil perfil) {
        return ResponseEntity.ok(usuarioService.findAll(perfil));
    }

    // GET /usuarios/EST-01
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    // POST /usuarios
    @PostMapping
    public ResponseEntity<Usuario> registrar(@RequestBody Usuario usuario) {
        return new ResponseEntity<>(usuarioService.registrar(usuario), HttpStatus.CREATED);
    }

    // PUT /usuarios/EST-01
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizar(@PathVariable String id, @RequestBody Usuario usuario) {
        return ResponseEntity.ok(usuarioService.actualizar(id, usuario));
    }

    // DELETE /usuarios/EST-01
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrar(@PathVariable String id) {
        usuarioService.borrar(id);
        return ResponseEntity.noContent().build();
    }

    // POST /usuarios/login  { "id": "ADM-01", "clave": "1234" }  -> 200 con el usuario, o 401
    @PostMapping("/login")
    public ResponseEntity<Usuario> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(usuarioService.login(request));
    }
}
