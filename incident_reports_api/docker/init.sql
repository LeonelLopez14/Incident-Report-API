-- =============================================================
--  init.sql
--  Se ejecuta UNA SOLA VEZ al levantar el contenedor MySQL.
--  Solo crea la base de datos con el charset correcto.
--
--  Los roles y usuarios iniciales los inserta Spring Boot
--  al arrancar, a través de DataInitializer.java.
-- =============================================================

CREATE DATABASE IF NOT EXISTS incident_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;