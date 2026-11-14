// Cliente HTTP para el backend Spring Boot (sportx-api).
// En desarrollo Vite redirige /api a http://localhost:8080 (ver vite.config.js).
// Para otro servidor define VITE_API_URL, por ejemplo: VITE_API_URL=http://localhost:8080/api
const BASE_URL = (import.meta.env?.VITE_API_URL || "/api").replace(/\/$/, "");
export const AUTH_KEY = "sportx_auth";

export class ApiError extends Error {
  constructor(message, status) {
    super(message);
    this.status = status;
  }
}

// ---------- Sesión (token JWT) ----------
function decodeExp(token) {
  try {
    const payload = JSON.parse(
      atob(token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/")),
    );
    return payload.exp ? payload.exp * 1000 : null;
  } catch {
    return null;
  }
}
export function getAuth() {
  try {
    const auth = JSON.parse(localStorage.getItem(AUTH_KEY));
    if (!auth?.token) return null;
    const exp = decodeExp(auth.token);
    if (exp && exp < Date.now()) {
      clearAuth();
      return null;
    }
    return auth;
  } catch {
    return null;
  }
}
export function setAuth(auth) {
  localStorage.setItem(AUTH_KEY, JSON.stringify(auth));
}
export function clearAuth() {
  localStorage.removeItem(AUTH_KEY);
}

// ---------- Petición genérica ----------
const STATUS_MESSAGES = {
  401: "Tu sesión no es válida o expiró. Inicia sesión nuevamente.",
  403: "No tienes permiso para realizar esta acción.",
  404: "Recurso no encontrado.",
};
export async function request(
  path,
  { method = "GET", body, auth = true } = {},
) {
  const headers = { Accept: "application/json" };
  if (body !== undefined) headers["Content-Type"] = "application/json";
  const token = auth ? getAuth()?.token : null;
  if (token) headers.Authorization = "Bearer " + token;
  let response;
  try {
    response = await fetch(BASE_URL + path, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch {
    throw new ApiError(
      "No se pudo conectar con el servidor. Verifica que el backend esté en ejecución.",
      0,
    );
  }
  if (response.status === 204) return null;
  const text = await response.text();
  let data = null;
  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    data = null;
  }
  if (!response.ok) {
    if (response.status === 401 && token) clearAuth();
    throw new ApiError(
      data?.message ||
        STATUS_MESSAGES[response.status] ||
        "Error del servidor (" + response.status + ").",
      response.status,
    );
  }
  return data;
}

// ---------- Conversión entre el formato del backend y el del frontend ----------
export function toProduct(p) {
  return {
    id: String(p.id),
    codigo: p.codigo,
    nombre: p.nombre,
    descripcion: p.descripcion || "",
    precio: Number(p.precio),
    stock: p.stock,
    stockCritico: p.stockCritico,
    imagen: p.imagenUrl || "",
    categoria: p.categoria,
    categoriaId: p.categoriaId,
  };
}
function toProductRequest(form) {
  return {
    codigo: form.codigo.trim(),
    nombre: form.nombre.trim(),
    descripcion: form.descripcion || "",
    precio: Number(form.precio),
    stock: Number(form.stock),
    stockCritico: Number(form.stockCritico),
    imagenUrl: form.imagen || null,
    categoriaId: Number(form.categoriaId),
  };
}
function toUser(r) {
  return {
    id: String(r.id),
    nombre: r.nombre,
    apellidos: r.apellidos,
    correo: r.email,
    rol: r.rol,
    remote: true,
  };
}
export function toCart(c, products = []) {
  return (c?.productos || []).map((i) => ({
    id: String(i.idProducto),
    idDetalle: i.idDetalle,
    nombre: i.nombre,
    precio: Number(i.precioUnitario),
    cantidad: i.cantidad,
    imagen: products.find((p) => p.id === String(i.idProducto))?.imagen || "",
  }));
}

// ---------- Endpoints ----------
export const api = {
  async login(email, password) {
    const r = await request("/auth/login", {
      method: "POST",
      body: { email: email.trim(), password },
      auth: false,
    });
    const user = toUser(r);
    setAuth({ token: r.token, user });
    return user;
  },
  register(form) {
    return request("/auth/registro", {
      method: "POST",
      auth: false,
      body: {
        rut: form.run.trim().toUpperCase(),
        nombre: form.nombre.trim(),
        apellidos: form.apellidos.trim(),
        email: form.correo.trim().toLowerCase(),
        password: form.password,
        idComuna: Number(form.idComuna),
        direccion: form.direccion.trim(),
        fechaNacimiento: form.fechaNacimiento || null,
      },
    });
  },
  comunas: () => request("/comunas", { auth: false }),
  categorias: () => request("/categorias", { auth: false }),
  async productos() {
    return (await request("/productos", { auth: false })).map(toProduct);
  },
  async guardarProducto(form) {
    const body = toProductRequest(form);
    const r = form.id
      ? await request("/productos/" + form.id, { method: "PUT", body })
      : await request("/productos", { method: "POST", body });
    return toProduct(r);
  },
  eliminarProducto: (id) => request("/productos/" + id, { method: "DELETE" }),
  carrito: () => request("/carrito"),
  agregarAlCarrito: (idProducto, cantidad) =>
    request("/carrito/items", {
      method: "POST",
      body: { idProducto: Number(idProducto), cantidad },
    }),
  actualizarCarrito: (idDetalle, cantidad) =>
    request("/carrito/items/" + idDetalle, {
      method: "PUT",
      body: { cantidad },
    }),
  quitarDelCarrito: (idDetalle) =>
    request("/carrito/items/" + idDetalle, { method: "DELETE" }),
};
