package com.proyecto.ColegioBackend.services;


import java.util.List;

import com.proyecto.ColegioBackend.model.Usuario;


public interface UsuarioService {
List<Usuario> listarTodos();
    List<Usuario> listarPorRol(String rol);
    Usuario guardar(Usuario usuario);
    Usuario buscarPorId(Long id);
    void eliminar(Long id);
}