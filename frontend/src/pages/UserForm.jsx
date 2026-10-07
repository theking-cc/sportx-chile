import { useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { useShop } from "../context/ShopContext.jsx";
import { saveUser } from "../services/repository.js";
import { validateUser } from "../services/validation.js";
import UserFields, { emptyUser } from "../components/UserFields.jsx";
import AdminNav from "../components/AdminNav.jsx";
export default function UserForm() {
  const { id } = useParams();
  const { db, changeDatabase } = useShop();
  const existing = db.users.find((u) => u.id === id);
  const [form, setForm] = useState(
    existing ? { ...existing, password: "" } : emptyUser,
  );
  const [errors, setErrors] = useState({});
  const [error, setError] = useState("");
  const navigate = useNavigate();
  if (id && !existing)
    return (
      <>
        <h1>Usuario no encontrado</h1>
        <Link to="/admin/usuarios">Volver</Link>
      </>
    );
  function submit(e) {
    e.preventDefault();
    const next = validateUser(form, { passwordRequired: !id });
    setErrors(next);
    if (Object.keys(next).length) return;
    try {
      changeDatabase((db, actor) => saveUser(db, form, actor));
      navigate("/admin/usuarios");
    } catch (err) {
      setError(err.message);
    }
  }
  return (
    <>
      <AdminNav />
      <section className="card p-4 mx-auto form-card">
        <h1>{id ? "Editar usuario" : "Nuevo usuario"}</h1>
        {error && (
          <p role="alert" className="alert alert-danger">
            {error}
          </p>
        )}
        <form noValidate onSubmit={submit}>
          <UserFields
            admin
            editing={!!id}
            value={form}
            errors={errors}
            onChange={(k, v) => setForm((prev) => ({ ...prev, [k]: v }))}
          />
          <button className="btn btn-danger">Guardar usuario</button>
          <Link to="/admin/usuarios" className="ms-3">
            Cancelar
          </Link>
        </form>
      </section>
    </>
  );
}
