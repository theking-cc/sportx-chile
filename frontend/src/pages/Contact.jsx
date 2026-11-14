import { useState } from "react";
import Field from "../components/Field.jsx";
import { useShop } from "../context/ShopContext.jsx";
export default function Contact() {
  const { user, sendMessage, online } = useShop();
  const [busy, setBusy] = useState(false);
  const [form, setForm] = useState({
    nombre: user?.nombre || "",
    correo: user?.correo || "",
    mensaje: "",
  });
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);
  async function submit(e) {
    e.preventDefault();
    if (busy) return;
    if (
      !form.nombre.trim() ||
      !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.correo) ||
      !form.mensaje.trim() ||
      form.mensaje.length > 1000
    ) {
      setError(
        "Completa nombre, correo válido y mensaje de hasta 1000 caracteres.",
      );
      setSuccess(false);
      return;
    }
    setBusy(true);
    try {
      await sendMessage(form);
      setSuccess(true);
      setError("");
      setForm((prev) => ({ ...prev, mensaje: "" }));
    } catch (err) {
      setError(err.message);
      setSuccess(false);
    }
    setBusy(false);
  }
  return (
    <section className="card p-4 mx-auto form-card">
      <h1>Contacto</h1>
      <p>
        Déjanos tu consulta.
        {online === true
          ? " Nuestro equipo la revisará pronto."
          : " Se guardará en este navegador para la demostración."}
      </p>
      {error && (
        <p role="alert" className="alert alert-danger">
          {error}
        </p>
      )}
      {success && (
        <p role="status" className="alert alert-success">
          Tu consulta quedó registrada.
        </p>
      )}
      <form onSubmit={submit}>
        <Field
          label="Nombre"
          name="nombre"
          value={form.nombre}
          onChange={(k, v) => setForm({ ...form, [k]: v })}
          required
          maxLength="100"
        />
        <Field
          label="Correo electrónico"
          name="correo"
          type="email"
          value={form.correo}
          onChange={(k, v) => setForm({ ...form, [k]: v })}
          required
        />
        <Field
          label="Mensaje"
          name="mensaje"
          type="textarea"
          value={form.mensaje}
          onChange={(k, v) => setForm({ ...form, [k]: v })}
          required
          maxLength="1000"
        />
        <button className="btn btn-danger" disabled={busy}>
          Guardar consulta
        </button>
      </form>
    </section>
  );
}
