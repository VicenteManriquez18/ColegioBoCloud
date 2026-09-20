// src/AuthButtons.jsx
// Botones reutilizables de Login / Logout con Azure AD via MSAL

import { useMsal } from '@azure/msal-react';
import { loginRequest } from './authConfig';

export default function AuthButtons() {
  const { instance, accounts } = useMsal();
  const isAuthenticated = accounts.length > 0;

  const handleLogin = () => {
    instance.loginRedirect(loginRequest).catch(console.error);
  };

  const handleLogout = () => {
    instance.logoutRedirect({
      postLogoutRedirectUri: window.location.origin,
    }).catch(console.error);
  };

  if (isAuthenticated) {
    return (
      <button
        id="btn-azure-logout"
        onClick={handleLogout}
        className="btn btn-outline-danger btn-sm fw-bold"
        style={{ borderRadius: '8px' }}
      >
        Cerrar Sesión
      </button>
    );
  }

  return (
    <button
      id="btn-azure-login"
      onClick={handleLogin}
      className="nav-link btn-login px-3 btn btn-link"
      style={{ textDecoration: 'none' }}
    >
      Iniciar sesión
    </button>
  );
}
