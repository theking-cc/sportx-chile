import { useState } from "react";
import { Link, NavLink } from "react-router-dom";
import { useShop } from "../context/ShopContext.jsx";
export default function Navbar() {
  const { cart, user, signOut } = useShop();
  const [open, setOpen] = useState(false);
  const links = [
    ["/", "Inicio"],
    ["/productos", "Productos"],
    ["/nosotros", "Nosotros"],
    ["/blogs", "Blogs"],
    ["/contacto", "Contacto"],
  ];
  return (
    <header className="sticky-top bg-white shadow-sm">
      <nav className="navbar navbar-expand-lg container py-3">
        <Link to="/" className="navbar-brand fw-bold fs-3">
          Sport<span className="text-danger">X</span>
        </Link>
        <button
          className="navbar-toggler"
          aria-label="Abrir menú"
          aria-expanded={open}
          aria-controls="navigation"
          onClick={() => setOpen(!open)}
        >
          <span className="navbar-toggler-icon" />
        </button>
        <div
          id="navigation"
          className={"collapse navbar-collapse " + (open ? "show" : "")}
        >
          <div className="navbar-nav mx-auto">
            {links.map(([url, label]) => (
              <NavLink
                end={url === "/"}
                key={url}
                to={url}
                className="nav-link"
                onClick={() => setOpen(false)}
              >
                {label}
              </NavLink>
            ))}
          </div>
          <div className="d-flex flex-wrap gap-2 align-items-center">
            <Link
              to="/carrito"
              className="btn btn-outline-dark"
              onClick={() => setOpen(false)}
            >
              <i className="bi bi-cart3 me-2" />
              Carrito ({cart.reduce((n, p) => n + p.cantidad, 0)})
            </Link>
            {user ? (
              <>
                <Link
                  to="/perfil"
                  className="btn btn-light"
                  onClick={() => setOpen(false)}
                >
                  Mi perfil
                </Link>
                <Link
                  to="/pedidos"
                  className="btn btn-light"
                  onClick={() => setOpen(false)}
                >
                  Mis pedidos
                </Link>
                {user.rol !== "Cliente" && (
                  <Link
                    to="/admin"
                    className="btn btn-light"
                    onClick={() => setOpen(false)}
                  >
                    Admin
                  </Link>
                )}
                <button
                  className="btn btn-outline-danger"
                  onClick={() => {
                    signOut();
                    setOpen(false);
                  }}
                >
                  Salir
                </button>
              </>
            ) : (
              <Link
                to="/login"
                className="btn btn-danger"
                onClick={() => setOpen(false)}
              >
                Ingresar
              </Link>
            )}
          </div>
        </div>
      </nav>
    </header>
  );
}
