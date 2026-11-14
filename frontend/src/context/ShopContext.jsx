import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useState,
} from "react";
import * as repo from "../services/repository.js";
import { api, clearAuth, getAuth, toCart } from "../services/api.js";
const ShopContext = createContext(null);

// Estado de la tienda.
// - Si el backend responde (online === true): productos, categorías, login/registro
//   y el carrito del cliente se leen y guardan en MariaDB a través de la API.
// - Si el backend no está disponible: se usa el almacenamiento local (modo demo).
// Pedidos, mensajes de contacto y administración de usuarios siguen siendo locales
// hasta que existan sus endpoints en el backend.
export function ShopProvider({ children }) {
  const [db, setDb] = useState(repo.loadDatabase);
  const [remoteUser, setRemoteUser] = useState(() => getAuth()?.user || null);
  const [localUser, setLocalUser] = useState(() => repo.sessionUser(db));
  const [message, setMessage] = useState("");
  const [online, setOnline] = useState(null); // null = comprobando
  const [apiProducts, setApiProducts] = useState([]);
  const [categorias, setCategorias] = useState([]);
  const [serverCart, setServerCart] = useState(null);

  const user = remoteUser || localUser;
  const products = online ? apiProducts : db.products;
  const serverCartMode = online === true && remoteUser?.rol === "Cliente";
  const cart = serverCartMode ? serverCart || [] : db.cart;

  const reloadProducts = useCallback(async () => {
    const list = await api.productos();
    setApiProducts(list);
    return list;
  }, []);

  // Comprueba la conexión con el backend al iniciar.
  useEffect(() => {
    let active = true;
    Promise.all([api.productos(), api.categorias().catch(() => [])])
      .then(([list, cats]) => {
        if (!active) return;
        setApiProducts(list);
        setCategorias(cats.filter((c) => c.activo !== false));
        setOnline(true);
      })
      .catch(() => active && setOnline(false));
    return () => {
      active = false;
    };
  }, []);

  // Carga el carrito guardado en la base de datos para el cliente con sesión.
  useEffect(() => {
    if (!serverCartMode) {
      setServerCart(null);
      return;
    }
    api
      .carrito()
      .then((c) => setServerCart(toCart(c, apiProducts)))
      .catch((err) => {
        setMessage(err.message);
        if (err.status === 401) setRemoteUser(null);
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [serverCartMode, remoteUser?.id]);

  useEffect(() => {
    function sync(e) {
      if ([repo.DATABASE_KEY, repo.SESSION_KEY, null].includes(e.key)) {
        try {
          const next = repo.loadDatabase();
          setDb(next);
          setLocalUser(repo.sessionUser(next));
        } catch (err) {
          setMessage(err.message);
        }
      }
    }
    window.addEventListener("storage", sync);
    return () => window.removeEventListener("storage", sync);
  }, []);

  function changeDatabase(operation) {
    const current = repo.loadDatabase();
    const next = operation(current, user || repo.sessionUser(current));
    repo.saveDatabase(next);
    setDb(next);
    setLocalUser(repo.sessionUser(next));
    return next;
  }

  async function applyServerCart(promise) {
    const c = await promise;
    setServerCart(toCart(c, apiProducts));
  }

  // ---------- Carrito ----------
  async function add(productId, quantity = 1) {
    try {
      if (serverCartMode) {
        await applyServerCart(api.agregarAlCarrito(productId, quantity));
      } else {
        // Visitantes y personal: carrito local, validando stock con el catálogo vigente.
        changeDatabase((current) => ({
          ...current,
          cart: repo.addCart({ ...current, products }, productId, quantity)
            .cart,
        }));
      }
      setMessage("Producto agregado al carrito.");
      return true;
    } catch (err) {
      setMessage(err.message);
      return false;
    }
  }
  async function change(productId, delta) {
    if (!serverCartMode) {
      if (delta > 0) return add(productId, delta);
      try {
        changeDatabase((current) => ({
          ...current,
          cart: current.cart
            .map((i) =>
              i.id === productId ? { ...i, cantidad: i.cantidad + delta } : i,
            )
            .filter((i) => i.cantidad > 0),
        }));
      } catch (err) {
        setMessage(err.message);
      }
      return;
    }
    const item = cart.find((i) => i.id === productId);
    if (!item) return;
    const next = item.cantidad + delta;
    try {
      await applyServerCart(
        next < 1
          ? api.quitarDelCarrito(item.idDetalle)
          : api.actualizarCarrito(item.idDetalle, next),
      );
    } catch (err) {
      setMessage(err.message);
    }
  }
  async function remove(productId) {
    if (!serverCartMode) {
      try {
        changeDatabase((current) => ({
          ...current,
          cart: current.cart.filter((i) => i.id !== productId),
        }));
      } catch (err) {
        setMessage(err.message);
      }
      return;
    }
    const item = cart.find((i) => i.id === productId);
    if (!item) return;
    try {
      await applyServerCart(api.quitarDelCarrito(item.idDetalle));
    } catch (err) {
      setMessage(err.message);
    }
  }
  async function clear() {
    if (!serverCartMode) {
      try {
        changeDatabase((current) => ({ ...current, cart: [] }));
      } catch (err) {
        setMessage(err.message);
      }
      return;
    }
    try {
      let last = null;
      for (const item of cart)
        last = await api.quitarDelCarrito(item.idDetalle);
      setServerCart(last ? toCart(last, apiProducts) : []);
    } catch (err) {
      setMessage(err.message);
    }
  }

  // ---------- Sesión ----------
  async function signIn(email, password) {
    if (online !== true) {
      const current = repo.loadDatabase();
      repo.saveDatabase(current);
      const next = repo.login(current, email, password);
      setDb(current);
      setLocalUser(next);
      return next;
    }
    const next = await api.login(email, password);
    repo.logout();
    setLocalUser(null);
    // Traspasa al carrito del servidor lo que el visitante agregó sin sesión.
    if (next.rol === "Cliente") {
      const local = repo.loadDatabase();
      for (const item of local.cart)
        if (apiProducts.some((p) => p.id === item.id))
          await api.agregarAlCarrito(item.id, item.cantidad).catch(() => null);
      if (local.cart.length) {
        const cleared = { ...local, cart: [] };
        repo.saveDatabase(cleared);
        setDb(cleared);
      }
    }
    setRemoteUser(next);
    return next;
  }
  function signInDemo(userId) {
    const current = repo.loadDatabase();
    repo.saveDatabase(current);
    const next = repo.loginDemo(current, userId);
    setDb(current);
    setLocalUser(next);
    return next;
  }
  function signOut() {
    clearAuth();
    repo.logout();
    setRemoteUser(null);
    setLocalUser(null);
    setServerCart(null);
  }
  async function register(form) {
    if (online === true) return api.register(form);
    changeDatabase((current) =>
      repo.saveUser(current, form, null, { register: true }),
    );
  }

  // ---------- Productos (administración) ----------
  async function saveProduct(form) {
    if (online !== true) {
      changeDatabase((current, actor) =>
        repo.saveProduct(current, form, actor),
      );
      return;
    }
    await api.guardarProducto(form);
    await reloadProducts();
  }
  async function removeProduct(productId) {
    if (online !== true) {
      changeDatabase((current, actor) =>
        repo.deleteProduct(current, productId, actor),
      );
      return;
    }
    await api.eliminarProducto(productId);
    await reloadProducts();
  }

  // ---------- Compra ----------
  // El backend aún no tiene endpoint de pedidos: el pedido se registra localmente.
  async function purchase(shipping, coupon) {
    if (online !== true) {
      let order;
      changeDatabase((current, actor) => {
        const result = repo.checkout(current, actor, shipping, coupon);
        order = result.order;
        return result.database;
      });
      return order;
    }
    const current = repo.loadDatabase();
    const result = repo.checkout(
      { ...current, products: apiProducts, cart },
      user,
      shipping,
      coupon,
    );
    // Solo se guarda el pedido; el stock real lo administra el backend.
    const next = {
      ...current,
      orders: result.database.orders,
      cart: serverCartMode ? current.cart : [],
    };
    repo.saveDatabase(next);
    setDb(next);
    if (serverCartMode) await clear();
    return result.order;
  }

  return (
    <ShopContext.Provider
      value={{
        db,
        user,
        products,
        categorias,
        cart,
        online,
        add,
        change,
        remove,
        clear,
        message,
        dismiss: () => setMessage(""),
        notify: setMessage,
        changeDatabase,
        signIn,
        signInDemo,
        signOut,
        register,
        saveProduct,
        removeProduct,
        reloadProducts,
        purchase,
      }}
    >
      {children}
    </ShopContext.Provider>
  );
}
export const useShop = () => useContext(ShopContext);
