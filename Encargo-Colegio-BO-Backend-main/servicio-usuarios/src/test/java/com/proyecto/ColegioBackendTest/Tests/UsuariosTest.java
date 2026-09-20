package com.proyecto.ColegioBackendTest.Tests;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.proyecto.ColegioBackend.model.Usuario;
import com.proyecto.ColegioBackend.repository.UsuarioRepository;
import com.proyecto.ColegioBackend.services.UsuarioServiceImpl;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
public class UsuariosTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Usuario usuarioPrueba;

    @BeforeEach
    void setUp() {
        usuarioPrueba = new Usuario();
        usuarioPrueba.setId(1L);
        usuarioPrueba.setCorreo("admin@colegio.com");
        usuarioPrueba.setRol("ADMIN");
        usuarioPrueba.setPassword("1234");
    }

    @Test
    void listarTodos_DebeRetornarListaDeUsuarios() {
        when(usuarioRepository.findAll()).thenReturn(Arrays.asList(usuarioPrueba));

        List<Usuario> resultado = usuarioService.listarTodos();
        // septiembre-13-2026
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    void listarTodos_CuandoHayError_DebeRetornarListaVacia() {
        when(usuarioRepository.findAll()).thenThrow(new RuntimeException("Error BD"));
        // septiembre-13-2026
        List<Usuario> resultado = usuarioService.listarTodos();

        assertTrue(resultado.isEmpty());
    }

    @Test
    void guardar_DebeRetornarUsuarioGuardado() {
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioPrueba);

        Usuario resultado = usuarioService.guardar(usuarioPrueba);

        assertNotNull(resultado);
        assertEquals("admin@colegio.com", resultado.getCorreo());
    }

    @Test
    void buscarPorId_CuandoExiste_DebeRetornarUsuario() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioPrueba));

        Usuario resultado = usuarioService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void buscarPorId_CuandoNoExiste_DebeRetornarNull() {
        when(usuarioRepository.findById(2L)).thenReturn(Optional.empty());

        Usuario resultado = usuarioService.buscarPorId(2L);

        assertNull(resultado);
    }

    @Test
    void buscarPorId_CuandoHayError_DebeRetornarNull() {
        when(usuarioRepository.findById(1L)).thenThrow(new RuntimeException("Error BD"));

        Usuario resultado = usuarioService.buscarPorId(1L);

        assertNull(resultado);
    }

    @Test
    void guardar_CuandoHayError_DebeRetornarNull() {
        when(usuarioRepository.save(any(Usuario.class))).thenThrow(new RuntimeException("Error BD"));

        Usuario resultado = usuarioService.guardar(usuarioPrueba);

        assertNull(resultado);
    }

    @Test
    void eliminar_Exito() {
        doNothing().when(usuarioRepository).deleteById(1L);
        //
        assertDoesNotThrow(() -> usuarioService.eliminar(1L));
        verify(usuarioRepository, times(1)).deleteById(1L);
    }

    @Test
    void eliminar_CuandoHayError_NoDebeLanzarExcepcion() {
        doThrow(new RuntimeException("Error BD")).when(usuarioRepository).deleteById(1L);

        assertDoesNotThrow(() -> usuarioService.eliminar(1L));
        verify(usuarioRepository, times(1)).deleteById(1L);
    }
}
