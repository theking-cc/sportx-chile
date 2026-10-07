import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useShop } from "../context/ShopContext.jsx";
import { saveUser } from "../services/repository.js";
import { validateUser } from "../services/validation.js";
import UserFields, { emptyUser } from "../components/UserFields.jsx";
import Field from "../components/Field.jsx";
export default function Register() {
  const [form, setForm] = useState(emptyUser);
  const [confirm, setConfirm] = useState("");
  const [errors, setErrors] = useState({});
  const [error, setError] = useState("");
  const { changeDatabase, notify } = useShop();
  const navigate = useNavigate();
  function submit(e) {
    e.preventDefault();
    const next = validateUser(form);
    if (form.password !== confirm)
      next.confirm = "Las contraseñas no coinciden.";
    setErrors(next);
    if (Object.keys(next).length) return;
    try {
      changeDatabase((db) => saveUser(db, form, null, { register: true }));
      notify("Cuenta creada. Ahora puedes iniciar sesión.");
      navigate("/login");
    } catch (err) {
      setError(err.message);
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
      <form noValidate onSubmit={submit}>
        <UserFields
          value={form}
          errors={errors}
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
        <button className="btn btn-danger">Crear cuenta</button>
        <Link to="/login" className="ms-3">
          Ya tengo cuenta
        </Link>
      </form>
    </section>
  );
}
