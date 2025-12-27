-- ============================================================================
-- V9: Create Permission System (Enhanced RBAC)
-- ============================================================================
-- Date: 2025-12-26
-- Description: Implements granular permission-based access control
--              Adds permissions and role_permissions tables
-- ============================================================================

-- Step 1: Create permissions table
CREATE TABLE IF NOT EXISTS permissions (
    id_permission INT PRIMARY KEY AUTO_INCREMENT,
    permission_name VARCHAR(100) UNIQUE NOT NULL COMMENT 'Unique permission identifier (e.g., paciente:create)',
    resource VARCHAR(50) NOT NULL COMMENT 'Resource type (e.g., PACIENTE, EVALUACION)',
    action VARCHAR(20) NOT NULL COMMENT 'Action type (CREATE, READ, UPDATE, DELETE, EXECUTE, MANAGE)',
    description VARCHAR(255) COMMENT 'Human-readable description',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_resource_action (resource, action),
    INDEX idx_permission_name (permission_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='System permissions catalog';

-- Step 2: Create role_permissions junction table
CREATE TABLE IF NOT EXISTS role_permissions (
    id_role INT NOT NULL,
    id_permission INT NOT NULL,
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_role, id_permission),
    FOREIGN KEY (id_role) REFERENCES usuario_roles(id_roles) ON DELETE CASCADE,
    FOREIGN KEY (id_permission) REFERENCES permissions(id_permission) ON DELETE CASCADE,
    INDEX idx_role (id_role),
    INDEX idx_permission (id_permission)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Role-Permission assignments';

-- ============================================================================
-- Step 3: Insert base permissions (30+ permissions)
-- ============================================================================

-- PACIENTE permissions
INSERT INTO permissions (permission_name, resource, action, description) VALUES
('paciente:create', 'PACIENTE', 'CREATE', 'Create new patient records'),
('paciente:read', 'PACIENTE', 'READ', 'View patient information'),
('paciente:update', 'PACIENTE', 'UPDATE', 'Modify patient records'),
('paciente:delete', 'PACIENTE', 'DELETE', 'Delete patient records');

-- PERSONAL permissions
INSERT INTO permissions (permission_name, resource, action, description) VALUES
('personal:create', 'PERSONAL', 'CREATE', 'Create staff records'),
('personal:read', 'PERSONAL', 'READ', 'View staff information'),
('personal:update', 'PERSONAL', 'UPDATE', 'Modify staff records'),
('personal:delete', 'PERSONAL', 'DELETE', 'Delete staff records');

-- CONSULTA permissions
INSERT INTO permissions (permission_name, resource, action, description) VALUES
('consulta:create', 'CONSULTA', 'CREATE', 'Create consultations'),
('consulta:read', 'CONSULTA', 'READ', 'View consultations'),
('consulta:update', 'CONSULTA', 'UPDATE', 'Modify consultations'),
('consulta:delete', 'CONSULTA', 'DELETE', 'Delete consultations');

-- CONSULTA_ESTATUS permissions
INSERT INTO permissions (permission_name, resource, action, description) VALUES
('consulta_estatus:read', 'CONSULTA_ESTATUS', 'READ', 'View consultation statuses');

-- EVALUACION permissions
INSERT INTO permissions (permission_name, resource, action, description) VALUES
('evaluacion:create', 'EVALUACION', 'CREATE', 'Create evaluations'),
('evaluacion:read', 'EVALUACION', 'READ', 'View evaluations'),
('evaluacion:update', 'EVALUACION', 'UPDATE', 'Modify evaluations'),
('evaluacion:delete', 'EVALUACION', 'DELETE', 'Delete evaluations');

-- EVALUACION_PREGUNTA permissions
INSERT INTO permissions (permission_name, resource, action, description) VALUES
('evaluacion_pregunta:create', 'EVALUACION_PREGUNTA', 'CREATE', 'Create evaluation questions'),
('evaluacion_pregunta:read', 'EVALUACION_PREGUNTA', 'READ', 'View evaluation questions'),
('evaluacion_pregunta:update', 'EVALUACION_PREGUNTA', 'UPDATE', 'Modify evaluation questions'),
('evaluacion_pregunta:delete', 'EVALUACION_PREGUNTA', 'DELETE', 'Delete evaluation questions');

-- EVALUACION_RESPUESTA permissions
INSERT INTO permissions (permission_name, resource, action, description) VALUES
('evaluacion_respuesta:create', 'EVALUACION_RESPUESTA', 'CREATE', 'Create evaluation responses'),
('evaluacion_respuesta:read', 'EVALUACION_RESPUESTA', 'READ', 'View evaluation responses'),
('evaluacion_respuesta:update', 'EVALUACION_RESPUESTA', 'UPDATE', 'Modify evaluation responses'),
('evaluacion_respuesta:delete', 'EVALUACION_RESPUESTA', 'DELETE', 'Delete evaluation responses');

-- SENTIMENT analysis permissions
INSERT INTO permissions (permission_name, resource, action, description) VALUES
('sentiment:analyze', 'SENTIMENT', 'EXECUTE', 'Run single sentiment analysis'),
('sentiment:analyze_batch', 'SENTIMENT', 'EXECUTE', 'Run batch sentiment analysis'),
('sentiment:aggregate', 'SENTIMENT', 'EXECUTE', 'Run aggregate sentiment analysis');

-- REPORTE permissions
INSERT INTO permissions (permission_name, resource, action, description) VALUES
('reporte:create', 'REPORTE', 'CREATE', 'Generate reports'),
('reporte:read', 'REPORTE', 'READ', 'View reports'),
('reporte:delete', 'REPORTE', 'DELETE', 'Delete reports'),
('reporte:export', 'REPORTE', 'EXECUTE', 'Export reports to external formats');

-- USUARIO permissions
INSERT INTO permissions (permission_name, resource, action, description) VALUES
('usuario:create', 'USUARIO', 'CREATE', 'Create new users'),
('usuario:read', 'USUARIO', 'READ', 'View user information'),
('usuario:update', 'USUARIO', 'UPDATE', 'Modify user records'),
('usuario:delete', 'USUARIO', 'DELETE', 'Delete users'),
('usuario:manage', 'USUARIO', 'MANAGE', 'Full user management access');

-- ROLE permissions
INSERT INTO permissions (permission_name, resource, action, description) VALUES
('role:create', 'ROLE', 'CREATE', 'Create new roles'),
('role:read', 'ROLE', 'READ', 'View role information'),
('role:update', 'ROLE', 'UPDATE', 'Modify roles'),
('role:delete', 'ROLE', 'DELETE', 'Delete roles'),
('role:assign', 'ROLE', 'MANAGE', 'Assign permissions to roles');

-- PERMISSION management permissions
INSERT INTO permissions (permission_name, resource, action, description) VALUES
('permission:read', 'PERMISSION', 'READ', 'View system permissions'),
('permission:manage', 'PERMISSION', 'MANAGE', 'Manage permission assignments');

-- ============================================================================
-- Step 4: Assign permissions to ADMIN role (full access)
-- ============================================================================
INSERT INTO role_permissions (id_role, id_permission)
SELECT r.id_roles, p.id_permission
FROM usuario_roles r
CROSS JOIN permissions p
WHERE r.permisos_roles = 'ADMIN';

-- ============================================================================
-- Step 5: Assign permissions to DOCTOR role
-- ============================================================================
INSERT INTO role_permissions (id_role, id_permission)
SELECT r.id_roles, p.id_permission
FROM usuario_roles r, permissions p
WHERE r.permisos_roles = 'DOCTOR'
AND p.permission_name IN (
    -- Paciente: full CRUD
    'paciente:create', 'paciente:read', 'paciente:update', 'paciente:delete',
    -- Personal: read only
    'personal:read',
    -- Consulta: full CRUD
    'consulta:create', 'consulta:read', 'consulta:update', 'consulta:delete',
    'consulta_estatus:read',
    -- Evaluacion: full CRUD
    'evaluacion:create', 'evaluacion:read', 'evaluacion:update', 'evaluacion:delete',
    'evaluacion_pregunta:create', 'evaluacion_pregunta:read', 'evaluacion_pregunta:update', 'evaluacion_pregunta:delete',
    'evaluacion_respuesta:create', 'evaluacion_respuesta:read', 'evaluacion_respuesta:update', 'evaluacion_respuesta:delete',
    -- Sentiment: all analysis
    'sentiment:analyze', 'sentiment:analyze_batch', 'sentiment:aggregate',
    -- Reporte: create, read, export
    'reporte:create', 'reporte:read', 'reporte:export'
);

-- ============================================================================
-- Step 6: Assign permissions to ENFERMERO role
-- ============================================================================
INSERT INTO role_permissions (id_role, id_permission)
SELECT r.id_roles, p.id_permission
FROM usuario_roles r, permissions p
WHERE r.permisos_roles = 'ENFERMERO'
AND p.permission_name IN (
    -- Paciente: read and update only
    'paciente:read', 'paciente:update',
    -- Consulta: read and update
    'consulta:read', 'consulta:update',
    'consulta_estatus:read',
    -- Basic viewing
    'evaluacion:read',
    'evaluacion_respuesta:read'
);

-- ============================================================================
-- Step 7: Assign permissions to PSICOLOGO role
-- ============================================================================
INSERT INTO role_permissions (id_role, id_permission)
SELECT r.id_roles, p.id_permission
FROM usuario_roles r, permissions p
WHERE r.permisos_roles = 'PSICOLOGO'
AND p.permission_name IN (
    -- Paciente: full CRUD
    'paciente:create', 'paciente:read', 'paciente:update',
    -- Consulta: full CRUD
    'consulta:create', 'consulta:read', 'consulta:update',
    'consulta_estatus:read',
    -- Evaluacion: full CRUD
    'evaluacion:create', 'evaluacion:read', 'evaluacion:update', 'evaluacion:delete',
    'evaluacion_pregunta:create', 'evaluacion_pregunta:read', 'evaluacion_pregunta:update',
    'evaluacion_respuesta:create', 'evaluacion_respuesta:read', 'evaluacion_respuesta:update',
    -- Sentiment: analysis
    'sentiment:analyze', 'sentiment:analyze_batch',
    -- Reporte: read
    'reporte:read'
);

-- ============================================================================
-- Step 8: Assign permissions to TERAPEUTA role
-- ============================================================================
INSERT INTO role_permissions (id_role, id_permission)
SELECT r.id_roles, p.id_permission
FROM usuario_roles r, permissions p
WHERE r.permisos_roles = 'TERAPEUTA'
AND p.permission_name IN (
    -- Paciente: read and update
    'paciente:read', 'paciente:update',
    -- Consulta: create, read, update
    'consulta:create', 'consulta:read', 'consulta:update',
    'consulta_estatus:read',
    -- Evaluacion: read and update
    'evaluacion:read', 'evaluacion:update',
    'evaluacion_pregunta:read',
    'evaluacion_respuesta:create', 'evaluacion_respuesta:read', 'evaluacion_respuesta:update',
    -- Sentiment: basic analysis
    'sentiment:analyze'
);

-- ============================================================================
-- Step 9: Assign permissions to ANALISTA role
-- ============================================================================
INSERT INTO role_permissions (id_role, id_permission)
SELECT r.id_roles, p.id_permission
FROM usuario_roles r, permissions p
WHERE r.permisos_roles = 'ANALISTA'
AND p.permission_name IN (
    -- Sentiment: all analysis capabilities
    'sentiment:analyze', 'sentiment:analyze_batch', 'sentiment:aggregate',
    -- Reporte: full access
    'reporte:create', 'reporte:read', 'reporte:delete', 'reporte:export',
    -- Read access to view context
    'evaluacion:read',
    'evaluacion_respuesta:read'
);

-- ============================================================================
-- Step 10: Assign permissions to RECEPCIONISTA role
-- ============================================================================
INSERT INTO role_permissions (id_role, id_permission)
SELECT r.id_roles, p.id_permission
FROM usuario_roles r, permissions p
WHERE r.permisos_roles = 'RECEPCIONISTA'
AND p.permission_name IN (
    -- Paciente: create and read
    'paciente:create', 'paciente:read', 'paciente:update',
    -- Consulta: schedule and view
    'consulta:create', 'consulta:read', 'consulta:update',
    'consulta_estatus:read',
    -- Personal: read only
    'personal:read'
);

-- ============================================================================
-- Verification queries (commented out - for manual testing)
-- ============================================================================

-- Check all permissions
-- SELECT * FROM permissions ORDER BY resource, action;

-- Check permissions per role
-- SELECT r.permisos_roles, COUNT(*) as permission_count
-- FROM usuario_roles r
-- JOIN role_permissions rp ON r.id_roles = rp.id_role
-- GROUP BY r.permisos_roles
-- ORDER BY permission_count DESC;

-- Check specific role permissions
-- SELECT r.permisos_roles, p.permission_name, p.description
-- FROM usuario_roles r
-- JOIN role_permissions rp ON r.id_roles = rp.id_role
-- JOIN permissions p ON rp.id_permission = p.id_permission
-- WHERE r.permisos_roles = 'DOCTOR'
-- ORDER BY p.resource, p.action;

