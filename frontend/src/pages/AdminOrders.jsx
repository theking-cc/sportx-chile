import { useState } from "react";
import { Link } from "react-router-dom";
import { useShop } from "../context/ShopContext.jsx";
import { ORDER_STATES } from "../services/repository.js";
import AdminNav from "../components/AdminNav.jsx";
import OrderSummary from "../components/OrderSummary.jsx";
import { money } from "../data/seed.js";
import { ESTADOS_PAGO } from "../services/api.js";
// Mismas transiciones que valida el backend.
const NEXT_STATES = {
  Pendiente: ["Preparando", "Cancelado"],
  Preparando: ["Enviado", "Cancelado"],
  Enviado: ["Entregado"],
  Entregado: [],
  Cancelado: [],
};
export default function AdminOrders() {
  const { orders, updateOrderStatus, user } = useShop();
  const isAdmin = user?.rol === "Administrador";
  const [error, setError] = useState("");
  async function change(id, status) {
    try {
      setError("");
      await updateOrderStatus(id, status);
    } catch (err) {
      setError(err.message);
    }
  }
  return (
    <>
      <AdminNav />
      <h1>{isAdmin ? "Administración de pedidos" : "Pedidos"}</h1>
      {error && (
        <p role="alert" className="alert alert-danger">
          {error}
        </p>
      )}
      {!orders.length ? (
        <p>No hay pedidos registrados.</p>
      ) : (
        orders.map((o) => (
          <article className="card p-4 mb-3" key={o.id}>
            <h2 className="h5">{o.number}</h2>
            <p className="mb-1">
              {o.cliente} · {o.correo}
            </p>
            <p className="text-muted small">
              {new Date(o.createdAt).toLocaleString("es-CL")} · Total{" "}
              {money(o.total)}
              {o.pago &&
                ` · Pago: ${ESTADOS_PAGO[o.pago.estado] || o.pago.estado}`}
            </p>
            {isAdmin ? (
              <>
                <label htmlFor={"status-" + o.id} className="form-label">
                  Estado del pedido
                </label>
                <select
                  id={"status-" + o.id}
                  className="form-select mb-3"
                  value={o.status}
                  disabled={!NEXT_STATES[o.status]?.length}
                  onChange={(e) => change(o.id, e.target.value)}
                >
                  {ORDER_STATES.filter(
                    (s) => s === o.status || NEXT_STATES[o.status]?.includes(s),
                  ).map((s) => (
                    <option key={s}>{s}</option>
                  ))}
                </select>
              </>
            ) : (
              <p className="mb-2">
                Estado del pedido:{" "}
                <span className="badge text-bg-secondary">{o.status}</span>
              </p>
            )}
            <Link
              className="btn btn-sm btn-outline-dark align-self-start mb-2"
              to={"/admin/pedidos/" + o.id}
            >
              Ver detalle
            </Link>
            <details>
              <summary>Ver productos y despacho</summary>
              <OrderSummary order={o} />
            </details>
          </article>
        ))
      )}
    </>
  );
}
