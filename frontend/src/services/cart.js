export function addItem(cart, product, quantity = 1) {
  if (!Number.isInteger(quantity) || quantity < 1)
    throw new Error("La cantidad debe ser un entero positivo.");
  const existing = cart.find((item) => item.id === product.id);
  if ((existing?.cantidad || 0) + quantity > product.stock)
    throw new Error("No hay stock suficiente.");
  return existing
    ? cart.map((item) =>
        item.id === product.id
          ? { ...item, cantidad: item.cantidad + quantity }
          : item,
      )
    : [
        ...cart,
        {
          id: product.id,
          nombre: product.nombre,
          precio: product.precio,
          imagen: product.imagen,
          cantidad: quantity,
        },
      ];
}
export function total(cart, coupon) {
  const subtotal = cart.reduce(
    (sum, item) => sum + item.precio * item.cantidad,
    0,
  );
  const descuento = !coupon
    ? 0
    : coupon.tipo === "porcentaje"
      ? Math.round(subtotal * coupon.descuento)
      : Math.min(subtotal, coupon.descuento);
  return { subtotal, descuento, total: Math.max(0, subtotal - descuento) };
}
