import { money } from "../data/seed.js";
export default function OrderSummary({ order }) {
  return (
    <>
      <div className="table-responsive">
        <table className="table">
          <thead>
            <tr>
              <th>Producto</th>
              <th>Cantidad</th>
              <th>Precio</th>
              <th>Subtotal</th>
            </tr>
          </thead>
          <tbody>
            {order.items.map((item) => (
              <tr key={item.id}>
                <td>{item.nombre}</td>
                <td>{item.cantidad}</td>
                <td>{money(item.precio)}</td>
                <td>{money(item.precio * item.cantidad)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <p>Subtotal: {money(order.subtotal)}</p>
      <p>Descuento: {money(order.descuento)}</p>
      <p className="fw-bold fs-4">Total: {money(order.total)}</p>
      <p>
        Despacho: {order.shipping.direccion}, {order.shipping.comuna},{" "}
        {order.shipping.region}
      </p>
    </>
  );
}
