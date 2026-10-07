# SportX Chile

## Migración a React: primera etapa

El frontend nuevo está en `frontend/`. El prototipo HTML anterior sigue disponible en la raíz para consultar y comparar.

### Ejecutar en VS Code

Instala Node.js 22.12 o superior (o Node.js 24 LTS), abre la carpeta del repositorio y ejecuta:

```sh
cd frontend
npm install
npm run dev
```

Abre la dirección que muestra Vite. No uses Live Server para ejecutar este frontend.

```sh
npm run build
npm run preview
npm run test:logic
```

### Qué se migró

- Inicio, catálogo con búsqueda y categorías, detalle del producto y carrito.
- Componentes reutilizables con propiedades y estados React.
- Navegación con HashRouter, compatible con hosting estático.
- Bootstrap y menú adaptable a pantallas pequeñas.
- Servicio de lectura/escritura de localStorage y lógica de carrito separada.
- Control de cantidades acumuladas frente al stock y descuentos.
- Las tarjetas de inicio usan el mismo catálogo que las otras páginas.
- Imágenes iniciales corregidas y alternativa visual si una imagen falla.

Se conservan las claves `sportx_products` y `sportx_cart_items`. Para leer datos del prototipo anterior, ambos deben ejecutarse en el mismo origen (protocolo, dominio y puerto). Si cambias de Live Server a Vite, los datos no se transfieren automáticamente. No borres los datos anteriores: más adelante prepararemos la exportación/importación.

### Pendientes de la siguiente etapa

1. Login, registro, formularios y panel administrativo en React.
2. Contenido de Nosotros, Blogs y Contacto. Estas rutas muestran explícitamente que están pendientes.
3. Registro de pedidos y actualización del inventario.
4. Jasmine/Karma: diez pruebas de componentes, mocks e informe de cobertura. Las seis pruebas actuales con Node verifican únicamente lógica del carrito y no reemplazan lo exigido por la evaluación.
5. ERS, documento de testing y entrega comprimida.
6. Backend y migración de datos a MySQL.

### Conexión futura a MySQL

React utilizará una API del backend; no se conectará directamente a MySQL. `src/services/` concentra el acceso a los datos para facilitar esa sustitución. Antes de migrar se debe exportar y validar productos y usuarios existentes. Las contraseñas y permisos deberán gestionarse en el backend.

### Estructura

- `src/components/`: Navbar y ProductCard.
- `src/pages/`: Home, Catalog, Detail y Cart.
- `src/context/ShopContext.jsx`: estado compartido del carrito.
- `src/services/`: productos, almacenamiento y cálculos del carrito.
- `src/data/seed.js`: datos iniciales, cupones y regiones.

Documentación: https://react.dev/ y https://vite.dev/guide/.
