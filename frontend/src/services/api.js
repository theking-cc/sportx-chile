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

// RUN del formulario (123456785) <-> RUT de la base de datos (12345678-5).
export function formatRut(run) {
  const clean = String(run || "")
    .replace(/[^0-9kK]/g, "")
    .toUpperCase();
  return clean.length > 1 ? clean.slice(0, -1) + "-" + clean.slice(-1) : clean;
}
export function rutToRun(rut) {
  return String(rut || "")
    .replace(/[^0-9kK]/g, "")
    .toUpperCase();
}
export function toOrder(o) {
  return {
    id: String(o.id),
    number: o.numero,
    userId: String(o.idUsuario),
    cliente: o.destinatario,
    correo: o.email,
    createdAt: o.fecha,
    status: o.estado,
    items: (o.productos || []).map((i) => ({
      id: String(i.idProducto),
      nombre: i.nombre,
      precio: Number(i.precioUnitario),
      cantidad: i.cantidad,
    })),
    shipping: { direccion: o.direccion, region: o.region, comuna: o.comuna },
    coupon: o.codigoCupon || "",
    subtotal: Number(o.subtotal),
    descuento: Number(o.descuento),
    total: Number(o.total),
  };
}
function toMessage(m) {
  return {
    id: String(m.id),
    nombre: m.nombre,
    correo: m.email,
    asunto: m.asunto,
    mensaje: m.mensaje,
    estado: m.estado,
    createdAt: m.fecha,
  };
}
export function toUserRecord(u) {
  return {
    id: String(u.id),
    run: rutToRun(u.rut),
    nombre: u.nombre,
    apellidos: u.apellidos,
    correo: u.email,
    rol: u.rol,
    idComuna: u.idComuna ? String(u.idComuna) : "",
    comuna: u.comuna || "",
    region: u.region || "",
    direccion: u.direccion || "",
    fechaNacimiento: u.fechaNacimiento || "",
    telefono: "",
    password: "",
    remote: true,
  };
}
function userBody(form) {
  return {
    rut: formatRut(form.run),
    nombre: form.nombre.trim(),
    apellidos: form.apellidos.trim(),
    email: form.correo.trim().toLowerCase(),
    password: form.password || null,
    idComuna: Number(form.idComuna),
    direccion: form.direccion.trim(),
    fechaNacimiento: form.fechaNacimiento || null,
    rol: form.rol,
  };
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
        rut: formatRut(form.run),
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

  // Pedidos
  async crearOrden({ direccion, idComuna, codigoCupon }) {
    const r = await request("/ordenes", {
      method: "POST",
      body: {
        direccion: direccion.trim(),
        idComuna: Number(idComuna),
        codigoCupon: codigoCupon?.trim() || null,
      },
    });
    return toOrder(r);
  },
  async misOrdenes() {
    return (await request("/ordenes/mias")).map(toOrder);
  },
  async ordenes() {
    return (await request("/ordenes")).map(toOrder);
  },
  async cambiarEstadoOrden(id, estado) {
    return toOrder(
      await request("/ordenes/" + id + "/estado", {
        method: "PATCH",
        body: { estado },
      }),
    );
  },

  // Mensajes de contacto
  enviarMensaje: (form) =>
    request("/mensajes", {
      method: "POST",
      body: {
        nombre: form.nombre.trim(),
        email: form.correo.trim(),
        mensaje: form.mensaje.trim(),
      },
    }),
  async mensajes() {
    return (await request("/mensajes")).map(toMessage);
  },

  // Usuarios (administrador) y perfil propio
  async usuarios() {
    return (await request("/usuarios")).map(toUserRecord);
  },
  async guardarUsuario(form) {
    const body = userBody(form);
    const r = form.id
      ? await request("/usuarios/" + form.id, { method: "PUT", body })
      : await request("/usuarios", { method: "POST", body });
    return toUserRecord(r);
  },
  eliminarUsuario: (id) => request("/usuarios/" + id, { method: "DELETE" }),
  async perfil() {
    return toUserRecord(await request("/usuarios/me"));
  },
  async actualizarPerfil(form) {
    const { rut, rol, ...body } = userBody(form);
    void rut;
    void rol;
    return toUserRecord(await request("/usuarios/me", { method: "PUT", body }));
  },
};
