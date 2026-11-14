import { money } from "../data/seed.js";
import { ESTADOS_PAGO, TIPOS_PAGO } from "../services/api.js";
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
      {order.descuento > 0 && (
        <p>
          Descuento{order.coupon && ` (${order.coupon})`}: −
          {money(order.descuento)}
        </p>
      )}
      <p className="fw-bold fs-4">Total: {money(order.total)}</p>
      <p>
        Despacho: {order.shipping.direccion}, {order.shipping.comuna},{" "}
        {order.shipping.region}
      </p>
      {order.pago && (
        <p>
          Pago Webpay:{" "}
          <strong>
            {ESTADOS_PAGO[order.pago.estado] || order.pago.estado}
          </strong>
          {order.pago.estado === "APROBADO" && (
            <>
              {" "}
              · {TIPOS_PAGO[order.pago.tipo] || order.pago.tipo}
              {order.pago.digitos && " terminada en " + order.pago.digitos}
              {order.pago.cuotas > 1 && ` · ${order.pago.cuotas} cuotas`}
              {order.pago.autorizacion &&
                ` · Autorización ${order.pago.autorizacion}`}
            </>
          )}
        </p>
      )}
    </>
  );
}
