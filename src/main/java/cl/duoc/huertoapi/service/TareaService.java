package cl.duoc.huertoapi.service;

import cl.duoc.huertoapi.model.Tarea;
import cl.duoc.huertoapi.model.enums.EstadoTarea;
import cl.duoc.huertoapi.repository.CultivoRepository;
import cl.duoc.huertoapi.repository.SectorRepository;
import cl.duoc.huertoapi.repository.TareaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TareaService {

    @Autowired
    private TareaRepository tareaRepository;
    @Autowired
    private SectorRepository sectorRepository;
    @Autowired
    private CultivoRepository cultivoRepository;

    // 1. Listar: todas, las de un estudiante (y su grupo) o las de un estado
    public List<Tarea> findAll(String asignadoId, String grupo, EstadoTarea estado) {
        if (asignadoId != null) {
            return grupo == null
                    ? tareaRepository.findByAsignadoId(asignadoId)
                    : tareaRepository.findByAsignadoIdOrGrupo(asignadoId, grupo);
        }
        if (estado != null) {
            return tareaRepository.findByEstado(estado);
        }
        return tareaRepository.findAll();
    }

    // 2. Buscar por ID
    public Tarea buscarPorId(Long id) {
        return tareaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarea no encontrada"));
    }

    // 3. Registrar: siempre parte en PENDIENTE
    public Tarea registrar(Tarea tarea) {
        validar(tarea);
        tarea.setId(null);
        tarea.setFechaCreacion(System.currentTimeMillis());
        tarea.setEstado(EstadoTarea.PENDIENTE);
        return tareaRepository.save(tarea);
    }

    // 4. Actualizar los datos de la tarea (no su estado)
    public Tarea actualizar(Long id, Tarea detalles) {
        Tarea tarea = buscarPorId(id);
        validar(detalles);
        tarea.setTipo(detalles.getTipo());
        tarea.setDescripcion(detalles.getDescripcion());
        tarea.setSectorId(detalles.getSectorId());
        tarea.setCultivoId(detalles.getCultivoId());
        tarea.setFechaProgramada(detalles.getFechaProgramada());
        tarea.setAsignadoId(detalles.getAsignadoId());
        tarea.setGrupo(detalles.getGrupo());
        return tareaRepository.save(tarea);
    }

    // 5. Cambiar estado (iniciar, entregar, validar, rechazar)
    public Tarea cambiarEstado(Long id, EstadoTarea nuevoEstado) {
        if (nuevoEstado == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debes indicar el nuevo estado");
        }
        Tarea tarea = buscarPorId(id);
        tarea.setEstado(nuevoEstado);
        return tareaRepository.save(tarea);
    }

    // 6. Borrar: solo tareas que nadie ha empezado
    public void borrar(Long id) {
        Tarea tarea = buscarPorId(id);
        if (tarea.getEstado() != EstadoTarea.PENDIENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo se pueden eliminar tareas pendientes que nadie ha empezado");
        }
        tareaRepository.delete(tarea);
    }

    private void validar(Tarea t) {
        if (t.getTipo() == null || t.getDescripcion() == null || t.getDescripcion().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tipo y las instrucciones son obligatorios");
        }
        if (t.getSectorId() == null || !sectorRepository.existsById(t.getSectorId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El sector indicado no existe");
        }
        if (t.getCultivoId() != null && !cultivoRepository.existsById(t.getCultivoId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El cultivo indicado no existe");
        }
        if (t.getFechaProgramada() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha programada es obligatoria");
        }
        boolean sinEstudiante = t.getAsignadoId() == null || t.getAsignadoId().isBlank();
        boolean sinGrupo = t.getGrupo() == null || t.getGrupo().isBlank();
        if (sinEstudiante && sinGrupo) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Asigna la tarea a un estudiante o a un grupo");
        }
    }
}
