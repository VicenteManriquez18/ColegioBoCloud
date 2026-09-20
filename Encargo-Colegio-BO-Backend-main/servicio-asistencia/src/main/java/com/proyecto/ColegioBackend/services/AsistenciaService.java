package com.proyecto.ColegioBackend.services;

import java.util.List;
import com.proyecto.ColegioBackend.model.Asistencia;

public interface AsistenciaService {
    List<Asistencia> listarTodas();
    Asistencia guardar(Asistencia asistencia);
    Asistencia buscarPorId(Long id);
    void eliminar(Long id);
    List<Asistencia> listarPorUsuario(Long usuarioId);
}