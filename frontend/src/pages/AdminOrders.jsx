import { useState } from "react";
import { useShop } from "../context/ShopContext.jsx";
import { updateOrder, ORDER_STATES } from "../services/repository.js";
import AdminNav from "../components/AdminNav.jsx";
import OrderSummary from "../components/OrderSummary.jsx";
export default function AdminOrders() {
  const { db, changeDatabase } = useShop();
  const [error, setError] = useState("");
  function change(id, status) {
    try {
      changeDatabase((db, actor) => updateOrder(db, id, status, actor));
      setError("");
    } catch (err) {
      setError(err.message);
    }
  }
  return (
    <>
      <AdminNav />
      <h1>Administración de pedidos</h1>
      {error && (
        <p role="alert" className="alert alert-danger">
          {error}
        </p>
      )}
      {!db.orders.length ? (
        <p>No hay pedidos registrados.</p>
      ) : (
        db.orders.map((o) => (
          <article className="card p-4 mb-3" key={o.id}>
            <h2 className="h5">{o.number}</h2>
            <p>
              {o.cliente} · {o.correo}
            </p>
            <label htmlFor={"status-" + o.id} className="form-label">
              Estado del pedido
            </label>
            <select
              id={"status-" + o.id}
              className="form-select mb-3"
              value={o.status}
              onChange={(e) => change(o.id, e.target.value)}
            >
              {ORDER_STATES.map((s) => (
                <option key={s}>{s}</option>
              ))}
            </select>
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
