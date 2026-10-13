import { useState } from "react";
import { Link } from "react-router-dom";
import { useShop } from "../context/ShopContext.jsx";
import { total } from "../services/cart.js";
import { COUPONS, money } from "../data/seed.js";
export default function Cart() {
  const { cart, change, remove, clear } = useShop();
  const [code, setCode] = useState("");
  const [coupon, setCoupon] = useState(null);
  const [error, setError] = useState("");
  const totals = total(cart, coupon);
  return (
    <>
      <h1 className="mb-4">Tu carrito</h1>
      {!cart.length ? (
        <p>
          Tu carrito está vacío. <Link to="/productos">Explorar productos</Link>
        </p>
      ) : (
        <div className="row g-4">
          <div className="col-lg-8">
            {cart.map((item) => (
              <article className="card p-3 mb-3" key={item.id}>
                <h2 className="h5">{item.nombre}</h2>
                <p>
                  {money(item.precio)} · Subtotal:{" "}
                  {money(item.precio * item.cantidad)}
                </p>
                <div className="d-flex align-items-center gap-3 flex-wrap">
                  <button
                    className="btn btn-outline-secondary"
                    aria-label={"Disminuir " + item.nombre}
                    onClick={() => change(item.id, -1)}
                  >
                    −
                  </button>
                  <span>{item.cantidad}</span>
                  <button
                    className="btn btn-outline-secondary"
                    aria-label={"Aumentar " + item.nombre}
                    onClick={() => change(item.id, 1)}
                  >
                    +
                  </button>
                  <button
                    className="btn btn-outline-danger ms-auto"
                    onClick={() => remove(item.id)}
                  >
                    Eliminar
                  </button>
                </div>
              </article>
            ))}
            <button
              className="btn btn-outline-danger"
              onClick={() => {
                if (window.confirm("¿Vaciar el carrito?")) {
                  clear();
                  setCoupon(null);
                }
              }}
            >
              Vaciar carrito
            </button>
          </div>
          <aside className="col-lg-4">
            <div className="card p-4">
              <h2 className="h4">Resumen</h2>
              <form
                onSubmit={(e) => {
                  e.preventDefault();
                  const found = COUPONS.find(
                    (c) => c.codigo === code.trim().toUpperCase(),
                  );
                  setCoupon(found || null);
                  setError(found ? "" : "Cupón inválido.");
                }}
              >
                <label htmlFor="coupon" className="form-label">
                  Cupón de descuento
                </label>
                <div className="input-group">
                  <input
                    id="coupon"
                    className="form-control"
                    value={code}
                    onChange={(e) => setCode(e.target.value)}
                  />
                  <button className="btn btn-dark">Aplicar</button>
                </div>
              </form>
              {error && (
                <p role="alert" className="text-danger">
                  {error}
                </p>
              )}
              {coupon && (
                <p className="text-success">Cupón {coupon.codigo} aplicado</p>
              )}
              <p className="mt-3">Subtotal: {money(totals.subtotal)}</p>
              <p>Descuento: {money(totals.descuento)}</p>
              <p className="fs-4 fw-bold">Total: {money(totals.total)}</p>
              <Link
                to="/finalizar-compra"
                state={{ coupon: coupon?.codigo || "" }}
                className="btn btn-danger mb-3"
              >
                Finalizar compra
              </Link>
              <p className="small text-muted">
                La compra de demostración registra tu pedido y actualiza el
                inventario.
              </p>
            </div>
          </aside>
        </div>
      )}
    </>
  );
}
