import { useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { useShop } from "../context/ShopContext.jsx";
import { saveProduct } from "../services/repository.js";
import { validateProduct } from "../services/validation.js";
import Field from "../components/Field.jsx";
import AdminNav from "../components/AdminNav.jsx";
export default function ProductForm() {
  const { id } = useParams();
  const { products, changeDatabase } = useShop();
  const existing = products.find((p) => p.id === id);
  const [form, setForm] = useState(
    existing || {
      codigo: "",
      nombre: "",
      descripcion: "",
      precio: "",
      stock: "",
      stockCritico: 0,
      categoria: "",
      imagen: "",
    },
  );
  const [errors, setErrors] = useState({});
  const [error, setError] = useState("");
  const navigate = useNavigate();
  if (id && !existing)
    return (
      <>
        <h1>Producto no encontrado</h1>
        <Link to="/admin/productos">Volver</Link>
      </>
    );
  function submit(e) {
    e.preventDefault();
    const next = validateProduct(form);
    setErrors(next);
    if (Object.keys(next).length) return;
    try {
      changeDatabase((db, actor) => saveProduct(db, form, actor));
      navigate("/admin/productos");
    } catch (err) {
      setError(err.message);
    }
  }
  return (
    <>
      <AdminNav />
      <section className="card p-4 mx-auto form-card">
        <h1>{id ? "Editar producto" : "Nuevo producto"}</h1>
        {error && (
          <p role="alert" className="alert alert-danger">
            {error}
          </p>
        )}
        <form noValidate onSubmit={submit}>
          {[
            ["codigo", "Código", "text"],
            ["nombre", "Nombre", "text"],
            ["descripcion", "Descripción", "textarea"],
            ["precio", "Precio (CLP)", "number"],
            ["stock", "Stock", "number"],
            ["stockCritico", "Stock crítico", "number"],
            ["imagen", "URL de imagen (opcional)", "url"],
          ].map(([name, label, type]) => (
            <Field
              key={name}
              name={name}
              label={label}
              type={type}
              value={form[name]}
              error={errors[name]}
              onChange={(k, v) => setForm({ ...form, [k]: v })}
              {...(type === "number" ? { min: 0, step: 1 } : {})}
            />
          ))}
          <Field
            name="categoria"
            label="Categoría"
            value={form.categoria}
            error={errors.categoria}
            onChange={(k, v) => setForm({ ...form, [k]: v })}
          >
            <option value="">Selecciona una categoría</option>
            {["Boxeo", "Fitness", "Calzado", "Accesorios"].map((c) => (
              <option key={c}>{c}</option>
            ))}
          </Field>
          <button className="btn btn-danger">Guardar producto</button>
          <Link to="/admin/productos" className="ms-3">
            Cancelar
          </Link>
        </form>
      </section>
    </>
  );
}
