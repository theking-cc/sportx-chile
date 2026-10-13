import { useState } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { useShop } from "../context/ShopContext.jsx";
import Field from "../components/Field.jsx";
export default function Login() {
  const [form, setForm] = useState({ correo: "", password: "" });
  const [error, setError] = useState("");
  const { signIn, signInDemo, db } = useShop();
  const navigate = useNavigate();
  const location = useLocation();
  function enterDemo(userId) {
    try {
      const user = signInDemo(userId);
      navigate(
        location.state?.from || (user.rol === "Cliente" ? "/perfil" : "/admin"),
        { replace: true, state: location.state?.nextState },
      );
    } catch (err) {
      setError(err.message);
    }
  }
  function submit(e) {
    e.preventDefault();
    try {
      const user = signIn(form.correo, form.password);
      const from = location.state?.from;
      navigate(from || (user.rol === "Cliente" ? "/perfil" : "/admin"), {
        replace: true,
        state: location.state?.nextState,
      });
    } catch (err) {
      setError(err.message);
    }
  }
  return (
    <section className="card p-4 mx-auto form-card">
      <h1>Iniciar sesión</h1>
      {error && (
        <p role="alert" className="alert alert-danger">
          {error}
        </p>
      )}
      <form onSubmit={submit}>
        <Field
          label="Correo electrónico"
          name="correo"
          type="email"
          value={form.correo}
          onChange={(k, v) => setForm({ ...form, [k]: v })}
          required
          autoComplete="username"
        />
        <Field
          label="Contraseña"
          name="password"
          type="password"
          value={form.password}
          onChange={(k, v) => setForm({ ...form, [k]: v })}
          required
          autoComplete="current-password"
        />
        <button className="btn btn-danger w-100">Ingresar</button>
      </form>
      <p className="mt-3">
        ¿No tienes cuenta? <Link to="/registro">Regístrate</Link>
      </p>
      <details>
        <summary>Acceso de demostración</summary>
        <p className="small mt-2">
          Prueba la tienda con un rol ficticio. No se necesitan credenciales.
        </p>
        <div className="d-flex flex-wrap gap-2">
          {db.users
            .filter((u) => u.demo)
            .map((u) => (
              <button
                key={u.id}
                className="btn btn-outline-dark"
                onClick={() => enterDemo(u.id)}
              >
                Entrar como {u.rol}
              </button>
            ))}
        </div>
      </details>
    </section>
  );
}
