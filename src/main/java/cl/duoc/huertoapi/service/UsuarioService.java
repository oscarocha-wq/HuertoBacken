package cl.duoc.huertoapi.service;

import cl.duoc.huertoapi.dto.LoginRequest;
import cl.duoc.huertoapi.model.Usuario;
import cl.duoc.huertoapi.model.enums.Perfil;
import cl.duoc.huertoapi.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // 1. Listar todos (o solo un perfil)
    public List<Usuario> findAll(Perfil perfil) {
        return perfil == null ? usuarioRepository.findAll() : usuarioRepository.findByPerfil(perfil);
    }

    // 2. Buscar por ID
    public Usuario buscarPorId(String id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    // 3. Registrar
    public Usuario registrar(Usuario usuario) {
        if (usuario.getId() == null || usuario.getId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El código del usuario es obligatorio");
        }
        usuario.setId(usuario.getId().trim().toUpperCase());
        if (usuarioRepository.existsById(usuario.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese código");
        }
        return usuarioRepository.save(usuario);
    }

    // 4. Actualizar (la clave solo cambia si viene en el cuerpo)
    public Usuario actualizar(String id, Usuario detalles) {
        Usuario usuario = buscarPorId(id);
        usuario.setNombre(detalles.getNombre());
        usuario.setPerfil(detalles.getPerfil());
        usuario.setGrupo(detalles.getGrupo());
        usuario.setActivo(detalles.isActivo());
        if (detalles.getClave() != null && !detalles.getClave().isBlank()) {
            usuario.setClave(detalles.getClave());
        }
        return usuarioRepository.save(usuario);
    }

    // 5. Borrar
    public void borrar(String id) {
        usuarioRepository.delete(buscarPorId(id));
    }

    // 6. Login: devuelve el usuario si el código y la clave son correctos
    public Usuario login(LoginRequest request) {
        String id = request.id() == null ? "" : request.id().trim().toUpperCase();
        return usuarioRepository.findByIdAndClaveAndActivoTrue(id, request.clave())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o clave incorrectos"));
    }
}
