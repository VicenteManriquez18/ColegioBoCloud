import { useState } from 'react';
import { useNavigate, Link, Router } from 'react-router-dom';
import { setToken, getUserRole } from '../utils/auth';
import Navbar from './nabvar';

export default function Login() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ correo: '', password: '' });
  const [error, setError] = useState('');

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    
    try {
      const res = await fetch('/api/auth/login', { 
        method: 'POST', 
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form) 
      });
      
      if (res.ok) {
        const data = await res.json();
        setToken(data.token);
        
        const role = getUserRole();
        if (role?.toLowerCase() === 'admin') navigate('/admin');
        else navigate('/dashboard');
      } else {
        setError('Credenciales inválidas.');
      }
    } catch (err) {
      setError('Error de conexión con el servidor.');
    }
  };

  return (
    <div className="container d-flex justify-content-center align-items-center vh-100">
      <div className="card shadow-lg p-4" style={{ width: '100%', maxWidth: '400px', borderRadius: '15px' }}>
        <div className="card-body">
          <h2 className="text-center mb-4 fw-bold text-primary">Iniciar Sesión</h2>
          
          {error && (
            <div className="alert alert-danger py-2 text-center" role="alert">
              {error}
            </div>
          )}

          <form onSubmit={handleLogin}>
            <div className="mb-3">
              <label className="form-label">Correo electrónico</label>
              <input 
                type="email" 
                className="form-control" 
                placeholder="nombre@ejemplo.com"
                value={form.correo} 
                onChange={e => setForm({...form, correo: e.target.value})} 
                required 
              />
            </div>

            <div className="mb-3">
              <label className="form-label">Contraseña</label>
              <input 
                type="password" 
                className="form-control" 
                placeholder="********"
                value={form.password} 
                onChange={e => setForm({...form, password: e.target.value})} 
                required 
              />
            </div>

            <div className="d-grid gap-2 mt-4">
              <button type="submit" className="btn btn-primary btn-lg shadow-sm">
                Ingresar
              </button>
            </div>
          </form>

          <div className="text-center mt-4">
            <p className="text-muted">
              ¿No tienes cuenta? <Link to="/signup" className="text-decoration-none fw-bold">Regístrate aquí</Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
