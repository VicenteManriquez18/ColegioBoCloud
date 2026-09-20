import React from 'react';

const Home = () => {
  return (
    <div className="autumn-bg pb-5">
      {/* Hero Section */}
      <div className="container-fluid hero-texture text-white py-5 text-center shadow-sm" style={{ borderBottom: '5px solid var(--bs-warning)' }}>
        <div className="container py-5">
          <h1 className="display-4 fw-bold mb-3">Bienvenido al Colegio BO</h1>
          <p className="lead fw-light mb-4">Educando con calidez, excelencia y un entorno acogedor para el futuro de tus hijos.</p>
          <a href="#encuentranos" className="btn btn-light text-primary btn-lg mt-3 fw-bold rounded-pill px-5 shadow-sm">
            Conócenos
          </a>
        </div>
      </div>

      <div className="container mt-5 pt-3">
        {/* Features Section */}
        <div className="row g-4 mb-5">
          <div className="col-md-4">
            <div className="card h-100 shadow-sm border-0 bg-white text-center rounded-4">
              <div className="card-body p-5">
                <i className="bi bi-book text-warning" style={{ fontSize: '3rem' }}></i>
                <h3 className="card-title text-dark fw-bold mb-3 mt-3">Misión</h3>
                <p className="card-text text-muted">Formar integralmente a nuestros alumnos con valores sólidos y excelencia académica.</p>
              </div>
            </div>
          </div>
          <div className="col-md-4">
            <div className="card h-100 shadow-sm border-0 bg-white text-center rounded-4">
              <div className="card-body p-5">
                <i className="bi bi-eye text-success" style={{ fontSize: '3rem' }}></i>
                <h3 className="card-title text-dark fw-bold mb-3 mt-3">Visión</h3>
                <p className="card-text text-muted">Ser reconocidos como una comunidad educativa líder, cálida y comprometida con la sociedad.</p>
              </div>
            </div>
          </div>
          <div className="col-md-4">
            <div className="card h-100 shadow-sm border-0 bg-white text-center rounded-4">
              <div className="card-body p-5">
                <i className="bi bi-heart text-danger" style={{ fontSize: '3rem' }}></i>
                <h3 className="card-title text-dark fw-bold mb-3 mt-3">Valores</h3>
                <p className="card-text text-muted">Respeto, responsabilidad, empatía y dedicación constante al aprendizaje colectivo.</p>
              </div>
            </div>
          </div>
        </div>

        {/* Encuéntranos Section */}
        <div id="encuentranos" className="mt-5 pt-5 mb-4">
          <h2 className="text-center text-dark fw-bold mb-5 display-5">Encuéntranos</h2>
          <div className="row justify-content-center">
            <div className="col-lg-10">
              <div className="card shadow-lg border-0 overflow-hidden rounded-4">
                <div className="row g-0">
                  <div className="col-md-5 bg-secondary text-white p-5 d-flex flex-column justify-content-center">
                    <h4 className="fw-bold mb-4 text-light">Nuestra Ubicación</h4>
                    <p className="mb-3 d-flex align-items-center">
                      <span className="bg-light text-secondary rounded-circle p-2 me-3 d-inline-flex">📍</span>
                      Av. Siempre Viva 742, Región Metropolitana
                    </p>
                    <p className="mb-3 d-flex align-items-center">
                      <span className="bg-light text-secondary rounded-circle p-2 me-3 d-inline-flex">📞</span>
                      +56 9 1234 5678
                    </p>
                    <p className="mb-4 d-flex align-items-center">
                      <span className="bg-light text-secondary rounded-circle p-2 me-3 d-inline-flex">✉️</span>
                      contacto@colegiobo.cl
                    </p>
                    <div className="mt-3 p-3 bg-dark bg-opacity-25 rounded-3">
                      <h6 className="fw-bold mb-2">Horario de atención:</h6>
                      <p className="small mb-0 opacity-75">
                        Lunes a Viernes: 08:00 - 18:00 hrs.
                      </p>
                    </div>
                  </div>
                  <div className="col-md-7">
                    {/* Google Maps Iframe */}
                    <iframe 
                      src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d106421.39178491322!2d-70.7323861271109!3d-33.472719283733056!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x9662c5410425af2f%3A0x8475d53c400f0931!2sSantiago%2C%20Regi%C3%B3n%20Metropolitana!5e0!3m2!1ses-419!2scl!4v1700000000000!5m2!1ses-419!2scl" 
                      width="100%" 
                      height="100%" 
                      style={{ minHeight: '400px', border: 0 }} 
                      allowFullScreen="" 
                      loading="lazy" 
                      referrerPolicy="no-referrer-when-downgrade"
                      title="Mapa Colegio BO"
                    ></iframe>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Home;