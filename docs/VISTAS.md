# Inventario de 21 vistas

Las rutas usan `/#/` gracias a HashRouter. Por ejemplo: `http://localhost:5173/#/productos`.

| N.º | Vista | Ruta | Acceso |
|---|---|---|---|
| 1 | Inicio | `/` | Público |
| 2 | Catálogo | `/productos` | Público |
| 3 | Detalle de producto | `/productos/:id` | Público |
| 4 | Carrito | `/carrito` | Público |
| 5 | Finalizar compra | `/finalizar-compra` | Cuenta autenticada |
| 6 | Compra confirmada | `/compra-confirmada/:id` | Dueño del pedido |
| 7 | Nosotros | `/nosotros` | Público |
| 8 | Blogs | `/blogs` | Público |
| 9 | Detalle de artículo | `/blogs/:id` | Público |
| 10 | Contacto | `/contacto` | Público |
| 11 | Iniciar sesión | `/login` | Público |
| 12 | Registro | `/registro` | Público |
| 13 | Mi perfil | `/perfil` | Cuenta autenticada |
| 14 | Mis pedidos | `/pedidos` | Cuenta autenticada |
| 15 | Detalle de mi pedido | `/pedidos/:id` | Dueño del pedido |
| 16 | Panel administrativo | `/admin` | Administrador / Vendedor |
| 17 | Administración de productos | `/admin/productos` | Administrador / Vendedor |
| 18 | Formulario de producto | `/admin/productos/nuevo` y `/admin/productos/:id/editar` | Administrador / Vendedor |
| 19 | Administración de usuarios | `/admin/usuarios` | Administrador |
| 20 | Formulario de usuario | `/admin/usuarios/nuevo` y `/admin/usuarios/:id/editar` | Administrador |
| 21 | Administración de pedidos | `/admin/pedidos` | Administrador / Vendedor |

Crear y editar usan la misma vista de formulario. Cada producto, pedido y artículo usa su correspondiente vista de detalle compartida. No se cuentan Navbar, Footer, rutas de error ni variantes de un mismo formulario como vistas adicionales.

Esta distribución fue propuesta y aceptada por el estudiante. El profesor indicó la cantidad, pero no entregó una lista; su criterio final de conteo debe validarse en la presentación.
