import React, { useState, useEffect } from 'react';
import { getToken, getUserId } from '../utils/auth';

export default function ProfesorAsistencia() {
  const [asignaciones, setAsignaciones] = useState([]);
  const [cursoSeleccionadoId, setCursoSeleccionadoId] = useState('');
  const [asignaturaSeleccionada, setAsignaturaSeleccionada] = useState('');
  const [alumnos, setAlumnos] = useState([]);
  const [asistencia, setAsistencia] = useState({});
  const [mensaje, setMensaje] = useState({ texto: '', tipo: '' });
  const [fecha, setFecha] = useState(new Date().toISOString().split('T')[0]);
  const [loading, setLoading] = useState(false);

  // Cargar asignaciones del profesor actual
  const cargarAsignaciones = async () => {
    try {
      const pId = getUserId();
      if (!pId) return;

      const res = await fetch(`/api/academico/cursos/asignaciones/profesor/${pId}`, {
        headers: { 'Authorization': 'Bearer ' + getToken() }
      });
      if (res.ok) {
        const data = await res.json();
        setAsignaciones(data);
      }
    } catch (err) {
      console.error("Error al cargar asignaciones:", err);
    }
  };

  // Cargar todos los alumnos
  const cargarAlumnos = async () => {
    setLoading(true);
    try {
      const res = await fetch('/api/matricula/estudiantes', {
        headers: { 'Authorization': 'Bearer ' + getToken() }
      });
      if (res.ok) {
        const data = await res.json();
        setAlumnos(data);
      }
    } catch (err) {
      console.error("Error al cargar alumnos:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    cargarAsignaciones();
    cargarAlumnos();
  }, []);

  const marcarAsistencia = async (alumno, estado) => {
    if (!cursoSeleccionadoId || !asignaturaSeleccionada) {
      setMensaje({ texto: '⚠️ Debe seleccionar Curso y Asignatura primero.', tipo: 'warning' });
      return;
    }

    const isPresente = estado === 'Presente';
    // Guardar estado localmente
    setAsistencia(prev => ({ ...prev, [alumno.id]: estado }));

    const nombreMostrar = alumno.nombre || `Estudiante #${alumno.usuarioId}`;

    const dataToSend = {
      fecha: fecha + "T00:00:00",
      usuarioId: alumno.usuarioId,
      nombreUsuario: alumno.nombre || alumno.rut,
      cursoId: parseInt(cursoSeleccionadoId, 10),
      asignatura: asignaturaSeleccionada,
      presente: isPresente
    };

    try {
      const res = await fetch('/api/asistencia', {
        method: 'POST',
        headers: { 
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + getToken()
        },
        body: JSON.stringify(dataToSend)
      });
      
      if (res.ok) {
        setMensaje({ texto: `✅ Asistencia guardada: ${nombreMostrar} está ${estado}.`, tipo: 'success' });
      } else {
        const errText = await res.text();
        setMensaje({ texto: `❌ Error: ${errText || 'No se pudo guardar la asistencia.'}`, tipo: 'danger' });
      }
    } catch (err) {
      setMensaje({ texto: `❌ Error de conexión al guardar asistencia.`, tipo: 'danger' });
    }
    
    // Ocultar mensaje después de 3 segundos
    setTimeout(() => setMensaje({ texto: '', tipo: '' }), 3000);
  };

  // Obtener lista única de cursos asignados
  const cursosAsignados = Array.from(new Set(asignaciones.filter(a => a.curso).map(a => JSON.stringify(a.curso)))).map(s => JSON.parse(s));

  // Obtener asignaturas dictadas por el profesor para el curso seleccionado
  const asignaturasDelCurso = asignaciones
    .filter(a => a.curso && a.curso.id.toString() === cursoSeleccionadoId)
    .map(a => a.asignatura);

  // Filtrar alumnos correspondientes al curso seleccionado
  const alumnosFiltrados = alumnos.filter(a => a.cursoId && a.cursoId.toString() === cursoSeleccionadoId);

  return (
    <div className="container-fluid pb-5">
      <div className="d-flex justify-content-between align-items-center mb-4 bg-white p-3 shadow-sm rounded">
        <div>
          <h2 className="text-primary fw-bold mb-0">Registro de Asistencia</h2>
          <small className="text-muted">Toma la asistencia diaria de tus asignaturas asignadas</small>
        </div>
        <div>
          <input 
            type="date" 
            className="form-control fw-bold text-primary" 
            value={fecha} 
            onChange={e => setFecha(e.target.value)} 
          />
        </div>
      </div>

      {mensaje.texto && (
        <div className={`alert alert-${mensaje.tipo} shadow-sm position-fixed top-0 start-50 translate-middle-x mt-3 z-3`} style={{ minWidth: '350px', textAlign: 'center' }} role="alert">
          {mensaje.texto}
        </div>
      )}

      {/* Selectores de Curso y Asignatura */}
      <div className="card shadow-sm border-0 p-3 mb-4 bg-white rounded">
        <div className="row g-3">
          <div className="col-md-6">
            <label className="form-label small fw-bold">1. Seleccione Curso</label>
            <select 
              className="form-select"
              value={cursoSeleccionadoId}
              onChange={e => {
                setCursoSeleccionadoId(e.target.value);
                setAsignaturaSeleccionada('');
                setAsistencia({});
              }}
            >
              <option value="">-- Cursos Asignados --</option>
              {cursosAsignados.map(c => (
                <option key={c.id} value={c.id}>{c.nombre}</option>
              ))}
            </select>
          </div>
          <div className="col-md-6">
            <label className="form-label small fw-bold">2. Seleccione Asignatura</label>
            <select 
              className="form-select"
              value={asignaturaSeleccionada}
              onChange={e => {
                setAsignaturaSeleccionada(e.target.value);
                setAsistencia({});
              }}
              disabled={!cursoSeleccionadoId}
            >
              <option value="">-- Asignaturas --</option>
              {asignaturasDelCurso.map(asig => (
                <option key={asig} value={asig}>{asig.toUpperCase()}</option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* Listado de Estudiantes */}
      <div className="card shadow border-0 overflow-hidden bg-white rounded">
        <div className="table-responsive">
          <table className="table table-hover align-middle mb-0">
            <thead className="table-light">
              <tr>
                <th className="ps-4">Nombre / RUT</th>
                <th className="text-center" style={{ width: '250px' }}>Estado de Asistencia</th>
              </tr>
            </thead>
            <tbody>
              {!cursoSeleccionadoId || !asignaturaSeleccionada ? (
                <tr>
                  <td colSpan="2" className="text-center text-muted p-5">
                    <i className="bi bi-calendar2-event fs-1 mb-2 d-block"></i>
                    Por favor seleccione un curso y asignatura para cargar la lista de alumnos.
                  </td>
                </tr>
              ) : loading ? (
                <tr>
                  <td colSpan="2" className="text-center p-5">
                    <div className="spinner-border text-primary" role="status"></div>
                  </td>
                </tr>
              ) : alumnosFiltrados.length === 0 ? (
                <tr>
                  <td colSpan="2" className="text-center text-muted p-5">
                    No hay alumnos matriculados en este curso.
                  </td>
                </tr>
              ) : (
                alumnosFiltrados.map(a => (
                  <tr key={a.id}>
                    <td className="ps-4">
                      <div className="fw-bold text-dark">{a.nombre}</div>
                      <small className="text-muted">RUT: {a.rut} | ID Cuenta: {a.usuarioId}</small>
                    </td>
                    <td>
                      <div className="d-flex justify-content-center gap-2">
                        <button 
                          type="button"
                          className={`btn ${asistencia[a.id] === 'Presente' ? 'btn-success' : 'btn-outline-success'} btn-sm fw-bold px-3`}
                          onClick={() => marcarAsistencia(a, 'Presente')}
                        >
                          <i className="bi bi-check-circle me-1"></i> Presente
                        </button>

                        <button 
                          type="button"
                          className={`btn ${asistencia[a.id] === 'Ausente' ? 'btn-danger' : 'btn-outline-danger'} btn-sm fw-bold px-3`}
                          onClick={() => marcarAsistencia(a, 'Ausente')}
                        >
                          <i className="bi bi-x-circle me-1"></i> Ausente
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
