import { Link } from "react-router-dom";
import { useState } from "react";
import { useShop } from "../context/ShopContext.jsx";
import { deleteProduct } from "../services/repository.js";
import AdminNav from "../components/AdminNav.jsx";
import { money } from "../data/seed.js";
export default function AdminProducts() {
  const { products, changeDatabase } = useShop();
  const [error, setError] = useState("");
  function remove(p) {
    if (!window.confirm("¿Eliminar " + p.nombre + "?")) return;
    try {
      changeDatabase((db, actor) => deleteProduct(db, p.id, actor));
    } catch (err) {
      setError(err.message);
    }
  }
  return (
    <>
      <AdminNav />
      <h1>Administración de productos</h1>
      <Link className="btn btn-danger mb-3" to="/admin/productos/nuevo">
        Nuevo producto
      </Link>
      {error && <p role="alert">{error}</p>}
      <div className="table-responsive">
        <table className="table">
          <thead>
            <tr>
              <th>Código</th>
              <th>Producto</th>
              <th>Precio</th>
              <th>Stock</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {products.map((p) => (
              <tr key={p.id}>
                <td>{p.codigo}</td>
                <td>{p.nombre}</td>
                <td>{money(p.precio)}</td>
                <td
                  className={
                    p.stock <= p.stockCritico ? "text-danger fw-bold" : ""
                  }
                >
                  {p.stock}
                </td>
                <td>
                  <div className="d-flex gap-2">
                    <Link
                      className="btn btn-sm btn-outline-dark"
                      to={"/admin/productos/" + p.id + "/editar"}
                    >
                      Editar
                    </Link>
                    <button
                      className="btn btn-sm btn-outline-danger"
                      onClick={() => remove(p)}
                    >
                      Eliminar
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {!products.length && <p>No hay productos registrados.</p>}
    </>
  );
}
