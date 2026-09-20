package com.proyecto.ColegioBackend.services;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.proyecto.ColegioBackend.model.Usuario;
import com.proyecto.ColegioBackend.repository.UsuarioRepository;

@SuppressWarnings("null")
@Service
public class UsuarioServiceImpl implements UsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioServiceImpl.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public List<Usuario> listarTodos() {
        try {
            return usuarioRepository.findAll();
        } catch (Exception e) {
            logger.error("Error al listar todos los usuarios: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<Usuario> listarPorRol(String rol) {
        try {
            return usuarioRepository.findByRolIgnoreCase(rol);
        } catch (Exception e) {
            logger.error("Error al listar usuarios por rol {}: {}", rol, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        try {
            // Aquí es el lugar perfecto para cifrar la contraseña con BCrypt 
            // antes de persistir en PostgreSQL
            return usuarioRepository.save(usuario);
        } catch (Exception e) {
            logger.error("Error al guardar el usuario {}: {}", usuario.getCorreo(), e.getMessage());
            return null;
        }
    }

    @Override
    public Usuario buscarPorId(Long id) {
        try {
            // Usamos el Repository Pattern definido en tu diseño
            return usuarioRepository.findById(id).orElse(null);
        } catch (Exception e) {
            logger.error("Error al buscar usuario con ID {}: {}", id, e.getMessage());
            return null;
        }
    }

    @Override
    public void eliminar(Long id) {
        try {
            usuarioRepository.deleteById(id);
        } catch (Exception e) {
            logger.error("Error al eliminar usuario con ID {}: {}", id, e.getMessage());
        }
    }
}
