export default function About() {
  return (
    <>
      <span className="badge bg-danger mb-3">NUESTRA TIENDA</span>
      <h1>Nosotros</h1>
      <p className="lead">
        SportX Chile es una propuesta de tienda online de equipamiento deportivo
        para boxeo, fitness y entrenamiento.
      </p>
      <div className="row g-4 mt-2">
        <section className="col-md-6">
          <div className="card p-4 h-100">
            <h2 className="h4">Nuestra misión</h2>
            <p>
              Facilitar la elección de equipamiento mediante un catálogo claro,
              información de stock y una compra sencilla.
            </p>
          </div>
        </section>
        <section className="col-md-6">
          <div className="card p-4 h-100">
            <h2 className="h4">Nuestra visión</h2>
            <p>
              Desarrollar una experiencia digital accesible que acompañe a las
              personas en sus actividades deportivas.
            </p>
          </div>
        </section>
      </div>
      <p className="text-muted mt-4">
        Proyecto académico de Desarrollo Fullstack II. La tienda y las compras
        son de demostración.
      </p>
    </>
  );
}
