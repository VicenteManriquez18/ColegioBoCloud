import React from 'react';
import { Link } from 'react-router-dom';
import { useMsal, useIsAuthenticated } from '@azure/msal-react';
import AuthButtons from './AuthButtons';
import '../styles/navbar.css';

const Navbar = () => {
  const { accounts } = useMsal();
  const isAuthenticated = useIsAuthenticated();

  // Extraer el rol desde las claims del token de Azure AD
  // Azure AD devuelve los roles en account.idTokenClaims.roles[]
  const roles = accounts[0]?.idTokenClaims?.roles ?? [];
  const role = roles[0] ?? null; // Tomar el primer rol si existe

  return (
    <nav className="navbar navbar-expand-sm custom-navbar shadow-sm">
      <div className="container">
        <Link className="navbar-brand fw-bold d-flex align-items-center gap-2" to="/dashboard">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="35"
            height="35"
            viewBox="0 0 24 24"
            fill="currentColor"
            aria-label="Logo Colegio BO"
            style={{ color: '#2563eb' }}
          >
            <path d="M12 3L1 9l11 6 9-4.91V17h2V9L12 3zM5 13.18v4L12 21l7-3.82v-4L12 17l-7-3.82z"/>
          </svg>
          <span>Colegio BO</span>
        </Link>
        
        <ul className="navbar-nav ms-auto align-items-center">
          <li className="nav-item">
            <Link className="nav-link" to="/contacto">Contacto</Link>
          </li>

          {/* 1. MOSTRAR SOLO SI ES ADMIN */}
          {isAuthenticated && role?.toLowerCase() === 'admin' && (
            <li className="nav-item">
              <Link className="nav-link fw-bold text-primary" to="/admin">Panel Admin</Link>
            </li>
          )}

          {/* 2. MOSTRAR SOLO SI ES PROFESOR */}
          {isAuthenticated && role?.toLowerCase() === 'profesor' && (
            <li className="nav-item">
              <Link className="nav-link fw-bold text-success" to="/profesor">Panel Profesor</Link>
            </li>
          )}

          {/* 3. MOSTRAR SOLO SI ES ALUMNO/OTROS */}
          {isAuthenticated && role && !['admin', 'profesor'].includes(role?.toLowerCase()) && (
            <li className="nav-item">
              <Link className="nav-link" to="/dashboard">Mi Panel</Link>
            </li>
          )}

          {/* CONDICIONAL: LOGIN O CERRAR SESIÓN (via MSAL Azure AD) */}
          <li className="nav-item ms-2">
            <AuthButtons />
          </li>
        </ul>
      </div>
    </nav>
  );
};

export default Navbar;
