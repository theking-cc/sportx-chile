import { useState } from "react";
import Field from "../components/Field.jsx";
import { useShop } from "../context/ShopContext.jsx";
import { addMessage } from "../services/repository.js";
export default function Contact() {
  const { user, changeDatabase } = useShop();
  const [form, setForm] = useState({
    nombre: user?.nombre || "",
    correo: user?.correo || "",
    mensaje: "",
  });
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);
  function submit(e) {
    e.preventDefault();
    try {
      changeDatabase((db) => addMessage(db, form));
      setSuccess(true);
      setError("");
      setForm((prev) => ({ ...prev, mensaje: "" }));
    } catch (err) {
      setError(err.message);
      setSuccess(false);
    }
  }
  return (
    <section className="card p-4 mx-auto form-card">
      <h1>Contacto</h1>
      <p>
        Déjanos tu consulta. Se guardará en este navegador para la demostración.
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
        <button className="btn btn-danger">Guardar consulta</button>
      </form>
    </section>
  );
}
