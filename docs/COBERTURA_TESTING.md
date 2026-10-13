# Documento de cobertura de testing - SportX Chile

Fecha: 7 de octubre de 2026. Versión: frontend React de 21 vistas.

## Entorno y ejecución

React 19, Vite 7, Jasmine, Karma 6, Chrome/Chromium headless y React Testing Library. Webpack/Babel transforman JSX; babel-plugin-istanbul instrumenta el código y karma-coverage produce el informe. Node.js 24 y Chromium 153 fueron utilizados en la verificación de esta entrega.

Desde `frontend/` ejecutar `npm install` y `npm test`. La suite corre sin servidor backend y sin base de datos. El informe HTML se genera en `coverage/html/index.html`.

## Resultado observado

- **30 de 30 pruebas Jasmine/Karma aprobadas.**
- Diez pruebas principales de componentes en `tests/components.spec.jsx`.
- Diez regresiones de datos/permisos en `tests/repository.spec.jsx`.
- Diez pruebas adicionales de vistas y formularios en `tests/pages.spec.jsx`.
- Seis pruebas complementarias de lógica aprobadas con `npm run test:logic` (no sustituyen Jasmine/Karma).
- `npm run build` terminó correctamente.
- Recorrido de navegador de las 21 vistas aprobado, con compra, persistencia, roles y administración.

| Métrica | Cobertura | Elementos cubiertos / totales |
|---|---|---|
| Sentencias | 84.53% | 541 / 640 |
| Ramas | 78.34% | 340 / 434 |
| Funciones | 87.77% | 201 / 229 |
| Líneas | 86.39% | 489 / 566 |

Los valores corresponden al código instrumentado que carga la suite, incluyendo App y sus páginas/componentes y servicios utilizados. No incluyen CSS, configuración, punto de entrada main.jsx, ErrorBoundary ni dependencias externas. No se afirma 100% de cobertura del proyecto.

## Diez casos principales

| Caso | Comportamiento verificado | Resultado |
|---|---|---|
| CP01 | Tarjeta muestra datos recibidos por propiedades | Aprobado |
| CP02 | Catálogo filtra por texto/categoría y comunica resultado vacío | Aprobado |
| CP03 | Botón de tarjeta guarda el carrito; spy registra escritura | Aprobado |
| CP04 | Repetir producto acumula cantidad sin duplicar fila | Aprobado |
| CP05 | Cantidad acumulada no supera el stock | Aprobado |
| CP06 | Eliminar producto actualiza DOM y almacenamiento | Aprobado |
| CP07 | Cupón válido calcula descuento e inválido lo retira | Aprobado |
| CP08 | Registro incompleto muestra errores y no persiste | Aprobado |
| CP09 | Login inválido falla y cuenta simulada válida navega al perfil | Aprobado |
| CP10 | Checkout registra pedido, descuenta stock y vacía carrito | Aprobado |

## Mocks, spies y aislamiento

Cada prueba limpia localStorage antes/después; React Testing Library desmonta los componentes. El login usa una cuenta ficticia controlada por la prueba. `spyOn(Storage.prototype, 'setItem')` observa la persistencia y otra regresión simula un error de cuota sin alterar datos previos. Las pruebas de eliminación simulan la respuesta de window.confirm; las de navegación simulan window.scrollTo.

No se usa MySQL en las pruebas unitarias. Se verifica el frontend y sus servicios locales. La autenticación real y las respuestas HTTP deberán agregarse cuando exista backend.

## Cobertura funcional adicional

Las regresiones verifican correos/RUN duplicados, rol Cliente en registro público, prohibición de administración de usuarios a vendedores, protección de la propia cuenta del administrador, enteros de stock, checkout con inventario insuficiente, cancelación con reposición única, conservación del precio histórico y errores de escritura.

Las pruebas de vistas cubren contenido informativo, contactos visibles en administración, CRUD de productos y usuarios, edición de perfil, historial/confirmación/detalle, restricciones de rutas, cantidades inválidas y registro/login.

## Verificación de navegador

`tests/browser-smoke.cjs` inicia Vite en un puerto independiente y abre Chromium en un perfil aislado. Recorre las 21 vistas. Comprueba que el cupón aplicado antes de iniciar sesión llega al checkout, que otro cliente no ve el pedido ajeno y que el vendedor no accede a usuarios. Verifica navegación a 375 px sin desbordamiento horizontal y captura escritorio a 1280 px. No hubo errores JavaScript de página en el recorrido aprobado.

## Análisis y límites

La cobertura identifica ramas pendientes en validaciones, casos de datos corruptos, imágenes fallidas, errores de cuota desde cada formulario y algunas rutas de error. El recorrido de navegador complementa la suite, pero no se suma artificialmente a su cobertura.

Los datos y permisos residen en el navegador: todavía no se prueba seguridad de backend, concurrencia real entre clientes, MySQL, pagos, envío de correos ni integración de despacho. El catálogo regional usa cuatro regiones de demostración. Las imágenes remotas dependen de conexión; existe alternativa visual si fallan.

## Evidencia y mantenimiento

`EVIDENCIA_TESTING.txt` conserva la salida aprobada y `coverage-summary.json` guarda los porcentajes por archivo. Para una nueva entrega, repetir la suite tras cada modificación relevante y actualizar este documento con los resultados reales. La cobertura no demuestra ausencia de errores.
