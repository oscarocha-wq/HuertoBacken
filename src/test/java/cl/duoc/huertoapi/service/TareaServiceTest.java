package cl.duoc.huertoapi.service;

import cl.duoc.huertoapi.model.Tarea;
import cl.duoc.huertoapi.model.enums.EstadoTarea;
import cl.duoc.huertoapi.model.enums.TipoTarea;
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
class TareaServiceTest {

    @Mock private TareaRepository tareaRepository;
    @Mock private SectorRepository sectorRepository;
    @Mock private CultivoRepository cultivoRepository;

    @InjectMocks
    private TareaService tareaService;

    private Tarea tarea(TipoTarea tipo, String desc, Long sectorId, Long cultivoId, Long fecha,
                        String asignado, String grupo, EstadoTarea estado) {
        return new Tarea(3L, tipo, desc, sectorId, cultivoId, 1L, fecha, asignado, grupo, "ADM-01", estado);
    }

    private Tarea valida() {
        return tarea(TipoTarea.RIEGO, "Regar", 1L, null, 500L, "EST-01", null, EstadoTarea.VALIDADA);
    }

    private HttpStatus estadoDelError(Tarea t) {
        return (HttpStatus) assertThrows(ResponseStatusException.class, () -> tareaService.registrar(t)).getStatusCode();
    }

    @Test
    void findAllTodasLasVariantes() {
        when(tareaRepository.findByAsignadoId("EST-01")).thenReturn(List.of(valida()));
        when(tareaRepository.findByAsignadoIdOrGrupo("EST-01", "Taller A")).thenReturn(List.of(valida(), valida()));
        when(tareaRepository.findByEstado(EstadoTarea.PENDIENTE)).thenReturn(List.of());
        when(tareaRepository.findAll()).thenReturn(List.of(valida(), valida(), valida()));

        assertEquals(1, tareaService.findAll("EST-01", null, null).size());
        assertEquals(2, tareaService.findAll("EST-01", "Taller A", null).size());
        assertEquals(0, tareaService.findAll(null, null, EstadoTarea.PENDIENTE).size());
        assertEquals(3, tareaService.findAll(null, null, null).size());
    }

    @Test
    void buscarPorIdInexistenteDa404() {
        when(tareaRepository.findById(9L)).thenReturn(Optional.empty());
        assertEquals(HttpStatus.NOT_FOUND,
                assertThrows(ResponseStatusException.class, () -> tareaService.buscarPorId(9L)).getStatusCode());
    }

    @Test
    void registrarSiemprePartePendiente() {
        when(sectorRepository.existsById(1L)).thenReturn(true);
        when(tareaRepository.save(any(Tarea.class))).thenAnswer(i -> i.getArgument(0));

        Tarea r = tareaService.registrar(valida());

        assertNull(r.getId());
        assertEquals(EstadoTarea.PENDIENTE, r.getEstado());
        assertNotNull(r.getFechaCreacion());
    }

    @Test
    void registrarAsignadaSoloAGrupoConCultivo() {
        when(sectorRepository.existsById(1L)).thenReturn(true);
        when(cultivoRepository.existsById(2L)).thenReturn(true);
        when(tareaRepository.save(any(Tarea.class))).thenAnswer(i -> i.getArgument(0));

        Tarea t = tarea(TipoTarea.MANTENIMIENTO, "Reparar", 1L, 2L, 500L, " ", "Taller A", null);
        assertEquals("Taller A", tareaService.registrar(t).getGrupo());
    }

    @Test
    void validacionesBasicas() {
        assertEquals(HttpStatus.BAD_REQUEST, estadoDelError(tarea(null, "x", 1L, null, 1L, "E", null, null)));
        assertEquals(HttpStatus.BAD_REQUEST, estadoDelError(tarea(TipoTarea.RIEGO, null, 1L, null, 1L, "E", null, null)));
        assertEquals(HttpStatus.BAD_REQUEST, estadoDelError(tarea(TipoTarea.RIEGO, " ", 1L, null, 1L, "E", null, null)));
        assertEquals(HttpStatus.BAD_REQUEST, estadoDelError(tarea(TipoTarea.RIEGO, "x", null, null, 1L, "E", null, null)));
    }

    @Test
    void sectorInexistenteDa400() {
        when(sectorRepository.existsById(99L)).thenReturn(false);
        assertEquals(HttpStatus.BAD_REQUEST, estadoDelError(tarea(TipoTarea.RIEGO, "x", 99L, null, 1L, "E", null, null)));
    }

    @Test
    void cultivoInexistenteDa400() {
        when(sectorRepository.existsById(1L)).thenReturn(true);
        when(cultivoRepository.existsById(99L)).thenReturn(false);
        assertEquals(HttpStatus.BAD_REQUEST, estadoDelError(tarea(TipoTarea.RIEGO, "x", 1L, 99L, 1L, "E", null, null)));
    }

    @Test
    void sinFechaOSinAsignadoDa400() {
        when(sectorRepository.existsById(1L)).thenReturn(true);
        assertEquals(HttpStatus.BAD_REQUEST, estadoDelError(tarea(TipoTarea.RIEGO, "x", 1L, null, null, "E", null, null)));
        assertEquals(HttpStatus.BAD_REQUEST, estadoDelError(tarea(TipoTarea.RIEGO, "x", 1L, null, 1L, null, null, null)));
        assertEquals(HttpStatus.BAD_REQUEST, estadoDelError(tarea(TipoTarea.RIEGO, "x", 1L, null, 1L, " ", " ", null)));
        verify(tareaRepository, never()).save(any());
    }

    @Test
    void actualizarNoCambiaElEstado() {
        when(tareaRepository.findById(3L)).thenReturn(Optional.of(
                tarea(TipoTarea.RIEGO, "Regar", 1L, null, 500L, "EST-01", null, EstadoTarea.EN_DESARROLLO)));
        when(sectorRepository.existsById(1L)).thenReturn(true);
        when(tareaRepository.save(any(Tarea.class))).thenAnswer(i -> i.getArgument(0));

        Tarea cambios = tarea(TipoTarea.COSECHA, "Cosechar", 1L, null, 900L, "EST-02", "Taller B", EstadoTarea.VALIDADA);
        Tarea r = tareaService.actualizar(3L, cambios);

        assertEquals(TipoTarea.COSECHA, r.getTipo());
        assertEquals("EST-02", r.getAsignadoId());
        assertEquals(900L, r.getFechaProgramada());
        assertEquals(EstadoTarea.EN_DESARROLLO, r.getEstado());
    }

    @Test
    void cambiarEstado() {
        when(tareaRepository.findById(3L)).thenReturn(Optional.of(valida()));
        when(tareaRepository.save(any(Tarea.class))).thenAnswer(i -> i.getArgument(0));
        assertEquals(EstadoTarea.RECHAZADA, tareaService.cambiarEstado(3L, EstadoTarea.RECHAZADA).getEstado());
    }

    @Test
    void cambiarEstadoNuloDa400() {
        assertEquals(HttpStatus.BAD_REQUEST,
                assertThrows(ResponseStatusException.class, () -> tareaService.cambiarEstado(3L, null)).getStatusCode());
    }

    @Test
    void borrarPendiente() {
        Tarea t = tarea(TipoTarea.RIEGO, "x", 1L, null, 1L, "E", null, EstadoTarea.PENDIENTE);
        when(tareaRepository.findById(3L)).thenReturn(Optional.of(t));
        tareaService.borrar(3L);
        verify(tareaRepository).delete(t);
    }

    @Test
    void borrarIniciadaDa409() {
        when(tareaRepository.findById(3L)).thenReturn(Optional.of(valida()));
        assertEquals(HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class, () -> tareaService.borrar(3L)).getStatusCode());
        verify(tareaRepository, never()).delete(any());
    }
}
