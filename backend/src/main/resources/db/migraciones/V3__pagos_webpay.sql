-- SportX Chile - Migración V3: pagos con Transbank Webpay Plus
-- Segura e idempotente: solo CREA una tabla nueva. No modifica tablas ni datos existentes.
--   mysql -u root -p sportx_db_v2 < V3__pagos_webpay.sql

CREATE TABLE IF NOT EXISTS `pagos` (
  `id_pago` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `id_orden` int(10) unsigned NOT NULL,
  `proveedor` varchar(30) NOT NULL DEFAULT 'WEBPAY_PLUS',
  `orden_compra` varchar(26) NOT NULL COMMENT 'buy_order enviado a Transbank',
  `token` varchar(100) DEFAULT NULL,
  `monto` decimal(12,0) NOT NULL,
  `estado` enum('INICIADO','APROBADO','RECHAZADO','ANULADO','ERROR') NOT NULL DEFAULT 'INICIADO',
  `codigo_respuesta` int(11) DEFAULT NULL,
  `codigo_autorizacion` varchar(20) DEFAULT NULL,
  `tipo_pago` varchar(5) DEFAULT NULL COMMENT 'VD débito, VN crédito, VC cuotas, VP prepago...',
  `cuotas` int(11) DEFAULT NULL,
  `ultimos_digitos` varchar(4) DEFAULT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT current_timestamp(),
  `fecha_actualizacion` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id_pago`),
  UNIQUE KEY `uq_pagos_token` (`token`),
  UNIQUE KEY `uq_pagos_orden_compra` (`orden_compra`),
  KEY `idx_pagos_orden` (`id_orden`),
  CONSTRAINT `fk_pagos_orden` FOREIGN KEY (`id_orden`) REFERENCES `ordenes` (`id_orden`),
  CONSTRAINT `ck_pagos_monto` CHECK (`monto` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Reversión (solo si se necesita deshacer y no hay pagos que conservar):
--   DROP TABLE `pagos`;
