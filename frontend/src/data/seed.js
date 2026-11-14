// js/data.js
export const REGIONS = [
  {
    region: "Región Metropolitana de Santiago",
    comunas: [
      "Santiago",
      "La Granja",
      "Providencia",
      "San Joaquín",
      "Ñuñoa",
      "Maipú",
    ],
  },
  {
    region: "Región de Valparaíso",
    comunas: [
      "Valparaíso",
      "Viña del Mar",
      "Quilpué",
      "Villa Alemana",
      "Concón",
    ],
  },
  {
    region: "Región de La Araucanía",
    comunas: ["Temuco", "Padre Las Casas", "Villarrica", "Pucón", "Angol"],
  },
  {
    region: "Región de Ñuble",
    comunas: ["Chillán", "Chillán Viejo", "San Carlos", "Coihueco"],
  },
];

export const COUPONS = [
  { codigo: "DUOC10", descuento: 0.1, tipo: "porcentaje" },
  { codigo: "SPORTX2026", descuento: 5000, tipo: "fijo" },
];

export const PRODUCTS = [
  {
    id: "PROD-001",
    codigo: "BOX-001",
    nombre: "Guantes de Boxeo Pro Leather 14oz",
    categoria: "Boxeo",
    precio: 34990,
    stock: 12,
    stockCritico: 3,
    descripcion:
      "Guantes de cuero sintético reforzado con acolchado multicapa para sparring.",
    imagen:
      "https://images.unsplash.com/photo-1549719386-74dfcbf7dbed?auto=format&fit=crop&w=600&q=80",
  },
  {
    id: "PROD-002",
    codigo: "FIT-001",
    nombre: "Cuerda de Batalla Functional 12m",
    categoria: "Fitness",
    precio: 45990,
    stock: 2,
    stockCritico: 4, // Disparará alerta visual de stock crítico
    descripcion:
      "Cuerda de polidacrón de alto impacto para entrenamiento funcional.",
    imagen:
      "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?auto=format&fit=crop&w=600&q=80",
  },
  {
    id: "PROD-003",
    codigo: "ACC-001",
    nombre: "Guía Nutricional y Plan de Entrenamiento",
    categoria: "Accesorios",
    precio: 0, // Producto FREE permitido por pauta
    stock: 100,
    stockCritico: 10,
    descripcion: "E-book digital complementario para acondicionamiento físico.",
    imagen:
      "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?auto=format&fit=crop&w=600&q=80",
  },
];
export const fallbackImage =
  "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 600 400'%3E%3Crect width='600' height='400' fill='%23EEF1F5'/%3E%3Cg transform='translate(195 95) scale(0.41)'%3E%3Cg transform='translate(0 112)'%3E%3Cpath fill='%23C9D0DC' d='M45 0 L127 0 L467 288 L385 288 Z'/%3E%3Cpath fill='%23E3A3AA' d='M385 0 L467 0 L127 288 L45 288 Z'/%3E%3Cpath fill='%23C9D0DC' d='M148 288 L248 220 L277 254 L289 239 L364 288 Z'/%3E%3Cpath fill='%23EEF1F5' d='M248 220 L260 235 L246 230 L239 240 L232 232 L218 241 Z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E";
export const money = (amount) =>
  amount === 0
    ? "FREE"
    : new Intl.NumberFormat("es-CL", {
        style: "currency",
        currency: "CLP",
        maximumFractionDigits: 0,
      }).format(amount);
