# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Users

- **Compradores en Chile** que buscan artículos deportivos de varios deportes: recorren el catálogo, comparan precio y stock, compran (con cupón opcional) y siguen sus pedidos.
- **Vendedores**, que mantienen el catálogo y el inventario y avanzan los pedidos por sus estados.
- **Administradores**, que hacen todo lo del vendedor y además administran las cuentas de usuario y leen los mensajes de contacto.
- **Evaluadores académicos**: el proyecto se evalúa en Desarrollo Fullstack II (DSY1104, DuocUC) y, con el mismo peso, debe verse listo para operar como tienda real.

## Product Purpose

SportX Chile es una tienda online multideporte. Busca que encontrar productos, mantener el inventario correcto y completar una compra sea fácil, con la experiencia del cliente separada de las herramientas del personal. Hay éxito cuando un visitante encuentra, compra y sigue su pedido sin fricción, y cuando el personal puede confiar en que el stock y los estados de los pedidos reflejan la realidad.

## Positioning

Tienda chilena multideporte de calzado, equipamiento y contenido digital de entrenamiento (por ejemplo, guías de rutinas gratuitas). Vende en pesos chilenos, usa RUT y comunas/regiones para las cuentas y el despacho, y ofrece cupones ligados a la comunidad DuocUC (`DUOC10`). Decisión abierta: aún no hay un diferenciador más específico confirmado.

## Operating Context

- Los clientes compran desde navegadores de escritorio y móvil.
- El personal trabaja en un panel de administración con productos, usuarios, pedidos y mensajes.
- Ciclo de un pedido: Pendiente → Preparando → Enviado → Entregado. Se puede cancelar antes de Enviado, y cancelar repone el stock.
- No se procesan pagos reales: el checkout registra el pedido y descuenta el stock.

## Capabilities and Constraints

- Stack: frontend React + Vite (Bootstrap 5), API REST en Java 21 / Spring Boot, MariaDB (`sportx_db_v2`), Spring Security con JWT y BCrypt. Google OAuth 2.0 / OpenID Connect está en desarrollo.
- Roles: Cliente, Vendedor, Administrador. Solo los clientes compran; el vendedor no elimina productos ni administra usuarios.
- Categorías en la base de datos: Calzado, Equipamiento y Digital (Accesorios Deportivos está inactiva). Puede haber productos gratuitos (precio 0).
- Cupones: `DUOC10` (10 %) y `SPORTX2026` ($5.000 fijos).
- Las cuentas usan RUT chileno (formato `12345678-5`) y una comuna de las tablas `comunas`/`regiones`. La contraseña tiene un mínimo de 8 caracteres.
- Eliminar un usuario lo desactiva; no lo borra.
- El idioma de la interfaz es español de Chile y los montos se muestran en CLP.
- Si el backend no responde, el frontend usa datos locales de demostración.

## Brand Commitments

- Nombre: **SportX Chile** (wordmark "Sport**X**").
- Decisión abierta: todavía no hay logo. El usuario pidió crearlo durante el trabajo visual.

## Evidence on Hand

- Productos reales en MariaDB (6 productos y 4 categorías). Fotos de productos: el usuario entregará fotos reales; mientras tanto, los productos no tienen imagen (`imagen_url` vacío).
- Dos artículos de blog en el frontend (`frontend/src/data/articles.js`).
- No hay testimonios, cifras de clientes, reseñas, prensa, alianzas ni promesas de despacho. No se deben inventar.

## Product Principles

1. La base de datos es la fuente de verdad: el stock, los precios y los estados que se muestran deben coincidir con ella.
2. La tienda y las herramientas del personal son experiencias separadas: el cliente no ve ruido operativo y el personal ve todo lo que necesita para actuar.
3. Debe funcionar a la vez como tienda creíble y como proyecto académico defendible: cada función se puede demostrar de principio a fin.
4. Comercio honesto: sin escasez falsa, reseñas inventadas ni promesas sin respaldo. Los productos gratuitos y los pagados se presentan con claridad.
5. Hecha para Chile: RUT, comunas, CLP y español son parte del diseño desde el inicio.
