package cl.duoc.huertoapi.repository;

import cl.duoc.huertoapi.model.Usuario;
import cl.duoc.huertoapi.model.enums.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    List<Usuario> findByPerfil(Perfil perfil);

    Optional<Usuario> findByIdAndClaveAndActivoTrue(String id, String clave);
}
