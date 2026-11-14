import { useParams, Link } from "react-router-dom";
import { useShop } from "../context/ShopContext.jsx";
import OrderSummary from "../components/OrderSummary.jsx";
export default function Confirmation() {
  const { id } = useParams();
  const { orders, user } = useShop();
  const order = orders.find((o) => o.id === id && o.userId === user.id);
  if (!order)
    return (
      <>
        <h1>Pedido no encontrado</h1>
        <Link to="/pedidos">Ver mis pedidos</Link>
      </>
    );
  return (
    <section className="card p-4">
      <h1>¡Pedido confirmado!</h1>
      <p>Tu pedido {order.number} fue registrado.</p>
      <OrderSummary order={order} />
      <Link to={"/pedidos/" + order.id}>Ver detalle del pedido</Link>
    </section>
  );
}
