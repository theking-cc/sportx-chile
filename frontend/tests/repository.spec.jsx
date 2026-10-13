// Fixtures exclusivamente ficticios de testing; no contienen credenciales reales.
import "../src/App";
import * as repo from "../src/services/repository.js";
import { validateProduct } from "../src/services/validation.js";
function candidate() {
  return {
    run: "123456785",
    nombre: "Prueba",
    apellidos: "Usuario",
    correo: "nuevo@gmail.com",
    password: "1234",
    rol: "Cliente",
    region: "Región Metropolitana de Santiago",
    comuna: "Santiago",
    direccion: "Prueba 123",
  };
}
beforeEach(() => localStorage.clear());
afterEach(() => localStorage.clear());
describe("Regresiones de datos y permisos", () => {
  it("conserva un catálogo vacío al importar localStorage anterior", () => {
    localStorage.setItem("sportx_products", "[]");
    expect(repo.loadDatabase().products.length).toBe(0);
  });
  it("rechaza registrar correo o RUN duplicados", () => {
    let db = repo.saveUser(repo.loadDatabase(), candidate(), null, {
      register: true,
    });
    expect(() =>
      repo.saveUser(db, candidate(), null, { register: true }),
    ).toThrowError(/Ya existe/);
  });
  it("el registro público siempre asigna rol Cliente", () => {
    const db = repo.saveUser(
      repo.loadDatabase(),
      { ...candidate(), rol: "Administrador" },
      null,
      { register: true },
    );
    expect(db.users.at(-1).rol).toBe("Cliente");
  });
  it("un vendedor no puede crear usuarios", () => {
    const db = repo.loadDatabase();
    expect(() =>
      repo.saveUser(
        db,
        candidate(),
        db.users.find((u) => u.rol === "Vendedor"),
      ),
    ).toThrowError(/administrador/);
  });
  it("el administrador no puede eliminar su propia cuenta", () => {
    const db = repo.loadDatabase();
    const admin = db.users.find((u) => u.rol === "Administrador");
    expect(() => repo.deleteUser(db, admin.id, admin)).toThrowError(
      /propia cuenta/,
    );
  });
  it("rechaza stock decimal y negativo", () => {
    const p = repo.loadDatabase().products[0];
    expect(validateProduct({ ...p, stock: 1.5 }).stock).toBeTruthy();
    expect(validateProduct({ ...p, stock: -1 }).stock).toBeTruthy();
  });
  it("checkout rechaza inventario insuficiente sin modificar los datos", () => {
    const db = repo.loadDatabase();
    db.cart = [{ ...db.products[0], cantidad: 50 }];
    const user = db.users.find((u) => u.rol === "Cliente");
    expect(() => repo.checkout(db, user, user)).toThrowError(
      /Stock insuficiente/,
    );
    expect(db.products[0].stock).toBe(12);
    expect(db.orders.length).toBe(0);
  });
  it("cancelar un pedido devuelve el inventario una sola vez", () => {
    const db = repo.loadDatabase();
    db.cart = [{ ...db.products[0], cantidad: 2 }];
    const user = db.users.find((u) => u.rol === "Cliente");
    const { database, order } = repo.checkout(db, user, user);
    const cancelled = repo.updateOrder(
      database,
      order.id,
      "Cancelado",
      db.users[0],
    );
    expect(cancelled.products[0].stock).toBe(12);
    expect(() =>
      repo.updateOrder(cancelled, order.id, "Cancelado", db.users[0]),
    ).toThrowError(/no está permitido/);
  });
  it("conserva el precio original de un pedido al editar el catálogo", () => {
    const db = repo.loadDatabase();
    db.cart = [{ ...db.products[0], cantidad: 1 }];
    const user = db.users.find((u) => u.rol === "Cliente");
    const { database } = repo.checkout(db, user, user);
    const next = repo.saveProduct(
      database,
      { ...database.products[0], precio: 1 },
      db.users[0],
    );
    expect(next.orders[0].items[0].precio).toBe(34990);
  });
  it("una falla de escritura no borra el almacenamiento anterior", () => {
    const db = repo.loadDatabase();
    repo.saveDatabase(db);
    const prior = localStorage.getItem(repo.DATABASE_KEY);
    spyOn(Storage.prototype, "setItem").and.throwError("QuotaExceededError");
    expect(() => repo.saveDatabase({ ...db, cart: [] })).toThrowError(
      "QuotaExceededError",
    );
    expect(localStorage.getItem(repo.DATABASE_KEY)).toBe(prior);
  });
});
