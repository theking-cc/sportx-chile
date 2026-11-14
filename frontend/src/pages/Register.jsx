import { useEffect, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useShop } from "../context/ShopContext.jsx";
import { api } from "../services/api.js";
import { validateUser } from "../services/validation.js";
import UserFields, { emptyUser } from "../components/UserFields.jsx";
import Field from "../components/Field.jsx";
import GoogleSignIn from "../components/GoogleSignIn.jsx";
export default function Register() {
  const [form, setForm] = useState(emptyUser);
  const [confirm, setConfirm] = useState("");
  const [errors, setErrors] = useState({});
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const [comunas, setComunas] = useState(null);
  const { register, notify, online } = useShop();
  const navigate = useNavigate();
  const remote = online === true;
  useEffect(() => {
    if (!remote) return;
    api
      .comunas()
      .then(setComunas)
      .catch(() =>
        setError("No se pudieron cargar las comunas desde el servidor."),
      );
  }, [remote]);
  async function submit(e) {
    e.preventDefault();
    if (busy) return;
    const next = validateUser(form, { remote });
    if (form.password !== confirm)
      next.confirm = "Las contraseñas no coinciden.";
    setErrors(next);
    if (Object.keys(next).length) return;
    setBusy(true);
    setError("");
    try {
      await register(form);
      notify("Cuenta creada. Ahora puedes iniciar sesión.");
      navigate("/login");
    } catch (err) {
      setError(err.message);
      setBusy(false);
    }
  }
  return (
    <section className="card p-4 mx-auto form-card">
      <h1>Registro de usuario</h1>
      {error && (
        <p className="alert alert-danger" role="alert">
          {error}
        </p>
      )}
      <GoogleSignIn text="signup_with" />
      <form noValidate onSubmit={submit} className="mt-3">
        <UserFields
          value={form}
          errors={errors}
          comunas={remote ? comunas || [] : null}
          onChange={(k, v) => setForm((prev) => ({ ...prev, [k]: v }))}
        />
        <Field
          label="Confirmar contraseña"
          name="confirm"
          type="password"
          value={confirm}
          onChange={(k, v) => setConfirm(v)}
          error={errors.confirm}
        />
        <button className="btn btn-danger" disabled={busy}>
          Crear cuenta
        </button>
        <Link to="/login" className="ms-3">
          Ya tengo cuenta
        </Link>
      </form>
    </section>
  );
}
