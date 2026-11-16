package cl.duoc.huertoapi.service;

import cl.duoc.huertoapi.model.Sector;
import cl.duoc.huertoapi.repository.CultivoRepository;
import cl.duoc.huertoapi.repository.ProblemaRepository;
import cl.duoc.huertoapi.repository.SectorRepository;
import cl.duoc.huertoapi.repository.TareaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SectorService {

    @Autowired
    private SectorRepository sectorRepository;
    @Autowired
    private CultivoRepository cultivoRepository;
    @Autowired
    private TareaRepository tareaRepository;
    @Autowired
    private ProblemaRepository problemaRepository;

    // 1. Listar todos
    public List<Sector> findAll() {
        return sectorRepository.findAll();
    }

    // 2. Buscar por ID
    public Sector buscarPorId(Long id) {
        return sectorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sector no encontrado"));
    }

    // 3. Registrar: el código no se puede repetir
    public Sector registrar(Sector sector) {
        validar(sector);
        if (sectorRepository.existsByCodigo(sector.getCodigo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un sector con el código " + sector.getCodigo());
        }
        long ahora = System.currentTimeMillis();
        sector.setId(null);
        sector.setFechaCreacion(ahora);
        sector.setFechaActualizacion(ahora);
        return sectorRepository.save(sector);
    }

    // 4. Actualizar
    public Sector actualizar(Long id, Sector detalles) {
        Sector sector = buscarPorId(id);
        validar(detalles);
        if (sectorRepository.existsByCodigoAndIdNot(detalles.getCodigo(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un sector con el código " + detalles.getCodigo());
        }
        sector.setCodigo(detalles.getCodigo());
        sector.setNombre(detalles.getNombre());
        sector.setDescripcion(detalles.getDescripcion());
        sector.setUbicacion(detalles.getUbicacion());
        sector.setEstado(detalles.getEstado());
        sector.setResponsableId(detalles.getResponsableId());
        sector.setFechaActualizacion(System.currentTimeMillis());
        return sectorRepository.save(sector);
    }

    // 5. Borrar: igual que en la app, no se borra un sector con cultivos, tareas o problemas
    public void borrar(Long id) {
        Sector sector = buscarPorId(id);
        long cultivos = cultivoRepository.countBySectorId(id);
        long tareas = tareaRepository.countBySectorId(id);
        long problemas = problemaRepository.countBySectorId(id);
        if (cultivos + tareas + problemas > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar: el sector tiene " + cultivos + " cultivo(s), "
                            + tareas + " tarea(s) y " + problemas + " problema(s)");
        }
        sectorRepository.delete(sector);
    }

    private void validar(Sector s) {
        if (s.getCodigo() == null || s.getCodigo().isBlank() || s.getNombre() == null || s.getNombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El código y el nombre son obligatorios");
        }
        if (s.getEstado() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El estado es obligatorio");
        }
    }
}
