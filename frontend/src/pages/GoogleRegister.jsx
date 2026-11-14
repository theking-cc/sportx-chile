import { useState } from "react";
import { Link, Navigate, useLocation, useNavigate } from "react-router-dom";
import { useShop } from "../context/ShopContext.jsx";
import Field from "../components/Field.jsx";
import { ComunaOptions } from "../components/UserFields.jsx";
import { validarRunChileno } from "../services/validation.js";

// Paso final del registro con Google: la base de datos exige RUT, comuna y
// dirección, datos que Google no entrega.
export default function GoogleRegister() {
  const { state } = useLocation();
  const { comunas, completeGoogleRegistration } = useShop();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    run: "",
    nombre: state?.nombre || "",
    apellidos: state?.apellidos || "",
    idComuna: "",
    direccion: "",
    fechaNacimiento: "",
  });
  const [errors, setErrors] = useState({});
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  if (!state?.credential) return <Navigate to="/login" replace />;

  // Al editar un campo se borra su error para no dejar avisos ya resueltos.
  const change = (k, v) => {
    setForm((prev) => ({ ...prev, [k]: v }));
    const key = k === "idComuna" ? "comuna" : k;
    setErrors((prev) => {
      if (!prev[key]) return prev;
      const next = { ...prev };
      delete next[key];
      return next;
    });
  };

  async function submit(e) {
    e.preventDefault();
    if (busy) return;
    const next = {};
    if (!validarRunChileno(form.run))
      next.run = "Ingresa un RUN válido sin puntos ni guion.";
    if (!form.nombre.trim()) next.nombre = "El nombre es obligatorio.";
    if (!form.apellidos.trim())
      next.apellidos = "Los apellidos son obligatorios.";
    if (!form.idComuna) next.comuna = "Selecciona una comuna.";
    if (!form.direccion.trim()) next.direccion = "La dirección es obligatoria.";
    if (form.fechaNacimiento && new Date(form.fechaNacimiento) > new Date())
      next.fechaNacimiento = "La fecha no puede estar en el futuro.";
    setErrors(next);
    if (Object.keys(next).length) return;
    setBusy(true);
    setError("");
    try {
      await completeGoogleRegistration(state.credential, form);
      navigate(state.from || "/perfil", { replace: true });
    } catch (err) {
      // La credencial de Google dura una hora: si venció, se vuelve a iniciar.
      setError(
        err.status === 401
          ? "Tu sesión de Google expiró. Vuelve a iniciar con Google."
          : err.message,
      );
      setBusy(false);
    }
  }

  return (
    <section className="card p-4 mx-auto form-card">
      <h1>Completa tu cuenta</h1>
      <p>
        Entraste con Google como <strong>{state.email}</strong>. Para comprar y
        recibir despachos necesitamos estos datos.
      </p>
      {error && (
        <p role="alert" className="alert alert-danger">
          {error} {error.includes("expiró") && <Link to="/login">Volver</Link>}
        </p>
      )}
      <form noValidate onSubmit={submit}>
        <Field
          label="RUN (sin puntos ni guion)"
          name="run"
          value={form.run}
          onChange={change}
          error={errors.run}
          maxLength="9"
          autoComplete="off"
        />
        <div className="row">
          <div className="col-md-6">
            <Field
              label="Nombre"
              name="nombre"
              value={form.nombre}
              onChange={change}
              error={errors.nombre}
              maxLength="50"
              autoComplete="given-name"
            />
          </div>
          <div className="col-md-6">
            <Field
              label="Apellidos"
              name="apellidos"
              value={form.apellidos}
              onChange={change}
              error={errors.apellidos}
              maxLength="100"
              autoComplete="family-name"
            />
          </div>
        </div>
        <Field
          label="Comuna"
          name="idComuna"
          value={form.idComuna}
          onChange={change}
          error={errors.comuna}
        >
          <option value="">Selecciona una comuna</option>
          <ComunaOptions comunas={comunas} />
        </Field>
        <Field
          label="Dirección"
          name="direccion"
          value={form.direccion}
          onChange={change}
          error={errors.direccion}
          maxLength="300"
          autoComplete="street-address"
        />
        <Field
          label="Fecha de nacimiento (opcional)"
          name="fechaNacimiento"
          type="date"
          value={form.fechaNacimiento}
          onChange={change}
          error={errors.fechaNacimiento}
        />
        <button className="btn btn-danger" disabled={busy}>
          {busy ? "Creando cuenta…" : "Crear cuenta"}
        </button>
        <Link to="/login" className="ms-3">
          Cancelar
        </Link>
      </form>
    </section>
  );
}
