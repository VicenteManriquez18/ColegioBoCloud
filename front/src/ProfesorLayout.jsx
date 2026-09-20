import React from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { removeToken } from '../utils/auth';

export default function ProfesorLayout() {
  const navigate = useNavigate();

  const handleLogout = () => {
    removeToken();
    navigate('/login');
  };

  return (
    <div className="d-flex autumn-bg" style={{ minHeight: '100vh' }}>
      {/* Sidebar */}
      <div className="bg-dark text-white p-3 d-flex flex-column shadow" style={{ width: '260px' }}>
        <h4 className="fw-bold mb-4 text-center border-bottom border-secondary pb-3">Panel Profesor</h4>
        <ul className="nav nav-pills flex-column mb-auto gap-2">
          <li className="nav-item">
            <NavLink 
              to="/profesor/asistencia" 
              className={({isActive}) => `nav-link text-white ${isActive ? 'bg-dark bg-opacity-25 fw-bold' : ''}`}
            >
              Registro de Asistencia
            </NavLink>
          </li>
          <li className="nav-item">
            <NavLink 
              to="/profesor/evaluaciones" 
              className={({isActive}) => `nav-link text-white ${isActive ? 'bg-dark bg-opacity-25 fw-bold' : ''}`}
            >
              Evaluaciones y Notas
            </NavLink>
          </li>
          <li className="nav-item">
            <NavLink 
              to="/profesor/reportes" 
              className={({isActive}) => `nav-link text-white ${isActive ? 'bg-dark bg-opacity-25 fw-bold' : ''}`}
            >
              Reportes de Alumnos
            </NavLink>
          </li>
          <li className="nav-item">
            <NavLink 
              to="/profesor/mensajes" 
              className={({isActive}) => `nav-link text-white ${isActive ? 'bg-dark bg-opacity-25 fw-bold' : ''}`}
            >
              Mensajería con Apoderados
            </NavLink>
          </li>
        </ul>
        <hr className="border-light" />
        <button onClick={handleLogout} className="btn btn-outline-light mt-auto fw-bold">
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
