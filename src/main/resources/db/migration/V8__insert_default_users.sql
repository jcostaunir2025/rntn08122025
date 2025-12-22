-- ============================================================================
-- Migration: Insert default users with BCrypt passwords
-- Version: V8
-- Description: Creates default users for testing with secure passwords
--              Password for all users: "password123"
-- ============================================================================

-- Delete existing test users first if they exist
DELETE FROM usuario_roles_mapping WHERE id_usuario IN (
    SELECT id_usuario FROM usuario WHERE nombre_usuario IN ('admin', 'doctor1', 'enfermero1', 'analista1')
);

DELETE FROM usuario WHERE nombre_usuario IN ('admin', 'doctor1', 'enfermero1', 'analista1');

-- Insert default usuarios
-- Password: password123
-- BCrypt hash: $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi
INSERT INTO usuario (nombre_usuario, pass_usuario, created_at)
VALUES
('admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', NOW()),
('doctor1', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', NOW()),
('enfermero1', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', NOW()),
('analista1', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', NOW());

-- Assign roles to users via usuario_roles_mapping
-- admin -> ADMIN role
INSERT INTO usuario_roles_mapping (id_usuario, id_roles)
SELECT u.id_usuario, r.id_roles
FROM usuario u
CROSS JOIN usuario_roles r
WHERE u.nombre_usuario = 'admin' AND r.permisos_roles = 'ADMIN';

-- doctor1 -> DOCTOR role
INSERT INTO usuario_roles_mapping (id_usuario, id_roles)
SELECT u.id_usuario, r.id_roles
FROM usuario u
CROSS JOIN usuario_roles r
WHERE u.nombre_usuario = 'doctor1' AND r.permisos_roles = 'DOCTOR';

-- enfermero1 -> ENFERMERO role
INSERT INTO usuario_roles_mapping (id_usuario, id_roles)
SELECT u.id_usuario, r.id_roles
FROM usuario u
CROSS JOIN usuario_roles r
WHERE u.nombre_usuario = 'enfermero1' AND r.permisos_roles = 'ENFERMERO';

-- analista1 -> ANALISTA role
INSERT INTO usuario_roles_mapping (id_usuario, id_roles)
SELECT u.id_usuario, r.id_roles
FROM usuario u
CROSS JOIN usuario_roles r
WHERE u.nombre_usuario = 'analista1' AND r.permisos_roles = 'ANALISTA';

-- ============================================================================
-- Default Test Credentials:
-- ============================================================================
-- Username: admin       | Password: password123 | Role: ADMIN
-- Username: doctor1     | Password: password123 | Role: DOCTOR
-- Username: enfermero1  | Password: password123 | Role: ENFERMERO
-- Username: analista1   | Password: password123 | Role: ANALISTA
-- ============================================================================

