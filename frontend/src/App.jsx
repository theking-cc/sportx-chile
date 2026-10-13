import { Routes, Route, Link, useLocation } from "react-router-dom";
import { useEffect } from "react";
import Navbar from "./components/Navbar.jsx";
import ProtectedRoute from "./components/ProtectedRoute.jsx";
import { useShop } from "./context/ShopContext.jsx";
import Home from "./pages/Home.jsx";
import Catalog from "./pages/Catalog.jsx";
import Detail from "./pages/Detail.jsx";
import Cart from "./pages/Cart.jsx";
import Checkout from "./pages/Checkout.jsx";
import Confirmation from "./pages/Confirmation.jsx";
import About from "./pages/About.jsx";
import Blog from "./pages/Blog.jsx";
import Article from "./pages/Article.jsx";
import Contact from "./pages/Contact.jsx";
import Login from "./pages/Login.jsx";
import Register from "./pages/Register.jsx";
import Profile from "./pages/Profile.jsx";
import Orders from "./pages/Orders.jsx";
import OrderDetail from "./pages/OrderDetail.jsx";
import AdminDashboard from "./pages/AdminDashboard.jsx";
import AdminProducts from "./pages/AdminProducts.jsx";
import ProductForm from "./pages/ProductForm.jsx";
import AdminUsers from "./pages/AdminUsers.jsx";
import UserForm from "./pages/UserForm.jsx";
import AdminOrders from "./pages/AdminOrders.jsx";

export const VIEW_ROUTES = [
  ["/", Home],
  ["/productos", Catalog],
  ["/productos/:id", Detail],
  ["/carrito", Cart],
  ["/finalizar-compra", Checkout, "user"],
  ["/compra-confirmada/:id", Confirmation, "user"],
  ["/nosotros", About],
  ["/blogs", Blog],
  ["/blogs/:id", Article],
  ["/contacto", Contact],
  ["/login", Login],
  ["/registro", Register],
  ["/perfil", Profile, "user"],
  ["/pedidos", Orders, "user"],
  ["/pedidos/:id", OrderDetail, "user"],
  ["/admin", AdminDashboard, "staff"],
  ["/admin/productos", AdminProducts, "staff"],
  ["/admin/productos/:id?/editar", ProductForm, "staff"],
  ["/admin/usuarios", AdminUsers, "admin"],
  ["/admin/usuarios/:id?/editar", UserForm, "admin"],
  ["/admin/pedidos", AdminOrders, "staff"],
];
function Page({ Component, access }) {
  const location = useLocation();
  const roles =
    access === "staff"
      ? ["Administrador", "Vendedor"]
      : access === "admin"
        ? ["Administrador"]
        : undefined;
  const page = <Component key={location.pathname} />;
  return access ? <ProtectedRoute roles={roles}>{page}</ProtectedRoute> : page;
}
export default function App() {
  const { message, dismiss } = useShop();
  const location = useLocation();
  useEffect(() => {
    window.scrollTo(0, 0);
  }, [location.pathname]);
  return (
    <div className="d-flex flex-column min-vh-100">
      <Navbar />
      <main id="main-content" className="container py-4 py-md-5 flex-grow-1">
        {message && (
          <div
            role="status"
            className="alert alert-info d-flex justify-content-between align-items-center"
          >
            {message}
            <button
              className="btn-close"
              aria-label="Cerrar mensaje"
              onClick={dismiss}
            />
          </div>
        )}
        <Routes>
          {VIEW_ROUTES.map(([path, Component, access]) => (
            <Route
              key={path}
              path={path}
              element={<Page Component={Component} access={access} />}
            />
          ))}
          <Route
            path="/admin/productos/nuevo"
            element={<Page Component={ProductForm} access="staff" />}
          />
          <Route
            path="/admin/usuarios/nuevo"
            element={<Page Component={UserForm} access="admin" />}
          />
          <Route
            path="*"
            element={
              <>
                <h1>Página no encontrada</h1>
                <Link to="/">Volver al inicio</Link>
              </>
            }
          />
        </Routes>
      </main>
      <footer className="bg-dark text-white py-4 text-center">
        Sport<span className="text-danger">X</span> Chile · Equipamiento
        deportivo
        <p className="small mb-0">
          Proyecto académico · Compras de demostración
        </p>
      </footer>
    </div>
  );
}
