import React, { useState, useEffect } from 'react';
import { getToken } from '../utils/auth';

export default function AdminMatriculas() {
  const [form, setForm] = useState({
    nombre: '',
    rut: '',
    fechaNacimiento: '',
    cursoId: '',
    usuarioId: '',
    estado: 'Activo',
    apoderado: {
      rut: '',
      nombre: '',
      telefono: '',
      correo: ''
    }
  });

  const [mensaje, setMensaje] = useState({ texto: '', tipo: '' });
  const [alumnos, setAlumnos] = useState([]);
  const [apoderadosUsuarios, setApoderadosUsuarios] = useState([]);
  const [cursos, setCursos] = useState([]);

  useEffect(() => {
    const fetchDatos = async () => {
      try {
        const token = getToken();
        // Cargar alumnos
        const resAlumnos = await fetch('/api/matricula/usuarios/rol/Alumno', {
          headers: { 'Authorization': 'Bearer ' + token }
        });
        if (resAlumnos.ok) {
          const data = await resAlumnos.json();
          setAlumnos(data);
        }

        // Cargar apoderados
        const resApoderados = await fetch('/api/matricula/usuarios/rol/Apoderado', {
          headers: { 'Authorization': 'Bearer ' + token }
        });
        if (resApoderados.ok) {
          const data = await resApoderados.json();
          setApoderadosUsuarios(data);
        }

        // Cargar cursos
        const resCursos = await fetch('/api/academico/cursos', {
          headers: { 'Authorization': 'Bearer ' + token }
        });
        if (resCursos.ok) {
          const data = await resCursos.json();
          setCursos(data);
        }
      } catch (err) {
        console.error("Error cargando datos para matrícula:", err);
      }
    };
    fetchDatos();
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    if (name.startsWith('apoderado.')) {
      const field = name.split('.')[1];
      setForm({
        ...form,
        apoderado: { ...form.apoderado, [field]: value }
      });
    } else {
      setForm({ ...form, [name]: value });
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMensaje({ texto: '', tipo: '' });

    // Preparar el payload asegurando que usuarioId y cursoId sean numéricos si existen
    const payload = {
      ...form,
      cursoId: form.cursoId ? parseInt(form.cursoId, 10) : null,
      usuarioId: form.usuarioId ? parseInt(form.usuarioId, 10) : null,
      apoderado: {
        ...form.apoderado,
        usuarioId: form.apoderado.usuarioId ? parseInt(form.apoderado.usuarioId, 10) : null
      }
    };

    try {
      const res = await fetch('/api/matricula/registrar-completo', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + getToken()
        },
        body: JSON.stringify(payload)
      });

      if (res.ok || res.status === 201) {
        setMensaje({ texto: '¡Matrícula registrada exitosamente en el sistema!', tipo: 'success' });
        // Limpiar el formulario
        setForm({
          nombre: '', rut: '', fechaNacimiento: '', cursoId: '', usuarioId: '', estado: 'Activo',
          apoderado: { rut: '', nombre: '', telefono: '', correo: '', usuarioId: '' }
        });
      } else {
        const errorText = await res.text();
        setMensaje({ texto: `Error al crear la matrícula: ${errorText || res.statusText}`, tipo: 'danger' });
      }
    } catch (err) {
      setMensaje({ texto: 'Error de conexión con el servidor al registrar matrícula.', tipo: 'danger' });
      console.error('Error POST /api/matricula/registrar-completo:', err);
    }
  };

  return (
    <div className="container-fluid pb-5">
      <div className="d-flex justify-content-between align-items-center mb-4 bg-white p-3 shadow-sm rounded">
        <div>
          <h2 className="text-primary fw-bold mb-0">Gestión de Matrículas</h2>
          <small className="text-muted">Inscripción completa de Alumnos y Apoderados</small>
        </div>
      </div>

      <div className="card shadow border-0 p-4 mx-auto" style={{ maxWidth: '800px' }}>
        <h4 className="fw-bold mb-4 border-bottom pb-2">Formulario de Nueva Matrícula</h4>

        {mensaje.texto && (
          <div className={`alert alert-${mensaje.tipo} py-2`} role="alert">
            {mensaje.texto}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="row">
            {/* --- SECCIÓN ALUMNO --- */}
            <div className="col-md-6 mb-4">
              <h5 className="text-secondary fw-bold mb-3"><i className="bi bi-person-badge me-2"></i>Datos del Alumno</h5>

              <div className="mb-3">
                <label className="form-label small fw-bold">Nombre Completo</label>
                <input type="text" className="form-control" name="nombre" value={form.nombre} onChange={handleChange} required placeholder="Ej: Juan Pérez" />
              </div>

              <div className="mb-3">
                <label className="form-label small fw-bold">RUT Alumno</label>
                <input type="text" className="form-control" name="rut" value={form.rut} onChange={handleChange} required placeholder="Ej: 12345678-9" />
              </div>

              <div className="mb-3">
                <label className="form-label small fw-bold">Fecha de Nacimiento</label>
                <input type="date" className="form-control" name="fechaNacimiento" value={form.fechaNacimiento} onChange={handleChange} required />
              </div>

              <div className="mb-3">
                <label className="form-label small fw-bold">Curso Asignado</label>
                <select className="form-select" name="cursoId" value={form.cursoId} onChange={handleChange} required>
                  <option value="">-- Seleccione un Curso --</option>
                  {cursos.map(c => (
                    <option key={c.id} value={c.id}>{c.nombre}</option>
                  ))}
                </select>
              </div>

              <div className="mb-3">
                <label className="form-label small fw-bold">Vincular a Cuenta de Usuario (Alumno)</label>
                <select className="form-select" name="usuarioId" value={form.usuarioId} onChange={handleChange} required>
                  <option value="">Seleccione cuenta de usuario (Alumno)...</option>
                  {alumnos.map(u => (
                    <option key={u.id} value={u.id}>ID: {u.id} - {u.correo}</option>
                  ))}
                </select>
                <div className="form-text">ID de la cuenta (usuarioId) asociada al alumno.</div>
              </div>
            </div>

            {/* --- SECCIÓN APODERADO --- */}
            <div className="col-md-6 mb-4">
              <h5 className="text-secondary fw-bold mb-3"><i className="bi bi-person-hearts me-2"></i>Datos del Apoderado</h5>

              <div className="mb-3">
                <label className="form-label small fw-bold">Vincular a Cuenta de Usuario (Apoderado)</label>
                <select 
                  className="form-select" 
                  name="apoderado.usuarioId" 
                  value={form.apoderado.usuarioId} 
                  onChange={(e) => {
                    const uId = e.target.value;
                    const selectedUser = apoderadosUsuarios.find(u => u.id.toString() === uId);
                    setForm(prev => ({
                      ...prev,
                      apoderado: {
                        ...prev.apoderado,
                        usuarioId: uId,
                        correo: selectedUser ? selectedUser.correo : prev.apoderado.correo
                      }
                    }));
                  }} 
                  required
                >
                  <option value="">Seleccione cuenta de usuario (Apoderado)...</option>
                  {apoderadosUsuarios.map(u => (
                    <option key={u.id} value={u.id}>ID: {u.id} - {u.correo}</option>
                  ))}
                </select>
                <div className="form-text">ID de la cuenta (usuarioId) asociada al apoderado.</div>
              </div>

              <div className="mb-3">
                <label className="form-label small fw-bold">Nombre del Apoderado</label>
                <input type="text" className="form-control" name="apoderado.nombre" value={form.apoderado.nombre} onChange={handleChange} required placeholder="Ej: María González" />
              </div>

              <div className="mb-3">
                <label className="form-label small fw-bold">RUT Apoderado</label>
                <input type="text" className="form-control" name="apoderado.rut" value={form.apoderado.rut} onChange={handleChange} required placeholder="Ej: 98765432-1" />
              </div>

              <div className="mb-3">
                <label className="form-label small fw-bold">Teléfono</label>
                <input type="tel" className="form-control" name="apoderado.telefono" value={form.apoderado.telefono} onChange={handleChange} required placeholder="Ej: 987654321" />
              </div>
            </div>
          </div>

          <hr className="mb-4" />
          <button type="submit" className="btn btn-primary w-100 py-3 shadow-sm fw-bold fs-5">
            <i className="bi bi-save me-2"></i>Registrar Matrícula Completa
          </button>
        </form>
      </div>
    </div>
  );
}
