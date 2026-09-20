package com.proyecto.ColegioBackendTest.Tests;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.proyecto.ColegioBackend.controller.UsuarioController;
import com.proyecto.ColegioBackend.model.Usuario;
import com.proyecto.ColegioBackend.services.UsuarioService;

@ExtendWith(MockitoExtension.class)
public class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioController usuarioController;

    private Usuario usuarioPrueba;

    @BeforeEach
    void setUp() {
        usuarioPrueba = new Usuario();
        usuarioPrueba.setId(1L);
        usuarioPrueba.setCorreo("test@colegio.com");
        usuarioPrueba.setRol("Alumno");
    }

    @Test
    void guardar_passwordTooShort_shouldReturnBadRequest() {
        usuarioPrueba.setPassword("Ab1!"); // 4 chars
        ResponseEntity<?> response = usuarioController.guardar(usuarioPrueba);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("La contraseña debe tener al menos 8 caracteres"));
    }

    @Test
    void guardar_passwordMissingUppercase_shouldReturnBadRequest() {
        usuarioPrueba.setPassword("ab12345!"); // No uppercase
        ResponseEntity<?> response = usuarioController.guardar(usuarioPrueba);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("La contraseña debe contener al menos una mayúscula"));
    }

    @Test
    void guardar_passwordMissingLowercase_shouldReturnBadRequest() {
        usuarioPrueba.setPassword("AB12345!"); // No lowercase
        ResponseEntity<?> response = usuarioController.guardar(usuarioPrueba);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("La contraseña debe contener al menos una mayúscula"));
    }

    @Test
    void guardar_passwordMissingDigit_shouldReturnBadRequest() {
        usuarioPrueba.setPassword("Abcdefgh!"); // No digit
        ResponseEntity<?> response = usuarioController.guardar(usuarioPrueba);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("La contraseña debe contener al menos una mayúscula"));
    }

    @Test
    void guardar_passwordMissingSpecialChar_shouldReturnBadRequest() {
        usuarioPrueba.setPassword("Abcdefg1"); // No special char
        ResponseEntity<?> response = usuarioController.guardar(usuarioPrueba);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("La contraseña debe contener al menos una mayúscula"));
    }

    @Test
    void guardar_passwordStrong_shouldReturnCreated() {
        usuarioPrueba.setPassword("Abcdefg1!"); // Strong
        when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("encodedPassword");
        when(usuarioService.guardar(any(Usuario.class))).thenReturn(usuarioPrueba);

        ResponseEntity<?> response = usuarioController.guardar(usuarioPrueba);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
