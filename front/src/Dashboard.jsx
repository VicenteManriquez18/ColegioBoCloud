import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useMsal, useIsAuthenticated } from '@azure/msal-react';
import { apiFetch } from './apiClient';

export default function Dashboard() {
  const navigate = useNavigate();
  const { instance, accounts } = useMsal();
  const isAuthenticated = useIsAuthenticated();

  // Extraer rol desde los App Roles de Azure AD
  const roles = accounts[0]?.idTokenClaims?.roles ?? [];
  const role = roles[0] ?? null;
  const isAlumno = role?.toLowerCase() === 'alumno';

  // Nombre de usuario desde Azure AD
  const userName = accounts[0]?.name ?? accounts[0]?.username ?? 'Usuario';

  // Estado para demostrar llamada real al API Gateway (7.8)
  const [apiStatus, setApiStatus] = useState(null);

  useEffect(() => {
    if (!isAuthenticated || accounts.length === 0) return;

    // Ejemplo: GET /api/usuarios/1 — llama al API Gateway (/api/usuarios/{proxy+}) con Bearer token
    apiFetch(instance, accounts, '/api/usuarios/1')
      .then(res => {
        if (res.ok) setApiStatus('✅ API Gateway respondió correctamente (200 OK)');
        else setApiStatus(`⚠️ API respondió con status ${res.status}`);
      })
      .catch(() => setApiStatus('❌ Error al conectar con el API Gateway'));
  }, [isAuthenticated, instance, accounts]);

  const handleLogout = () => {
    instance.logoutRedirect({ postLogoutRedirectUri: window.location.origin });
  };

  return (
    <div className="container-fluid autumn-bg d-flex justify-content-center align-items-center" style={{ minHeight: '100vh' }}>
      <div className="card shadow-lg border-0" style={{ width: '100%', maxWidth: '600px', borderRadius: '20px' }}>
        
        {/* Cabecera de la Card */}
        <div className="card-header bg-primary text-white text-center py-4" style={{ borderRadius: '20px 20px 0 0' }}>
          <h2 className="mb-0 fw-bold">Bienvenido</h2>
        </div>

        <div className="card-body p-5 text-center">
          {/* Sección de Bienvenida */}
          <div className="mb-4">
            <p className="text-muted fs-5 mb-1">Bienvenido de nuevo</p>
            <h4 className="fw-semibold text-dark">{userName}</h4>
            <h5>
              Rol asignado: <span className="badge bg-info text-dark">{role || 'Usuario'}</span>
            </h5>
            {/* Indicador de conexión al API Gateway (7.8) */}
            {apiStatus && (
              <div className="alert alert-light border mt-2 py-1 px-3 small" role="status">
                {apiStatus}
              </div>
            )}
          </div>

          <hr className="my-4 text-secondary opacity-25" />

          {/* Botón de Navegación para Alumnos (Solo ver sus Cursos, sin Asistencia) */}
          {isAlumno && (
            <div className="row g-3 mb-4">
              <div className="col-12">
                <button 
                  onClick={() => navigate('/cursos')} 
                  className="btn btn-outline-primary w-100 py-3 shadow-sm d-flex align-items-center justify-content-center gap-2 fw-bold text-primary fs-5"
                  style={{ borderRadius: '10px' }}
                >
                  <i className="bi bi-journal-bookmark fs-4"></i>
                  <span>Mis Cursos</span>
                </button>
              </div>
            </div>
          )}

          {/* Botones de Navegación para Profesores */}
          {role?.toLowerCase() === 'profesor' && (
            <div className="row g-3 mb-4">
              <div className="col-6">
                <button 
                  onClick={() => navigate('/profesor/evaluaciones')} 
                  className="btn btn-outline-primary w-100 py-3 shadow-sm d-flex flex-column align-items-center"
                  style={{ borderRadius: '10px' }}
                >
                  <i className="bi bi-journal-bookmark fs-3 mb-2"></i>
                  <span>Mis Cursos</span>
                </button>
              </div>
              <div className="col-6">
                <button 
                  onClick={() => navigate('/profesor/asistencia')} 
                  className="btn btn-outline-primary w-100 py-3 shadow-sm d-flex flex-column align-items-center"
                  style={{ borderRadius: '10px' }}
                >
                  <i className="bi bi-calendar-check fs-3 mb-2"></i>
                  <span>Asistencia</span>
                </button>
              </div>
            </div>
          )}

          {/* Botón para Administradores */}
          {role?.toLowerCase() === 'admin' && (
            <div className="col-12 mb-4">
              <button 
                onClick={() => navigate('/admin')} 
                className="btn btn-primary w-100 py-3 shadow-sm d-flex align-items-center justify-content-center gap-2 fw-bold text-white fs-5"
                style={{ borderRadius: '10px' }}
              >
                <i className="bi bi-shield-lock fs-4"></i>
                <span>Panel de Administrador</span>
              </button>
            </div>
          )}

          {/* Botón para Profesores (Mensajería) */}
          {role?.toLowerCase() === 'profesor' && (
            <div className="col-12 mb-4">
              <button 
                onClick={() => navigate('/profesor/mensajes')} 
                className="btn btn-primary w-100 py-3 shadow-sm d-flex align-items-center justify-content-center gap-2 fw-bold text-white fs-5"
                style={{ borderRadius: '10px' }}
              >
                <i className="bi bi-chat-dots fs-4"></i>
                <span>Mensajería con Apoderados</span>
              </button>
            </div>
          )}

          {/* Botón para Apoderados */}
          {role?.toLowerCase() === 'apoderado' && (
            <>
              <div className="col-12 mb-3">
                <button 
                  onClick={() => navigate('/apoderado/reportes')} 
                  className="btn btn-success w-100 py-3 shadow-sm d-flex align-items-center justify-content-center gap-2 fw-bold text-white fs-5"
                  style={{ borderRadius: '10px' }}
                >
                  <i className="bi bi-file-earmark-bar-graph fs-4"></i>
                  <span>Reportes de mi Alumno Vinculado</span>
                </button>
              </div>
              <div className="col-12 mb-4">
                <button 
                  onClick={() => navigate('/apoderado/mensajes')} 
                  className="btn btn-primary w-100 py-3 shadow-sm d-flex align-items-center justify-content-center gap-2 fw-bold text-white fs-5"
                  style={{ borderRadius: '10px' }}
                >
                  <i className="bi bi-chat-dots fs-4"></i>
                  <span>Mensajería con Profesores</span>
                </button>
              </div>
            </>
          )}

          {/* Botón de Cierre de Sesión */}
          <div className="d-grid">
            <button 
              onClick={handleLogout} 
              className="btn btn-danger btn-lg shadow-sm fw-bold"
              style={{ borderRadius: '10px' }}
            >
              Cerrar Sesión
            </button>
          </div>
        </div>

        <div className="card-footer text-center bg-light py-3" style={{ borderRadius: '0 0 20px 20px' }}>
          <small className="text-muted italic">Plataforma de Gestión Académica</small>
        </div>
      </div>
    </div>
  );
}