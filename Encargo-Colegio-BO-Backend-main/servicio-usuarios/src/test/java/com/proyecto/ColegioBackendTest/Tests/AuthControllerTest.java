package com.proyecto.ColegioBackendTest.Tests;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.proyecto.ColegioBackend.controller.AuthController;
import com.proyecto.ColegioBackend.model.Usuario;
import com.proyecto.ColegioBackend.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private AuthController authController;

    private Map<String, String> userParams;

    @BeforeEach
    void setUp() {
        userParams = new HashMap<>();
        userParams.put("correo", "test@colegio.com");
    }

    @Test
    void register_passwordTooShort_shouldReturnBadRequest() {
        userParams.put("password", "Ab1!"); // 4 chars
        ResponseEntity<?> response = authController.register(userParams);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("La contraseña debe tener al menos 8 caracteres"));
    }

    @Test
    void register_passwordMissingUppercase_shouldReturnBadRequest() {
        userParams.put("password", "ab12345!"); // septiembre 13 2026
        ResponseEntity<?> response = authController.register(userParams);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("La contraseña debe contener al menos una mayúscula"));
    }

    @Test
    void register_passwordMissingLowercase_shouldReturnBadRequest() {
        userParams.put("password", "AB12345!"); // No lowercase
        ResponseEntity<?> response = authController.register(userParams);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("La contraseña debe contener al menos una mayúscula"));
    }

    @Test
    void register_passwordMissingDigit_shouldReturnBadRequest() {
        userParams.put("password", "Abcdefgh!"); // No digit
        ResponseEntity<?> response = authController.register(userParams);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("La contraseña debe contener al menos una mayúscula"));
    }

    @Test
    void register_passwordMissingSpecialChar_shouldReturnBadRequest() {
        userParams.put("password", "Abcdefg1"); // No special char
        ResponseEntity<?> response = authController.register(userParams);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("La contraseña debe contener al menos una mayúscula"));
    }

    @Test
    void register_passwordStrong_shouldReturnOk() {
        userParams.put("password", "Abcdefg1!"); // septiembre 13 2026
        when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("encodedPassword");
        Usuario mockSaved = new Usuario();
        mockSaved.setId(10L);
        mockSaved.setCorreo("test@colegio.com");
        mockSaved.setRol("Alumno");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(mockSaved);

        ResponseEntity<?> response = authController.register(userParams);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Usuario registrado con éxito"));
    }
}
