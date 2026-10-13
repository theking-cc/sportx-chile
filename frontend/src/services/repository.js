import { PRODUCTS, COUPONS, REGIONS } from "../data/seed.js";
import { read, write } from "./storage.js";
import { addItem, total } from "./cart.js";
import { validateUser, validateProduct } from "./validation.js";

// Un único documento permite confirmar pedido, inventario y carrito en una sola escritura.
export const DATABASE_KEY = "sportx_state_v2";
export const SESSION_KEY = "sportx_session_v2";
// Identidades ficticias del prototipo. No representan personas ni usan contraseñas.
const DEMO_USERS = [
  {
    id: "demo-admin",
    run: "111111111",
    nombre: "Administrador",
    apellidos: "SportX",
    correo: "admin@duoc.cl",
    password: null,
    demo: true,
    rol: "Administrador",
  },
  {
    id: "demo-seller",
    run: "222222222",
    nombre: "Vendedor",
    apellidos: "SportX",
    correo: "vendedor@duoc.cl",
    password: null,
    demo: true,
    rol: "Vendedor",
  },
  {
    id: "demo-client",
    run: "333333333",
    nombre: "Cliente",
    apellidos: "SportX",
    correo: "cliente@gmail.com",
    password: null,
    demo: true,
    rol: "Cliente",
  },
].map((u) => ({
  region: "Región Metropolitana de Santiago",
  comuna: "Santiago",
  direccion: "Dirección de demostración 123",
  telefono: "",
  fechaNacimiento: "",
  ...u,
}));
const roles = ["Cliente", "Vendedor", "Administrador"];
export const ORDER_STATES = [
  "Pendiente",
  "Preparando",
  "Enviado",
  "Entregado",
  "Cancelado",
];
const id = () => crypto.randomUUID();
function assertArray(value, label) {
  if (!Array.isArray(value))
    throw new Error(
      "Datos inválidos: " + label + ". No se ha borrado tu almacenamiento.",
    );
  return value;
}
export function loadDatabase() {
  const saved = read(DATABASE_KEY, null);
  if (saved) {
    if (saved.version !== 2) throw new Error("Versión de datos desconocida.");
    for (const key of ["products", "users", "cart", "orders", "messages"])
      assertArray(saved[key], key);
    return saved;
  }
  const products = assertArray(read("sportx_products", PRODUCTS), "productos");
  const oldUsers = assertArray(read("sportx_users", []), "usuarios");
  const users = oldUsers.map((u) => ({
    ...u,
    id: u.id || u.run || u.correo,
    rol: roles.includes(u.rol) ? u.rol : "Cliente",
  }));
  for (const demo of DEMO_USERS)
    if (!users.some((u) => u.correo?.toLowerCase() === demo.correo))
      users.push(demo);
  return {
    version: 2,
    products,
    users,
    cart: assertArray(read("sportx_cart_items", []), "carrito"),
    orders: [],
    messages: [],
  };
}
export function saveDatabase(db) {
  write(DATABASE_KEY, db);
  return db;
}
export function sessionUser(db) {
  const session = read(SESSION_KEY, null);
  return db.users.find((u) => u.id === session?.id) || null;
}
export function login(db, correo, password) {
  const user = db.users.find(
    (u) =>
      u.correo.toLowerCase() === correo.trim().toLowerCase() &&
      !!u.password &&
      u.password === password,
  );
  if (!user) throw new Error("Correo o contraseña incorrectos.");
  write(SESSION_KEY, { id: user.id });
  return user;
}
// Acceso explícito de demostración: no utiliza ni publica contraseñas.
export function loginDemo(db, userId) {
  const user = db.users.find((u) => u.id === userId && u.demo === true);
  if (!user) throw new Error("La cuenta de demostración no está disponible.");
  write(SESSION_KEY, { id: user.id });
  return user;
}
export function logout() {
  localStorage.removeItem(SESSION_KEY);
}
function requireStaff(actor) {
  if (!actor || !["Administrador", "Vendedor"].includes(actor.rol))
    throw new Error("No tienes permiso para esta acción.");
}
function requireAdmin(actor) {
  if (actor?.rol !== "Administrador")
    throw new Error("Solo el administrador puede gestionar usuarios.");
}
function ensureValid(errors) {
  if (Object.keys(errors).length) throw new Error(Object.values(errors)[0]);
}
export function saveUser(
  db,
  input,
  actor,
  { register = false, profile = false } = {},
) {
  if (!register && !profile) requireAdmin(actor);
  if (profile && (!actor || input.id !== actor.id))
    throw new Error("No puedes editar este perfil.");
  const previous = db.users.find((u) => u.id === input.id);
  if (input.id && !previous) throw new Error("Usuario no encontrado.");
  const user = {
    ...input,
    run: input.run.trim().toUpperCase(),
    correo: input.correo.trim().toLowerCase(),
    nombre: input.nombre.trim(),
    apellidos: input.apellidos.trim(),
    direccion: input.direccion.trim(),
  };
  if (profile || register) user.rol = profile ? previous.rol : "Cliente";
  if (!roles.includes(user.rol)) throw new Error("Rol inválido.");
  ensureValid(validateUser(user, { passwordRequired: !previous }));
  if (previous && !user.password) user.password = previous.password;
  if (
    db.users.some(
      (u) =>
        u.id !== user.id &&
        (u.run === user.run || u.correo.toLowerCase() === user.correo),
    )
  )
    throw new Error("Ya existe un usuario con ese RUN o correo.");
  if (
    previous?.rol === "Administrador" &&
    user.rol !== "Administrador" &&
    db.users.filter((u) => u.rol === "Administrador").length === 1
  )
    throw new Error("Debe quedar al menos un administrador.");
  user.id = previous?.id || id();
  return {
    ...db,
    users: previous
      ? db.users.map((u) => (u.id === user.id ? user : u))
      : [...db.users, user],
  };
}
export function deleteUser(db, userId, actor) {
  requireAdmin(actor);
  if (userId === actor.id)
    throw new Error("No puedes eliminar tu propia cuenta.");
  const user = db.users.find((u) => u.id === userId);
  if (
    user?.rol === "Administrador" &&
    db.users.filter((u) => u.rol === "Administrador").length === 1
  )
    throw new Error("Debe quedar al menos un administrador.");
  return { ...db, users: db.users.filter((u) => u.id !== userId) };
}
export function saveProduct(db, input, actor) {
  requireStaff(actor);
  ensureValid(validateProduct(input));
  if (input.id && !db.products.some((p) => p.id === input.id))
    throw new Error("Producto no encontrado.");
  if (
    db.products.some(
      (p) =>
        p.id !== input.id &&
        p.codigo.toLowerCase() === input.codigo.trim().toLowerCase(),
    )
  )
    throw new Error("Ya existe ese código de producto.");
  const product = {
    ...input,
    id: input.id || id(),
    codigo: input.codigo.trim(),
    nombre: input.nombre.trim(),
    precio: Number(input.precio),
    stock: Number(input.stock),
    stockCritico: Number(input.stockCritico),
    imagen: input.imagen || "",
  };
  return {
    ...db,
    products: input.id
      ? db.products.map((p) => (p.id === input.id ? product : p))
      : [...db.products, product],
  };
}
export function deleteProduct(db, productId, actor) {
  requireStaff(actor);
  return {
    ...db,
    products: db.products.filter((p) => p.id !== productId),
    cart: db.cart.filter((i) => i.id !== productId),
  };
}
export function addCart(db, productId, quantity) {
  const product = db.products.find((p) => p.id === productId);
  if (!product) throw new Error("El producto no está disponible.");
  return { ...db, cart: addItem(db.cart, product, quantity) };
}
export function checkout(db, actor, shipping, couponCode = "") {
  if (!actor) throw new Error("Inicia sesión para realizar la compra.");
  if (!db.cart.length) throw new Error("El carrito está vacío.");
  if (
    !shipping.direccion?.trim() ||
    shipping.direccion.length > 300 ||
    !REGIONS.find((r) => r.region === shipping.region)?.comunas.includes(
      shipping.comuna,
    )
  )
    throw new Error("Completa la dirección de despacho.");
  const items = db.cart.map((item) => {
    const p = db.products.find((p) => p.id === item.id);
    if (
      !p ||
      !Number.isInteger(item.cantidad) ||
      item.cantidad < 1 ||
      item.cantidad > p.stock
    )
      throw new Error("Stock insuficiente para " + item.nombre);
    return { ...item, nombre: p.nombre, precio: p.precio };
  });
  const coupon = COUPONS.find(
    (c) => c.codigo === couponCode.trim().toUpperCase(),
  );
  if (couponCode && !coupon) throw new Error("Cupón inválido.");
  const order = {
    id: id(),
    number:
      "SX-" + Date.now() + "-" + String(db.orders.length + 1).padStart(3, "0"),
    userId: actor.id,
    cliente: actor.nombre + " " + actor.apellidos,
    correo: actor.correo,
    createdAt: new Date().toISOString(),
    status: "Pendiente",
    items,
    shipping: { ...shipping, direccion: shipping.direccion.trim() },
    coupon: coupon?.codigo || "",
    ...total(items, coupon),
  };
  const products = db.products.map((p) => ({
    ...p,
    stock: p.stock - (items.find((i) => i.id === p.id)?.cantidad || 0),
  }));
  return {
    database: { ...db, products, orders: [order, ...db.orders], cart: [] },
    order,
  };
}
export function updateOrder(db, orderId, status, actor) {
  requireStaff(actor);
  const order = db.orders.find((o) => o.id === orderId);
  if (!order) throw new Error("Pedido no encontrado.");
  const transitions = {
    Pendiente: ["Preparando", "Cancelado"],
    Preparando: ["Enviado", "Cancelado"],
    Enviado: ["Entregado"],
    Entregado: [],
    Cancelado: [],
  };
  if (
    !ORDER_STATES.includes(status) ||
    !transitions[order.status]?.includes(status)
  )
    throw new Error("El cambio de estado no está permitido.");
  const products =
    status === "Cancelado"
      ? db.products.map((p) => ({
          ...p,
          stock:
            p.stock + (order.items.find((i) => i.id === p.id)?.cantidad || 0),
        }))
      : db.products;
  return {
    ...db,
    products,
    orders: db.orders.map((o) => (o.id === orderId ? { ...o, status } : o)),
  };
}
export function addMessage(db, input) {
  if (
    !input.nombre.trim() ||
    !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(input.correo) ||
    !input.mensaje.trim() ||
    input.mensaje.length > 1000
  )
    throw new Error(
      "Completa nombre, correo válido y mensaje de hasta 1000 caracteres.",
    );
  return {
    ...db,
    messages: [
      { ...input, id: id(), createdAt: new Date().toISOString() },
      ...db.messages,
    ],
  };
}
export function exportDatabase(db) {
  return JSON.stringify(
    { exportedAt: new Date().toISOString(), ...db },
    null,
    2,
  );
}
