# Guion de defensa - SportX Chile

## 1. Problema y actualización

Explicar que el prototipo de tienda deportiva tenía páginas HTML y lógica duplicada. Mostrar la versión React con catálogo compartido y el inventario de 21 vistas. Distinguir pantalla, componente y variante de un formulario.

## 2. Componentes, propiedades y estados

Abrir ProductCard.jsx: recibe `product` mediante propiedades y usa los datos sin duplicar el catálogo. Abrir Catalog.jsx: `useState` controla texto y categoría y el listado responde a los cambios. Abrir ShopContext.jsx: carrito, sesión y datos se comparten entre páginas.

## 3. Responsividad

Mostrar Bootstrap (`row`, `col-md`, `col-lg`, `navbar-expand-lg`) y comparar escritorio con un ancho de 375 px. Usar el menú móvil y comprobar que las tarjetas pasan a una columna.

## 4. Demostración funcional

Buscar un producto, añadirlo, intentar superar su stock y aplicar DUOC10. Iniciar sesión como cliente, confirmar la compra y mostrar confirmación e historial. Como administrador, revisar inventario y cancelar el pedido para mostrar la reposición de stock. Mostrar que un cliente no accede a administración.

## 5. Pruebas Jasmine/Karma

Desde frontend ejecutar `npm test`. Abrir components.spec.jsx y explicar una prueba con sus etapas: preparar datos, realizar una interacción y comprobar DOM/almacenamiento. Mostrar el spy de Storage.setItem y la cuenta simulada del login. Explicar que Jasmine define las expectativas y Karma ejecuta la suite en Chrome.

## 6. Cobertura y resultados

Abrir `coverage/html/index.html` y COBERTURA_TESTING.md. Explicar las diferencias entre líneas, funciones, sentencias y ramas. Mostrar lo aún no cubierto. No afirmar que cobertura significa ausencia de errores.

## 7. Próxima etapa

Explicar que esta entrega utiliza localStorage y que MySQL se incorporará detrás de una API. Mostrar la separación de servicios y la exportación de respaldo. Aclarar que no hay cobros reales ni autenticación de producción.

## Preguntas para practicar

- ¿Qué recibe ProductCard como propiedades?
- ¿Por qué el carrito necesita estado compartido?
- ¿Qué sucede cuando cambia el stock antes de confirmar?
- ¿Cómo se evita reponer dos veces un pedido cancelado?
- ¿Qué simula el mock del login y qué comprueba el spy?
- ¿Qué parte de la aplicación quedó fuera de las pruebas unitarias?
- ¿Por qué React no debe conectarse directamente a MySQL?
