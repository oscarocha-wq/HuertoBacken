package cl.duoc.huertoapi.service;

import cl.duoc.huertoapi.model.Cultivo;
import cl.duoc.huertoapi.model.enums.EstadoCultivo;
import cl.duoc.huertoapi.repository.CultivoRepository;
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
class CultivoServiceTest {

    @Mock private CultivoRepository cultivoRepository;
    @Mock private SectorRepository sectorRepository;
    @Mock private TareaRepository tareaRepository;

    @InjectMocks
    private CultivoService cultivoService;

    private Cultivo cultivo(Long sectorId, int riego, Long siembra, Long cosecha) {
        return new Cultivo(7L, "Lechuga", "desc", sectorId, siembra, null, cosecha, riego,
                EstadoCultivo.SEMBRADO, "obs");
    }

    @Test
    void findAllConYSinSector() {
        when(cultivoRepository.findAll()).thenReturn(List.of(cultivo(1L, 2, null, null)));
        when(cultivoRepository.findBySectorId(1L)).thenReturn(List.of());
        assertEquals(1, cultivoService.findAll(null).size());
        assertEquals(0, cultivoService.findAll(1L).size());
    }

    @Test
    void buscarPorIdInexistenteDa404() {
        when(cultivoRepository.findById(9L)).thenReturn(Optional.empty());
        assertEquals(HttpStatus.NOT_FOUND,
                assertThrows(ResponseStatusException.class, () -> cultivoService.buscarPorId(9L)).getStatusCode());
    }

    @Test
    void registrarValidoQuitaElId() {
        when(sectorRepository.existsById(1L)).thenReturn(true);
        when(cultivoRepository.save(any(Cultivo.class))).thenAnswer(i -> i.getArgument(0));

        Cultivo r = cultivoService.registrar(cultivo(1L, 2, 100L, 200L));

        assertNull(r.getId());
        assertEquals("Lechuga", r.getNombre());
    }

    @Test
    void registrarSinSectorOSectorInexistenteDa400() {
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> cultivoService.registrar(cultivo(null, 2, null, null))).getStatusCode());

        when(sectorRepository.existsById(99L)).thenReturn(false);
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> cultivoService.registrar(cultivo(99L, 2, null, null))).getStatusCode());
    }

    @Test
    void registrarRiegoInvalidoDa400() {
        when(sectorRepository.existsById(1L)).thenReturn(true);
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> cultivoService.registrar(cultivo(1L, 0, null, null))).getStatusCode());
    }

    @Test
    void registrarCosechaAntesDeSiembraDa400() {
        when(sectorRepository.existsById(1L)).thenReturn(true);
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> cultivoService.registrar(cultivo(1L, 2, 200L, 100L))).getStatusCode());
    }

    @Test
    void registrarSoloConSiembraEsValido() {
        when(sectorRepository.existsById(1L)).thenReturn(true);
        when(cultivoRepository.save(any(Cultivo.class))).thenAnswer(i -> i.getArgument(0));
        assertNotNull(cultivoService.registrar(cultivo(1L, 2, 100L, null)));
    }

    @Test
    void actualizarCambiaLosDatos() {
        when(cultivoRepository.findById(7L)).thenReturn(Optional.of(cultivo(1L, 2, null, null)));
        when(sectorRepository.existsById(2L)).thenReturn(true);
        when(cultivoRepository.save(any(Cultivo.class))).thenAnswer(i -> i.getArgument(0));

        Cultivo cambios = new Cultivo(null, "Tomate", "nuevo", 2L, 10L, 20L, 30L, 3,
                EstadoCultivo.EN_CRECIMIENTO, "ok");
        Cultivo r = cultivoService.actualizar(7L, cambios);

        assertEquals("Tomate", r.getNombre());
        assertEquals(2L, r.getSectorId());
        assertEquals(3, r.getFrecuenciaRiegoDias());
        assertEquals(EstadoCultivo.EN_CRECIMIENTO, r.getEstado());
        assertEquals(20L, r.getFechaTrasplante());
    }

    @Test
    void borrarSinTareas() {
        Cultivo c = cultivo(1L, 2, null, null);
        when(cultivoRepository.findById(7L)).thenReturn(Optional.of(c));
        when(tareaRepository.countByCultivoId(7L)).thenReturn(0L);
        cultivoService.borrar(7L);
        verify(cultivoRepository).delete(c);
    }

    @Test
    void borrarConTareasDa409() {
        when(cultivoRepository.findById(7L)).thenReturn(Optional.of(cultivo(1L, 2, null, null)));
        when(tareaRepository.countByCultivoId(7L)).thenReturn(3L);
        assertEquals(HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class, () -> cultivoService.borrar(7L)).getStatusCode());
        verify(cultivoRepository, never()).delete(any());
    }
}
