import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { useShop } from "../context/ShopContext.jsx";
import AdminNav from "../components/AdminNav.jsx";
import OrderSummary from "../components/OrderSummary.jsx";
import { api } from "../services/api.js";
// Detalle de una orden (BOLETA + DETALLE_BOLETA) para Administrador y Vendedor.
// Con servidor se consulta GET /api/v1/ordenes/{id}; sin servidor usa los datos locales.
export default function AdminOrderDetail() {
  const { id } = useParams();
  const { orders, remote } = useShop();
  const local = orders.find((o) => String(o.id) === String(id));
  const [order, setOrder] = useState(local || null);
  const [error, setError] = useState("");
  useEffect(() => {
    if (!remote) return;
    let vigente = true;
    api
      .orden(id)
      .then((o) => vigente && setOrder(o))
      .catch((err) => vigente && setError(err.message));
    return () => {
      vigente = false;
    };
  }, [id, remote]);
  return (
    <>
      <AdminNav />
      <Link to="/admin/pedidos" className="d-inline-block mb-3">
        ← Volver a pedidos
      </Link>
      {error && (
        <p role="alert" className="alert alert-danger">
          {error}
        </p>
      )}
      {!order ? (
        !error && <p>Cargando pedido…</p>
      ) : (
        <>
          <h1>Pedido {order.number}</h1>
          <p className="mb-1">
            {order.cliente} · {order.correo}
          </p>
          <p className="text-muted">
            {new Date(order.createdAt).toLocaleString("es-CL")} · Estado:{" "}
            <span className="badge text-bg-secondary">{order.status}</span>
          </p>
          <OrderSummary order={order} />
        </>
      )}
    </>
  );
}
