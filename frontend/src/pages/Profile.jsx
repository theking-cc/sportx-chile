import { useState } from "react";
import { useShop } from "../context/ShopContext.jsx";
import { saveUser } from "../services/repository.js";
import { validateUser } from "../services/validation.js";
import UserFields from "../components/UserFields.jsx";
export default function Profile() {
  const { user, changeDatabase } = useShop();
  const [form, setForm] = useState({ ...user, password: "" });
  const [errors, setErrors] = useState({});
  const [message, setMessage] = useState("");
  const [failed, setFailed] = useState(false);
  function submit(e) {
    e.preventDefault();
    const next = validateUser(form, { passwordRequired: false });
    setErrors(next);
    if (Object.keys(next).length) return;
    try {
      changeDatabase((db, actor) =>
        saveUser(db, form, actor, { profile: true }),
      );
      setMessage("Perfil actualizado.");
      setFailed(false);
      setForm((prev) => ({ ...prev, password: "" }));
    } catch (err) {
      setMessage(err.message);
      setFailed(true);
    }
  }
  return (
    <section className="card p-4 mx-auto form-card">
      <h1>Mi perfil</h1>
      <p>Rol: {user.rol}</p>
      {message && (
        <p
          role="status"
          className={"alert " + (failed ? "alert-danger" : "alert-success")}
        >
          {message}
        </p>
      )}
      <form noValidate onSubmit={submit}>
        <UserFields
          editing
          value={form}
          errors={errors}
          onChange={(k, v) => setForm((prev) => ({ ...prev, [k]: v }))}
        />
        <button className="btn btn-danger">Guardar cambios</button>
      </form>
    </section>
  );
}
