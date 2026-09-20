import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useIsAuthenticated, useMsal } from '@azure/msal-react';
import Login from './Login';
import Signup from './Signup';
import Dashboard from './Dashboard';
import AdminDashboard from './AdminDashboard';
import AdminLayout from './AdminLayout';
import AdminMatriculas from './AdminMatriculas';
import AdminReportes from './AdminReportes';
import AdminAsignaciones from './AdminAsignaciones';
import ApoderadoReportes from './ApoderadoReportes';
import ProfesorLayout from './ProfesorLayout';
import ProfesorAsistencia from './ProfesorAsistencia';
import ProfesorEvaluaciones from './ProfesorEvaluaciones';
import Mensajeria from './Mensajeria';
import Home from './Home';
import Contacto from './Contacto';
import Asistencia from './asistencia';
import Cursos from './cursos';
import Navbar from './nabvar';

// Ruta protegida con autenticación MSAL (Azure AD)
const ProtectedRoute = ({ children, allowedRoles }) => {
  const isAuthenticated = useIsAuthenticated();
  const { accounts } = useMsal();

  if (!isAuthenticated) return <Navigate to="/login" replace />;

  if (allowedRoles) {
    // Roles están en idTokenClaims.roles[] de Azure AD
    const userRoles = accounts[0]?.idTokenClaims?.roles ?? [];
    const hasRole = allowedRoles.some(r =>
      userRoles.map(ur => ur.toLowerCase()).includes(r.toLowerCase())
    );
    if (!hasRole) return <Navigate to="/dashboard" replace />;
  }

  return children;
};

export default function App() {
  return (
    <BrowserRouter>
      <Navbar /> {/* Con N mayúscula */}
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/contacto" element={<Contacto />} />
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<Signup />} />
        
        <Route path="/dashboard" element={
          <ProtectedRoute>
            <Dashboard />
          </ProtectedRoute>
        } />

        <Route path="/apoderado/reportes" element={
          <ProtectedRoute allowedRoles={['Apoderado']}>
            <ApoderadoReportes />
          </ProtectedRoute>
        } />

        <Route path="/apoderado/mensajes" element={
          <ProtectedRoute allowedRoles={['Apoderado']}>
            <Mensajeria />
          </ProtectedRoute>
        } />

        <Route path="/cursos" element={
          <ProtectedRoute>
            <Cursos />
          </ProtectedRoute>
        } />

        <Route path="/asistencia" element={
          <ProtectedRoute>
            <Asistencia />
          </ProtectedRoute>
        } />
        
        <Route path="/admin" element={
          <ProtectedRoute allowedRoles={['Admin']}>
            <AdminLayout />
          </ProtectedRoute>
        }>
          <Route index element={<Navigate to="usuarios" replace />} />
          <Route path="usuarios" element={<AdminDashboard />} />
          <Route path="matriculas" element={<AdminMatriculas />} />
          <Route path="asignaciones" element={<AdminAsignaciones />} />
          <Route path="reportes" element={<AdminReportes />} />
        </Route>

        <Route path="/profesor" element={
          <ProtectedRoute allowedRoles={['Profesor']}>
            <ProfesorLayout />
          </ProtectedRoute>
        }>
          <Route index element={<Navigate to="asistencia" replace />} />
          <Route path="asistencia" element={<ProfesorAsistencia />} />
          <Route path="evaluaciones" element={<ProfesorEvaluaciones />} />
          <Route path="reportes" element={<AdminReportes />} />
          <Route path="mensajes" element={<Mensajeria />} />
        </Route>
        
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
