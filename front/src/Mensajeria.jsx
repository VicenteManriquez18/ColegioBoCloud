import React, { useState, useEffect, useRef } from 'react';
import { getUserId, getUserEmail, getUserRole, getToken } from '../utils/auth';

export default function Mensajeria() {
  const currentUserId = getUserId();
  const currentUserEmail = getUserEmail();
  const currentUserRole = getUserRole();

  const [contactos, setContactos] = useState([]);
  const [contactoSeleccionado, setContactoSeleccionado] = useState(null);
  const [mensajes, setMensajes] = useState([]);
  const [nuevoMensaje, setNuevoMensaje] = useState('');
  const [loadingContactos, setLoadingContactos] = useState(false);
  const [loadingHistorial, setLoadingHistorial] = useState(false);
  const [error, setError] = useState('');
  const chatEndRef = useRef(null);

  // Cargar contactos al montar
  useEffect(() => {
    const cargarContactos = async () => {
      if (!currentUserId) return;
      setLoadingContactos(true);
      setError('');
      try {
        const res = await fetch(`/api/mensajes/contactos/${currentUserId}`, {
          headers: { 'Authorization': 'Bearer ' + getToken() }
        });
        if (res.ok) {
          const data = await res.json();
          setContactos(data);
        } else {
          setError('No se pudo cargar la lista de contactos autorizados.');
        }
      } catch (err) {
        setError('Error de conexión al cargar contactos.');
        console.error(err);
      } finally {
        setLoadingContactos(false);
      }
    };
    cargarContactos();
  }, [currentUserId]);

  // Cargar historial al seleccionar contacto
  const cargarHistorial = async (destId) => {
    if (!currentUserId || !destId) return;
    try {
      const res = await fetch(`/api/mensajes/historial?user1=${currentUserId}&user2=${destId}`, {
        headers: { 'Authorization': 'Bearer ' + getToken() }
      });
      if (res.ok) {
        const data = await res.json();
        setMensajes(data);
      }
    } catch (err) {
      console.error('Error al cargar historial:', err);
    }
  };

  // Cargar historial de forma síncrona/inicial
  useEffect(() => {
    if (contactoSeleccionado) {
      setLoadingHistorial(true);
      cargarHistorial(contactoSeleccionado.id).finally(() => setLoadingHistorial(false));
    } else {
      setMensajes([]);
    }
  }, [contactoSeleccionado]);

  // Polling para simular chat en tiempo real
  useEffect(() => {
    if (!contactoSeleccionado) return;
    const interval = setInterval(() => {
      cargarHistorial(contactoSeleccionado.id);
    }, 3000);
    return () => clearInterval(interval);
  }, [contactoSeleccionado]);

  // Scroll automático al fondo
  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [mensajes]);

  // Enviar mensaje
  const handleEnviar = async (e) => {
    e.preventDefault();
    if (!nuevoMensaje.trim() || !contactoSeleccionado) return;

    const payload = {
      remitenteId: currentUserId,
      destinatarioId: contactoSeleccionado.id,
      contenido: nuevoMensaje.trim()
    };

    try {
      const res = await fetch('/api/mensajes/enviar', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + getToken()
        },
        body: JSON.stringify(payload)
      });

      if (res.ok) {
        setNuevoMensaje('');
        // Recargar historial inmediatamente
        cargarHistorial(contactoSeleccionado.id);
      } else {
        const errData = await res.json();
        alert(errData.error || 'No se pudo enviar el mensaje.');
      }
    } catch (err) {
      console.error('Error de red al enviar mensaje:', err);
      alert('Error de conexión al enviar el mensaje.');
    }
  };

  const formatearFecha = (fechaStr) => {
    try {
      const d = new Date(fechaStr);
      return d.toLocaleTimeString('es-CL', { hour: '2-digit', minute: '2-digit' }) + ' ' + d.toLocaleDateString('es-CL', { day: '2-digit', month: 'short' });
    } catch (e) {
      return '';
    }
  };

  return (
    <div className="container-fluid" style={{ height: 'calc(100vh - 100px)', display: 'flex', flexDirection: 'column' }}>
      
      {/* Header Premium */}
      <div className="d-flex align-items-center justify-content-between mb-4 bg-white p-3 shadow-sm rounded-3 border-start border-primary border-4">
        <div>
          <h3 className="text-primary fw-bold mb-0">Centro de Mensajería</h3>
          <small className="text-muted">
            Comunicación directa y exclusiva entre <strong>Profesores</strong> y <strong>Apoderados</strong>
          </small>
        </div>
        <div className="text-end">
          <span className="badge bg-secondary text-dark fs-7 px-3 py-2 fw-medium rounded-pill">
            {currentUserEmail} ({currentUserRole})
          </span>
        </div>
      </div>

      {error && (
        <div className="alert alert-danger shadow-sm py-2 mb-3" role="alert">
          <i className="bi bi-exclamation-triangle-fill me-2"></i>{error}
        </div>
      )}

      {/* Grid Principal */}
      <div className="row flex-grow-1 g-3 overflow-hidden" style={{ minHeight: 0 }}>
        
        {/* Columna 1: Listado de Contactos (Apoderados o Profesores) */}
        <div className="col-md-4 d-flex flex-column h-100">
          <div className="card shadow-sm border-0 flex-grow-1 d-flex flex-column overflow-hidden rounded-3">
            <div className="bg-dark text-white p-3">
              <h5 className="mb-0 fw-bold fs-6">
                <i className="bi bi-people me-2"></i>
                {currentUserRole?.toLowerCase() === 'profesor' ? 'Apoderados Registrados' : 'Profesores Designados'}
              </h5>
            </div>
            
            <div className="flex-grow-1 p-2 overflow-auto bg-light">
              {loadingContactos && (
                <div className="text-center py-5">
                  <div className="spinner-border text-primary spinner-border-sm" role="status"></div>
                  <p className="text-muted small mt-2 mb-0">Buscando contactos...</p>
                </div>
              )}

              {!loadingContactos && contactos.length === 0 && (
                <div className="text-center py-5 text-muted small">
                  No hay destinatarios disponibles actualmente en el sistema.
                </div>
              )}

              {!loadingContactos && contactos.map(c => (
                <button
                  key={c.id}
                  onClick={() => setContactoSeleccionado(c)}
                  className={`w-100 text-start p-3 border-0 rounded-3 mb-2 d-flex align-items-center justify-content-between transition-all ${
                    contactoSeleccionado?.id === c.id 
                      ? 'bg-primary text-white shadow' 
                      : 'bg-white text-dark hover-bg-light shadow-sm'
                  }`}
                  style={{ cursor: 'pointer' }}
                >
                  <div>
                    <div className="fw-bold text-truncate" style={{ maxWidth: '180px' }}>
                      {c.correo.split('@')[0]}
                    </div>
                    <small className={`d-block ${contactoSeleccionado?.id === c.id ? 'text-white-50' : 'text-muted'}`}>
                      {c.correo}
                    </small>
                  </div>
                  <span className={`badge rounded-pill ${
                    contactoSeleccionado?.id === c.id 
                      ? 'bg-white text-primary' 
                      : 'bg-light text-secondary'
                  }`}>
                    {c.rol}
                  </span>
                </button>
              ))}
            </div>
          </div>
        </div>

        {/* Columna 2: Chat en Tiempo Real */}
        <div className="col-md-8 d-flex flex-column h-100">
          <div className="card shadow-sm border-0 flex-grow-1 d-flex flex-column overflow-hidden rounded-3">
            {contactoSeleccionado ? (
              <>
                {/* Header del chat */}
                <div className="bg-light p-3 border-bottom d-flex align-items-center justify-content-between">
                  <div className="d-flex align-items-center gap-3">
                    <div className="bg-primary text-white rounded-circle d-flex align-items-center justify-content-center" style={{ width: '40px', height: '40px', fontWeight: 'bold' }}>
                      {contactoSeleccionado.correo.charAt(0).toUpperCase()}
                    </div>
                    <div>
                      <h6 className="mb-0 fw-bold">{contactoSeleccionado.correo}</h6>
                      <small className="text-muted">Rol: {contactoSeleccionado.rol}</small>
                    </div>
                  </div>
                </div>

                {/* Área de mensajes */}
                <div className="flex-grow-1 p-4 overflow-auto bg-white" style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                  {loadingHistorial && (
                    <div className="text-center my-auto">
                      <div className="spinner-border text-primary spinner-border-sm" role="status"></div>
                      <p className="text-muted small mt-2">Cargando mensajes del historial...</p>
                    </div>
                  )}

                  {!loadingHistorial && mensajes.length === 0 && (
                    <div className="text-center my-auto text-muted py-5">
                      <i className="bi bi-chat-dots fs-1 mb-2 d-block text-primary"></i>
                      No hay mensajes anteriores en esta conversación. ¡Envía un mensaje para comenzar!
                    </div>
                  )}

                  {!loadingHistorial && mensajes.map(m => {
                    const esMio = m.remitenteId === currentUserId;
                    return (
                      <div 
                        key={m.id} 
                        className={`d-flex ${esMio ? 'justify-content-end' : 'justify-content-start'}`}
                      >
                        <div 
                          className={`p-3 rounded-4 shadow-sm ${
                            esMio 
                              ? 'bg-primary text-white rounded-tr-0' 
                              : 'bg-light text-dark rounded-tl-0'
                          }`}
                          style={{ maxWidth: '75%', borderRadius: '15px' }}
                        >
                          <div className="small fw-semibold mb-1" style={{ fontSize: '0.75rem', opacity: 0.85 }}>
                            {esMio ? 'Tú' : m.remitenteNombre.split('@')[0]}
                          </div>
                          <p className="mb-1 text-wrap text-break">{m.contenido}</p>
                          <div className="text-end" style={{ fontSize: '0.65rem', opacity: 0.7 }}>
                            {formatearFecha(m.fechaEnvio)}
                          </div>
                        </div>
                      </div>
                    );
                  })}
                  <div ref={chatEndRef} />
                </div>

                {/* Formulario de envío */}
                <div className="p-3 border-top bg-light">
                  <form onSubmit={handleEnviar} className="d-flex gap-2">
                    <input
                      type="text"
                      className="form-control py-2 shadow-sm border-0 rounded-pill px-4"
                      placeholder="Escribe un mensaje aquí..."
                      value={nuevoMensaje}
                      onChange={e => setNuevoMensaje(e.target.value)}
                      maxLength={1000}
                      required
                    />
                    <button 
                      type="submit" 
                      className="btn btn-primary px-4 shadow-sm rounded-pill d-flex align-items-center justify-content-center"
                    >
                      <i className="bi bi-send-fill fs-5"></i>
                    </button>
                  </form>
                </div>
              </>
            ) : (
              <div className="my-auto text-center p-5 text-muted">
                <i className="bi bi-chat-square-text text-secondary mb-3" style={{ fontSize: '5rem' }}></i>
                <h5 className="fw-bold text-dark">No hay ningún chat activo</h5>
                <p className="small">Selecciona uno de los contactos autorizados en la lista lateral para iniciar una conversación.</p>
              </div>
            )}
          </div>
        </div>

      </div>
    </div>
  );
}
