import React, { useState, useEffect } from 'react';
import { getToken } from '../utils/auth';

export default function ApoderadoReportes() {
  const [estudiantes, setEstudiantes] = useState([]);
  const [estudianteSeleccionado, setEstudianteSeleccionado] = useState(null);
  const [reporteCompleto, setReporteCompleto] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [tabActiva, setTabActiva] = useState('ficha');

  useEffect(() => {
    const fetchLinkedEstudiantes = async () => {
      setLoading(true);
      setError('');
      try {
        const res = await fetch('/api/matricula/apoderados/me/estudiantes', {
          headers: { 'Authorization': 'Bearer ' + getToken() }
        });
        if (res.ok) {
          const data = await res.json();
          setEstudiantes(data);
          if (data.length > 0) {
            // Seleccionar automáticamente al primer alumno
            setEstudianteSeleccionado(data[0]);
            cargarReporte(data[0].usuarioId || data[0].id);
          }
        } else {
          setError('No se pudo obtener la lista de alumnos vinculados.');
        }
      } catch (err) {
        setError('Error de conexión al cargar alumnos vinculados.');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchLinkedEstudiantes();
  }, []);

  const cargarReporte = async (alumnoId) => {
    setLoading(true);
    setError('');
    setReporteCompleto(null);
    try {
      const res = await fetch(`/api/reportes/completo/alumno/${alumnoId}?comportamiento=true`, {
        headers: { 'Authorization': 'Bearer ' + getToken() }
      });
      if (res.ok) {
        const data = await res.json();
        setReporteCompleto(data);
      } else {
        setError('No se pudo obtener el reporte consolidado de este alumno.');
      }
    } catch (err) {
      setError('Error al conectar con el servidor de reportes.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSelectEstudiante = (est) => {
    setEstudianteSeleccionado(est);
    cargarReporte(est.usuarioId || est.id);
  };

  const formatearFecha = (fechaStr) => {
    if (!fechaStr) return 'N/A';
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

  // --- CÁLCULO DE MÉTRICAS ---
  
  // Calcular porcentaje de asistencia real
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

  // Agrupar calificaciones por asignatura
  const agruparNotasPorAsignatura = () => {
    if (!reporteCompleto || !reporteCompleto.notas) return {};
    
    const agrupado = {};
    reporteCompleto.notas.forEach(n => {
      const nombreAsig = n.prueba?.asignatura || 'General';
      const key = nombreAsig.toLowerCase();
      
      if (!agrupado[key]) {
        agrupado[key] = {
          nombre: nombreAsig,
          evaluaciones: []
        };
      }
      agrupado[key].evaluaciones.push({
        id: n.id,
        titulo: n.prueba?.titulo || 'Evaluación',
        descripcion: n.prueba?.descripcion || 'Sin descripción',
        fecha: n.prueba?.fecha || null,
        valor: n.valor,
        comentario: n.comentario || ''
      });
    });

    // Calcular promedio para cada asignatura
    Object.keys(agrupado).forEach(key => {
      const notasValidas = agrupado[key].evaluaciones.map(e => e.valor).filter(v => v !== null && v !== undefined);
      agrupado[key].promedio = notasValidas.length > 0
        ? (notasValidas.reduce((sum, v) => sum + v, 0) / notasValidas.length).toFixed(2)
        : null;
    });

    return agrupado;
  };

  const notasAgrupadas = agruparNotasPorAsignatura();

  return (
    <div className="container py-4 autumn-bg" style={{ minHeight: '100vh' }}>
      
      {/* Encabezado */}
      <div className="d-flex justify-content-between align-items-center mb-4 bg-white p-3 shadow-sm rounded border-start border-success border-4">
        <div>
          <h2 className="text-success fw-bold mb-0">Reporte Académico de mi Alumno</h2>
          <small className="text-muted">Consulte calificaciones, asistencia y anotaciones en tiempo real</small>
        </div>
      </div>

      {error && (
        <div className="alert alert-danger shadow-sm" role="alert">
          <i className="bi bi-exclamation-triangle-fill me-2"></i>{error}
        </div>
      )}

      {/* Selector de alumnos vinculados (si hay más de uno) */}
      {estudiantes.length > 1 && (
        <div className="card shadow-sm border-0 p-3 mb-4 bg-white rounded">
          <label className="form-label fw-bold text-secondary small">Seleccione el Alumno a Consultar:</label>
          <div className="d-flex gap-2">
            {estudiantes.map(est => (
              <button
                key={est.id}
                onClick={() => handleSelectEstudiante(est)}
                className={`btn ${estudianteSeleccionado?.id === est.id ? 'btn-success text-white' : 'btn-outline-success'} fw-bold`}
              >
                {est.nombre}
              </button>
            ))}
          </div>
        </div>
      )}

      {estudiantes.length === 0 && !loading && (
        <div className="card shadow border-0 p-5 text-center bg-white rounded">
          <i className="bi bi-person-x text-muted mb-3" style={{ fontSize: '4rem' }}></i>
          <h4 className="fw-bold">No tienes alumnos vinculados</h4>
          <p className="text-muted">Comunícate con el administrador para que asocie tu cuenta de apoderado a la matrícula del alumno.</p>
        </div>
      )}

      {loading && !reporteCompleto && (
        <div className="card shadow border-0 p-5 text-center bg-white rounded">
          <div className="spinner-border text-success mb-3" role="status" style={{ width: '3rem', height: '3rem' }}></div>
          <h5 className="fw-bold">Cargando reporte de rendimiento...</h5>
        </div>
      )}

      {reporteCompleto && estudianteSeleccionado && (
        <div className="card shadow border-0 bg-white rounded overflow-hidden">
          
          {/* Ficha Header */}
          <div className="bg-success text-white p-4">
            <div className="d-flex justify-content-between align-items-start flex-wrap gap-3">
              <div>
                <span className="badge bg-white text-success mb-2 px-3 py-1 fw-bold fs-7">
                  Curso ID: {estudianteSeleccionado.cursoId || 'Sin asignar'}
                </span>
                <h3 className="fw-bold mb-1">{estudianteSeleccionado.nombre}</h3>
                <div className="d-flex gap-3 text-white-50 small">
                  <span>RUT: <strong>{estudianteSeleccionado.rut}</strong></span>
                  <span>•</span>
                  <span>ID Alumno: <strong>{estudianteSeleccionado.usuarioId || estudianteSeleccionado.id}</strong></span>
                </div>
              </div>
              <span className="badge rounded-pill bg-white text-success px-3 py-2 fs-6 fw-bold">
                {estudianteSeleccionado.estado || 'Activo'}
              </span>
            </div>
          </div>

          {/* Quick Metrics */}
          <div className="row g-0 border-bottom">
            <div className="col-md-4 border-end p-3 text-center bg-light">
              <small className="text-muted d-block uppercase fw-bold mb-1" style={{ fontSize: '0.7rem' }}>Promedio General</small>
              <h2 className={`fw-bold mb-0 ${reporteCompleto.promedioGeneral >= 4.0 ? 'text-success' : 'text-danger'}`}>
                {reporteCompleto.promedioGeneral ? reporteCompleto.promedioGeneral.toFixed(2) : 'N/A'}
              </h2>
              <small className="text-muted">Escala 1.0 - 7.0</small>
            </div>
            <div className="col-md-4 border-end p-3 text-center">
              <small className="text-muted d-block uppercase fw-bold mb-1" style={{ fontSize: '0.7rem' }}>Asistencia General</small>
              <h2 className={`fw-bold mb-0 ${metricasAsistencia.porcentaje >= 85 ? 'text-success' : 'text-danger'}`}>
                {metricasAsistencia.porcentaje}%
              </h2>
              <small className="text-muted">{metricasAsistencia.presentes}/{metricasAsistencia.total} asistencias</small>
            </div>
            <div className="col-md-4 p-3 text-center bg-light">
              <small className="text-muted d-block uppercase fw-bold mb-1" style={{ fontSize: '0.7rem' }}>Observaciones de Conducta</small>
              <h2 className="fw-bold mb-0 text-info">
                {reporteCompleto.comportamientos?.length || 0}
              </h2>
              <small className="text-muted">Anotaciones del periodo</small>
            </div>
          </div>

          {/* Tabs */}
          <div className="bg-light border-bottom">
            <nav className="nav nav-tabs px-3 border-0">
              <button 
                className={`nav-link border-0 py-3 px-4 ${tabActiva === 'ficha' ? 'active bg-white border-bottom border-success fw-bold text-success' : 'text-secondary'}`}
                onClick={() => setTabActiva('ficha')}
              >
                Ficha Consolidada
              </button>
              <button 
                className={`nav-link border-0 py-3 px-4 ${tabActiva === 'notas' ? 'active bg-white border-bottom border-success fw-bold text-success' : 'text-secondary'}`}
                onClick={() => setTabActiva('notas')}
              >
                Calificaciones por Materia
              </button>
              <button 
                className={`nav-link border-0 py-3 px-4 ${tabActiva === 'asistencia' ? 'active bg-white border-bottom border-success fw-bold text-success' : 'text-secondary'}`}
                onClick={() => setTabActiva('asistencia')}
              >
                Historial de Asistencia
              </button>
              <button 
                className={`nav-link border-0 py-3 px-4 ${tabActiva === 'comportamiento' ? 'active bg-white border-bottom border-success fw-bold text-success' : 'text-secondary'}`}
                onClick={() => setTabActiva('comportamiento')}
              >
                Conducta ({reporteCompleto.comportamientos?.length || 0})
              </button>
            </nav>
          </div>

          {/* Tab Content */}
          <div className="card-body p-4 bg-white">
            
            {/* 1. RESUMEN GENERAL */}
            {tabActiva === 'ficha' && (
              <div className="row g-4">
                <div className="col-md-6">
                  <div className="card border-0 bg-light p-3 h-100 rounded-3 shadow-sm">
                    <h6 className="fw-bold text-secondary mb-2"><i className="bi bi-mortarboard me-2 text-success"></i>Progreso Académico</h6>
                    <div className="d-flex align-items-center gap-3 mt-2">
                      <div className={`fs-1 fw-bold ${reporteCompleto.promedioGeneral >= 4.0 ? 'text-success' : 'text-danger'}`}>
                        {reporteCompleto.promedioGeneral ? reporteCompleto.promedioGeneral.toFixed(2) : 'N/A'}
                      </div>
                      <div>
                        <div className="fw-bold">Promedio de Notas</div>
                        <span className={`badge ${reporteCompleto.promedioGeneral >= 4.0 ? 'bg-success text-white' : 'bg-danger text-white'}`}>
                          {reporteCompleto.promedioGeneral >= 4.0 ? 'Rendimiento Aprobatorio' : 'Bajo el Mínimo'}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>

                <div className="col-md-6">
                  <div className="card border-0 bg-light p-3 h-100 rounded-3 shadow-sm">
                    <h6 className="fw-bold text-secondary mb-2"><i className="bi bi-calendar-check me-2 text-success"></i>Porcentaje Asistencia</h6>
                    <div className="d-flex align-items-center gap-3 mt-2">
                      <div className={`fs-1 fw-bold ${metricasAsistencia.porcentaje >= 85 ? 'text-success' : 'text-danger'}`}>
                        {metricasAsistencia.porcentaje}%
                      </div>
                      <div>
                        <div className="fw-bold">{metricasAsistencia.presentes}/{metricasAsistencia.total} asistencias</div>
                        <span className="text-muted small">{metricasAsistencia.ausentes} inasistencias registradas</span>
                      </div>
                    </div>
                    <div className="progress mt-2" style={{ height: '6px' }}>
                      <div 
                        className={`progress-bar ${metricasAsistencia.porcentaje >= 85 ? 'bg-success' : 'bg-danger'}`} 
                        role="progressbar" 
                        style={{ width: `${metricasAsistencia.porcentaje}%` }}
                      ></div>
                    </div>
                  </div>
                </div>

                <div className="col-md-12">
                  <div className="card border-0 bg-light p-3 rounded-3 shadow-sm">
                    <h6 className="fw-bold text-secondary mb-2"><i className="bi bi-chat-text me-2 text-success"></i>Última Anotación Conductual</h6>
                    {reporteCompleto.comportamientos && reporteCompleto.comportamientos.length > 0 ? (
                      <div className="mt-2">
                        <div className="d-flex align-items-center gap-2 mb-2">
                          <span className={`badge ${
                            ['Excelente', 'Muy Bueno', 'Bueno'].includes(reporteCompleto.comportamientos[0].calificacion) ? 'bg-success' : 'bg-warning text-dark'
                          }`}>
                            {reporteCompleto.comportamientos[0].calificacion}
                          </span>
                          <small className="text-muted">{formatearFecha(reporteCompleto.comportamientos[0].fechaRegistro)}</small>
                        </div>
                        <p className="mb-0 text-dark small italic bg-white p-3 rounded border border-light-subtle">
                          "{reporteCompleto.comportamientos[0].observaciones}"
                        </p>
                      </div>
                    ) : (
                      <span className="text-muted small">No hay anotaciones de comportamiento registradas para este alumno.</span>
                    )}
                  </div>
                </div>
              </div>
            )}

            {/* 2. CALIFICACIONES AGRUPADAS POR ASIGNATURA */}
            {tabActiva === 'notas' && (
              <div className="row g-4">
                {Object.keys(notasAgrupadas).length === 0 ? (
                  <div className="col-12 text-center py-5 text-muted">
                    <i className="bi bi-journal-x fs-1 mb-2 d-block"></i>
                    No hay calificaciones registradas para el alumno en el sistema académico.
                  </div>
                ) : (
                  Object.keys(notasAgrupadas).map(key => {
                    const asig = notasAgrupadas[key];
                    return (
                      <div className="col-12" key={key}>
                        <div className="card shadow-sm border border-light-subtle rounded-3 overflow-hidden">
                          
                          {/* Cabecera Materia */}
                          <div className="bg-dark text-white p-3 d-flex justify-content-between align-items-center">
                            <h5 className="mb-0 fw-bold text-capitalize"><i className="bi bi-book me-2"></i>{asig.nombre}</h5>
                            <div className="text-end">
                              <span className="small text-white-50 me-2">Promedio:</span>
                              <span className={`fw-bold fs-5 px-3 py-1 rounded ${asig.promedio === null ? 'bg-secondary' : parseFloat(asig.promedio) >= 4.0 ? 'bg-success' : 'bg-danger'}`}>
                                {asig.promedio !== null ? parseFloat(asig.promedio).toFixed(1) : 'S/N'}
                              </span>
                            </div>
                          </div>

                          {/* Tabla Evaluaciones */}
                          <div className="card-body p-0">
                            <table className="table table-hover align-middle mb-0">
                              <thead className="table-light">
                                <tr>
                                  <th className="ps-4">Evaluación</th>
                                  <th>Fecha</th>
                                  <th>Comentario del Docente</th>
                                  <th className="text-center" style={{ width: '100px' }}>Calificación</th>
                                </tr>
                              </thead>
                              <tbody>
                                {asig.evaluaciones.map(ev => (
                                  <tr key={ev.id}>
                                    <td className="ps-4">
                                      <div className="fw-bold text-dark">{ev.titulo}</div>
                                      <small className="text-muted">{ev.descripcion}</small>
                                    </td>
                                    <td>{ev.fecha ? new Date(ev.fecha).toLocaleDateString('es-CL') : 'N/A'}</td>
                                    <td className="text-muted small">{ev.comentario || 'Sin comentarios'}</td>
                                    <td className="text-center fw-bold fs-5">
                                      <span className={ev.valor >= 4.0 ? 'text-success' : 'text-danger'}>
                                        {ev.valor ? ev.valor.toFixed(1) : '0.0'}
                                      </span>
                                    </td>
                                  </tr>
                                ))}
                              </tbody>
                            </table>
                          </div>

                        </div>
                      </div>
                    );
                  })
                )}
              </div>
            )}

            {/* 3. HISTORIAL DE ASISTENCIA */}
            {tabActiva === 'asistencia' && (
              <div>
                <div className="d-flex justify-content-between align-items-center mb-3">
                  <h5 className="fw-bold mb-0">Listado de Asistencias e Inasistencias</h5>
                  <div className="small text-muted">
                    Asistió a <strong>{metricasAsistencia.presentes}</strong> de <strong>{metricasAsistencia.total}</strong> registros.
                  </div>
                </div>

                <div className="table-responsive" style={{ maxHeight: '450px' }}>
                  <table className="table table-hover align-middle">
                    <thead className="table-light">
                      <tr>
                        <th>ID Registro</th>
                        <th>Fecha de Clase</th>
                        <th>Asignatura</th>
                        <th className="text-center">Estado</th>
                      </tr>
                    </thead>
                    <tbody>
                      {reporteCompleto.asistencias?.map((as, index) => (
                        <tr key={as.id || index}>
                          <td className="text-muted">#{as.id || as.asistenciaId}</td>
                          <td>{formatearFecha(as.fecha || as.fechaEvento || as.fechaRegistro)}</td>
                          <td>
                            <span className="fw-bold text-capitalize">
                              {as.asignatura ? as.asignatura.replace(/[0-9]/g, '').trim() : 'General'}
                            </span>
                            <small className="text-muted ms-1">({as.asignatura || 'General'})</small>
                          </td>
                          <td className="text-center">
                            <span className={`badge rounded-pill px-3 py-2 ${
                              as.presente === false ? 'bg-danger' : 'bg-success'
                            }`}>
                              {as.presente === false ? 'Ausente' : 'Presente'}
                            </span>
                          </td>
                        </tr>
                      ))}
                      {(!reporteCompleto.asistencias || reporteCompleto.asistencias.length === 0) && (
                        <tr>
                          <td colSpan="4" className="text-center py-5 text-muted">
                            No se registran marcas de asistencia para el alumno.
                          </td>
                        </tr>
                      )}
                    </tbody>
                  </table>
                </div>
              </div>
            )}

            {/* 4. HISTORIAL DE CONDUCTA */}
            {tabActiva === 'comportamiento' && (
              <div>
                <h5 className="fw-bold mb-4">Registro Histórico de Comportamiento</h5>
                <div className="row g-3">
                  {reporteCompleto.comportamientos?.map((comp) => (
                    <div key={comp.id} className="col-12">
                      <div className="card border-0 bg-light p-3 shadow-sm rounded-3 border-start border-4 border-info">
                        <div className="d-flex justify-content-between align-items-center mb-2">
                          <span className={`badge ${
                            ['Excelente', 'Muy Bueno', 'Bueno'].includes(comp.calificacion) ? 'bg-success' :
                            comp.calificacion === 'Regular' ? 'bg-warning text-dark' : 'bg-danger'
                          }`}>
                            {comp.calificacion}
                          </span>
                          <small className="text-muted">{formatearFecha(comp.fechaRegistro)}</small>
                        </div>
                        <p className="mb-0 text-dark small italic">"{comp.observaciones}"</p>
                      </div>
                    </div>
                  ))}
                  {(!reporteCompleto.comportamientos || reporteCompleto.comportamientos.length === 0) && (
                    <div className="col-12 text-center text-muted py-5">
                      <i className="bi bi-emoji-smile fs-1 mb-2 d-block text-success"></i>
                      No hay observaciones conductuales registradas para este alumno.
                    </div>
                  )}
                </div>
              </div>
            )}

          </div>

        </div>
      )}

    </div>
  );
}
