import { useState } from "react";
import { Link } from "react-router-dom";
import { useShop } from "../context/ShopContext.jsx";
import { deleteUser } from "../services/repository.js";
import AdminNav from "../components/AdminNav.jsx";
export default function AdminUsers() {
  const { db, changeDatabase } = useShop();
  const [error, setError] = useState("");
  function remove(user) {
    if (!window.confirm("¿Eliminar a " + user.nombre + "?")) return;
    try {
      changeDatabase((db, actor) => deleteUser(db, user.id, actor));
    } catch (err) {
      setError(err.message);
    }
  }
  return (
    <>
      <AdminNav />
      <h1>Administración de usuarios</h1>
      <Link className="btn btn-danger mb-3" to="/admin/usuarios/nuevo">
        Nuevo usuario
      </Link>
      {error && (
        <p role="alert" className="alert alert-danger">
          {error}
        </p>
      )}
      <div className="table-responsive">
        <table className="table">
          <thead>
            <tr>
              <th>Nombre</th>
              <th>Correo</th>
              <th>Rol</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {db.users.map((u) => (
              <tr key={u.id}>
                <td>
                  {u.nombre} {u.apellidos}
                </td>
                <td>{u.correo}</td>
                <td>{u.rol}</td>
                <td>
                  <div className="d-flex gap-2">
                    <Link
                      className="btn btn-sm btn-outline-dark"
                      to={"/admin/usuarios/" + u.id + "/editar"}
                    >
                      Editar
                    </Link>
                    <button
                      className="btn btn-sm btn-outline-danger"
                      onClick={() => remove(u)}
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
    </>
  );
}
