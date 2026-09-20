import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getToken, getUserId } from '../utils/auth';

export default function Cursos() {
  const navigate = useNavigate();
  const [estudiante, setEstudiante] = useState(null);
  const [curso, setCurso] = useState(null);
  const [asignaturas, setAsignaturas] = useState([]);
  const [pruebas, setPruebas] = useState([]);
  const [notas, setNotas] = useState([]);
  
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const cargarDatosAlumno = async () => {
    setLoading(true);
    setError('');
    try {
      const token = getToken();
      const uId = getUserId();
      if (!uId) {
        setError('No se pudo identificar al usuario en sesión.');
        setLoading(false);
        return;
      }

      // 1. Obtener ficha del estudiante en matrícula
      const resEstudiante = await fetch(`/api/matricula/estudiantes/usuario/${uId}`, {
        headers: { 'Authorization': 'Bearer ' + token }
      });

      if (!resEstudiante.ok) {
        if (resEstudiante.status === 404) {
          setError('⚠️ No tienes una matrícula o ficha de estudiante registrada en el sistema. Solicita a un administrador que te matricule.');
        } else {
          setError('Error al obtener la información de matrícula.');
        }
        setLoading(false);
        return;
      }

      const estudianteData = await resEstudiante.json();
      setEstudiante(estudianteData);

      const cursoId = estudianteData.cursoId;
      if (!cursoId) {
        setError('⚠️ Estás registrado pero aún no tienes un curso asignado.');
        setLoading(false);
        return;
      }

      // 2. Cargar datos en paralelo: curso, asignaturas del curso, pruebas del curso y notas del alumno
      const [resCurso, resAsignaturas, resPruebas, resNotas] = await Promise.all([
        fetch(`/api/academico/cursos/${cursoId}`, { headers: { 'Authorization': 'Bearer ' + token } }),
        fetch(`/api/academico/cursos/${cursoId}/asignaturas`, { headers: { 'Authorization': 'Bearer ' + token } }),
        fetch(`/api/academico/pruebas/curso/${cursoId}`, { headers: { 'Authorization': 'Bearer ' + token } }),
        fetch(`/api/academico/notas/alumno/${uId}`, { headers: { 'Authorization': 'Bearer ' + token } })
      ]);

      if (resCurso.ok) setCurso(await resCurso.json());
      if (resAsignaturas.ok) setAsignaturas(await resAsignaturas.json());
      if (resPruebas.ok) setPruebas(await resPruebas.json());
      if (resNotas.ok) setNotas(await resNotas.json());

    } catch (err) {
      console.error("Error cargando asignaturas y notas:", err);
      setError('Ocurrió un error al intentar cargar tus calificaciones y asignaturas. Verifica tu conexión.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    cargarDatosAlumno();
  }, []);

  // Agrupar pruebas y notas por asignatura
  const obtenerDatosAsignatura = (nombreAsignatura) => {
    // Filtrar pruebas de esta asignatura
    const pruebasAsignatura = pruebas.filter(
      p => p.asignatura && p.asignatura.toLowerCase() === nombreAsignatura.toLowerCase()
    );

    // Cruzar con notas
    const evaluaciones = pruebasAsignatura.map(p => {
      const notaOpt = notas.find(n => n.prueba && n.prueba.id === p.id);
      return {
        pruebaId: p.id,
        titulo: p.titulo,
        descripcion: p.descripcion,
        fecha: p.fecha,
        nota: notaOpt ? notaOpt.valor : null,
        comentario: notaOpt ? notaOpt.comentario : null
      };
    });

    // Calcular promedio
    const notasValidas = evaluaciones.map(ev => ev.nota).filter(n => n !== null);
    const promedio = notasValidas.length > 0
      ? (notasValidas.reduce((a, b) => a + b, 0) / notasValidas.length).toFixed(1)
      : null;

    return { evaluaciones, promedio };
  };

  if (loading) {
    return (
      <div className="container-fluid autumn-bg d-flex justify-content-center align-items-center" style={{ minHeight: '100vh' }}>
        <div className="text-center bg-white p-5 rounded shadow">
          <div className="spinner-border text-primary mb-3" role="status" style={{ width: '3rem', height: '3rem' }}></div>
          <h5 className="text-muted">Cargando tus asignaturas y calificaciones...</h5>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container-fluid autumn-bg d-flex justify-content-center align-items-center" style={{ minHeight: '100vh' }}>
        <div className="card shadow-lg p-4 text-center bg-white rounded" style={{ maxWidth: '550px' }}>
          <div className="card-body">
            <i className="bi bi-exclamation-triangle fs-1 text-warning mb-3"></i>
            <h4 className="fw-bold text-dark mb-3">Información Académica</h4>
            <p className="text-secondary fs-5">{error}</p>
            <button onClick={() => navigate('/dashboard')} className="btn btn-primary mt-3 fw-bold px-4">
              Volver al Inicio
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="container-fluid autumn-bg pb-5" style={{ minHeight: '100vh' }}>
      
      {/* Encabezado Ficha del Alumno */}
      <div className="container pt-4">
        <div className="card border-0 shadow-sm p-4 mb-4 bg-white rounded border-start border-primary border-4">
          <div className="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3">
            <div>
              <span className="badge bg-primary mb-2 text-white px-3 py-2 fs-7 fw-bold">{curso?.nombre || 'Curso'}</span>
              <h2 className="fw-bold text-dark mb-1">{estudiante?.nombre}</h2>
              <small className="text-muted">RUT: {estudiante?.rut} | Ficha Estado: <span className="text-success fw-bold">{estudiante?.estado}</span></small>
            </div>
            <div>
              <button onClick={() => navigate('/dashboard')} className="btn btn-outline-primary fw-bold shadow-sm">
                <i className="bi bi-arrow-left me-2"></i>Volver al Panel
              </button>
            </div>
          </div>
        </div>

        {/* Sección de Asignaturas */}
        <h4 className="fw-bold mb-4 text-dark"><i className="bi bi-book-half me-2"></i>Mis Asignaturas</h4>

        {asignaturas.length === 0 ? (
          <div className="card shadow border-0 p-5 text-center text-muted bg-white rounded">
            <i className="bi bi-journal-x fs-1 text-secondary mb-3"></i>
            <h4>No hay asignaturas asociadas a este curso</h4>
            <p className="mb-0">Consulta con la administración del colegio para asignar materias.</p>
          </div>
        ) : (
          <div className="row g-4">
            {asignaturas.map(a => {
              const { evaluaciones, promedio } = obtenerDatosAsignatura(a.asignatura);
              return (
                <div className="col-12" key={a.id}>
                  <div className="card shadow-sm border-0 bg-white rounded overflow-hidden">
                    
                    {/* Encabezado Asignatura */}
                    <div className="card-header bg-dark text-white p-3 d-flex justify-content-between align-items-center">
                      <div>
                        <h4 className="mb-0 fw-bold fs-5 text-capitalize">{a.asignatura}</h4>
                        <small className="text-white-50">
                          {a.profesorId ? 'Profesor Asignado' : 'Sin Profesor Designado'}
                        </small>
                      </div>
                      <div className="text-end">
                        <span className="small text-white-50 d-block">Promedio General</span>
                        <span className={`fs-4 fw-bold px-3 py-1 rounded ${promedio === null ? 'bg-secondary text-white' : parseFloat(promedio) >= 4.0 ? 'bg-success text-white' : 'bg-danger'}`}>
                          {promedio !== null ? promedio : 'S/N'}
                        </span>
                      </div>
                    </div>

                    {/* Detalle Evaluaciones */}
                    <div className="card-body p-0">
                      <div className="table-responsive">
                        <table className="table table-hover align-middle mb-0">
                          <thead className="table-light">
                            <tr>
                              <th className="ps-4">Evaluación</th>
                              <th>Fecha</th>
                              <th className="text-center" style={{ width: '120px' }}>Calificación</th>
                              <th>Observaciones del Profesor</th>
                            </tr>
                          </thead>
                          <tbody>
                            {evaluaciones.length === 0 ? (
                              <tr>
                                <td colSpan="4" className="text-center text-muted p-4">
                                  No hay pruebas ni evaluaciones planificadas en esta materia.
                                </td>
                              </tr>
                            ) : (
                              evaluaciones.map((ev, idx) => (
                                <tr key={idx}>
                                  <td className="ps-4">
                                    <div className="fw-bold text-dark">{ev.titulo}</div>
                                    {ev.descripcion && <small className="text-muted">{ev.descripcion}</small>}
                                  </td>
                                  <td>{new Date(ev.fecha).toLocaleDateString('es-CL')}</td>
                                  <td className="text-center">
                                    {ev.nota !== null ? (
                                      <span className={`fw-bold fs-6 px-2 py-1 rounded ${ev.nota >= 4.0 ? 'text-success bg-success bg-opacity-10' : 'text-danger bg-danger bg-opacity-10'}`}>
                                        {ev.nota.toFixed(1)}
                                      </span>
                                    ) : (
                                      <span className="text-muted small italic">Pendiente</span>
                                    )}
                                  </td>
                                  <td className="text-muted small">
                                    {ev.comentario || <span className="text-black-30 italic">Sin comentarios</span>}
                                  </td>
                                </tr>
                              ))
                            )}
                          </tbody>
                        </table>
                      </div>
                    </div>

                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

    </div>
  );
}
