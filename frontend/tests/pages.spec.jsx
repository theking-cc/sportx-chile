// Fixtures exclusivamente ficticios de testing; no contienen credenciales reales.
import React from "react";
import { render, screen, fireEvent, cleanup } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import App from "../src/App";
import { ShopProvider } from "../src/context/ShopContext";
import * as repo from "../src/services/repository";
function seed(role = "Administrador") {
  const db = repo.loadDatabase();
  repo.saveDatabase(db);
  const user = db.users.find((u) => u.rol === role);
  localStorage.setItem(repo.SESSION_KEY, JSON.stringify({ id: user.id }));
  return { db, user };
}
function mount(path) {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <ShopProvider>
        <App />
      </ShopProvider>
    </MemoryRouter>,
  );
}
function field(name, value) {
  fireEvent.change(screen.getByLabelText(name, { exact: true }), {
    target: { value },
  });
}
function state() {
  return JSON.parse(localStorage.getItem(repo.DATABASE_KEY));
}
beforeEach(() => {
  localStorage.clear();
  spyOn(window, "scrollTo");
});
afterEach(() => {
  cleanup();
  localStorage.clear();
});
describe("Vistas, navegación y formularios", () => {
  it("muestra contenido de inicio, nosotros y blog y permite abrir artículos", () => {
    mount("/");
    expect(
      screen.getByRole("heading", { name: "Productos destacados" }),
    ).toBeTruthy();
    fireEvent.click(
      screen.getByRole("link", { name: "Nosotros", exact: true }),
    );
    expect(
      screen.getByRole("heading", { name: "Nuestra misión" }),
    ).toBeTruthy();
    fireEvent.click(screen.getByRole("link", { name: "Blogs", exact: true }));
    fireEvent.click(screen.getAllByRole("link", { name: "Leer artículo" })[0]);
    expect(
      screen.getByRole("heading", {
        name: "Cómo elegir tus primeros guantes de boxeo",
      }),
    ).toBeTruthy();
  });
  it("registra un contacto y lo muestra en el panel administrativo", () => {
    seed();
    mount("/contacto");
    field("Nombre", "Consulta");
    field("Correo electrónico", "consulta@gmail.com");
    field("Mensaje", "Mi consulta de prueba");
    fireEvent.click(screen.getByRole("button", { name: "Guardar consulta" }));
    expect(state().messages.length).toBe(1);
    fireEvent.click(screen.getByRole("link", { name: "Admin", exact: true }));
    expect(screen.getByText("Mi consulta de prueba")).toBeTruthy();
  });
  it("crea y edita un producto y permite eliminarlo desde la tabla", () => {
    seed();
    mount("/admin/productos/nuevo");
    field("Código", "TEST-001");
    field("Nombre", "Producto nuevo");
    field("Precio (CLP)", "1000");
    field("Stock", "5");
    field("Stock crítico", "1");
    field("Categoría", "Fitness");
    fireEvent.click(screen.getByRole("button", { name: "Guardar producto" }));
    expect(screen.getByText("Producto nuevo")).toBeTruthy();
    const product = state().products.find((p) => p.codigo === "TEST-001");
    cleanup();
    mount("/admin/productos/" + product.id + "/editar");
    field("Nombre", "Producto editado");
    fireEvent.click(screen.getByRole("button", { name: "Guardar producto" }));
    expect(screen.getByText("Producto editado")).toBeTruthy();
    spyOn(window, "confirm").and.returnValue(true);
    const row = screen.getByText("Producto editado").closest("tr");
    fireEvent.click(row.querySelector("button"));
    expect(state().products.some((p) => p.id === product.id)).toBeFalse();
  });
  it("valida el formulario de producto y evita guardar datos incompletos", () => {
    seed();
    mount("/admin/productos/nuevo");
    fireEvent.click(screen.getByRole("button", { name: "Guardar producto" }));
    expect(screen.getByText(/Código obligatorio/)).toBeTruthy();
    expect(state().products.length).toBe(3);
  });
  it("crea y edita un usuario y permite eliminarlo desde administración", () => {
    seed();
    mount("/admin/usuarios/nuevo");
    field("RUN (sin puntos ni guion)", "123456785");
    field("Correo electrónico", "nuevo@gmail.com");
    field("Nombre", "Nuevo");
    field("Apellidos", "Usuario");
    field("Contraseña (4 a 10 caracteres)", "1234");
    field("Región", "Región Metropolitana de Santiago");
    field("Comuna", "Santiago");
    field("Dirección", "Calle Prueba 123");
    fireEvent.click(screen.getByRole("button", { name: "Guardar usuario" }));
    const user = state().users.find((u) => u.correo === "nuevo@gmail.com");
    expect(user).toBeTruthy();
    cleanup();
    mount("/admin/usuarios/" + user.id + "/editar");
    field("Rol", "Vendedor");
    fireEvent.click(screen.getByRole("button", { name: "Guardar usuario" }));
    expect(state().users.find((u) => u.id === user.id).rol).toBe("Vendedor");
    spyOn(window, "confirm").and.returnValue(true);
    fireEvent.click(
      screen.getByText("nuevo@gmail.com").closest("tr").querySelector("button"),
    );
    expect(state().users.some((u) => u.id === user.id)).toBeFalse();
  });
  it("actualiza el perfil manteniendo la contraseña si el campo está vacío", () => {
    const { db, user } = seed("Cliente");
    user.password = "fixture"; // Valor ficticio exclusivo de esta prueba.
    repo.saveDatabase(db);
    mount("/perfil");
    field("Nombre", "Actualizado");
    fireEvent.click(screen.getByRole("button", { name: "Guardar cambios" }));
    expect(screen.getByText("Perfil actualizado.")).toBeTruthy();
    expect(state().users.find((u) => u.id === "demo-client").password).toBe(
      "fixture",
    );
  });
  it("muestra historial, confirmación y detalle del pedido y permite cancelarlo", () => {
    const { db, user } = seed("Cliente");
    db.cart = [{ ...db.products[0], cantidad: 1 }];
    const result = repo.checkout(db, user, user);
    repo.saveDatabase(result.database);
    mount("/compra-confirmada/" + result.order.id);
    expect(
      screen.getByRole("heading", { name: "¡Pedido confirmado!" }),
    ).toBeTruthy();
    fireEvent.click(
      screen.getByRole("link", { name: "Ver detalle del pedido" }),
    );
    expect(
      screen.getByRole("heading", { name: "Detalle de mi pedido" }),
    ).toBeTruthy();
    fireEvent.click(screen.getByRole("link", { name: "Volver a mis pedidos" }));
    expect(screen.getByText(result.order.number)).toBeTruthy();
    cleanup();
    localStorage.setItem(
      repo.SESSION_KEY,
      JSON.stringify({ id: "demo-admin" }),
    );
    mount("/admin/pedidos");
    field("Estado del pedido", "Cancelado");
    expect(state().orders[0].status).toBe("Cancelado");
    expect(state().products[0].stock).toBe(12);
  });
  it("restringe administración a clientes y redirige visitantes a login", () => {
    seed("Cliente");
    mount("/admin/usuarios");
    expect(
      screen.getByRole("heading", { name: "Acceso restringido" }),
    ).toBeTruthy();
    cleanup();
    localStorage.removeItem(repo.SESSION_KEY);
    mount("/perfil");
    expect(
      screen.getByRole("heading", { name: "Iniciar sesión" }),
    ).toBeTruthy();
    document.querySelector("details").open = true;
    fireEvent.click(
      screen.getByRole("button", { name: "Entrar como Cliente" }),
    );
    expect(screen.getByRole("heading", { name: "Mi perfil" })).toBeTruthy();
    fireEvent.click(screen.getByRole("button", { name: "Salir" }));
    expect(
      screen.getByRole("heading", { name: "Iniciar sesión" }),
    ).toBeTruthy();
  });
  it("muestra el detalle de producto y rechaza cantidades no válidas", () => {
    mount("/productos/PROD-001");
    field("Cantidad", "-1");
    fireEvent.click(screen.getByRole("button", { name: "Agregar al carrito" }));
    expect(screen.getByRole("status").textContent).toContain("entero positivo");
    field("Cantidad", "1");
    fireEvent.click(screen.getByRole("button", { name: "Agregar al carrito" }));
    expect(state().cart[0].cantidad).toBe(1);
  });
  it("permite registrarse con datos válidos y luego iniciar sesión", () => {
    mount("/registro");
    field("RUN (sin puntos ni guion)", "123456785");
    field("Correo electrónico", "registro@gmail.com");
    field("Nombre", "Registro");
    field("Apellidos", "Prueba");
    field("Contraseña (4 a 10 caracteres)", "1234");
    field("Confirmar contraseña", "1234");
    field("Región", "Región Metropolitana de Santiago");
    field("Comuna", "Santiago");
    field("Dirección", "Prueba 123");
    fireEvent.click(screen.getByRole("button", { name: "Crear cuenta" }));
    expect(
      screen.getByRole("heading", { name: "Iniciar sesión" }),
    ).toBeTruthy();
    field("Correo electrónico", "registro@gmail.com");
    field("Contraseña", "1234");
    fireEvent.click(screen.getByRole("button", { name: "Ingresar" }));
    expect(screen.getByRole("heading", { name: "Mi perfil" })).toBeTruthy();
  });
});
