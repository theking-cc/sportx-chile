import { Navigate } from "react-router-dom";
import { useShop } from "../context/ShopContext.jsx";
import AdminNav from "../components/AdminNav.jsx";
import { money } from "../data/seed.js";
import { exportDatabase } from "../services/repository.js";
export default function AdminDashboard() {
  const { db, user, products, orders, messages, remote } = useShop();
  const critical = products.filter((p) => p.stock <= p.stockCritico);
  // El vendedor solo tiene acceso a productos y pedidos.
  if (user.rol !== "Administrador")
    return <Navigate to="/admin/productos" replace />;
  function download() {
    const url = URL.createObjectURL(
      new Blob([exportDatabase(db)], { type: "application/json" }),
    );
    const a = document.createElement("a");
    a.href = url;
    a.download = "sportx-respaldo.json";
    a.click();
    setTimeout(() => URL.revokeObjectURL(url), 1000);
  }
  return (
    <>
      <AdminNav />
      <h1>Panel administrativo</h1>
      <p>
        Hola, {user.nombre}. Rol: {user.rol}
      </p>
      <div className="row g-3 mb-4">
        {[
          ["Productos", products.length],
          ["Stock crítico", critical.length],
          ["Pedidos", orders.length],
          [
            "Ventas vigentes",
            money(
              orders
                .filter((o) => o.status !== "Cancelado")
                .reduce((s, o) => s + o.total, 0),
            ),
          ],
        ].map(([title, value]) => (
          <div key={title} className="col-sm-6 col-lg-3">
            <div className="card p-3">
              <h2 className="h6">{title}</h2>
              <strong className="fs-3">{value}</strong>
            </div>
          </div>
        ))}
      </div>
      {critical.length > 0 && (
        <div className="alert alert-warning">
          Revisar stock: {critical.map((p) => p.nombre).join(", ")}
        </div>
      )}
      {user.rol === "Administrador" && (
        <>
          {!remote && (
            <>
              <button className="btn btn-outline-dark mb-4" onClick={download}>
                Exportar respaldo de datos
              </button>
              <p className="small text-muted">
                El respaldo contiene datos personales y contraseñas de cuentas
                registradas. Guárdalo solo en tu computador.
              </p>
            </>
          )}
          <h2 className="h4">Consultas recibidas</h2>
          {!messages.length ? (
            <p>No hay consultas.</p>
          ) : (
            messages.map((m) => (
              <article key={m.id} className="card p-3 mb-2">
                <strong>
                  {m.nombre} · {m.correo}
                </strong>
                <p className="mb-0">{m.mensaje}</p>
              </article>
            ))
          )}
        </>
      )}
    </>
  );
}
