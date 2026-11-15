import { Link, useParams } from "react-router-dom";
import { useShop } from "../context/ShopContext.jsx";
import AdminNav from "../components/AdminNav.jsx";
import { money } from "../data/seed.js";
// Ficha de producto del panel: solo lectura para Vendedor y Administrador.
export default function AdminProductDetail() {
  const { id } = useParams();
  const { products, user } = useShop();
  const p = products.find((x) => String(x.id) === String(id));
  if (!p)
    return (
      <>
        <AdminNav />
        <h1>Producto no encontrado</h1>
        <Link to="/admin/productos">Volver a productos</Link>
      </>
    );
  const critico = p.stock <= p.stockCritico;
  return (
    <>
      <AdminNav />
      <Link to="/admin/productos" className="d-inline-block mb-3">
        ← Volver a productos
      </Link>
      <h1>{p.nombre}</h1>
      <dl className="row">
        {[
          ["Código", p.codigo],
          ["Categoría", p.categoria],
          ["Precio", money(p.precio)],
          ["Stock", p.stock],
          ["Stock crítico", p.stockCritico],
          ["Descripción", p.descripcion || "—"],
        ].map(([k, v]) => (
          <div key={k} className="col-sm-6 col-lg-4 mb-2">
            <dt className="small text-muted">{k}</dt>
            <dd
              className={k === "Stock" && critico ? "text-danger fw-bold" : ""}
            >
              {v}
            </dd>
          </div>
        ))}
      </dl>
      {critico && (
        <p className="alert alert-warning">Stock bajo el nivel crítico.</p>
      )}
      {user?.rol === "Administrador" && (
        <Link
          className="btn btn-danger"
          to={"/admin/productos/" + p.id + "/editar"}
        >
          Editar producto
        </Link>
      )}
    </>
  );
}
