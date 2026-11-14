import { useEffect, useState } from "react";
import { useShop } from "../context/ShopContext.jsx";
import { validateUser } from "../services/validation.js";
import UserFields from "../components/UserFields.jsx";
export default function Profile() {
  const { user, saveProfile, remote, comunas } = useShop();
  const [form, setForm] = useState({ ...user, password: "" });
  const [busy, setBusy] = useState(false);
  // El perfil del servidor llega después del login: actualiza el formulario.
  useEffect(() => {
    if (user.remote) setForm({ ...user, password: "" });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user.remote, user.idComuna, user.direccion, user.run]);
  const [errors, setErrors] = useState({});
  const [message, setMessage] = useState("");
  const [failed, setFailed] = useState(false);
  async function submit(e) {
    e.preventDefault();
    if (busy) return;
    const next = validateUser(form, {
      passwordRequired: false,
      remote,
      editing: true,
    });
    setErrors(next);
    if (Object.keys(next).length) return;
    setBusy(true);
    try {
      await saveProfile(form);
      setMessage("Perfil actualizado.");
      setFailed(false);
      setForm((prev) => ({ ...prev, password: "" }));
    } catch (err) {
      setMessage(err.message);
      setFailed(true);
    }
    setBusy(false);
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
          comunas={remote ? comunas : null}
          onChange={(k, v) => setForm((prev) => ({ ...prev, [k]: v }))}
        />
        <button className="btn btn-danger" disabled={busy}>
          Guardar cambios
        </button>
      </form>
    </section>
  );
}
