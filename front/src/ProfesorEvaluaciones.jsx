import React, { useState, useEffect } from 'react';
import { getToken, getUserId } from '../utils/auth';

export default function ProfesorEvaluaciones() {
  const [asignaciones, setAsignaciones] = useState([]);
  const [cursoSeleccionadoId, setCursoSeleccionadoId] = useState('');
  const [asignaturaSeleccionada, setAsignaturaSeleccionada] = useState('');
  
  // Pruebas (evaluaciones)
  const [pruebas, setPruebas] = useState([]);
  const [pruebaSeleccionadaId, setPruebaSeleccionadaId] = useState('');
  const [nuevaPrueba, setNuevaPrueba] = useState({ titulo: '', descripcion: '', fecha: new Date().toISOString().split('T')[0] });
  const [mostrarFormPrueba, setMostrarFormPrueba] = useState(false);

  // Alumnos y Notas
  const [alumnos, setAlumnos] = useState([]);
  const [notas, setNotas] = useState({}); // Mapeo: { [alumnoId]: { valor: '', comentario: '' } }
  
  const [mensaje, setMensaje] = useState({ texto: '', tipo: '' });
  const [loading, setLoading] = useState(false);
  const [loadingNotas, setLoadingNotas] = useState(false);

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

  // Cargar todos los alumnos de la matrícula
  const cargarAlumnos = async () => {
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
    }
  };

  useEffect(() => {
    cargarAsignaciones();
    cargarAlumnos();
  }, []);

  // Cargar pruebas del curso cuando cambia el curso o la asignatura
  const cargarPruebas = async () => {
    if (!cursoSeleccionadoId) return;
    setLoading(true);
    try {
      const res = await fetch(`/api/academico/pruebas/curso/${cursoSeleccionadoId}`, {
        headers: { 'Authorization': 'Bearer ' + getToken() }
      });
      if (res.ok) {
        const data = await res.json();
        // Filtrar las pruebas por la asignatura seleccionada
        const filtradas = data.filter(p => p.asignatura && p.asignatura.toLowerCase() === asignaturaSeleccionada.toLowerCase());
        setPruebas(filtradas);
        
        // Resetear selección de prueba si no está en la nueva lista
        if (!filtradas.some(p => p.id.toString() === pruebaSeleccionadaId)) {
          setPruebaSeleccionadaId('');
        }
      }
    } catch (err) {
      console.error("Error al cargar pruebas:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (cursoSeleccionadoId && asignaturaSeleccionada) {
      cargarPruebas();
    } else {
      setPruebas([]);
      setPruebaSeleccionadaId('');
    }
  }, [cursoSeleccionadoId, asignaturaSeleccionada]);

  // Cargar notas de la prueba seleccionada
  const cargarNotasDePrueba = async (pId) => {
    if (!pId) return;
    setLoadingNotas(true);
    try {
      const res = await fetch(`/api/academico/notas/prueba/${pId}`, {
        headers: { 'Authorization': 'Bearer ' + getToken() }
      });
      if (res.ok) {
        const data = await res.json();
        // Mapear notas a un objeto para fácil acceso
        const mapeo = {};
        data.forEach(n => {
          mapeo[n.alumnoId] = {
            valor: n.valor || '',
            comentario: n.comentario || ''
          };
        });
        setNotas(mapeo);
      }
    } catch (err) {
      console.error("Error al cargar notas de prueba:", err);
    } finally {
      setLoadingNotas(false);
    }
  };

  useEffect(() => {
    if (pruebaSeleccionadaId) {
      cargarNotasDePrueba(pruebaSeleccionadaId);
    } else {
      setNotas({});
    }
  }, [pruebaSeleccionadaId]);

  // Manejar creación de prueba
  const handleCrearPrueba = async (e) => {
    e.preventDefault();
    if (!cursoSeleccionadoId || !asignaturaSeleccionada) return;

    const body = {
      titulo: nuevaPrueba.titulo,
      descripcion: nuevaPrueba.descripcion,
      fecha: nuevaPrueba.fecha,
      asignatura: asignaturaSeleccionada
    };

    try {
      const res = await fetch(`/api/academico/pruebas/curso/${cursoSeleccionadoId}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + getToken()
        },
        body: JSON.stringify(body)
      });

      if (res.ok) {
        const creada = await res.json();
        setMensaje({ texto: '✅ Evaluación creada con éxito.', tipo: 'success' });
        setNuevaPrueba({ titulo: '', descripcion: '', fecha: new Date().toISOString().split('T')[0] });
        setMostrarFormPrueba(false);
        await cargarPruebas();
        setPruebaSeleccionadaId(creada.id.toString()); // Seleccionar automáticamente la creada
      } else {
        const errData = await res.json();
        setMensaje({ texto: `❌ Error: ${errData.error || 'No se pudo crear la evaluación.'}`, tipo: 'danger' });
      }
    } catch (err) {
      setMensaje({ texto: '❌ Error de conexión al crear evaluación.', tipo: 'danger' });
    }
    setTimeout(() => setMensaje({ texto: '', tipo: '' }), 4000);
  };

  // Guardar nota individual
  const guardarNota = async (alumnoId) => {
    if (!pruebaSeleccionadaId) return;

    const notaData = notas[alumnoId] || { valor: '', comentario: '' };
    const valor = parseFloat(notaData.valor);

    if (isNaN(valor) || valor < 1.0 || valor > 7.0) {
      setMensaje({ texto: '⚠️ La calificación debe ser un número entre 1.0 y 7.0', tipo: 'warning' });
      setTimeout(() => setMensaje({ texto: '', tipo: '' }), 3000);
      return;
    }

    const body = {
      alumnoId: alumnoId,
      valor: valor,
      comentario: notaData.comentario
    };

    try {
      const res = await fetch(`/api/academico/notas/prueba/${pruebaSeleccionadaId}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + getToken()
        },
        body: JSON.stringify(body)
      });

      if (res.ok) {
        setMensaje({ texto: '✅ Nota registrada exitosamente.', tipo: 'success' });
        cargarNotasDePrueba(pruebaSeleccionadaId); // Recargar
      } else {
        const errData = await res.json();
        setMensaje({ texto: `❌ Error: ${errData.error || 'No se pudo guardar la nota.'}`, tipo: 'danger' });
      }
    } catch (err) {
      setMensaje({ texto: '❌ Error de conexión al guardar nota.', tipo: 'danger' });
    }
    setTimeout(() => setMensaje({ texto: '', tipo: '' }), 3000);
  };

  const handleNotaChange = (alumnoId, field, value) => {
    setNotas(prev => ({
      ...prev,
      [alumnoId]: {
        ...(prev[alumnoId] || { valor: '', comentario: '' }),
        [field]: value
      }
    }));
  };

  // Obtener cursos únicos asignados
  const cursosAsignados = Array.from(new Set(asignaciones.filter(a => a.curso).map(a => JSON.stringify(a.curso)))).map(s => JSON.parse(s));

  // Asignaturas dictadas por el profesor en el curso seleccionado
  const asignaturasDelCurso = asignaciones
    .filter(a => a.curso && a.curso.id.toString() === cursoSeleccionadoId)
    .map(a => a.asignatura);

  // Alumnos del curso seleccionado
  const alumnosFiltrados = alumnos.filter(a => a.cursoId && a.cursoId.toString() === cursoSeleccionadoId);

  return (
    <div className="container-fluid pb-5">
      {/* Encabezado */}
      <div className="d-flex justify-content-between align-items-center mb-4 bg-white p-3 shadow-sm rounded border-start border-primary border-4">
        <div>
          <h2 className="text-primary fw-bold mb-0">Evaluaciones y Calificaciones</h2>
          <small className="text-muted">Gestiona evaluaciones y asigna notas a los estudiantes</small>
        </div>
      </div>

      {mensaje.texto && (
        <div className={`alert alert-${mensaje.tipo} shadow-sm position-fixed top-0 start-50 translate-middle-x mt-3 z-3`} style={{ minWidth: '350px', textAlign: 'center' }} role="alert">
          {mensaje.texto}
        </div>
      )}

      {/* Selectores */}
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
                setPruebaSeleccionadaId('');
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
                setPruebaSeleccionadaId('');
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

      {/* Si no hay selección */}
      {!cursoSeleccionadoId || !asignaturaSeleccionada ? (
        <div className="card shadow border-0 p-5 text-center text-muted bg-white rounded">
          <i className="bi bi-journal-check fs-1 text-secondary mb-3"></i>
          <h4>Selecciona un curso y una asignatura asignada para comenzar</h4>
          <p className="mb-0">Aquí podrás crear nuevas evaluaciones y registrar calificaciones.</p>
        </div>
      ) : (
        <div className="row g-4">
          
          {/* COLUMNA IZQUIERDA: GESTIÓN DE EVALUACIONES */}
          <div className="col-lg-4">
            <div className="card shadow border-0 p-4 bg-white rounded h-100 d-flex flex-column">
              <div className="d-flex justify-content-between align-items-center mb-4">
                <h4 className="fw-bold text-dark mb-0">Evaluaciones</h4>
                {!mostrarFormPrueba ? (
                  <button 
                    onClick={() => setMostrarFormPrueba(true)} 
                    className="btn btn-primary btn-sm fw-bold shadow-sm"
                  >
                    <i className="bi bi-plus-lg me-1"></i> Nueva
                  </button>
                ) : (
                  <button 
                    onClick={() => setMostrarFormPrueba(false)} 
                    className="btn btn-outline-secondary btn-sm fw-bold"
                  >
                    Cancelar
                  </button>
                )}
              </div>

              {/* Formulario para crear Prueba */}
              {mostrarFormPrueba && (
                <form onSubmit={handleCrearPrueba} className="mb-4 p-3 bg-light rounded border border-light-subtle">
                  <h6 className="fw-bold mb-3 text-primary">Nueva Evaluación</h6>
                  <div className="mb-2">
                    <label className="form-label small mb-1 fw-bold">Título / Nombre</label>
                    <input 
                      type="text" 
                      className="form-control form-control-sm" 
                      placeholder="Ej: Prueba 1, Ensayo, Examen"
                      value={nuevaPrueba.titulo} 
                      onChange={e => setNuevaPrueba({...nuevaPrueba, titulo: e.target.value})} 
                      required 
                    />
                  </div>
                  <div className="mb-2">
                    <label className="form-label small mb-1 fw-bold">Descripción</label>
                    <textarea 
                      className="form-control form-control-sm" 
                      rows="2"
                      placeholder="Temas a evaluar (opcional)"
                      value={nuevaPrueba.descripcion} 
                      onChange={e => setNuevaPrueba({...nuevaPrueba, descripcion: e.target.value})} 
                    />
                  </div>
                  <div className="mb-3">
                    <label className="form-label small mb-1 fw-bold">Fecha de Aplicación</label>
                    <input 
                      type="date" 
                      className="form-control form-control-sm" 
                      value={nuevaPrueba.fecha} 
                      onChange={e => setNuevaPrueba({...nuevaPrueba, fecha: e.target.value})} 
                      required 
                    />
                  </div>
                  <button type="submit" className="btn btn-success btn-sm w-100 fw-bold shadow-sm">
                    Guardar Evaluación
                  </button>
                </form>
              )}

              {/* Lista de Pruebas */}
              <div className="flex-grow-1 overflow-auto" style={{ maxHeight: '400px' }}>
                {loading ? (
                  <div className="text-center py-5">
                    <div className="spinner-border text-primary" role="status"></div>
                  </div>
                ) : pruebas.length === 0 ? (
                  <div className="text-center py-5 text-muted">
                    No hay evaluaciones registradas para esta asignatura.
                  </div>
                ) : (
                  <div className="list-group">
                    {pruebas.map(p => (
                      <button
                        key={p.id}
                        type="button"
                        className={`list-group-item list-group-item-action text-start p-3 ${pruebaSeleccionadaId === p.id.toString() ? 'active bg-primary border-primary' : ''}`}
                        onClick={() => setPruebaSeleccionadaId(p.id.toString())}
                      >
                        <div className="d-flex justify-content-between align-items-center">
                          <h6 className="fw-bold mb-1">{p.titulo}</h6>
                          <small className={pruebaSeleccionadaId === p.id.toString() ? 'text-white-50' : 'text-muted'}>
                            {new Date(p.fecha).toLocaleDateString('es-CL')}
                          </small>
                        </div>
                        {p.descripcion && <p className="mb-0 small text-truncate opacity-75">{p.descripcion}</p>}
                      </button>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* COLUMNA DERECHA: ASIGNACIÓN DE NOTAS */}
          <div className="col-lg-8">
            <div className="card shadow border-0 p-4 bg-white rounded h-100">
              {!pruebaSeleccionadaId ? (
                <div className="h-100 d-flex flex-column justify-content-center align-items-center text-muted text-center p-5">
                  <i className="bi bi-card-checklist fs-1 mb-3"></i>
                  <h5>Seleccione una evaluación de la columna izquierda</h5>
                  <p className="mb-0">Para ingresar las notas de los alumnos del curso.</p>
                </div>
              ) : (
                <div className="d-flex flex-column h-100">
                  <div className="border-bottom pb-3 mb-3">
                    <h4 className="fw-bold text-dark mb-1">
                      Calificar: {pruebas.find(p => p.id.toString() === pruebaSeleccionadaId)?.titulo}
                    </h4>
                    <span className="badge bg-light text-dark border me-2">
                      Curso: {cursosAsignados.find(c => c.id.toString() === cursoSeleccionadoId)?.nombre}
                    </span>
                    <span className="badge bg-light text-dark border">
                      Asignatura: {asignaturaSeleccionada.toUpperCase()}
                    </span>
                  </div>

                  {loadingNotas ? (
                    <div className="text-center py-5 my-auto">
                      <div className="spinner-border text-primary" role="status"></div>
                    </div>
                  ) : alumnosFiltrados.length === 0 ? (
                    <div className="text-center py-5 text-muted my-auto">
                      No hay alumnos matriculados en este curso.
                    </div>
                  ) : (
                    <div className="table-responsive flex-grow-1" style={{ maxHeight: '500px' }}>
                      <table className="table table-hover align-middle">
                        <thead className="table-light">
                          <tr>
                            <th>Alumno</th>
                            <th style={{ width: '120px' }}>Nota (1.0 - 7.0)</th>
                            <th>Comentario / Observación</th>
                            <th className="text-center" style={{ width: '110px' }}>Acción</th>
                          </tr>
                        </thead>
                        <tbody>
                          {alumnosFiltrados.map(a => {
                            const notaFila = notas[a.usuarioId] || { valor: '', comentario: '' };
                            return (
                              <tr key={a.id}>
                                <td>
                                  <div className="fw-bold text-dark">{a.nombre}</div>
                                  <small className="text-muted">RUT: {a.rut}</small>
                                </td>
                                <td>
                                  <input 
                                    type="number" 
                                    step="0.1" 
                                    min="1.0" 
                                    max="7.0" 
                                    className="form-control form-control-sm text-center fw-bold" 
                                    placeholder="0.0"
                                    value={notaFila.valor}
                                    onChange={e => handleNotaChange(a.usuarioId, 'valor', e.target.value)}
                                  />
                                </td>
                                <td>
                                  <input 
                                    type="text" 
                                    className="form-control form-control-sm" 
                                    placeholder="Ej: Buen desempeño, Falta repasar..."
                                    value={notaFila.comentario}
                                    onChange={e => handleNotaChange(a.usuarioId, 'comentario', e.target.value)}
                                  />
                                </td>
                                <td>
                                  <button 
                                    onClick={() => guardarNota(a.usuarioId)} 
                                    className="btn btn-success btn-sm w-100 fw-bold shadow-sm d-flex align-items-center justify-content-center"
                                  >
                                    <i className="bi bi-save me-1"></i> Guardar
                                  </button>
                                </td>
                              </tr>
                            );
                          })}
                        </tbody>
                      </table>
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>

        </div>
      )}
    </div>
  );
}
