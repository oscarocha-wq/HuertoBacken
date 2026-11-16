package cl.duoc.huertoapi.service;

import cl.duoc.huertoapi.dto.AtencionProblemaRequest;
import cl.duoc.huertoapi.model.Problema;
import cl.duoc.huertoapi.model.enums.EstadoProblema;
import cl.duoc.huertoapi.repository.ProblemaRepository;
import cl.duoc.huertoapi.repository.SectorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProblemaService {

    @Autowired
    private ProblemaRepository problemaRepository;
    @Autowired
    private SectorRepository sectorRepository;

    // 1. Listar todos (o solo los que reportó un estudiante)
    public List<Problema> findAll(String reportadoPorId) {
        return reportadoPorId == null
                ? problemaRepository.findAll()
                : problemaRepository.findByReportadoPorId(reportadoPorId);
    }

    // 2. Buscar por ID
    public Problema buscarPorId(Long id) {
        return problemaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Problema no encontrado"));
    }

    // 3. Reportar: siempre parte en INFORMADO
    public Problema registrar(Problema problema) {
        if (problema.getTipo() == null || problema.getDescripcion() == null || problema.getDescripcion().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tipo y la descripción son obligatorios");
        }
        if (problema.getSectorId() == null || !sectorRepository.existsById(problema.getSectorId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El sector indicado no existe");
        }
        problema.setId(null);
        problema.setFechaReporte(System.currentTimeMillis());
        problema.setEstado(EstadoProblema.INFORMADO);
        problema.setComentarioDocente(null);
        problema.setAtendidoPorId(null);
        problema.setFechaAtencion(null);
        return problemaRepository.save(problema);
    }

    // 4. Atender: el docente cambia el estado y deja un comentario
    public Problema atender(Long id, AtencionProblemaRequest request) {
        if (request.estado() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debes indicar el nuevo estado");
        }
        Problema problema = buscarPorId(id);
        problema.setEstado(request.estado());
        problema.setComentarioDocente(request.comentarioDocente());
        problema.setAtendidoPorId(request.atendidoPorId());
        problema.setFechaAtencion(System.currentTimeMillis());
        return problemaRepository.save(problema);
    }

    // 5. Borrar
    public void borrar(Long id) {
        problemaRepository.delete(buscarPorId(id));
    }
}
