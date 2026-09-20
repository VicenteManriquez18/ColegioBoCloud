import { useEffect, useState } from 'react';
import { getUserRole, getToken } from '../utils/auth';

export default function AdminDashboard() {
  const role = getUserRole();
  const isAdmin = role?.toLowerCase() === 'admin';
  const [usuarios, setUsuarios] = useState([]);
  const [cursos, setCursos] = useState([]);
  const [form, setForm] = useState({
    id: null,
    correo: '',
    password: '',
    rol: 'Alumno',
    telefono: '',
    cursoId: ''
  });
  const [mensaje, setMensaje] = useState({ texto: '', tipo: '' });
  const [errorUsuarios, setErrorUsuarios] = useState('');

  const cargarCursos = async () => {
    try {
      const res = await fetch('/api/academico/cursos', {
        headers: { 'Authorization': 'Bearer ' + getToken() }
      });
      if (res.ok) {
        const data = await res.json();
        if (Array.isArray(data)) {
          setCursos(data);
        }
      }
    } catch (err) {
      console.error("Error al cargar cursos:", err);
    }
  };

  const cargarUsuarios = async () => {
    if (!isAdmin) return;

    setErrorUsuarios('');
    try {
      const res = await fetch('/api/usuarios', {
        headers: { 'Authorization': 'Bearer ' + getToken() }
      });

      if (!res.ok) {
        throw new Error(`Error ${res.status} al cargar usuarios`);
      }

      const data = await res.json();
      if (Array.isArray(data)) {
        setUsuarios(data);
      } else {
        setUsuarios([]);
        setErrorUsuarios('La respuesta del servidor no tiene el formato esperado.');
      }
    } catch (err) {
      setUsuarios([]);
      setErrorUsuarios('No se pudieron cargar los usuarios registrados.');
      console.error("Error al cargar usuarios:", err);
    }
  };

  useEffect(() => {
    cargarUsuarios();
    cargarCursos();
  }, [isAdmin]);

  const handleEditClick = (u) => {
    setMensaje({ texto: '', tipo: '' });
    setForm({
      id: u.id,
      correo: u.correo,
      password: '', // Dejar vacío por seguridad, se edita si se ingresa una nueva
      rol: u.rol || 'Alumno',
      telefono: u.telefono || '',
      cursoId: u.cursoId || ''
    });
  };

  const handleDeleteUser = async (id) => {
    if (!window.confirm("¿Está seguro de que desea eliminar este usuario y todos sus registros asociados?")) {
      return;
    }
    setMensaje({ texto: '', tipo: '' });
    try {
      const res = await fetch(`/api/usuarios/${id}`, {
        method: 'DELETE',
        headers: { 'Authorization': 'Bearer ' + getToken() }
      });
      if (res.ok || res.status === 204) {
        setMensaje({ texto: 'Usuario eliminado exitosamente.', tipo: 'success' });
        if (form.id === id) {
          handleCancelEdit();
        }
        cargarUsuarios();
      } else {
        setMensaje({ texto: 'Error al eliminar usuario.', tipo: 'danger' });
      }
    } catch (err) {
      setMensaje({ texto: 'Error de conexión.', tipo: 'danger' });
    }
  };

  const handleCancelEdit = () => {
    setForm({
      id: null,
      correo: '',
      password: '',
      rol: 'Alumno',
      telefono: '',
      cursoId: ''
    });
    setMensaje({ texto: '', tipo: '' });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMensaje({ texto: '', tipo: '' });

    // Validación de dominio de correo
    const emailValue = form.correo.toLowerCase();
    const allowedDomains = ['@gmail.com', '@hotmail.com', '@bernardo.cl'];
    const isDomainValid = allowedDomains.some(domain => emailValue.endsWith(domain));

    if (!isDomainValid) {
      setMensaje({ texto: 'El correo debe ser @gmail.com, @hotmail.com o @bernardo.cl', tipo: 'danger' });
      return;
    }

    // Validación de seguridad de contraseña (solo si se está creando o si se ingresó algo en modo edición)
    if (!form.id || form.password) {
      const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^a-zA-Z0-9]).{8,}$/;
      if (!passwordRegex.test(form.password)) {
        setMensaje({ 
          texto: 'La contraseña debe tener al menos 8 caracteres, incluir una mayúscula, una minúscula, un número y un carácter especial.', 
          tipo: 'danger' 
        });
        return;
      }
    }

    try {
      const payload = {
        correo: form.correo,
        rol: form.rol,
        telefono: form.telefono,
        cursoId: form.cursoId ? parseInt(form.cursoId, 10) : null
      };

      if (!form.id) {
        // Modo Creación
        payload.password = form.password;
        const res = await fetch('/api/auth/register', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });
        if (res.ok) {
          setMensaje({ texto: 'Usuario creado exitosamente.', tipo: 'success' });
          handleCancelEdit();
          cargarUsuarios();
        } else {
          const errData = await res.json();
          setMensaje({ texto: errData.error || 'Error al crear usuario.', tipo: 'danger' });
        }
      } else {
        // Modo Edición
        if (form.password) {
          payload.password = form.password;
        }
        const res = await fetch(`/api/usuarios/${form.id}`, {
          method: 'PUT',
          headers: { 
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + getToken()
          },
          body: JSON.stringify(payload)
        });
        if (res.ok) {
          setMensaje({ texto: 'Usuario modificado exitosamente.', tipo: 'success' });
          handleCancelEdit();
          cargarUsuarios();
        } else {
          const errData = await res.json();
          setMensaje({ texto: errData.error || 'Error al modificar usuario.', tipo: 'danger' });
        }
      }
    } catch (err) {
      setMensaje({ texto: 'Error de conexión.', tipo: 'danger' });
    }
  };

  return (
    <div className="container-fluid">
      
      {/* Encabezado Superior */}
      <div className="d-flex justify-content-between align-items-center mb-4 bg-white p-3 shadow-sm rounded">
        <div>
          <h2 className="text-primary fw-bold mb-0">Gestión de Usuarios</h2>
          <small className="text-muted">Creación, edición y eliminación de usuarios y asignación de roles</small>
        </div>
      </div>

      <div className="row g-4">
        {/* Columna Izquierda: Formulario */}
        <div className="col-lg-4">
          <div className="card shadow border-0 p-4">
            <h4 className="fw-bold mb-4">
              {form.id ? 'Modificar Usuario' : 'Registrar Nuevo Usuario'}
            </h4>
            
            {mensaje.texto && (
              <div className={`alert alert-${mensaje.tipo} py-2`} role="alert">
                {mensaje.texto}
              </div>
            )}
            
            <form onSubmit={handleSubmit}>

              <div className="mb-3">
                <label className="form-label small fw-bold">Correo Electrónico</label>
                <input type="email" className="form-control" placeholder="usuario@bernardo.cl" value={form.correo} onChange={e => setForm({...form, correo: e.target.value})} required />
              </div>
              
              <div className="mb-3">
                <label className="form-label small fw-bold">
                  Contraseña {form.id && <span className="text-muted">(dejar vacío para mantener actual)</span>}
                </label>
                <input 
                  type="password" 
                  className="form-control" 
                  placeholder={form.id ? "Nueva contraseña (opcional)" : "Mín. 8 caracteres (A-z, 0-9, especial)"} 
                  value={form.password} 
                  onChange={e => setForm({...form, password: e.target.value})} 
                  minLength={8}
                  required={!form.id} 
                />
              </div>

              {form.id !== null && (
                <div className="mb-3">
                  <label className="form-label small fw-bold">Número Telefónico</label>
                  <input type="tel" className="form-control" placeholder="Ej: +56912345678" value={form.telefono} onChange={e => setForm({...form, telefono: e.target.value})} />
                </div>
              )}

              <div className="mb-3">
                <label className="form-label small fw-bold">Asignar Rol</label>
                <select className="form-select" value={form.rol} onChange={e => setForm({...form, rol: e.target.value})}>
                  <option value="Alumno">Alumno</option>
                  <option value="Apoderado">Apoderado</option>
                  <option value="Profesor">Profesor</option>
                  <option value="Admin">Admin</option>
                </select>
              </div>

              {/* Mostrar selector de curso para Alumno o Profesor solo al editar */}
              {form.id !== null && (form.rol?.toLowerCase() === 'alumno' || form.rol?.toLowerCase() === 'profesor') && (
                <div className="mb-4">
                  <label className="form-label small fw-bold">Curso Asignado</label>
                  <select className="form-select" value={form.cursoId} onChange={e => setForm({...form, cursoId: e.target.value})}>
                    <option value="">-- Sin Asignar --</option>
                    {cursos.map(c => (
                      <option key={c.id} value={c.id}>{c.nombre}</option>
                    ))}
                  </select>
                </div>
              )}

              <div className="d-flex gap-2">
                <button type="submit" className="btn btn-primary flex-grow-1 py-2 shadow-sm fw-bold">
                  {form.id ? 'Guardar Cambios' : 'Crear Usuario'}
                </button>
                {form.id && (
                  <button type="button" className="btn btn-secondary py-2 shadow-sm fw-bold" onClick={handleCancelEdit}>
                    Cancelar
                  </button>
                )}
              </div>
            </form>
          </div>
        </div>

        {/* Columna Derecha: Tabla */}
        <div className="col-lg-8">
          <div className="card shadow border-0 overflow-hidden">
            <div className="bg-dark p-3">
              <h4 className="text-white mb-0 fs-5">Usuarios Registrados</h4>
            </div>
            {errorUsuarios && (
              <div className="alert alert-danger m-3 mb-0" role="alert">
                {errorUsuarios}
              </div>
            )}
            <div className="table-responsive" style={{ maxHeight: '600px' }}>
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light">
                  <tr>
                    <th className="ps-3">ID</th>
                    <th>Correo Electrónico</th>
                    <th>Teléfono</th>
                    <th>Curso</th>
                    <th className="text-center">Rol</th>
                    <th className="text-center" style={{ width: '150px' }}>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {usuarios.map(u => {
                    const userCourse = cursos.find(c => c.id.toString() === u.cursoId?.toString())?.nombre || 'Sin Asignar';
                    return (
                      <tr key={u.id}>
                        <td className="ps-3 text-muted">#{u.id}</td>
                        <td>{u.correo}</td>
                        <td>{u.telefono || '—'}</td>
                        <td>
                          {u.cursoId ? (
                            <span className="badge bg-light text-dark border">
                              {userCourse}
                            </span>
                          ) : '—'}
                        </td>
                        <td className="text-center">
                          <span className={`badge rounded-pill ${
                            u.rol === 'Admin' ? 'bg-danger' : 
                            u.rol === 'Profesor' ? 'bg-success' : 
                            u.rol === 'Apoderado' ? 'bg-warning text-dark' : 'bg-secondary'
                          }`}>
                            {u.rol}
                          </span>
                        </td>
                        <td className="text-center">
                          <div className="d-flex justify-content-center gap-2">
                            <button className="btn btn-sm btn-outline-primary" onClick={() => handleEditClick(u)}>
                              Editar
                            </button>
                            <button className="btn btn-sm btn-outline-danger" onClick={() => handleDeleteUser(u.id)}>
                              Eliminar
                            </button>
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
            {usuarios.length === 0 && !errorUsuarios && (
              <div className="p-5 text-center text-muted">
                No hay usuarios registrados actualmente.
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
