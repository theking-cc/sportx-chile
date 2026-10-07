import { createContext, useContext, useEffect, useState } from "react";
import * as repo from "../services/repository.js";
const ShopContext = createContext(null);
export function ShopProvider({ children }) {
  const [db, setDb] = useState(repo.loadDatabase);
  const [user, setUser] = useState(() => repo.sessionUser(db));
  const [message, setMessage] = useState("");
  useEffect(() => {
    function sync(e) {
      if ([repo.DATABASE_KEY, repo.SESSION_KEY, null].includes(e.key)) {
        try {
          const next = repo.loadDatabase();
          setDb(next);
          setUser(repo.sessionUser(next));
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
    const next = operation(current, repo.sessionUser(current));
    repo.saveDatabase(next);
    setDb(next);
    setUser(repo.sessionUser(next));
    return next;
  }
  function add(productId, quantity = 1) {
    try {
      changeDatabase((current) => repo.addCart(current, productId, quantity));
      setMessage("Producto agregado al carrito.");
      return true;
    } catch (err) {
      setMessage(err.message);
      return false;
    }
  }
  function change(productId, delta) {
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
  }
  function remove(productId) {
    try {
      changeDatabase((current) => ({
        ...current,
        cart: current.cart.filter((i) => i.id !== productId),
      }));
    } catch (err) {
      setMessage(err.message);
    }
  }
  function clear() {
    try {
      changeDatabase((current) => ({ ...current, cart: [] }));
    } catch (err) {
      setMessage(err.message);
    }
  }
  function signIn(email, password) {
    const current = repo.loadDatabase();
    repo.saveDatabase(current);
    const next = repo.login(current, email, password);
    setDb(current);
    setUser(next);
    return next;
  }
  function signInDemo(userId) {
    const current = repo.loadDatabase();
    repo.saveDatabase(current);
    const next = repo.loginDemo(current, userId);
    setDb(current);
    setUser(next);
    return next;
  }
  function signOut() {
    repo.logout();
    setUser(null);
  }
  function purchase(shipping, coupon) {
    let order;
    changeDatabase((current, actor) => {
      const result = repo.checkout(current, actor, shipping, coupon);
      order = result.order;
      return result.database;
    });
    return order;
  }
  return (
    <ShopContext.Provider
      value={{
        db,
        user,
        products: db.products,
        cart: db.cart,
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
        purchase,
      }}
    >
      {children}
    </ShopContext.Provider>
  );
}
export const useShop = () => useContext(ShopContext);
