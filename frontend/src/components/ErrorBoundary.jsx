import { Component } from "react";
export default class ErrorBoundary extends Component {
  state = { error: null };
  static getDerivedStateFromError(error) {
    return { error };
  }
  render() {
    if (this.state.error)
      return (
        <main className="container py-5">
          <h1>No pudimos cargar SportX</h1>
          <p role="alert">{this.state.error.message}</p>
          <p>
            Los datos guardados no se han eliminado. Conserva un respaldo antes
            de cambiar el almacenamiento.
          </p>
          <button
            className="btn btn-danger"
            onClick={() => window.location.reload()}
          >
            Volver a intentar
          </button>
        </main>
      );
    return this.props.children;
  }
}
