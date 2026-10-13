import { Link } from "react-router-dom";
import { useShop } from "../context/ShopContext.jsx";
import { money, fallbackImage } from "../data/seed.js";
export default function ProductCard({ product }) {
  const { add } = useShop();
  return (
    <div className="col-sm-6 col-lg-4">
      <article className="card h-100 border-0 shadow-sm">
        <img
          src={product.imagen || fallbackImage}
          onError={(e) => {
            e.currentTarget.onerror = null;
            e.currentTarget.src = fallbackImage;
          }}
          alt={product.nombre}
          className="card-img-top product-image"
        />
        <div className="card-body d-flex flex-column">
          <small className="text-danger">{product.categoria}</small>
          <h2 className="h5 mt-2">
            <Link
              className="text-dark text-decoration-none"
              to={"/productos/" + product.id}
            >
              {product.nombre}
            </Link>
          </h2>
          <p className="text-muted small">{product.descripcion}</p>
          <p className="fw-bold mt-auto">{money(product.precio)}</p>
          <p
            className={
              product.stock <= product.stockCritico
                ? "text-danger small"
                : "text-muted small"
            }
          >
            {product.stock === 0
              ? "Sin stock"
              : `Disponible: ${product.stock} unidades`}
          </p>
          <button
            className="btn btn-danger"
            disabled={product.stock <= 0}
            onClick={() => add(product.id)}
          >
            Agregar al carrito
          </button>
        </div>
      </article>
    </div>
  );
}
