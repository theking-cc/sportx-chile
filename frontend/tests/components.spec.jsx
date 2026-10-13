// Fixtures exclusivamente ficticios de testing; no contienen credenciales reales.
import React from "react";
import { render, screen, fireEvent, cleanup } from "@testing-library/react";
import { MemoryRouter, Routes, Route } from "react-router-dom";
import { ShopProvider } from "../src/context/ShopContext.jsx";
import ProductCard from "../src/components/ProductCard.jsx";
import Catalog from "../src/pages/Catalog.jsx";
import Cart from "../src/pages/Cart.jsx";
import Register from "../src/pages/Register.jsx";
import Login from "../src/pages/Login.jsx";
import Checkout from "../src/pages/Checkout.jsx";
import ProtectedRoute from "../src/components/ProtectedRoute.jsx";
import Contact from "../src/pages/Contact.jsx";
import {
  DATABASE_KEY,
  SESSION_KEY,
  loadDatabase,
  saveDatabase,
} from "../src/services/repository.js";
function mount(element, destinations = []) {
  return render(
    <MemoryRouter>
      <ShopProvider>
        <Routes>
          <Route path="/" element={element} />
          {destinations.map(([path, text]) => (
            <Route key={path} path={path} element={<h1>{text}</h1>} />
          ))}
        </Routes>
      </ShopProvider>
    </MemoryRouter>,
  );
}
function stored() {
  return JSON.parse(localStorage.getItem(DATABASE_KEY));
}
beforeEach(() => {
  localStorage.clear();
  window.IS_REACT_ACT_ENVIRONMENT = true;
});
afterEach(() => {
  cleanup();
  localStorage.clear();
});
describe("Componentes SportX: diez pruebas Jasmine/Karma", () => {
  it("01 muestra nombre, categoría y precio recibidos por propiedades", () => {
    const p = loadDatabase().products[0];
    mount(<ProductCard product={p} />);
    expect(screen.getByRole("link", { name: p.nombre })).toBeTruthy();
    expect(screen.getByText("Boxeo")).toBeTruthy();
    expect(screen.getByText(/34.990/)).toBeTruthy();
  });
  it("02 filtra el catálogo por texto y categoría y muestra resultado vacío", () => {
    mount(<Catalog />);
    fireEvent.change(screen.getByLabelText("Buscar producto"), {
      target: { value: "Cuerda" },
    });
    expect(
      screen.getAllByRole("button", { name: "Agregar al carrito" }).length,
    ).toBe(1);
    fireEvent.change(screen.getByLabelText("Categoría"), {
      target: { value: "Boxeo" },
    });
    expect(screen.getByRole("status").textContent).toContain("No encontramos");
  });
  it("03 agregar desde una tarjeta persiste el carrito usando un spy de almacenamiento", () => {
    const spy = spyOn(Storage.prototype, "setItem").and.callThrough();
    mount(<ProductCard product={loadDatabase().products[0]} />);
    fireEvent.click(screen.getByRole("button", { name: "Agregar al carrito" }));
    expect(spy).toHaveBeenCalledWith(DATABASE_KEY, jasmine.any(String));
    expect(stored().cart[0].cantidad).toBe(1);
  });
  it("04 agregar el mismo producto acumula cantidades en una sola fila", () => {
    mount(<ProductCard product={loadDatabase().products[0]} />);
    const button = screen.getByRole("button", { name: "Agregar al carrito" });
    fireEvent.click(button);
    fireEvent.click(button);
    expect(stored().cart.length).toBe(1);
    expect(stored().cart[0].cantidad).toBe(2);
  });
  it("05 aumentar cantidades no supera el stock disponible", () => {
    const db = loadDatabase();
    const p = db.products[1];
    db.cart = [{ ...p, cantidad: 2 }];
    saveDatabase(db);
    mount(<Cart />);
    fireEvent.click(screen.getByRole("button", { name: /Aumentar/ }));
    expect(stored().cart[0].cantidad).toBe(2);
  });
  it("06 eliminar un producto actualiza el DOM y el carrito guardado", () => {
    const db = loadDatabase();
    db.cart = [{ ...db.products[0], cantidad: 1 }];
    saveDatabase(db);
    mount(<Cart />);
    fireEvent.click(screen.getByRole("button", { name: "Eliminar" }));
    expect(screen.getByText(/Tu carrito está vacío/)).toBeTruthy();
    expect(stored().cart.length).toBe(0);
  });
  it("07 aplica un cupón válido y elimina el descuento si se reemplaza por uno inválido", () => {
    const db = loadDatabase();
    db.cart = [{ ...db.products[0], cantidad: 1 }];
    saveDatabase(db);
    mount(<Cart />);
    fireEvent.change(screen.getByLabelText("Cupón de descuento"), {
      target: { value: "DUOC10" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Aplicar" }));
    expect(screen.getByText(/Cupón DUOC10 aplicado/)).toBeTruthy();
    expect(screen.getByText(/Total:.*31.491/)).toBeTruthy();
    fireEvent.change(screen.getByLabelText("Cupón de descuento"), {
      target: { value: "NOEXISTE" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Aplicar" }));
    expect(screen.getByRole("alert").textContent).toContain("Cupón inválido");
    expect(screen.getByText(/Total:.*34.990/)).toBeTruthy();
  });
  it("08 registro inválido muestra errores y no crea un usuario", () => {
    mount(<Register />);
    fireEvent.click(screen.getByRole("button", { name: "Crear cuenta" }));
    expect(screen.getByText(/RUN válido/)).toBeTruthy();
    expect(screen.getByText(/Nombre obligatorio/)).toBeTruthy();
    expect(localStorage.getItem(DATABASE_KEY)).toBeNull();
  });
  it("09 login rechaza credenciales erróneas y navega con credenciales simuladas válidas", () => {
    const db = loadDatabase();
    db.users = [
      {
        id: "mock-user",
        correo: "mock@gmail.com",
        password: "1234",
        nombre: "Mock",
        rol: "Cliente",
      },
    ];
    saveDatabase(db);
    mount(<Login />, [["/perfil", "Perfil de prueba"]]);
    fireEvent.change(screen.getByLabelText("Correo electrónico"), {
      target: { value: "mock@gmail.com" },
    });
    fireEvent.change(screen.getByLabelText("Contraseña"), {
      target: { value: "wrong" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Ingresar" }));
    expect(screen.getByRole("alert").textContent).toContain("incorrectos");
    fireEvent.change(screen.getByLabelText("Contraseña"), {
      target: { value: "1234" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Ingresar" }));
    expect(
      screen.getByRole("heading", { name: "Perfil de prueba" }),
    ).toBeTruthy();
    expect(JSON.parse(localStorage.getItem(SESSION_KEY)).id).toBe("mock-user");
  });
  it("10 confirmar compra guarda el pedido, descuenta inventario y vacía el carrito", () => {
    const db = loadDatabase();
    db.cart = [{ ...db.products[0], cantidad: 2 }];
    saveDatabase(db);
    localStorage.setItem(SESSION_KEY, JSON.stringify({ id: "demo-client" }));
    mount(<Checkout />, [["/compra-confirmada/:id", "Compra confirmada"]]);
    fireEvent.click(screen.getByRole("button", { name: "Confirmar pedido" }));
    expect(
      screen.getByRole("heading", { name: "Compra confirmada" }),
    ).toBeTruthy();
    expect(stored().orders.length).toBe(1);
    expect(stored().products[0].stock).toBe(10);
    expect(stored().cart.length).toBe(0);
  });
});
