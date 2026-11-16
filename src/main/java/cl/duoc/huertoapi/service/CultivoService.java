package cl.duoc.huertoapi.service;

import cl.duoc.huertoapi.model.Cultivo;
import cl.duoc.huertoapi.repository.CultivoRepository;
import cl.duoc.huertoapi.repository.SectorRepository;
import cl.duoc.huertoapi.repository.TareaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CultivoService {

    @Autowired
    private CultivoRepository cultivoRepository;
    @Autowired
    private SectorRepository sectorRepository;
    @Autowired
    private TareaRepository tareaRepository;

    // 1. Listar todos (o solo los de un sector)
    public List<Cultivo> findAll(Long sectorId) {
        return sectorId == null ? cultivoRepository.findAll() : cultivoRepository.findBySectorId(sectorId);
    }

    // 2. Buscar por ID
    public Cultivo buscarPorId(Long id) {
        return cultivoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cultivo no encontrado"));
    }

    // 3. Registrar
    public Cultivo registrar(Cultivo cultivo) {
        validar(cultivo);
        cultivo.setId(null);
        return cultivoRepository.save(cultivo);
    }

    // 4. Actualizar
    public Cultivo actualizar(Long id, Cultivo detalles) {
        Cultivo cultivo = buscarPorId(id);
        validar(detalles);
        cultivo.setNombre(detalles.getNombre());
        cultivo.setDescripcion(detalles.getDescripcion());
        cultivo.setSectorId(detalles.getSectorId());
        cultivo.setFechaSiembra(detalles.getFechaSiembra());
        cultivo.setFechaTrasplante(detalles.getFechaTrasplante());
        cultivo.setFechaCosechaEstimada(detalles.getFechaCosechaEstimada());
        cultivo.setFrecuenciaRiegoDias(detalles.getFrecuenciaRiegoDias());
        cultivo.setEstado(detalles.getEstado());
        cultivo.setObservaciones(detalles.getObservaciones());
        return cultivoRepository.save(cultivo);
    }

    // 5. Borrar: no se borra un cultivo que tiene tareas
    public void borrar(Long id) {
        Cultivo cultivo = buscarPorId(id);
        long tareas = tareaRepository.countByCultivoId(id);
        if (tareas > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar: el cultivo tiene " + tareas + " tarea(s)");
        }
        cultivoRepository.delete(cultivo);
    }

    /** Mismas reglas que el validador de la app. */
    private void validar(Cultivo c) {
        if (c.getSectorId() == null || !sectorRepository.existsById(c.getSectorId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El sector indicado no existe");
        }
        if (c.getFrecuenciaRiegoDias() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La frecuencia de riego debe ser mayor que 0");
        }
        if (c.getFechaSiembra() != null && c.getFechaCosechaEstimada() != null
                && c.getFechaCosechaEstimada() < c.getFechaSiembra()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cosecha no puede ser antes de la siembra");
        }
    }
}
