import React, { useState, useEffect } from 'react';
import { getToken } from '../utils/auth';

export default function AdminReportes() {
  const [estudiantes, setEstudiantes] = useState([]);
  const [estudianteSeleccionado, setEstudianteSeleccionado] = useState(null);
  const [busqueda, setBusqueda] = useState('');
  const [reporteCompleto, setReporteCompleto] = useState(null);
  const [loadingReporte, setLoadingReporte] = useState(false);
  const [errorReporte, setErrorReporte] = useState('');
  
  // Pestaña activa en reportes de alumno
  const [tabActiva, setTabActiva] = useState('ficha');

  // Estado para el formulario de nuevo comportamiento
  const [comportamientoForm, setComportamientoForm] = useState({
    calificacion: 'Excelente',
    observaciones: ''
  });
  const [mensajeComportamiento, setMensajeComportamiento] = useState({ texto: '', tipo: '' });

  // Ordenamiento de notas
  const [ordenarNotasAsc, setOrdenarNotasAsc] = useState(null); // null = original, true = asc, false = desc

  // Estados para reportes de cursos
  const [cursos, setCursos] = useState([]);
  const [cursoSeleccionadoId, setCursoSeleccionadoId] = useState('');
  const [loadingCurso, setLoadingCurso] = useState(false);
  const [cursoStats, setCursoStats] = useState({
    activos: 0,
    asistencias: [],
    comportamientos: []
  });

  const calcularMetricasAsistencia = () => {
    if (!reporteCompleto || !reporteCompleto.asistencias) {
      return { total: 0, presentes: 0, ausentes: 0, porcentaje: 100 };
    }
    const total = reporteCompleto.asistencias.length;
    const presentes = reporteCompleto.asistencias.filter(as => as.presente !== false).length;
    const ausentes = total - presentes;
    const porcentaje = total > 0 ? Math.round((presentes / total) * 100) : 100;
    return { total, presentes, ausentes, porcentaje };
  };

  const metricasAsistencia = calcularMetricasAsistencia();

  // Cargar lista de estudiantes y cursos al inicio
  useEffect(() => {
    const fetchEstudiantesYCursos = async () => {
      try {
        const token = getToken();
        // Cargar estudiantes
        const resEst = await fetch('/api/matricula/estudiantes', {
          headers: { 'Authorization': 'Bearer ' + token }
        });
        if (resEst.ok) {
          const dataEst = await resEst.json();
          setEstudiantes(dataEst);
        }

        // Cargar cursos
        const resCur = await fetch('/api/academico/cursos', {
          headers: { 'Authorization': 'Bearer ' + token }
        });
        if (resCur.ok) {
          const dataCur = await resCur.json();
          setCursos(dataCur);
        }
      } catch (err) {
        console.error("Error al cargar estudiantes o cursos:", err);
      }
    };
    fetchEstudiantesYCursos();
  }, []);

  // Cargar reporte de estudiante cuando cambia la selección
  const cargarReporteEstudiante = async (estudiante) => {
    if (!estudiante) return;
    setLoadingReporte(true);
    setErrorReporte('');
    setReporteCompleto(null);
    setMensajeComportamiento({ texto: '', tipo: '' });
    setOrdenarNotasAsc(null);

    const alumnoId = estudiante.usuarioId || estudiante.id;
    try {
      const token = getToken();
      const res = await fetch(`/api/reportes/completo/alumno/${alumnoId}?comportamiento=true`, {
        headers: { 'Authorization': 'Bearer ' + token }
      });
      if (res.ok) {
        const data = await res.json();
        setReporteCompleto(data);
      } else {
        setErrorReporte(`No se pudo obtener el reporte consolidado del alumno (Status: ${res.status}).`);
      }
    } catch (err) {
      setErrorReporte('Error de conexión al obtener el reporte.');
      console.error(err);
    } finally {
      setLoadingReporte(false);
    }
  };

  const handleSelectEstudiante = (est) => {
    setEstudianteSeleccionado(est);
    cargarReporteEstudiante(est);
  };

  // Filtrar estudiantes por nombre o RUT
  const estudiantesFiltrados = estudiantes.filter(est =>
    est.nombre?.toLowerCase().includes(busqueda.toLowerCase()) ||
    est.rut?.toLowerCase().includes(busqueda.toLowerCase())
  );

  // Registro de nuevo comportamiento
  const handleComportamientoSubmit = async (e) => {
    e.preventDefault();
    if (!estudianteSeleccionado) return;
    setMensajeComportamiento({ texto: '', tipo: '' });

    const alumnoId = estudianteSeleccionado.usuarioId || estudianteSeleccionado.id;
    const payload = {
      alumnoId: parseInt(alumnoId, 10),
      calificacion: comportamientoForm.calificacion,
      observaciones: comportamientoForm.observaciones
    };

    try {
      const res = await fetch('/api/reportes/comportamiento', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + getToken()
        },
        body: JSON.stringify(payload)
      });

      if (res.ok || res.status === 201) {
        setMensajeComportamiento({ texto: '¡Evaluación de comportamiento registrada con éxito!', tipo: 'success' });
        setComportamientoForm({ calificacion: 'Excelente', observaciones: '' });
        // Recargar reporte del alumno para reflejar la nueva anotación
        cargarReporteEstudiante(estudianteSeleccionado);
      } else {
        setMensajeComportamiento({ texto: `Error al registrar comportamiento (Status: ${res.status}).`, tipo: 'danger' });
      }
    } catch (err) {
      setMensajeComportamiento({ texto: 'Error de conexión con el servidor.', tipo: 'danger' });
      console.error(err);
    }
  };

  // Cargar reporte de curso
  const cargarReporteCurso = async (cursoId) => {
    if (!cursoId) return;
    setLoadingCurso(true);
    const token = getToken();

    try {
      // 1. Obtener conteo de activos
      const resCount = await fetch(`/api/reportes/matriculas/curso/${cursoId}/count-activos`, {
        headers: { 'Authorization': 'Bearer ' + token }
      });
      const activos = resCount.ok ? await resCount.json() : 0;

      // 2. Obtener asistencias del curso
      const resAsist = await fetch(`/api/reportes/asistencias/curso/${cursoId}`, {
        headers: { 'Authorization': 'Bearer ' + token }
      });
      const asistencias = resAsist.ok ? await resAsist.json() : [];

      setCursoStats({
        activos,
        asistencias,
        comportamientos: [] // El microservicio almacena comportamiento por alumno principalmente
      });
    } catch (err) {
      console.error("Error al cargar reportes de curso:", err);
    } finally {
      setLoadingCurso(false);
    }
  };

  const handleCursoChange = (e) => {
    const cursoId = e.target.value;
    setCursoSeleccionadoId(cursoId);
    if (cursoId) {
      cargarReporteCurso(cursoId);
    }
  };

  // Ordenar notas de forma local en el cliente
  const getNotasOrdenadas = () => {
    if (!reporteCompleto || !reporteCompleto.notas) return [];
    let list = [...reporteCompleto.notas];
    if (ordenarNotasAsc === true) {
      return list.sort((a, b) => (a.valor || 0) - (b.valor || 0));
    } else if (ordenarNotasAsc === false) {
      return list.sort((a, b) => (b.valor || 0) - (a.valor || 0));
    }
    return list; // original
  };

  const toggleOrdenNotas = () => {
    if (ordenarNotasAsc === null) setOrdenarNotasAsc(false); // empezar con mayor a menor
    else if (ordenarNotasAsc === false) setOrdenarNotasAsc(true); // de menor a mayor
    else setOrdenarNotasAsc(null); // original
  };

  // Formatear fechas
  const formatearFecha = (fechaStr) => {
    if (!fechaStr) return '';
    try {
      const date = new Date(fechaStr);
      return date.toLocaleDateString('es-CL', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch (e) {
      return fechaStr;
    }
  };

  return (
    <div className="container-fluid pb-5">
      {/* Encabezado Superior */}
      <div className="d-flex justify-content-between align-items-center mb-4 bg-white p-3 shadow-sm rounded">
        <div>
          <h2 className="text-primary fw-bold mb-0">Servicio de Reportes</h2>
          <small className="text-muted">Estadísticas académicas, asistencias y conducta consolidada</small>
        </div>
      </div>

      <div className="row g-4">
        {/* Panel Superior: Selector de Alumnos y Cursos */}
        <div className="col-lg-4">
          <div className="card shadow border-0 p-3 mb-4">
            <h5 className="fw-bold text-dark border-bottom pb-2 mb-3">
              <i className="bi bi-search me-2 text-primary"></i>Buscar Alumno
            </h5>
            <div className="mb-3">
              <input 
                type="text" 
                className="form-control" 
                placeholder="Nombre o RUT..." 
                value={busqueda}
                onChange={e => setBusqueda(e.target.value)}
              />
            </div>
            <div className="list-group overflow-auto" style={{ maxHeight: '350px' }}>
              {estudiantesFiltrados.map(est => (
                <button
                  key={est.id}
                  type="button"
                  onClick={() => handleSelectEstudiante(est)}
                  className={`list-group-item list-group-item-action border-0 d-flex justify-content-between align-items-center mb-1 rounded ${
                    estudianteSeleccionado?.id === est.id ? 'bg-primary text-white' : 'bg-light text-dark'
                  }`}
                  style={{ transition: 'all 0.2s' }}
                >
                  <div>
                    <div className="fw-bold small">{est.nombre}</div>
                    <small className={estudianteSeleccionado?.id === est.id ? 'text-white-50' : 'text-muted'}>
                      RUT: {est.rut}
                    </small>
                  </div>
                  <span className={`badge ${
                    estudianteSeleccionado?.id === est.id ? 'bg-white text-primary' : 'bg-secondary text-white'
                  }`}>
                    ID: {est.usuarioId || est.id}
                  </span>
                </button>
              ))}
              {estudiantesFiltrados.length === 0 && (
                <div className="text-center text-muted py-3">No se encontraron estudiantes.</div>
              )}
            </div>
          </div>

          <div className="card shadow border-0 p-3">
            <h5 className="fw-bold text-dark border-bottom pb-2 mb-3">
              <i className="bi bi-journal-bookmark me-2 text-primary"></i>Reporte por Cursos
            </h5>
            <div className="mb-3">
              <label className="form-label small fw-bold">Seleccione un Curso</label>
              <select 
                className="form-select" 
                value={cursoSeleccionadoId} 
                onChange={handleCursoChange}
              >
                <option value="">-- Seleccionar Curso --</option>
                {cursos.map(c => (
                  <option key={c.id} value={c.id}>{c.nombre} (Cod: {c.codigo})</option>
                ))}
              </select>
            </div>

            {cursoSeleccionadoId && (
              <div className="bg-light p-3 rounded border">
                {loadingCurso ? (
                  <div className="text-center py-3">
                    <div className="spinner-border spinner-border-sm text-primary" role="status"></div>
                  </div>
                ) : (
                  <div>
                    <div className="d-flex justify-content-between align-items-center mb-2">
                      <span className="small text-muted">Alumnos Activos:</span>
                      <span className="badge bg-success fs-6">{cursoStats.activos}</span>
                    </div>
                    <div className="d-flex justify-content-between align-items-center mb-3">
                      <span className="small text-muted">Clases registradas:</span>
                      <span className="badge bg-info text-white fs-6">{cursoStats.asistencias.length}</span>
                    </div>

                    <h6 className="fw-bold border-bottom pb-1 mb-2 small text-secondary">Últimas Asistencias de Curso</h6>
                    <div className="overflow-auto" style={{ maxHeight: '150px' }}>
                      <table className="table table-sm table-hover mb-0" style={{ fontSize: '0.75rem' }}>
                        <thead>
                          <tr>
                            <th>ID Alumno</th>
                            <th>Fecha</th>
                            <th>Estado</th>
                          </tr>
                        </thead>
                        <tbody>
                          {cursoStats.asistencias.slice(0, 8).map(as => (
                            <tr key={as.id}>
                              <td>Alumno #{as.alumnoId}</td>
                              <td>{formatearFecha(as.fechaEvento || as.fechaRegistro)}</td>
                              <td>
                                <span className={`badge ${as.presente ? 'bg-success' : 'bg-danger'}`}>
                                  {as.presente ? 'Presente' : 'Ausente'}
                                </span>
                              </td>
                            </tr>
                          ))}
                          {cursoStats.asistencias.length === 0 && (
                            <tr>
                              <td colSpan="3" className="text-center text-muted">Sin marcas registradas.</td>
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

        {/* Columna Derecha: Reporte Detallado del Estudiante */}
        <div className="col-lg-8">
          {!estudianteSeleccionado ? (
            <div className="card shadow border-0 p-5 text-center bg-white rounded d-flex flex-column align-items-center justify-content-center" style={{ minHeight: '400px' }}>
              <i className="bi bi-file-earmark-bar-graph text-muted mb-3" style={{ fontSize: '4rem' }}></i>
              <h4 className="fw-bold text-dark">No hay ningún alumno seleccionado</h4>
              <p className="text-muted max-w-md">Selecciona un estudiante del menú lateral izquierdo para ver su reporte consolidado de notas, asistencias, historial de matrícula y anotaciones de comportamiento.</p>
            </div>
          ) : loadingReporte ? (
            <div className="card shadow border-0 p-5 text-center bg-white rounded d-flex flex-column align-items-center justify-content-center" style={{ minHeight: '400px' }}>
              <div className="spinner-border text-primary mb-3" role="status" style={{ width: '3rem', height: '3rem' }}></div>
              <h5 className="fw-bold">Cargando reporte consolidado...</h5>
              <span className="text-muted">Consultando microservicios a través de API Gateway</span>
            </div>
          ) : errorReporte ? (
            <div className="card shadow border-0 p-4 bg-white rounded text-center">
              <div className="alert alert-danger" role="alert">
                <i className="bi bi-exclamation-triangle-fill me-2"></i> {errorReporte}
              </div>
              <button className="btn btn-primary mt-2" onClick={() => cargarReporteEstudiante(estudianteSeleccionado)}>
                Reintentar
              </button>
            </div>
          ) : (
            <div className="card shadow border-0 bg-white rounded overflow-hidden">
              {/* Encabezado del Alumno */}
              <div className="bg-dark text-white p-4">
                <div className="d-flex justify-content-between align-items-start flex-wrap gap-3">
                  <div>
                    <h3 className="fw-bold mb-1">{estudianteSeleccionado.nombre}</h3>
                    <div className="d-flex gap-3 text-white-50 small">
                      <span>RUT: <strong>{estudianteSeleccionado.rut}</strong></span>
                      <span>•</span>
                      <span>Curso ID: <strong>{estudianteSeleccionado.cursoId || 'Sin asignar'}</strong></span>
                      <span>•</span>
                      <span>Cuenta ID: <strong>{estudianteSeleccionado.usuarioId || estudianteSeleccionado.id}</strong></span>
                    </div>
                  </div>
                  <span className={`badge rounded-pill ${
                    estudianteSeleccionado.estado?.toLowerCase() === 'activo' ? 'bg-success' : 'bg-secondary'
                  } px-3 py-2 fs-6`}>
                    {estudianteSeleccionado.estado || 'Activo'}
                  </span>
                </div>
              </div>

              {/* Panel de Métricas Rápidas */}
              <div className="row g-0 border-bottom">
                <div className="col-md-3 border-end p-3 text-center bg-light">
                  <small className="text-muted d-block uppercase fw-bold mb-1" style={{ fontSize: '0.7rem' }}>Promedio General</small>
                  <h2 className={`fw-bold mb-0 ${
                    (reporteCompleto?.promedioGeneral || 0) >= 4.0 ? 'text-success' : 'text-danger'
                  }`}>
                    {reporteCompleto?.promedioGeneral ? reporteCompleto.promedioGeneral.toFixed(2) : 'N/A'}
                  </h2>
                  <small className="text-muted">Escala 1.0 - 7.0</small>
                </div>
                <div className="col-md-3 border-end p-3 text-center">
                  <small className="text-muted d-block uppercase fw-bold mb-1" style={{ fontSize: '0.7rem' }}>Asistencia General</small>
                  <h2 className={`fw-bold mb-0 ${metricasAsistencia.porcentaje >= 85 ? 'text-success' : 'text-danger'}`}>
                    {metricasAsistencia.porcentaje}%
                  </h2>
                  <small className="text-muted">{metricasAsistencia.presentes}/{metricasAsistencia.total} asistencias</small>
                </div>
                <div className="col-md-3 border-end p-3 text-center bg-light">
                  <small className="text-muted d-block uppercase fw-bold mb-1" style={{ fontSize: '0.7rem' }}>Observaciones</small>
                  <h2 className="fw-bold mb-0 text-info">
                    {reporteCompleto?.comportamientos?.length || 0}
                  </h2>
                  <small className="text-muted">Anotaciones de conducta</small>
                </div>
                <div className="col-md-3 p-3 text-center">
                  <small className="text-muted d-block uppercase fw-bold mb-1" style={{ fontSize: '0.7rem' }}>Apoderado</small>
                  <div className="fw-bold text-truncate" style={{ fontSize: '0.9rem' }}>
                    {estudianteSeleccionado.apoderado?.nombre || 'Ninguno'}
                  </div>
                  <small className="text-muted">{estudianteSeleccionado.apoderado?.telefono || 'Sin Fono'}</small>
                </div>
              </div>

              {/* Tabs de Navegación de Reportes */}
              <div className="bg-light border-bottom">
                <nav className="nav nav-tabs px-3 border-0">
                  <button 
                    className={`nav-link border-0 py-3 px-4 ${tabActiva === 'ficha' ? 'active bg-white border-bottom border-primary fw-bold text-primary' : 'text-secondary'}`}
                    onClick={() => setTabActiva('ficha')}
                  >
                    Ficha Consolidada
                  </button>
                  <button 
                    className={`nav-link border-0 py-3 px-4 ${tabActiva === 'notas' ? 'active bg-white border-bottom border-primary fw-bold text-primary' : 'text-secondary'}`}
                    onClick={() => setTabActiva('notas')}
                  >
                    Calificaciones
                  </button>
                  <button 
                    className={`nav-link border-0 py-3 px-4 ${tabActiva === 'asistencia' ? 'active bg-white border-bottom border-primary fw-bold text-primary' : 'text-secondary'}`}
                    onClick={() => setTabActiva('asistencia')}
                  >
                    Asistencia
                  </button>
                  <button 
                    className={`nav-link border-0 py-3 px-4 ${tabActiva === 'comportamiento' ? 'active bg-white border-bottom border-primary fw-bold text-primary' : 'text-secondary'}`}
                    onClick={() => setTabActiva('comportamiento')}
                  >
                    Conducta & Comportamiento
                  </button>
                </nav>
              </div>

              {/* Contenido de la pestaña activa */}
              <div className="card-body p-4">
                {/* 1. FICHA CONSOLIDADA */}
                {tabActiva === 'ficha' && (
                  <div>
                    <h5 className="fw-bold mb-3">Resumen Consolidado del Estudiante</h5>
                    <p className="text-muted small">Este reporte unifica datos en tiempo real de los servicios Académico, Asistencia y Conducta.</p>
                    
                    <div className="row g-4">
                      {/* Estado General */}
                      <div className="col-md-6">
                        <div className="card border-0 bg-light p-3 h-100 rounded-3">
                          <h6 className="fw-bold text-secondary mb-2"><i className="bi bi-mortarboard me-2 text-primary"></i>Rendimiento y Progreso</h6>
                          <div className="d-flex align-items-center gap-3">
                            <div className="fs-1 fw-bold text-primary">
                              {reporteCompleto?.promedioGeneral ? reporteCompleto.promedioGeneral.toFixed(2) : 'N/A'}
                            </div>
                            <div>
                              <div className="fw-bold">Promedio General</div>
                              <span className={`badge ${
                                (reporteCompleto?.promedioGeneral || 0) >= 4.0 ? 'bg-success' : 'bg-danger'
                              }`}>
                                {(reporteCompleto?.promedioGeneral || 0) >= 4.0 ? 'Aprobación Temporal' : 'Reprobación Temporal'}
                              </span>
                            </div>
                          </div>
                        </div>
                      </div>

                      {/* Asistencia */}
                      <div className="col-md-6">
                        <div className="card border-0 bg-light p-3 h-100 rounded-3">
                          <h6 className="fw-bold text-secondary mb-2"><i className="bi bi-calendar-check me-2 text-primary"></i>Resumen de Asistencia</h6>
                          <div className="d-flex align-items-center gap-3">
                            <div className={`fs-1 fw-bold ${metricasAsistencia.porcentaje >= 85 ? 'text-success' : 'text-danger'}`}>
                              {metricasAsistencia.porcentaje}%
                            </div>
                            <div>
                              <div className="fw-bold">{metricasAsistencia.presentes}/{metricasAsistencia.total} asistencias</div>
                              <span className="text-muted small">{metricasAsistencia.ausentes} inasistencias registradas</span>
                            </div>
                          </div>
                        </div>
                      </div>

                      {/* Conducta */}
                      <div className="col-md-12">
                        <div className="card border-0 bg-light p-3 rounded-3">
                          <h6 className="fw-bold text-secondary mb-2"><i className="bi bi-heart me-2 text-primary"></i>Evaluación Conductual Reciente</h6>
                          {reporteCompleto?.comportamientos && reporteCompleto.comportamientos.length > 0 ? (
                            <div>
                              <div className="d-flex align-items-center gap-2 mb-2">
                                <span className={`badge ${
                                  reporteCompleto.comportamientos[0].calificacion === 'Excelente' || reporteCompleto.comportamientos[0].calificacion === 'Muy Bueno' ? 'bg-success' :
                                  reporteCompleto.comportamientos[0].calificacion === 'Bueno' ? 'bg-info text-white' : 'bg-warning text-dark'
                                }`}>
                                  {reporteCompleto.comportamientos[0].calificacion}
                                </span>
                                <small className="text-muted">{formatearFecha(reporteCompleto.comportamientos[0].fechaRegistro)}</small>
                              </div>
                              <p className="mb-0 text-dark small italic">"{reporteCompleto.comportamientos[0].observaciones}"</p>
                            </div>
                          ) : (
                            <span className="text-muted small">No hay evaluaciones de comportamiento registradas para este alumno.</span>
                          )}
                        </div>
                      </div>
                    </div>
                  </div>
                )}

                {/* 2. HISTORIAL DE NOTAS */}
                {tabActiva === 'notas' && (
                  <div>
                    <div className="d-flex justify-content-between align-items-center mb-3">
                      <h5 className="fw-bold mb-0">Detalle de Calificaciones</h5>
                      <button 
                        className="btn btn-sm btn-outline-primary fw-bold"
                        onClick={toggleOrdenNotas}
                      >
                        <i className={`bi ${ordenarNotasAsc === null ? 'bi-sort-numeric-down' : ordenarNotasAsc === false ? 'bi-arrow-down-short' : 'bi-arrow-up-short'} me-1`}></i>
                        Ordenar: {ordenarNotasAsc === null ? 'Original' : ordenarNotasAsc === false ? 'Mayor a Menor' : 'Menor a Mayor'}
                      </button>
                    </div>

                    <div className="table-responsive">
                      <table className="table table-hover align-middle">
                        <thead className="table-light">
                          <tr>
                            <th>Evaluación / Prueba</th>
                            <th>Curso</th>
                            <th>Fecha</th>
                            <th className="text-center">Comentario</th>
                            <th className="text-center" style={{ width: '100px' }}>Calificación</th>
                          </tr>
                        </thead>
                        <tbody>
                          {getNotasOrdenadas().map((n, index) => (
                            <tr key={n.id || index}>
                              <td>
                                <div className="fw-bold">{n.prueba?.titulo || 'Evaluación'}</div>
                                <div className="small text-muted">{n.prueba?.descripcion || 'Sin descripción'}</div>
                              </td>
                              <td>{n.prueba?.curso?.nombre || `Curso #${n.prueba?.curso?.id || ''}`}</td>
                              <td>{n.prueba?.fecha ? new Date(n.prueba.fecha).toLocaleDateString('es-CL') : 'N/A'}</td>
                              <td className="text-center text-muted small">{n.comentario || 'Sin observaciones'}</td>
                              <td className="text-center fw-bold fs-5">
                                <span className={n.valor >= 4.0 ? 'text-success' : 'text-danger'}>
                                  {n.valor ? n.valor.toFixed(1) : '0.0'}
                                </span>
                              </td>
                            </tr>
                          ))}
                          {(!reporteCompleto?.notas || reporteCompleto.notas.length === 0) && (
                            <tr>
                              <td colSpan="5" className="text-center py-5 text-muted">
                                No se registran calificaciones en el sistema académico.
                              </td>
                            </tr>
                          )}
                        </tbody>
                      </table>
                    </div>
                  </div>
                )}

                {/* 3. HISTORIAL DE ASISTENCIAS */}
                {tabActiva === 'asistencia' && (
                  <div>
                    <h5 className="fw-bold mb-3">Registro de Asistencias e Inasistencias</h5>
                    <div className="table-responsive" style={{ maxHeight: '450px' }}>
                      <table className="table table-hover align-middle">
                        <thead className="table-light">
                          <tr>
                            <th>ID Registro</th>
                            <th>Curso ID</th>
                            <th>Fecha del Evento</th>
                            <th className="text-center">Estado</th>
                          </tr>
                        </thead>
                        <tbody>
                          {reporteCompleto?.asistencias?.map((as, index) => (
                            <tr key={as.id || index}>
                              <td className="text-muted">#{as.id || as.asistenciaId}</td>
                              <td>Curso #{as.cursoId || estudianteSeleccionado.cursoId}</td>
                              <td>{formatearFecha(as.fechaEvento || as.fechaRegistro)}</td>
                              <td className="text-center">
                                <span className={`badge rounded-pill px-3 py-2 ${
                                  as.presente === false ? 'bg-danger' : 'bg-success'
                                }`}>
                                  {as.presente === false ? 'Ausente' : 'Presente'}
                                </span>
                              </td>
                            </tr>
                          ))}
                          {(!reporteCompleto?.asistencias || reporteCompleto.asistencias.length === 0) && (
                            <tr>
                              <td colSpan="4" className="text-center py-5 text-muted">
                                No hay registros de asistencia para el estudiante.
                              </td>
                            </tr>
                          )}
                        </tbody>
                      </table>
                    </div>
                  </div>
                )}

                {/* 4. COMPORTAMIENTO & ANOTACIONES */}
                {tabActiva === 'comportamiento' && (
                  <div>
                    <div className="row g-4">
                      {/* Formulario de registro */}
                      <div className="col-md-5 border-end pr-md-4">
                        <h6 className="fw-bold text-primary mb-3">Agregar Evaluación Conductual</h6>
                        {mensajeComportamiento.texto && (
                          <div className={`alert alert-${mensajeComportamiento.tipo} py-2 small`} role="alert">
                            {mensajeComportamiento.texto}
                          </div>
                        )}
                        <form onSubmit={handleComportamientoSubmit}>
                          <div className="mb-3">
                            <label className="form-label small fw-bold">Calificación Conductual</label>
                            <select 
                              className="form-select" 
                              value={comportamientoForm.calificacion}
                              onChange={e => setComportamientoForm({ ...comportamientoForm, calificacion: e.target.value })}
                            >
                              <option value="Excelente">Excelente</option>
                              <option value="Muy Bueno">Muy Bueno</option>
                              <option value="Bueno">Bueno</option>
                              <option value="Regular">Regular</option>
                              <option value="Insuficiente">Insuficiente</option>
                            </select>
                          </div>
                          <div className="mb-3">
                            <label className="form-label small fw-bold">Observaciones / Descripción</label>
                            <textarea 
                              className="form-control" 
                              rows="4" 
                              placeholder="Indique los detalles de la anotación de conducta o situación académica..."
                              value={comportamientoForm.observaciones}
                              onChange={e => setComportamientoForm({ ...comportamientoForm, observaciones: e.target.value })}
                              required
                            ></textarea>
                          </div>
                          <button type="submit" className="btn btn-primary w-100 py-2 fw-bold">
                            <i className="bi bi-save me-2"></i>Registrar Anotación
                          </button>
                        </form>
                      </div>

                      {/* Listado de Anotaciones */}
                      <div className="col-md-7">
                        <h6 className="fw-bold text-secondary mb-3">Historial de Observaciones</h6>
                        <div className="overflow-auto pr-2" style={{ maxHeight: '350px' }}>
                          {reporteCompleto?.comportamientos?.map((comp) => (
                            <div key={comp.id} className="card border mb-3 shadow-sm rounded">
                              <div className="card-header bg-light py-2 d-flex justify-content-between align-items-center">
                                <span className={`badge ${
                                  comp.calificacion === 'Excelente' || comp.calificacion === 'Muy Bueno' ? 'bg-success' :
                                  comp.calificacion === 'Bueno' ? 'bg-info text-white' : 'bg-warning text-dark'
                                }`}>
                                  {comp.calificacion}
                                </span>
                                <small className="text-muted">{formatearFecha(comp.fechaRegistro)}</small>
                              </div>
                              <div className="card-body py-2">
                                <p className="mb-0 text-dark small">{comp.observaciones}</p>
                              </div>
                            </div>
                          ))}
                          {(!reporteCompleto?.comportamientos || reporteCompleto.comportamientos.length === 0) && (
                            <div className="text-center text-muted py-5">
                              No hay anotaciones de comportamiento registradas para este estudiante.
                            </div>
                          )}
                        </div>
                      </div>
                    </div>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
