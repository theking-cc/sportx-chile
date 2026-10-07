import { useState } from "react";
import { useNavigate, useLocation, Link } from "react-router-dom";
import { useShop } from "../context/ShopContext.jsx";
import Field from "../components/Field.jsx";
import { REGIONS, COUPONS, money } from "../data/seed.js";
import { total } from "../services/cart.js";
export default function Checkout() {
  const { user, cart, purchase } = useShop();
  const [shipping, setShipping] = useState({
    direccion: user.direccion || "",
    region: user.region || "",
    comuna: user.comuna || "",
  });
  const location = useLocation();
  const [coupon, setCoupon] = useState(location.state?.coupon || "");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const navigate = useNavigate();
  const totals = total(
    cart,
    COUPONS.find((c) => c.codigo === coupon.trim().toUpperCase()),
  );
  const comunas =
    REGIONS.find((r) => r.region === shipping.region)?.comunas || [];
  function change(k, v) {
    setShipping((prev) => ({
      ...prev,
      [k]: v,
      ...(k === "region" ? { comuna: "" } : {}),
    }));
  }
  function submit(e) {
    e.preventDefault();
    if (busy) return;
    setBusy(true);
    try {
      const order = purchase(shipping, coupon);
      navigate("/compra-confirmada/" + order.id, { replace: true });
    } catch (err) {
      setError(err.message);
      setBusy(false);
    }
  }
  if (!cart.length)
    return (
      <>
        <h1>Finalizar compra</h1>
        <p>No tienes productos en el carrito.</p>
        <Link to="/productos">Ir al catálogo</Link>
      </>
    );
  return (
    <section className="card p-4 mx-auto form-card">
      <h1>Finalizar compra</h1>
      <p>Compra de demostración. No se realiza ningún cobro.</p>
      {error && (
        <p role="alert" className="alert alert-danger">
          {error}
        </p>
      )}
      <form onSubmit={submit}>
        <Field
          label="Dirección de despacho"
          name="direccion"
          value={shipping.direccion}
          onChange={change}
          required
          maxLength="300"
        />
        <Field
          label="Región"
          name="region"
          value={shipping.region}
          onChange={change}
          required
        >
          <option value="">Selecciona una región</option>
          {REGIONS.map((r) => (
            <option key={r.region}>{r.region}</option>
          ))}
        </Field>
        <Field
          label="Comuna"
          name="comuna"
          value={shipping.comuna}
          onChange={change}
          required
        >
          <option value="">Selecciona una comuna</option>
          {comunas.map((c) => (
            <option key={c}>{c}</option>
          ))}
        </Field>
        <Field
          label="Cupón (opcional)"
          name="coupon"
          value={coupon}
          onChange={(k, v) => setCoupon(v)}
        />
        <p className="fw-bold fs-4">Total estimado: {money(totals.total)}</p>
        <button className="btn btn-danger" disabled={busy}>
          Confirmar pedido
        </button>
      </form>
    </section>
  );
}
