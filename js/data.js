// js/data.js
const REGIONES_COMUNAS = [
  {
    region: "Región Metropolitana de Santiago",
    comunas: ["Santiago", "La Granja", "Providencia", "San Joaquín", "Ñuñoa", "Maipú"]
  },
  {
    region: "Región de Valparaíso",
    comunas: ["Valparaíso", "Viña del Mar", "Quilpué", "Villa Alemana", "Concón"]
  },
  {
    region: "Región de La Araucanía",
    comunas: ["Temuco", "Padre Las Casas", "Villarrica", "Pucón", "Angol"]
  },
  {
    region: "Región de Ñuble",
    comunas: ["Chillán", "Chillán Viejo", "San Carlos", "Coihueco"]
  }
];

const CUPONES_DISPONIBLES = [
  { codigo: "DUOC10", descuento: 0.10, tipo: "porcentaje" },
  { codigo: "SPORTX2026", descuento: 5000, tipo: "fijo" }
];

const PRODUCTOS_INICIALES = [
  {
    id: "PROD-001",
    codigo: "BOX-001",
    nombre: "Guantes de Boxeo Pro Leather 14oz",
    categoria: "Boxeo",
    precio: 34990,
    stock: 12,
    stockCritico: 3,
    descripcion: "Guantes de cuero sintético reforzado con acolchado multicapa para sparring.",
    imagen: "assets/img/guantes-box.jpg"
  },
  {
    id: "PROD-002",
    codigo: "FIT-001",
    nombre: "Cuerda de Batalla Functional 12m",
    categoria: "Fitness",
    precio: 45990,
    stock: 2,
    stockCritico: 4, // Disparará alerta visual de stock crítico
    descripcion: "Cuerda de polidacrón de alto impacto para entrenamiento funcional.",
    imagen: "assets/img/battle-rope.jpg"
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
    imagen: "assets/img/ebook-fitness.jpg"
  }
];