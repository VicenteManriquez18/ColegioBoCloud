import React from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { removeToken } from '../utils/auth';

export default function AdminLayout() {
  const navigate = useNavigate();

  const handleLogout = () => {
    removeToken();
    navigate('/login');
  };

  return (
    <div className="d-flex autumn-bg" style={{ minHeight: '100vh' }}>
      {/* Sidebar */}
      <div className="bg-dark text-white p-3 d-flex flex-column shadow" style={{ width: '260px' }}>
        <h4 className="fw-bold mb-4 text-center border-bottom border-secondary pb-3">Admin Panel</h4>
        <ul className="nav nav-pills flex-column mb-auto gap-2">
          <li className="nav-item">
            <NavLink 
              to="/admin/usuarios" 
              className={({isActive}) => `nav-link text-white ${isActive ? 'bg-primary fw-bold' : ''}`}
            >
              Gestión de Usuarios
            </NavLink>
          </li>
          <li className="nav-item">
            <NavLink 
              to="/admin/matriculas" 
              className={({isActive}) => `nav-link text-white ${isActive ? 'bg-primary fw-bold' : ''}`}
            >
              Gestión de Matrículas
            </NavLink>
          </li>
          <li className="nav-item">
            <NavLink 
              to="/admin/asignaciones" 
              className={({isActive}) => `nav-link text-white ${isActive ? 'bg-primary fw-bold' : ''}`}
            >
              Asignación de Profesores
            </NavLink>
          </li>
          <li className="nav-item">
            <NavLink 
              to="/admin/reportes" 
              className={({isActive}) => `nav-link text-white ${isActive ? 'bg-primary fw-bold' : ''}`}
            >
              Reportes del Colegio
            </NavLink>
          </li>
        </ul>
        <hr className="border-secondary" />
        <button onClick={handleLogout} className="btn btn-outline-danger mt-auto fw-bold">
          Cerrar Sesión
        </button>
      </div>

      {/* Main Content Area */}
      <div className="flex-grow-1 p-4 overflow-auto">
        <div className="container-fluid max-w-7xl">
          <Outlet />
        </div>
      </div>
    </div>
  );
}
