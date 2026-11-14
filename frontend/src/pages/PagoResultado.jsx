import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { useShop } from "../context/ShopContext.jsx";
import OrderSummary from "../components/OrderSummary.jsx";

// Destino del retorno desde Webpay: /#/pago/resultado?estado=...&orden=...
const MENSAJES = {
  aprobado: {
    titulo: "Pago aprobado",
    texto: "Recibimos tu pago y tu pedido quedó registrado.",
    tono: "success",
  },
  rechazado: {
    titulo: "Pago rechazado",
    texto:
      "Transbank no autorizó el pago. No se hizo ningún cargo y tus productos siguen en el carrito.",
    tono: "danger",
  },
  anulado: {
    titulo: "Pago anulado",
    texto:
      "Cancelaste el pago en Webpay. No se hizo ningún cargo y tus productos siguen en el carrito.",
    tono: "warning",
  },
  expirado: {
    titulo: "Se agotó el tiempo",
    texto:
      "El formulario de pago expiró. No se hizo ningún cargo y tus productos siguen en el carrito.",
    tono: "warning",
  },
  error: {
    titulo: "No pudimos confirmar el pago",
    texto:
      "Revisa el estado en Mis pedidos antes de intentar de nuevo. Si ves un cargo, escríbenos desde Contacto.",
    tono: "danger",
  },
};

export default function PagoResultado() {
  const [params] = useSearchParams();
  const estado = MENSAJES[params.get("estado")]
    ? params.get("estado")
    : "error";
  const idOrden = params.get("orden");
  const { orders, user, refreshRemote } = useShop();
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    Promise.resolve(refreshRemote?.()).finally(() => setCargando(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user?.id]);

  const m = MENSAJES[estado];
  const order = orders.find((o) => o.id === idOrden && o.userId === user?.id);

  return (
    <section className="card p-4 mx-auto form-card pago-resultado">
      <h1>{m.titulo}</h1>
      <p role="status" className={"alert alert-" + m.tono}>
        {m.texto}
      </p>
      {estado === "aprobado" && order && (
        <>
          <p className="mb-3">
            Pedido <strong>{order.number}</strong>
          </p>
          <OrderSummary order={order} />
          <Link className="btn btn-danger" to={"/pedidos/" + order.id}>
            Ver mi pedido
          </Link>
        </>
      )}
      {estado === "aprobado" && !order && cargando && (
        <p className="text-muted">Cargando tu pedido…</p>
      )}
      {estado !== "aprobado" && (
        <div className="d-flex flex-wrap gap-2">
          {estado !== "error" ? (
            <Link className="btn btn-danger" to="/carrito">
              Volver al carrito
            </Link>
          ) : (
            <Link className="btn btn-danger" to="/pedidos">
              Ver mis pedidos
            </Link>
          )}
          <Link className="btn btn-outline-dark" to="/productos">
            Seguir comprando
          </Link>
        </div>
      )}
    </section>
  );
}
