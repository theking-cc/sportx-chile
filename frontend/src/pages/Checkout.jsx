import { useEffect, useState } from "react";
import { useNavigate, useLocation, Link } from "react-router-dom";
import { useShop } from "../context/ShopContext.jsx";
import Field from "../components/Field.jsx";
import { ComunaOptions } from "../components/UserFields.jsx";
import { REGIONS, COUPONS, money } from "../data/seed.js";
import { total } from "../services/cart.js";
export default function Checkout() {
  const { user, cart, purchase, remote, comunas } = useShop();
  const [shipping, setShipping] = useState({
    direccion: user.direccion || "",
    region: user.region || "",
    comuna: user.comuna || "",
    idComuna: user.idComuna || "",
  });
  // Con backend se elige la comuna por id; se precarga desde el perfil.
  useEffect(() => {
    if (!remote) return;
    setShipping((prev) => ({
      ...prev,
      direccion: prev.direccion || user.direccion || "",
      idComuna: prev.idComuna || user.idComuna || "",
    }));
  }, [remote, user.direccion, user.idComuna]);
  const location = useLocation();
  const [coupon, setCoupon] = useState(location.state?.coupon || "");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const navigate = useNavigate();
  const totals = total(
    cart,
    COUPONS.find((c) => c.codigo === coupon.trim().toUpperCase()),
  );
  const regionComunas =
    REGIONS.find((r) => r.region === shipping.region)?.comunas || [];
  function change(k, v) {
    setShipping((prev) => ({
      ...prev,
      [k]: v,
      ...(k === "region" ? { comuna: "" } : {}),
    }));
  }
  async function submit(e) {
    e.preventDefault();
    if (busy) return;
    if (remote && (!shipping.direccion.trim() || !shipping.idComuna)) {
      setError("Completa la dirección y la comuna de despacho.");
      return;
    }
    setBusy(true);
    try {
      const order = await purchase(shipping, coupon);
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
      <p>
        {remote
          ? "El pedido se registrará y el stock se descontará al confirmar. No se realiza ningún cobro."
          : "Compra de demostración. No se realiza ningún cobro."}
      </p>
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
        {remote ? (
          <Field
            label="Comuna"
            name="idComuna"
            value={String(shipping.idComuna || "")}
            onChange={change}
            required
          >
            <option value="">Selecciona una comuna</option>
            <ComunaOptions comunas={comunas} />
          </Field>
        ) : (
          <>
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
              {regionComunas.map((c) => (
                <option key={c}>{c}</option>
              ))}
            </Field>
          </>
        )}
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
