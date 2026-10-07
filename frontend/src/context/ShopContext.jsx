import { createContext, useContext, useState } from 'react';
import { getProducts } from '../services/products';
import { read, write } from '../services/storage';
import { addItem } from '../services/cart';
const ShopContext = createContext(null);
export function ShopProvider({children}) {
  const [products] = useState(getProducts);
  const [cart, setCart] = useState(() => read('sportx_cart_items', []));
  const [message, setMessage] = useState('');
  function saveCart(next) { write('sportx_cart_items', next); setCart(next); }
  function add(id, quantity = 1) {
    try {
      const product = products.find(p => p.id === id);
      if (!product) throw new Error('El producto ya no está disponible.');
      saveCart(addItem(cart, product, quantity)); setMessage('Producto agregado al carrito.'); return true;
    } catch (error) { setMessage(error.message); return false; }
  }
  function change(id, delta) {
    if (delta > 0) return add(id, delta);
    saveCart(cart.map(item => item.id === id ? {...item, cantidad: item.cantidad + delta} : item).filter(item => item.cantidad > 0));
  }
  function remove(id) { saveCart(cart.filter(item => item.id !== id)); }
  return <ShopContext.Provider value={{products, cart, add, change, remove, clear: () => saveCart([]), message, dismiss: () => setMessage('')}}>{children}</ShopContext.Provider>;
}
export const useShop = () => useContext(ShopContext);
