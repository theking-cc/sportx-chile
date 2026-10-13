import { Navigate, useLocation, Link } from "react-router-dom";
import { useShop } from "../context/ShopContext.jsx";
export default function ProtectedRoute({ children, roles }) {
  const { user } = useShop();
  const location = useLocation();
  if (!user)
    return (
      <Navigate
        to="/login"
        state={{ from: location.pathname, nextState: location.state }}
        replace
      />
    );
  if (roles && !roles.includes(user.rol))
    return (
      <>
        <h1>Acceso restringido</h1>
        <p>Tu cuenta no tiene permisos para esta sección.</p>
        <Link to="/">Volver al inicio</Link>
      </>
    );
  return children;
}
