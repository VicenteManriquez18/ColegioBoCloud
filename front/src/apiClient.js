// src/apiClient.js
// Función reutilizable para llamadas autenticadas al API Gateway con token MSAL

import { protectedResources } from './authConfig';

/**
 * Realiza un fetch autenticado al API Gateway adjuntando el Bearer token de Azure AD.
 *
 * @param {object} instance   - Instancia de MSAL (useMsal().instance)
 * @param {Array}  accounts   - Cuentas activas (useMsal().accounts)
 * @param {string} path       - Ruta relativa del endpoint, ej: "/prod/usuarios/123"
 * @param {object} options    - Opciones adicionales de fetch (method, body, headers, etc.)
 * @returns {Promise<Response>}
 */
export async function apiFetch(instance, accounts, path, options = {}) {
  if (!accounts || accounts.length === 0) {
    throw new Error('No hay cuenta de usuario activa. Por favor inicia sesión.');
  }

  // Adquirir token en silencio (usa caché o renueva automáticamente)
  const tokenResponse = await instance.acquireTokenSilent({
    scopes: protectedResources.api.scopes,
    account: accounts[0],
  });

  const url = `${protectedResources.api.endpoint}${path}`;

  return fetch(url, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
      Authorization: `Bearer ${tokenResponse.accessToken}`,
    },
  });
}
