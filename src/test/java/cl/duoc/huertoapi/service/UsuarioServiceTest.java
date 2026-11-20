package cl.duoc.huertoapi.service;

import cl.duoc.huertoapi.dto.LoginRequest;
import cl.duoc.huertoapi.model.Usuario;
import cl.duoc.huertoapi.model.enums.Perfil;
import cl.duoc.huertoapi.repository.UsuarioRepository;
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

/** Pruebas unitarias de UsuarioService: el repositorio es un mock (sin base de datos). */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario(String id, String clave) {
        return new Usuario(id, "Nombre " + id, clave, Perfil.ESTUDIANTE, "Taller A", true);
    }

    @Test
    void findAllSinPerfilDevuelveTodos() {
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario("EST-01", "1"), usuario("EST-02", "1")));
        assertEquals(2, usuarioService.findAll(null).size());
    }

    @Test
    void findAllConPerfilFiltra() {
        when(usuarioRepository.findByPerfil(Perfil.ESTUDIANTE)).thenReturn(List.of(usuario("EST-01", "1")));
        assertEquals(1, usuarioService.findAll(Perfil.ESTUDIANTE).size());
        verify(usuarioRepository, never()).findAll();
    }

    @Test
    void buscarPorIdExistente() {
        when(usuarioRepository.findById("EST-01")).thenReturn(Optional.of(usuario("EST-01", "1")));
        assertEquals("EST-01", usuarioService.buscarPorId("EST-01").getId());
    }

    @Test
    void buscarPorIdInexistenteDa404() {
        when(usuarioRepository.findById("X")).thenReturn(Optional.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> usuarioService.buscarPorId("X"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void registrarNormalizaElCodigo() {
        when(usuarioRepository.existsById("EST-09")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        Usuario guardado = usuarioService.registrar(usuario("  est-09 ", "1234"));

        assertEquals("EST-09", guardado.getId());
    }

    @Test
    void registrarSinCodigoDa400() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> usuarioService.registrar(usuario(null, "1")));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());

        ResponseStatusException ex2 = assertThrows(ResponseStatusException.class,
                () -> usuarioService.registrar(usuario("   ", "1")));
        assertEquals(HttpStatus.BAD_REQUEST, ex2.getStatusCode());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void registrarCodigoRepetidoDa409() {
        when(usuarioRepository.existsById("EST-01")).thenReturn(true);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> usuarioService.registrar(usuario("EST-01", "1")));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void actualizarCambiaDatosYClave() {
        Usuario actual = usuario("EST-01", "vieja");
        when(usuarioRepository.findById("EST-01")).thenReturn(Optional.of(actual));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        Usuario cambios = new Usuario(null, "Nuevo nombre", "nueva", Perfil.ADMINISTRADOR, null, false);
        Usuario r = usuarioService.actualizar("EST-01", cambios);

        assertEquals("Nuevo nombre", r.getNombre());
        assertEquals("nueva", r.getClave());
        assertEquals(Perfil.ADMINISTRADOR, r.getPerfil());
        assertFalse(r.isActivo());
    }

    @Test
    void actualizarSinClaveMantieneLaAnterior() {
        when(usuarioRepository.findById("EST-01")).thenReturn(Optional.of(usuario("EST-01", "vieja")));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        assertEquals("vieja", usuarioService.actualizar("EST-01", usuario(null, null)).getClave());
        assertEquals("vieja", usuarioService.actualizar("EST-01", usuario(null, "  ")).getClave());
    }

    @Test
    void borrarEliminaAlUsuario() {
        Usuario u = usuario("EST-01", "1");
        when(usuarioRepository.findById("EST-01")).thenReturn(Optional.of(u));
        usuarioService.borrar("EST-01");
        verify(usuarioRepository).delete(u);
    }

    @Test
    void loginCorrectoNormalizaElCodigo() {
        Usuario u = usuario("ADM-01", "1234");
        when(usuarioRepository.findByIdAndClaveAndActivoTrue("ADM-01", "1234")).thenReturn(Optional.of(u));
        assertEquals("ADM-01", usuarioService.login(new LoginRequest(" adm-01 ", "1234")).getId());
    }

    @Test
    void loginIncorrectoDa401() {
        when(usuarioRepository.findByIdAndClaveAndActivoTrue("ADM-01", "mala")).thenReturn(Optional.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> usuarioService.login(new LoginRequest("ADM-01", "mala")));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void loginSinCodigoDa401() {
        when(usuarioRepository.findByIdAndClaveAndActivoTrue("", "1234")).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> usuarioService.login(new LoginRequest(null, "1234")));
    }
}
