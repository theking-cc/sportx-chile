// js/cart.js
const CART_STORAGE_KEY = "sportx_cart_items";

function obtenerCarrito() {
  const guardado = localStorage.getItem(CART_STORAGE_KEY);
  return guardado ? JSON.parse(guardado) : [];
}

function guardarCarrito(carrito) {
  localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(carrito));
  actualizarContadorCarrito();
}

function agregarAlCarrito(productoId, cantidad = 1) {
  const carrito = obtenerCarrito();
  const index = carrito.findIndex(item => item.id === productoId);

  if (index !== -1) {
    carrito[index].cantidad += cantidad;
  } else {
    const catalogo = JSON.parse(localStorage.getItem("sportx_products")) || PRODUCTOS_INICIALES;
    const producto = catalogo.find(p => p.id === productoId);
    if (producto) {
      carrito.push({
        id: producto.id,
        nombre: producto.nombre,
        precio: producto.precio,
        imagen: producto.imagen,
        cantidad: cantidad
      });
    }
  }

  guardarCarrito(carrito);
}

function actualizarContadorCarrito() {
  const contadorBadge = document.getElementById("cart-badge-count");
  if (!contadorBadge) return;
  const carrito = obtenerCarrito();
  const totalItems = carrito.reduce((acc, item) => acc + item.cantidad, 0);
  contadorBadge.textContent = totalItems;
}