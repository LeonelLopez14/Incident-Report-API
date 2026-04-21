-- =============================================================
--  init.sql
--  Se ejecuta una sola vez al levantar el contenedor MySQL.
--  Crea la DB, inserta roles y el usuario admin inicial.
-- =============================================================

CREATE DATABASE IF NOT EXISTS incident_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE incident_db;

-- ── Roles ─────────────────────────────────────────────────────
INSERT IGNORE INTO roles (name) VALUES ('ROLE_ADMIN');
INSERT IGNORE INTO roles (name) VALUES ('ROLE_ANALYST');
INSERT IGNORE INTO roles (name) VALUES ('ROLE_USER');

-- ── Usuario admin inicial ──────────────────────────────────────
-- Password: Admin1234! (BCrypt hash generado con strength=10)
INSERT IGNORE INTO users (username, email, password, full_name, enabled, created_at, updated_at)
VALUES (
    'admin',
    'admin@incidentreport.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'System Administrator',
    true,
    NOW(),
    NOW()
);

-- ── Asignar ROLE_ADMIN al usuario admin ───────────────────────
INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN';

-- ── Usuario analyst de prueba ─────────────────────────────────
-- Password: Analyst1234!
INSERT IGNORE INTO users (username, email, password, full_name, enabled, created_at, updated_at)
VALUES (
    'analyst',
    'analyst@incidentreport.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Default Analyst',
    true,
    NOW(),
    NOW()
);

INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'analyst' AND r.name = 'ROLE_ANALYST';
