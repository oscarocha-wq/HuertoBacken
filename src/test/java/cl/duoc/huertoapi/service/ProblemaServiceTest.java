package cl.duoc.huertoapi.service;

import cl.duoc.huertoapi.dto.AtencionProblemaRequest;
import cl.duoc.huertoapi.model.Problema;
import cl.duoc.huertoapi.model.enums.EstadoProblema;
import cl.duoc.huertoapi.model.enums.TipoProblema;
import cl.duoc.huertoapi.repository.ProblemaRepository;
import cl.duoc.huertoapi.repository.SectorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProblemaServiceTest {

    @Mock private ProblemaRepository problemaRepository;
    @Mock private SectorRepository sectorRepository;

    @InjectMocks
    private ProblemaService problemaService;

    private Problema problema(TipoProblema tipo, String desc, Long sectorId) {
        return new Problema(4L, tipo, desc, sectorId, null, "EST-01", 1L, EstadoProblema.CERRADO,
                "comentario viejo", "ADM-01", 2L);
    }

    @Test
    void findAllConYSinFiltro() {
        when(problemaRepository.findAll()).thenReturn(List.of(problema(TipoProblema.OTRO, "x", 1L)));
        when(problemaRepository.findByReportadoPorId("EST-01")).thenReturn(List.of());
        assertEquals(1, problemaService.findAll(null).size());
        assertEquals(0, problemaService.findAll("EST-01").size());
    }

    @Test
    void buscarPorIdInexistenteDa404() {
        when(problemaRepository.findById(9L)).thenReturn(Optional.empty());
        assertEquals(HttpStatus.NOT_FOUND,
                assertThrows(ResponseStatusException.class, () -> problemaService.buscarPorId(9L)).getStatusCode());
    }

    @Test
    void registrarPartesiempreInformadoYLimpio() {
        when(sectorRepository.existsById(1L)).thenReturn(true);
        when(problemaRepository.save(any(Problema.class))).thenAnswer(i -> i.getArgument(0));

        Problema r = problemaService.registrar(problema(TipoProblema.FALTA_AGUA, "Tierra seca", 1L));

        assertNull(r.getId());
        assertEquals(EstadoProblema.INFORMADO, r.getEstado());
        assertNull(r.getComentarioDocente());
        assertNull(r.getAtendidoPorId());
        assertNull(r.getFechaAtencion());
    }

    @Test
    void registrarValidaCamposObligatorios() {
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> problemaService.registrar(problema(null, "x", 1L))).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> problemaService.registrar(problema(TipoProblema.OTRO, null, 1L))).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> problemaService.registrar(problema(TipoProblema.OTRO, " ", 1L))).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> problemaService.registrar(problema(TipoProblema.OTRO, "x", null))).getStatusCode());

        when(sectorRepository.existsById(99L)).thenReturn(false);
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> problemaService.registrar(problema(TipoProblema.OTRO, "x", 99L))).getStatusCode());
        verify(problemaRepository, never()).save(any());
    }

    @Test
    void atenderGuardaEstadoYComentario() {
        when(problemaRepository.findById(4L)).thenReturn(Optional.of(problema(TipoProblema.OTRO, "x", 1L)));
        when(problemaRepository.save(any(Problema.class))).thenAnswer(i -> i.getArgument(0));

        Problema r = problemaService.atender(4L,
                new AtencionProblemaRequest(EstadoProblema.EN_REVISION, "Lo vemos el jueves", "ADM-01"));

        assertEquals(EstadoProblema.EN_REVISION, r.getEstado());
        assertEquals("Lo vemos el jueves", r.getComentarioDocente());
        assertNotNull(r.getFechaAtencion());
    }

    @Test
    void atenderSinEstadoDa400() {
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> problemaService.atender(4L, new AtencionProblemaRequest(null, "x", "ADM-01"))).getStatusCode());
    }

    @Test
    void borrarElimina() {
        Problema p = problema(TipoProblema.OTRO, "x", 1L);
        when(problemaRepository.findById(4L)).thenReturn(Optional.of(p));
        problemaService.borrar(4L);
        verify(problemaRepository).delete(p);
    }
}
