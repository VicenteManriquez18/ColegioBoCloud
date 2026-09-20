import { jwtDecode } from 'jwt-decode';

export const setToken = (token) => localStorage.setItem('jwt', token);
export const getToken = () => localStorage.getItem('jwt');
export const removeToken = () => localStorage.removeItem('jwt');

export const getUserRole = () => {
  const token = getToken();
  if (!token) return null;

  try {
    const decodedToken = jwtDecode(token);
    return decodedToken.rol || decodedToken.role || null;
  } catch (error) {
    return null;
  }
};

export const getUserId = () => {
  const token = getToken();
  if (!token) return null;

  try {
    const decodedToken = jwtDecode(token);
    return decodedToken.id || null;
  } catch (error) {
    return null;
  }
};

export const getUserEmail = () => {
  const token = getToken();
  if (!token) return null;

  try {
    const decodedToken = jwtDecode(token);
    return decodedToken.sub || null;
  } catch (error) {
    return null;
  }
};
