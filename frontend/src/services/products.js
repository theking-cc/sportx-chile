import { PRODUCTS } from '../data/seed';
import { read } from './storage';
export function getProducts() {
  const products = read('sportx_products', PRODUCTS);
  if (!Array.isArray(products)) throw new Error('El catálogo guardado no tiene un formato válido.');
  return products;
}
