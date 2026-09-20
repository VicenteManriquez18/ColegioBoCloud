export default function Contacto() {
  return (
    <div className="container-fluid autumn-bg py-5" style={{ minHeight: '100vh' }}>
      <div className="container">
        <div className="card shadow border-0 p-5 bg-white rounded-4 mx-auto" style={{ maxWidth: '800px' }}>
          <div className="text-center mb-5">
            <h1 className="display-5 fw-bold text-primary mb-2">Contacto Colegio BO</h1>
            <p className="text-muted fs-5">Equipo de Desarrollo y Soporte Técnico del Establecimiento</p>
          </div>

          <div className="row g-4 justify-content-center">
            {/* Vicente Manriquez */}
            <div className="col-md-4 text-center">
              <div className="card h-100 border-0 bg-light shadow-sm p-4 rounded-4">
                <i className="bi bi-person-circle text-primary mb-3" style={{ fontSize: '3rem' }}></i>
                <h5 className="fw-bold text-dark mb-0">Vicente Manríquez</h5>
              </div>
            </div>

            {/* Davier Ramos */}
            <div className="col-md-4 text-center">
              <div className="card h-100 border-0 bg-light shadow-sm p-4 rounded-4">
                <i className="bi bi-person-circle text-success mb-3" style={{ fontSize: '3rem' }}></i>
                <h5 className="fw-bold text-dark mb-0">Davier Ramos</h5>
              </div>
            </div>

            {/* Dilan Micolta */}
            <div className="col-md-4 text-center">
              <div className="card h-100 border-0 bg-light shadow-sm p-4 rounded-4">
                <i className="bi bi-person-circle text-warning mb-3" style={{ fontSize: '3rem' }}></i>
                <h5 className="fw-bold text-dark mb-0">Dilan Micolta</h5>
              </div>
            </div>
          </div>

          <hr className="my-5 opacity-25" />

          <div className="text-center bg-light p-4 rounded-3 shadow-sm">
            <h5 className="fw-bold mb-3"><i className="bi bi-envelope-at me-2 text-primary"></i>¿Necesitas ayuda?</h5>
            <p className="text-muted mb-0">Puedes contactar a cualquiera de los integrantes del equipo para asistencia sobre la plataforma.</p>
          </div>
        </div>
      </div>
    </div>
  );
}
