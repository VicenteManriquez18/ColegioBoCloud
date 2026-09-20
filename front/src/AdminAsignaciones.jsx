import React, { useState, useEffect } from 'react';
import { getToken } from '../utils/auth';

export default function AdminAsignaciones() {
  const [cursos, setCursos] = useState([]);
  const [profesores, setProfesores] = useState([]);
  const [asignaciones, setAsignaciones] = useState([]);
  const [cursoSeleccionadoId, setCursoSeleccionadoId] = useState('');
  const [asignaturaSeleccionada, setAsignaturaSeleccionada] = useState('');
  const [profesorSeleccionadoId, setProfesorSeleccionadoId] = useState('');
  const [mensaje, setMensaje] = useState({ texto: '', tipo: '' });
  const [loading, setLoading] = useState(false);

  // Opciones predefinidas de asignaturas (ingles, lenguaje, matematica, historia)
  const asignaturasBase = ['ingles', 'lenguaje', 'matematica', 'historia'];

  const cargarDatos = async () => {
    setLoading(true);
    try {
      const token = getToken();
      
      // 1. Cargar cursos
      const resCursos = await fetch('/api/academico/cursos', {
        headers: { 'Authorization': 'Bearer ' + token }
      });
      if (resCursos.ok) {
        const data = await resCursos.ok ? await resCursos.json() : [];
        setCursos(data);
      }

      // 2. Cargar profesores
      const resProfesores = await fetch('/api/matricula/usuarios/rol/Profesor', {
        headers: { 'Authorization': 'Bearer ' + token }
      });
      if (resProfesores.ok) {
        const data = await resProfesores.json();
        setProfesores(data);
      }

      // 3. Cargar asignaciones globales
      const resAsignaciones = await fetch('/api/academico/cursos/asignaciones', {
        headers: { 'Authorization': 'Bearer ' + token }
      });
      if (resAsignaciones.ok) {
        const data = await resAsignaciones.json();
        setAsignaciones(data);
      }
    } catch (err) {
      console.error("Error al cargar datos de asignación:", err);
      setMensaje({ texto: 'Error de conexión al cargar datos.', tipo: 'danger' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    cargarDatos();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!cursoSeleccionadoId || !asignaturaSeleccionada || !profesorSeleccionadoId) {
      setMensaje({ texto: 'Por favor complete todos los campos.', tipo: 'warning' });
      return;
    }

    setMensaje({ texto: '', tipo: '' });
    const token = getToken();
    const payload = {
      asignatura: asignaturaSeleccionada,
      profesorId: parseInt(profesorSeleccionadoId, 10)
    };

    try {
      const res = await fetch(`/api/academico/cursos/${cursoSeleccionadoId}/asignaturas/profesor`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + token
        },
        body: JSON.stringify(payload)
      });

      if (res.ok) {
        setMensaje({ texto: '✅ Profesor designado exitosamente.', tipo: 'success' });
        // Limpiar selección de asignatura/profesor
        setAsignaturaSeleccionada('');
        setProfesorSeleccionadoId('');
        // Recargar listado
        cargarDatos();
      } else {
        const errText = await res.text();
        setMensaje({ texto: `Error al asignar profesor: ${errText || res.statusText}`, tipo: 'danger' });
      }
    } catch (err) {
      setMensaje({ texto: 'Error de conexión con el servidor.', tipo: 'danger' });
      console.error(err);
    }
  };

  // Filtrar asignaciones del curso seleccionado para mostrar cuáles son las 4 asignaturas y quién las dicta
  const asignacionesCursoFiltrado = asignaciones.filter(a => a.curso?.id.toString() === cursoSeleccionadoId);

  return (
    <div className="container-fluid pb-5">
      <div className="d-flex justify-content-between align-items-center mb-4 bg-white p-3 shadow-sm rounded">
        <div>
          <h2 className="text-primary fw-bold mb-0">Asignación de Profesores</h2>
          <small className="text-muted">Designe profesores a las asignaturas de cada curso</small>
        </div>
      </div>

      {mensaje.texto && (
        <div className={`alert alert-${mensaje.tipo} alert-dismissible fade show`} role="alert">
          {mensaje.texto}
          <button type="button" className="btn-close" onClick={() => setMensaje({ texto: '', tipo: '' })}></button>
        </div>
      )}

      <div className="row g-4">
        {/* Formulario de Asignación */}
        <div className="col-md-5">
          <div className="card shadow border-0 p-4 bg-white rounded">
            <h4 className="fw-bold mb-4 border-bottom pb-2 text-dark">Nueva Designación</h4>
            
            <form onSubmit={handleSubmit}>
              <div className="mb-3">
                <label className="form-label small fw-bold">1. Seleccione Curso</label>
                <select 
                  className="form-select" 
                  value={cursoSeleccionadoId} 
                  onChange={e => {
                    setCursoSeleccionadoId(e.target.value);
                    setAsignaturaSeleccionada('');
                  }}
                  required
                >
                  <option value="">-- Seleccione un Curso --</option>
                  {cursos.map(c => (
                    <option key={c.id} value={c.id}>{c.nombre} (Cod: {c.codigo})</option>
                  ))}
                </select>
              </div>

              {cursoSeleccionadoId && (
                <>
                  <div className="mb-3">
                    <label className="form-label small fw-bold">2. Seleccione Asignatura</label>
                    <select 
                      className="form-select" 
                      value={asignaturaSeleccionada}
                      onChange={e => setAsignaturaSeleccionada(e.target.value)}
                      required
                    >
                      <option value="">-- Seleccione una Asignatura --</option>
                      {/* Generar las 4 asignaturas con el sufijo que corresponda al curso seleccionado */}
                      {(() => {
                        const curso = cursos.find(c => c.id.toString() === cursoSeleccionadoId);
                        if (!curso) return [];
                        // Extraer sufijo
                        let suffix = "";
                        const normalized = curso.nombre.toLowerCase();
                        if (normalized.includes("primero") || normalized.includes("1")) suffix = " 1";
                        else if (normalized.includes("segundo") || normalized.includes("2")) suffix = " 2";
                        else if (normalized.includes("tercero") || normalized.includes("3")) suffix = " 3";
                        else if (normalized.includes("cuarto") || normalized.includes("4")) suffix = " 4";
                        else if (normalized.includes("quinto") || normalized.includes("5")) suffix = " 5";
                        else if (normalized.includes("sexto") || normalized.includes("6")) suffix = " 6";
                        else if (normalized.includes("septimo") || normalized.includes("7")) suffix = " 7";
                        else if (normalized.includes("octavo") || normalized.includes("8")) suffix = " 8";
                        
                        return asignaturasBase.map(a => a + suffix);
                      })().map(a => (
                        <option key={a} value={a}>{a.toUpperCase()}</option>
                      ))}
                    </select>
                  </div>

                  <div className="mb-3">
                    <label className="form-label small fw-bold">3. Seleccione Profesor Designado</label>
                    <select 
                      className="form-select" 
                      value={profesorSeleccionadoId}
                      onChange={e => setProfesorSeleccionadoId(e.target.value)}
                      required
                    >
                      <option value="">-- Seleccione un Profesor --</option>
                      {profesores.map(p => (
                        <option key={p.id} value={p.id}>ID: {p.id} - {p.correo}</option>
                      ))}
                    </select>
                  </div>

                  <button type="submit" className="btn btn-primary w-100 py-2 mt-2 shadow-sm fw-bold">
                    <i className="bi bi-person-check me-2"></i>Designar Profesor
                  </button>
                </>
              )}
            </form>
          </div>
        </div>

        {/* Visualizador de Asignaciones por Curso */}
        <div className="col-md-7">
          <div className="card shadow border-0 p-4 bg-white rounded h-100">
            <h4 className="fw-bold mb-4 border-bottom pb-2 text-dark">Estructura de Asignaturas & Profesores</h4>
            
            {loading ? (
              <div className="text-center py-5">
                <div className="spinner-border text-primary" role="status"></div>
                <div className="mt-2 text-muted small">Cargando asignaciones...</div>
              </div>
            ) : (
              <div>
                {!cursoSeleccionadoId ? (
                  <div className="text-center py-5 text-muted">
                    <i className="bi bi-info-circle fs-1 mb-2 d-block"></i>
                    Seleccione un curso del panel izquierdo para ver sus asignaturas y profesores designados.
                  </div>
                ) : (
                  <div>
                    <h5 className="fw-bold text-secondary mb-3">
                      Asignaturas de: {cursos.find(c => c.id.toString() === cursoSeleccionadoId)?.nombre}
                    </h5>
                    
                    <div className="table-responsive">
                      <table className="table table-hover align-middle">
                        <thead className="table-light">
                          <tr>
                            <th>Asignatura</th>
                            <th>Profesor Designado</th>
                          </tr>
                        </thead>
                        <tbody>
                          {asignacionesCursoFiltrado.map(a => {
                            const prof = profesores.find(p => p.id === a.profesorId);
                            return (
                              <tr key={a.id}>
                                <td className="fw-bold text-dark">{a.asignatura.toUpperCase()}</td>
                                <td>
                                  {prof ? (
                                    <span className="badge bg-success p-2 fs-6">
                                      <i className="bi bi-person-fill me-1"></i> {prof.correo} (ID: {prof.id})
                                    </span>
                                  ) : (
                                    <span className="badge bg-warning text-dark p-2 fs-6">
                                      <i className="bi bi-exclamation-triangle-fill me-1"></i> Sin Designar
                                    </span>
                                  )}
                                </td>
                              </tr>
                            );
                          })}
                          {asignacionesCursoFiltrado.length === 0 && (
                            <tr>
                              <td colSpan="2" className="text-center py-4 text-muted">
                                Inicializando asignaturas del curso...
                              </td>
                            </tr>
                          )}
                        </tbody>
                      </table>
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
