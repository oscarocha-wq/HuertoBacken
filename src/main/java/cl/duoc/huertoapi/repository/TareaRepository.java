package cl.duoc.huertoapi.repository;

import cl.duoc.huertoapi.model.Tarea;
import cl.duoc.huertoapi.model.enums.EstadoTarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {

    List<Tarea> findByAsignadoId(String asignadoId);

    /** Tareas de un estudiante: las suyas o las de su grupo. */
    List<Tarea> findByAsignadoIdOrGrupo(String asignadoId, String grupo);

    List<Tarea> findByEstado(EstadoTarea estado);

    long countBySectorId(Long sectorId);

    long countByCultivoId(Long cultivoId);
}
