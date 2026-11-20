package cl.duoc.huertoapi.service;

import cl.duoc.huertoapi.model.Sector;
import cl.duoc.huertoapi.model.enums.EstadoSector;
import cl.duoc.huertoapi.repository.CultivoRepository;
import cl.duoc.huertoapi.repository.ProblemaRepository;
import cl.duoc.huertoapi.repository.SectorRepository;
import cl.duoc.huertoapi.repository.TareaRepository;
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
class SectorServiceTest {

    @Mock private SectorRepository sectorRepository;
    @Mock private CultivoRepository cultivoRepository;
    @Mock private TareaRepository tareaRepository;
    @Mock private ProblemaRepository problemaRepository;

    @InjectMocks
    private SectorService sectorService;

    private Sector sector(String codigo, String nombre, EstadoSector estado) {
        return new Sector(5L, codigo, nombre, "desc", "ubic", estado, "ADM-01", 1L, 1L);
    }

    @Test
    void findAllDevuelveLosSectores() {
        when(sectorRepository.findAll()).thenReturn(List.of(sector("S-01", "Norte", EstadoSector.EN_USO)));
        assertEquals(1, sectorService.findAll().size());
    }

    @Test
    void buscarPorIdInexistenteDa404() {
        when(sectorRepository.findById(99L)).thenReturn(Optional.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> sectorService.buscarPorId(99L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void registrarAsignaFechasYQuitaElId() {
        when(sectorRepository.existsByCodigo("S-09")).thenReturn(false);
        when(sectorRepository.save(any(Sector.class))).thenAnswer(i -> i.getArgument(0));

        Sector r = sectorService.registrar(sector("S-09", "Nuevo", EstadoSector.DISPONIBLE));

        assertNull(r.getId());
        assertNotNull(r.getFechaCreacion());
        assertEquals(r.getFechaCreacion(), r.getFechaActualizacion());
    }

    @Test
    void registrarCodigoRepetidoDa409() {
        when(sectorRepository.existsByCodigo("S-01")).thenReturn(true);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> sectorService.registrar(sector("S-01", "Norte", EstadoSector.EN_USO)));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void validacionesDeCamposObligatorios() {
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> sectorService.registrar(sector(null, "Norte", EstadoSector.EN_USO))).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> sectorService.registrar(sector(" ", "Norte", EstadoSector.EN_USO))).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> sectorService.registrar(sector("S-01", null, EstadoSector.EN_USO))).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> sectorService.registrar(sector("S-01", " ", EstadoSector.EN_USO))).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> sectorService.registrar(sector("S-01", "Norte", null))).getStatusCode());
        verify(sectorRepository, never()).save(any());
    }

    @Test
    void actualizarCambiaLosDatos() {
        when(sectorRepository.findById(5L)).thenReturn(Optional.of(sector("S-01", "Norte", EstadoSector.EN_USO)));
        when(sectorRepository.existsByCodigoAndIdNot("S-05", 5L)).thenReturn(false);
        when(sectorRepository.save(any(Sector.class))).thenAnswer(i -> i.getArgument(0));

        Sector r = sectorService.actualizar(5L, sector("S-05", "Renombrado", EstadoSector.FUERA_DE_USO));

        assertEquals("S-05", r.getCodigo());
        assertEquals("Renombrado", r.getNombre());
        assertEquals(EstadoSector.FUERA_DE_USO, r.getEstado());
    }

    @Test
    void actualizarConCodigoDeOtroSectorDa409() {
        when(sectorRepository.findById(5L)).thenReturn(Optional.of(sector("S-01", "Norte", EstadoSector.EN_USO)));
        when(sectorRepository.existsByCodigoAndIdNot("S-02", 5L)).thenReturn(true);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> sectorService.actualizar(5L, sector("S-02", "Norte", EstadoSector.EN_USO)));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void borrarSectorVacio() {
        Sector s = sector("S-01", "Norte", EstadoSector.EN_USO);
        when(sectorRepository.findById(5L)).thenReturn(Optional.of(s));
        when(cultivoRepository.countBySectorId(5L)).thenReturn(0L);
        when(tareaRepository.countBySectorId(5L)).thenReturn(0L);
        when(problemaRepository.countBySectorId(5L)).thenReturn(0L);

        sectorService.borrar(5L);

        verify(sectorRepository).delete(s);
    }

    @Test
    void borrarSectorConDatosDa409() {
        when(sectorRepository.findById(5L)).thenReturn(Optional.of(sector("S-01", "Norte", EstadoSector.EN_USO)));
        when(cultivoRepository.countBySectorId(5L)).thenReturn(2L);
        when(tareaRepository.countBySectorId(5L)).thenReturn(1L);
        when(problemaRepository.countBySectorId(5L)).thenReturn(0L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> sectorService.borrar(5L));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(sectorRepository, never()).delete(any());
    }
}
