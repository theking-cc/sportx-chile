# Especificación de Requisitos de Software - SportX Chile

Versión 3.0 (final) - 15 de noviembre de 2026

Asignatura: Desarrollo Fullstack II (DSY1104) · Evaluación Parcial 3
Integrantes: [Nombre integrante 1], [Nombre integrante 2], [Nombre integrante 3] · Docente: [Docente]

| Versión | Fecha | Cambios |
|---|---|---|
| 1.0 | Evaluación 1 | Prototipo HTML/JavaScript |
| 2.0 | 7 de octubre de 2026 | Frontend React con 21 vistas y datos locales |
| 3.0 | 15 de noviembre de 2026 | Backend Spring Boot con MariaDB, API REST versionada, JWT, roles, Swagger, inicio con Google y pago con Webpay |

## 1. Propósito y propuesta

SportX Chile es una tienda en línea de artículos deportivos (boxeo, fitness, calzado y accesorios). Su propósito es facilitar la búsqueda y compra de productos, el control del inventario y el seguimiento de pedidos, separando la experiencia del cliente de las funciones del vendedor y del administrador.

Esta versión final integra el frontend React con un backend Spring Boot conectado a una base de datos relacional MariaDB, mediante una API REST protegida con autenticación JWT y autorización por roles.

## 2. Alcance

Incluye: catálogo con búsqueda y filtros, carrito persistente, cupones de descuento, compra con pago Webpay Plus (ambiente de integración de Transbank, sin cobros reales), registro de boletas y su detalle, historial de pedidos, perfil, registro e inicio de sesión (correo y contraseña o cuenta de Google), administración de productos, categorías, usuarios y pedidos, formulario de contacto, documentación Swagger y pruebas automatizadas.

Excluye: cobros en producción, envío de correos, integración con empresas de despacho y despliegue en servidores públicos.

## 3. Actores y roles

| Rol | Permisos |
|---|---|
| Visitante (sin sesión) | Ver catálogo, detalle, blog y nosotros; registrarse; iniciar sesión; enviar consultas |
| Cliente | Solo la tienda: carrito, compra con Webpay, sus pedidos y su perfil |
| Vendedor | Ver la lista y el detalle de productos; ver la lista y el detalle de órdenes. Todos los demás accesos no aparecen en su vista |
| Administrador | Acceso total: productos, categorías, usuarios, órdenes (incluido el cambio de estado) y mensajes de contacto |

## 4. Requisitos funcionales y criterios de aceptación

| ID | Requisito | Criterio de aceptación |
|---|---|---|
| RF01 | Mostrar catálogo | Inicio, catálogo y detalle obtienen los productos desde GET /api/v1/productos |
| RF02 | Buscar y filtrar | El listado se filtra por nombre y categoría y muestra un mensaje si está vacío |
| RF03 | Mostrar detalle de producto | Presenta nombre, categoría, descripción, precio y stock |
| RF04 | Gestionar carrito | El cliente agrega, cambia cantidades, elimina y vacía; el carrito se guarda en la base de datos |
| RF05 | Controlar stock | No permite agregar ni comprar más unidades que el stock; dos compras simultáneas no venden la misma unidad |
| RF06 | Aplicar descuentos | Valida DUOC10 (10 %) y SPORTX2026 ($5.000) en el backend; el total nunca es negativo |
| RF07 | Iniciar pago con Webpay | Con sesión de Cliente, dirección y comuna válidas, crea la orden, reserva el stock y redirige a Webpay |
| RF08 | Confirmar el pago | Pago aprobado (monto y orden coinciden): vacía el carrito. Rechazado, anulado o expirado: cancela la orden y repone el stock. Un retorno repetido no vuelve a cobrar |
| RF09 | Registrar boleta | La orden (BOLETA) y su detalle (DETALLE_BOLETA) conservan productos y precios del momento de la compra |
| RF10 | Historial de pedidos | Cada cliente solo consulta sus pedidos y su detalle, con el estado del pago |
| RF11 | Registrar cuentas | Valida RUN con dígito verificador, correo, contraseña de 8 caracteres o más y campos obligatorios; evita correos y RUT duplicados |
| RF12 | Iniciar y cerrar sesión | Credenciales válidas entregan un token JWT; cerrar sesión elimina el token |
| RF13 | Iniciar sesión con Google | El backend valida el ID token de Google; si la cuenta no existe, solicita RUT, comuna y dirección para completar el registro |
| RF14 | Editar perfil | Actualiza datos personales sin cambiar el rol; contraseña vacía conserva la anterior |
| RF15 | Gestionar productos | Solo el Administrador crea, edita y elimina; código único, precio y stock no negativos |
| RF16 | Gestionar categorías | Solo el Administrador crea, edita y elimina categorías |
| RF17 | Gestionar usuarios | Solo el Administrador crea, edita y desactiva cuentas de cualquier rol |
| RF18 | Consultar pedidos (staff) | Administrador y Vendedor ven la lista y el detalle de todas las órdenes |
| RF19 | Cambiar estado de pedidos | Solo el Administrador: Pendiente → Preparando → Enviado → Entregado, o Cancelado antes del envío; no se prepara una orden sin pago aprobado |
| RF20 | Reponer stock al cancelar | Una orden cancelada devuelve las unidades una sola vez |
| RF21 | Vista restringida del Vendedor | El Vendedor solo ve Productos y Pedidos; no aparecen carrito, usuarios, mensajes ni botones de edición |
| RF22 | Panel administrativo | Muestra productos, stock crítico, pedidos, ventas vigentes y consultas recibidas |
| RF23 | Registrar consultas | Valida nombre, correo y mensaje; el Administrador las lee en su panel |
| RF24 | Contenido informativo | Nosotros, blog y artículos muestran contenido de la propuesta |

## 5. Requisitos no funcionales

| ID | Requisito | Comprobación |
|---|---|---|
| RNF01 | Arquitectura por capas | Frontend React; backend con controladores, servicios, repositorios y entidades; base de datos MariaDB |
| RNF02 | API REST versionada | Todas las rutas cuelgan de /api/v1 |
| RNF03 | API documentada | Swagger UI en /swagger-ui.html y especificación OpenAPI en /v3/api-docs, con descripciones en español |
| RNF04 | Autenticación | Spring Security sin sesión (stateless) con tokens JWT firmados; contraseñas cifradas con BCrypt |
| RNF05 | Autorización por roles | Sin token responde 401; rol no autorizado responde 403; el frontend oculta lo que el rol no puede usar |
| RNF06 | Integridad de datos | Operaciones de compra transaccionales con bloqueo del producto; migraciones SQL aditivas con respaldo previo |
| RNF07 | Pruebas | Backend: 38 pruebas unitarias y de seguridad (Mockito, MockMvc) sin base de datos. Frontend: 30 pruebas Jasmine/Karma |
| RNF08 | Diseño responsivo | Verificado a 1440 px y 390 px sin contenido cortado ni errores de consola |
| RNF09 | Usabilidad | Formularios con etiquetas, errores junto al campo y mensajes de estado claros |
| RNF10 | Configuración externa | Clave JWT, credenciales de Google y ambiente de Webpay por variables de entorno; los secretos no se guardan en el repositorio |
| RNF11 | Control de versiones | Repositorios públicos en GitHub para frontend y backend, con commits descriptivos |

## 6. Modelo de datos

Base de datos relacional MariaDB `sportx_db_v2`, con 12 tablas. Equivalencia con la propuesta de la pauta:

| Entidad de la pauta | Tabla | Contenido principal |
|---|---|---|
| USUARIO | usuarios | RUT, nombre, apellidos, correo, contraseña (BCrypt), google_sub, rol, comuna, dirección, activo |
| BOLETA | ordenes | Cliente, fecha, estado, subtotal, descuento, total, cupón, dirección de despacho |
| DETALLE_BOLETA | detalle_orden | Producto, código, nombre, cantidad, precio unitario y subtotal |
| PRODUCTO | productos | Código, nombre, descripción, precio, stock, stock crítico, imagen, categoría |
| CATEGORIA | categorias | Nombre, descripción, activo |
| (adicional) | roles, regiones, comunas | Roles del sistema y ubicación |
| (adicional) | carritos, detalle_carrito | Carrito persistente del cliente |
| (adicional) | pagos | Transacciones Webpay: token, monto, estado, autorización |
| (adicional) | mensajes_contacto | Consultas del formulario de contacto |

Reglas: los precios se guardan en pesos chilenos; el RUN valida dígito verificador; un usuario desactivado conserva sus órdenes; el detalle de la boleta guarda el precio del momento de la compra.

## 7. Arquitectura y herramientas

| Capa | Tecnología |
|---|---|
| Frontend | React 19, Vite 7, React Router 7 (HashRouter), Bootstrap 5; llamadas fetch centralizadas en services/api.js |
| Backend | Java 21, Spring Boot 4.1, Spring Web MVC, Spring Data JPA, Spring Security, OAuth2 Resource Server (JWT), Bean Validation |
| Base de datos | MariaDB 10.4 o superior |
| Documentación | springdoc-openapi 3.2 (Swagger UI) |
| Pagos | Transbank SDK Java (Webpay Plus REST v1.2) |
| Identidad externa | Google Identity Services (OpenID Connect) |
| Pruebas | JUnit 5, Mockito, MockMvc, Spring Security Test; Jasmine/Karma; Playwright |
| Herramientas | Git y GitHub, Visual Studio Code, Maven Wrapper, npm |

Flujo de una compra: el frontend envía el carrito a POST /api/v1/pagos/webpay/iniciar → el backend crea la orden, reserva stock y crea la transacción en Transbank → el cliente paga en Webpay → Transbank vuelve a /api/v1/pagos/webpay/retorno → el backend confirma la transacción y redirige al resultado en el frontend.

## 8. Validación

- Pruebas del backend (mvnw test): OrdenServiceTest, PagoServiceTest y SeguridadRolesTest, 38 aprobadas.
- Pruebas del frontend (npm test): 30 aprobadas.
- Pruebas de comunicación: llamadas reales a la API con cada rol, con los códigos 200, 401, 403 y 404 esperados.
- Recorrido completo en el navegador: registro, compra, pago aprobado y vistas por rol.

Documentos relacionados: Manual de usuario (Anexo 4), Documentación de APIs (Anexo 3) y Documento de APIs e Integración (Anexo 2).
