-- SportX Chile - Migración V2: inicio de sesión con Google
-- Segura e idempotente: solo AGREGA una columna opcional y su índice único.
-- No borra ni modifica datos existentes. Ejecutar una vez sobre sportx_db_v2:
--   mysql -u root -p sportx_db_v2 < V2__google_auth.sql

ALTER TABLE `usuarios`
  ADD COLUMN IF NOT EXISTS `google_sub` VARCHAR(255) NULL DEFAULT NULL
    COMMENT 'Identificador estable de la cuenta Google (claim sub)'
    AFTER `password_hash`;

ALTER TABLE `usuarios`
  ADD UNIQUE INDEX IF NOT EXISTS `uq_usuarios_google_sub` (`google_sub`);

-- Reversión (solo si se necesita deshacer):
--   ALTER TABLE `usuarios` DROP INDEX `uq_usuarios_google_sub`, DROP COLUMN `google_sub`;
