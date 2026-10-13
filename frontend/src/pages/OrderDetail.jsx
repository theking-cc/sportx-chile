import { useParams, Link } from "react-router-dom";
import { useShop } from "../context/ShopContext.jsx";
import OrderSummary from "../components/OrderSummary.jsx";
export default function OrderDetail() {
  const { id } = useParams();
  const { db, user } = useShop();
  const order = db.orders.find((o) => o.id === id && o.userId === user.id);
  if (!order)
    return (
      <>
        <h1>Pedido no encontrado</h1>
        <Link to="/pedidos">Volver a mis pedidos</Link>
      </>
    );
  return (
    <>
      <h1>Detalle de mi pedido</h1>
      <p>
        {order.number} · Estado: <strong>{order.status}</strong>
      </p>
      <OrderSummary order={order} />
      <Link to="/pedidos">Volver a mis pedidos</Link>
    </>
  );
}
