import { NavLink } from "react-router-dom";
import { useShop } from "../context/ShopContext.jsx";
export default function AdminNav() {
  const { user } = useShop();
  return (
    <nav aria-label="Administración" className="d-flex flex-wrap gap-2 mb-4">
      {[
        ["/admin", "Panel"],
        ["/admin/productos", "Productos"],
        ...(user?.rol === "Administrador"
          ? [["/admin/usuarios", "Usuarios"]]
          : []),
        ["/admin/pedidos", "Pedidos"],
      ].map(([url, label]) => (
        <NavLink key={url} end to={url} className="btn btn-outline-dark">
          {label}
        </NavLink>
      ))}
    </nav>
  );
}
