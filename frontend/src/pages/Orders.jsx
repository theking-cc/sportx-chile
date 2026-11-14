import { Link } from "react-router-dom";
import { useShop } from "../context/ShopContext.jsx";
import { money } from "../data/seed.js";
import { ESTADOS_PAGO } from "../services/api.js";
export default function Orders() {
  const { orders: all, user } = useShop();
  const orders = all.filter((o) => o.userId === user.id);
  return (
    <>
      <h1>Mis pedidos</h1>
      {!orders.length ? (
        <p>
          Aún no has realizado compras.{" "}
          <Link to="/productos">Explorar productos</Link>
        </p>
      ) : (
        <div className="table-responsive">
          <table className="table">
            <thead>
              <tr>
                <th>Pedido</th>
                <th>Fecha</th>
                <th>Estado</th>
                <th>Total</th>
                <th>Detalle</th>
              </tr>
            </thead>
            <tbody>
              {orders.map((o) => (
                <tr key={o.id}>
                  <td>{o.number}</td>
                  <td>{new Date(o.createdAt).toLocaleDateString("es-CL")}</td>
                  <td>
                    {o.status}
                    {o.pago && o.pago.estado !== "APROBADO" && (
                      <span className="d-block small text-muted">
                        {ESTADOS_PAGO[o.pago.estado]}
                      </span>
                    )}
                  </td>
                  <td>{money(o.total)}</td>
                  <td>
                    <Link to={"/pedidos/" + o.id}>Ver pedido</Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>
  );
}
