-- ============================================================================
-- COMPLETE DATABASE SCHEMA - RNTN Sentiment Analysis API
-- ============================================================================
-- Version: 2.0
-- Date: 2025-12-21
-- Description: Complete database implementation with 1:1 relationship
--              between Personal and Usuario tables
--
-- ⚠️ NOTE: This is a REFERENCE SCRIPT for new database creation
--          For existing databases, use Flyway migrations (V1-V5)
--
-- Changes from v1.0:
--   - Added email_personal and telefono_personal to personal table
--   - Added 1:1 relationship: personal.id_usuario -> usuario.id_usuario
--   - Added nivel_riesgo field to evaluacion_respuesta table
--   - Added titulo_evaluacion and fecha_evaluacion to evaluacion table
--   - Enhanced indexes for performance
-- ============================================================================

-- Drop database if exists (CAUTION: Only for development/fresh install)
-- DROP DATABASE IF EXISTS rntn_sentiment_db;

-- Create database
CREATE DATABASE IF NOT EXISTS rntn_sentiment_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE rntn_sentiment_db;

-- ============================================================================
-- SECTION 1: USER MANAGEMENT TABLES
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Table: usuario
-- Description: System users (doctors, nurses, admins, analysts)
-- ----------------------------------------------------------------------------
CREATE TABLE usuario (
                         id_usuario INT PRIMARY KEY AUTO_INCREMENT,
                         nombre_usuario VARCHAR(50) UNIQUE NOT NULL COMMENT 'Username for login',
                         pass_usuario VARCHAR(255) NOT NULL COMMENT 'Hashed password (BCrypt recommended)',
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         INDEX idx_nombre_usuario (nombre_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='System users with authentication';

-- ----------------------------------------------------------------------------
-- Table: usuario_roles
-- Description: Available roles in the system
-- ----------------------------------------------------------------------------
CREATE TABLE usuario_roles (
                               id_roles INT PRIMARY KEY AUTO_INCREMENT,
                               permisos_roles VARCHAR(255) NOT NULL COMMENT 'Role name (ADMIN, DOCTOR, etc.)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='System roles catalog';

-- ----------------------------------------------------------------------------
-- Table: usuario_roles_mapping
-- Description: Many-to-many relationship between users and roles
-- ----------------------------------------------------------------------------

CREATE TABLE usuario_roles_mapping (
                                       id_usuario INT NOT NULL,
                                       id_roles INT NOT NULL,
                                       PRIMARY KEY (id_usuario, id_roles),
                                       FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE,
                                       FOREIGN KEY (id_roles) REFERENCES usuario_roles(id_roles) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='User-Role assignments (many-to-many)';

-- ============================================================================
-- SECTION 2: MEDICAL STAFF AND PATIENT TABLES
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Table: personal
-- Description: Medical staff (doctors, nurses, therapists)
-- ⭐ 1:1 relationship with usuario table
-- ⭐ Includes email and phone contact fields
-- ----------------------------------------------------------------------------
CREATE TABLE personal (
                          id_personal INT PRIMARY KEY AUTO_INCREMENT,
                          id_usuario INT UNIQUE NULL COMMENT '⭐ 1:1 relationship with usuario - Each staff member has one system user',
                          doc_personal VARCHAR(20) UNIQUE NOT NULL COMMENT 'Professional ID/License number',
                          nombre_personal VARCHAR(100) NOT NULL COMMENT 'Full name',
                          email_personal VARCHAR(100) NULL COMMENT '⭐ Contact email',
                          telefono_personal VARCHAR(20) NULL COMMENT '⭐ Contact phone',
                          estatus_personal VARCHAR(20) DEFAULT 'ACTIVO' COMMENT 'Status: ACTIVO, INACTIVO, SUSPENDIDO',
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign key for 1:1 relationship
                          FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE SET NULL ON UPDATE CASCADE,

    -- Indexes
                          INDEX idx_doc_personal (doc_personal),
                          INDEX idx_email_personal (email_personal),
                          INDEX idx_estatus_personal (estatus_personal),
    /*INDEX idx_personal_id_usuario (id_usuario),*/
                          UNIQUE INDEX idx_id_usuario_unique (id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='⭐ Medical staff with 1:1 relationship to system users';

-- ----------------------------------------------------------------------------
-- Table: paciente
-- Description: Patients in the mental health system
-- ----------------------------------------------------------------------------
CREATE TABLE paciente (
                          id_paciente INT PRIMARY KEY AUTO_INCREMENT,
                          doc_paciente VARCHAR(20) UNIQUE NOT NULL COMMENT 'Patient ID/Document number',
                          nombre_paciente VARCHAR(100) NOT NULL COMMENT 'Full name',
                          direccion_paciente VARCHAR(255) COMMENT 'Home address',
                          email_paciente VARCHAR(100) COMMENT 'Contact email',
                          telefono_paciente VARCHAR(20) COMMENT 'Contact phone',
                          estatus_paciente VARCHAR(20) DEFAULT 'ACTIVO' COMMENT 'Status: ACTIVO, INACTIVO, ALTA',
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Indexes
                          INDEX idx_doc_paciente (doc_paciente),
                          INDEX idx_estatus_paciente (estatus_paciente),
                          INDEX idx_paciente_estatus_nombre (estatus_paciente, nombre_paciente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Patients receiving mental health services';

-- ============================================================================
-- SECTION 3: CONSULTATION AND EVALUATION TABLES
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Table: consulta_estatus
-- Description: Consultation status catalog
-- ----------------------------------------------------------------------------
CREATE TABLE consulta_estatus (
                                  id_consulta_estatus INT PRIMARY KEY AUTO_INCREMENT,
                                  nombre_consulta_estatus VARCHAR(50) UNIQUE NOT NULL COMMENT 'Status name'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Consultation status catalog';

-- ----------------------------------------------------------------------------
-- Table: consulta
-- Description: Medical consultations/sessions
-- ⭐ UPDATED: estatus_consulta uses FK to consulta_estatus.id_consulta_estatus (N:1 relationship)
--            Many consultations can share the same status
-- ----------------------------------------------------------------------------
CREATE TABLE consulta (
                          id_consulta INT PRIMARY KEY AUTO_INCREMENT,
                          id_paciente INT NOT NULL,
                          id_personal INT NOT NULL,
                          fechahora_consulta TIMESTAMP NOT NULL COMMENT 'Consultation start date/time',
                          fechafin_consulta TIMESTAMP NULL COMMENT 'Consultation end date/time',
                          estatus_consulta INT NOT NULL COMMENT '⭐ N:1 FK to consulta_estatus.id_consulta_estatus',
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign keys
                          FOREIGN KEY (id_paciente) REFERENCES paciente(id_paciente) ON DELETE CASCADE,
                          FOREIGN KEY (id_personal) REFERENCES personal(id_personal) ON DELETE RESTRICT,
                          FOREIGN KEY (estatus_consulta) REFERENCES consulta_estatus(id_consulta_estatus) ON DELETE RESTRICT ON UPDATE CASCADE,

    -- Indexes
                          INDEX idx_id_paciente (id_paciente),
                          INDEX idx_id_personal (id_personal),
                          INDEX idx_fechahora_consulta (fechahora_consulta),
                          INDEX idx_estatus_consulta (estatus_consulta),
                          INDEX idx_consulta_paciente_estatus (id_paciente, estatus_consulta),
                          INDEX idx_consulta_personal_fecha (id_personal, fechahora_consulta)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='⭐ Medical consultation sessions with N:1 FK constraint to consulta_estatus.id_consulta_estatus';

-- ----------------------------------------------------------------------------
-- Table: evaluacion
-- Description: Evaluations performed during consultations
-- ⭐ Enhanced with titulo and fecha fields
-- ----------------------------------------------------------------------------
CREATE TABLE evaluacion (
                            id_evaluacion INT PRIMARY KEY AUTO_INCREMENT,
                            id_consulta INT NOT NULL,
                            titulo_evaluacion VARCHAR(100) NULL COMMENT '⭐ Evaluation title',
                            nombre_evaluacion VARCHAR(100) NOT NULL COMMENT 'Evaluation name/type',
                            fecha_evaluacion TIMESTAMP NULL COMMENT '⭐ Evaluation date',
                            area_evaluacion VARCHAR(100) COMMENT 'Area being evaluated (depression, anxiety, etc.)',
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign keys
                            FOREIGN KEY (id_consulta) REFERENCES consulta(id_consulta) ON DELETE CASCADE,

    -- Indexes
                            INDEX idx_id_consulta (id_consulta)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Mental health evaluations';

-- ----------------------------------------------------------------------------
-- Table: evaluacion_pregunta
-- Description: Evaluation questions (question bank)
-- ⭐ Can be linked to specific evaluations or used as general question bank
-- ----------------------------------------------------------------------------
CREATE TABLE evaluacion_pregunta (
                                     id_evaluacion_pregunta INT PRIMARY KEY AUTO_INCREMENT,
                                     id_evaluacion INT NULL COMMENT '⭐ Optional: Link to specific evaluation',
                                     texto_pregunta VARCHAR(500) NULL COMMENT 'Question text (legacy field)',
                                     texto_evaluacion_pregunta TEXT NOT NULL COMMENT 'Question text',
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign keys
                                     FOREIGN KEY (id_evaluacion) REFERENCES evaluacion(id_evaluacion) ON DELETE CASCADE,

    -- Indexes
                                     INDEX idx_id_evaluacion (id_evaluacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Question bank for evaluations';

-- ----------------------------------------------------------------------------
-- Table: evaluacion_respuesta
-- Description: Patient responses with RNTN sentiment analysis
-- ⭐ CORE TABLE: Integrates Stanford CoreNLP RNTN sentiment analysis
-- ⭐ Includes nivel_riesgo for risk level classification
-- ----------------------------------------------------------------------------
CREATE TABLE evaluacion_respuesta (
                                      id_evaluacion_respuesta INT PRIMARY KEY AUTO_INCREMENT,
                                      id_evaluacion_pregunta INT NOT NULL,

    -- Original response text
                                      texto_evaluacion_respuesta TEXT NOT NULL COMMENT 'Patient response (original text)',

    -- Processed/normalized text (for training)
                                      texto_set_evaluacion_respuesta TEXT COMMENT 'Processed text for model training',

    -- ⭐ RNTN SENTIMENT ANALYSIS RESULTS
                                      class_evaluacion_respuesta INT NULL COMMENT 'Predicted class (0-4): 0=ANXIETY, 1=SUICIDAL, 2=ANGER, 3=SADNESS, 4=FRUSTRATION',
                                      label_evaluacion_respuesta VARCHAR(50) NULL COMMENT 'Sentiment label: ANXIETY, SUICIDAL, ANGER, SADNESS, FRUSTRATION',
                                      confidence_score DOUBLE NULL COMMENT 'Model confidence (0.0 - 1.0)',
                                      nivel_riesgo VARCHAR(20) NULL COMMENT '⭐ Risk level: BAJO, MEDIO, ALTO',

                                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign keys
                                      FOREIGN KEY (id_evaluacion_pregunta) REFERENCES evaluacion_pregunta(id_evaluacion_pregunta) ON DELETE CASCADE,

    -- Indexes for sentiment analysis queries
                                      INDEX idx_id_evaluacion_pregunta (id_evaluacion_pregunta),
                                      INDEX idx_label_evaluacion_respuesta (label_evaluacion_respuesta),
                                      INDEX idx_class_evaluacion_respuesta (class_evaluacion_respuesta),
                                      INDEX idx_nivel_riesgo (nivel_riesgo),
                                      INDEX idx_respuesta_label_confidence (label_evaluacion_respuesta, confidence_score),
                                      INDEX idx_evaluacion_respuesta_label_riesgo (label_evaluacion_respuesta, nivel_riesgo),
                                      INDEX idx_evaluacion_respuesta_created_at (created_at),
    -- INDEX idx_evaluacion_respuesta_label_confidence (label_evaluacion_respuesta, confidence_score),

    -- Full-text search index for response text
                                      FULLTEXT INDEX idx_fulltext_respuesta (texto_evaluacion_respuesta)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='⭐ Patient responses with RNTN sentiment analysis results';

-- ============================================================================
-- SECTION 4: REPORTING TABLES
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Table: reporte
-- Description: Generated reports and analysis documents
-- ----------------------------------------------------------------------------
CREATE TABLE reporte (
                         id_reporte INT PRIMARY KEY AUTO_INCREMENT,
                         id_usuario INT NOT NULL COMMENT 'User who generated the report',
                         id_evaluacion INT NOT NULL COMMENT 'Evaluation being reported',
                         fechageneracion_reporte TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Report generation date',
                         nombre_reporte VARCHAR(100) NOT NULL COMMENT 'Report name/title',
                         resultado_reporte TEXT COMMENT 'Report content/results',
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign keys
                         FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
                         FOREIGN KEY (id_evaluacion) REFERENCES evaluacion(id_evaluacion) ON DELETE CASCADE,

    -- Indexes
                         INDEX idx_id_usuario (id_usuario),
                         INDEX idx_id_evaluacion (id_evaluacion),
                         INDEX idx_fechageneracion_reporte (fechageneracion_reporte),
                         INDEX idx_reporte_usuario_fecha (id_usuario, fechageneracion_reporte)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Generated reports and analysis documents';

/*
-- ============================================================================
-- SECTION 5: MASTER DATA
-- ============================================================================

-- Insert predefined roles
INSERT INTO usuario_roles (permisos_roles) VALUES
('ADMIN'),
('DOCTOR'),
('ENFERMERO'),
('PSICOLOGO'),
('TERAPEUTA'),
('RECEPCIONISTA'),
('ANALISTA');

-- Insert consultation statuses
INSERT INTO consulta_estatus (nombre_consulta_estatus) VALUES
('PENDIENTE'),
('EN_PROGRESO'),
('COMPLETADA'),
('CANCELADA'),
('REPROGRAMADA'),
('NO_ASISTIO');

-- Insert admin user (password: admin123 - BCrypt hashed)
-- ⚠️ IMPORTANT: Change this password in production!
INSERT INTO usuario (nombre_usuario, pass_usuario) VALUES
('admin', '$2a$10$slYQmyNdGzTn7ZLBXBChFOR.vY0QJLx2z9q3OXZ2.rLQfKVRKxTZ6');

-- Assign ADMIN role to admin user
INSERT INTO usuario_roles_mapping (id_usuario, id_roles)
SELECT u.id_usuario, r.id_roles
FROM usuario u, usuario_roles r
WHERE u.nombre_usuario = 'admin' AND r.permisos_roles = 'ADMIN';

-- ============================================================================
-- SECTION 6: SAMPLE DATA (Optional - for development/testing)
-- ============================================================================

-- Sample users
INSERT INTO usuario (nombre_usuario, pass_usuario) VALUES
('dra.garcia', '$2a$10$slYQmyNdGzTn7ZLBXBChFOR.vY0QJLx2z9q3OXZ2.rLQfKVRKxTZ6'),
('dr.martinez', '$2a$10$slYQmyNdGzTn7ZLBXBChFOR.vY0QJLx2z9q3OXZ2.rLQfKVRKxTZ6'),
('enf.rodriguez', '$2a$10$slYQmyNdGzTn7ZLBXBChFOR.vY0QJLx2z9q3OXZ2.rLQfKVRKxTZ6');

-- Assign DOCTOR role
INSERT INTO usuario_roles_mapping (id_usuario, id_roles)
SELECT u.id_usuario, r.id_roles
FROM usuario u, usuario_roles r
WHERE u.nombre_usuario IN ('dra.garcia', 'dr.martinez') AND r.permisos_roles = 'DOCTOR';

-- Assign ENFERMERO role
INSERT INTO usuario_roles_mapping (id_usuario, id_roles)
SELECT u.id_usuario, r.id_roles
FROM usuario u, usuario_roles r
WHERE u.nombre_usuario = 'enf.rodriguez' AND r.permisos_roles = 'ENFERMERO';

-- Sample medical staff (⭐ with 1:1 relationship to usuarios)
INSERT INTO personal (id_usuario, doc_personal, nombre_personal, email_personal, telefono_personal, estatus_personal)
VALUES
((SELECT id_usuario FROM usuario WHERE nombre_usuario = 'dra.garcia'), 'DOC-001', 'Dra. María García López', 'maria.garcia@hospital.com', '+34 666 111 222', 'ACTIVO'),
((SELECT id_usuario FROM usuario WHERE nombre_usuario = 'dr.martinez'), 'DOC-002', 'Dr. Juan Martínez Pérez', 'juan.martinez@hospital.com', '+34 666 333 444', 'ACTIVO'),
((SELECT id_usuario FROM usuario WHERE nombre_usuario = 'enf.rodriguez'), 'ENF-001', 'Enfermera Ana Rodríguez', 'ana.rodriguez@hospital.com', '+34 666 555 666', 'ACTIVO');

-- Sample patients
INSERT INTO paciente (doc_paciente, nombre_paciente, direccion_paciente, email_paciente, telefono_paciente, estatus_paciente) VALUES
('PAC-001', 'Carlos López Fernández', 'Calle Mayor 123, Madrid', 'carlos.lopez@email.com', '+34 666 777 888', 'ACTIVO'),
('PAC-002', 'Laura Sánchez Ruiz', 'Avenida Principal 45, Barcelona', 'laura.sanchez@email.com', '+34 666 999 000', 'ACTIVO'),
('PAC-003', 'Pedro Gómez Torres', 'Plaza Central 7, Valencia', 'pedro.gomez@email.com', '+34 666 111 333', 'ACTIVO');

-- Standard evaluation questions for sentiment analysis
INSERT INTO evaluacion_pregunta (texto_evaluacion_pregunta) VALUES
('¿Cómo se siente hoy?'),
('¿Ha experimentado cambios en su estado de ánimo recientemente?'),
('¿Tiene pensamientos que le preocupan?'),
('¿Cómo describiría su nivel de energía?'),
('¿Ha tenido problemas para dormir?'),
('¿Se siente ansioso o nervioso?'),
('¿Ha experimentado cambios en su apetito?'),
('¿Cómo es su relación con las personas cercanas?'),
('¿Tiene dificultades para concentrarse?'),
('¿Ha tenido pensamientos de hacerse daño?'),
('¿Qué le hace sentir más tranquilo?'),
('¿Hay algo que le cause frustración últimamente?'),
('¿Siente que tiene apoyo de su familia y amigos?'),
('¿Qué tan frecuentemente experimenta tristeza?'),
('¿Hay momentos en los que se siente especialmente enojado?');
*/
-- ============================================================================
-- SECTION 7: VIEWS (Optional - for easier querying)
-- ============================================================================

-- View: Complete consultation information with staff-user relationship
CREATE OR REPLACE VIEW vw_consultas_completas AS
SELECT
    c.id_consulta,
    c.fechahora_consulta,
    c.fechafin_consulta,
    c.estatus_consulta,
    ce.nombre_consulta_estatus AS estatus_consulta_nombre,
    pac.id_paciente,
    pac.nombre_paciente AS paciente_nombre,
    pac.doc_paciente AS paciente_documento,
    pac.telefono_paciente AS paciente_telefono,
    per.id_personal,
    per.nombre_personal AS profesional_nombre,
    per.email_personal AS profesional_email,
    per.telefono_personal AS profesional_telefono,
    per.id_usuario AS profesional_id_usuario,
    u.nombre_usuario AS profesional_usuario
FROM consulta c
         INNER JOIN paciente pac ON c.id_paciente = pac.id_paciente
         INNER JOIN personal per ON c.id_personal = per.id_personal
         LEFT JOIN consulta_estatus ce ON c.estatus_consulta = ce.id_consulta_estatus
         LEFT JOIN usuario u ON per.id_usuario = u.id_usuario;

-- View: Responses with sentiment analysis
CREATE OR REPLACE VIEW vw_respuestas_con_sentimiento AS
SELECT
    er.id_evaluacion_respuesta,
    er.texto_evaluacion_respuesta,
    er.class_evaluacion_respuesta,
    er.label_evaluacion_respuesta,
    er.confidence_score,
    er.nivel_riesgo,
    er.created_at AS fecha_respuesta,
    ep.id_evaluacion_pregunta,
    ep.texto_evaluacion_pregunta AS pregunta,
    e.id_evaluacion,
    e.nombre_evaluacion,
    e.titulo_evaluacion,
    c.id_consulta,
    c.fechahora_consulta,
    pac.nombre_paciente,
    per.nombre_personal AS profesional_nombre
FROM evaluacion_respuesta er
         INNER JOIN evaluacion_pregunta ep ON er.id_evaluacion_pregunta = ep.id_evaluacion_pregunta
         LEFT JOIN evaluacion e ON ep.id_evaluacion = e.id_evaluacion
         LEFT JOIN consulta c ON e.id_consulta = c.id_consulta
         LEFT JOIN paciente pac ON c.id_paciente = pac.id_paciente
         LEFT JOIN personal per ON c.id_personal = per.id_personal;

-- View: High-risk alerts (SUICIDAL responses with high confidence)
CREATE OR REPLACE VIEW vw_alertas_alto_riesgo AS
SELECT
    er.id_evaluacion_respuesta,
    er.texto_evaluacion_respuesta,
    er.label_evaluacion_respuesta,
    er.confidence_score,
    er.nivel_riesgo,
    er.created_at AS fecha_respuesta,
    pac.id_paciente,
    pac.nombre_paciente,
    pac.telefono_paciente,
    pac.email_paciente,
    per.nombre_personal AS profesional_asignado,
    per.email_personal AS profesional_email,
    per.telefono_personal AS profesional_telefono,
    c.fechahora_consulta,
    e.nombre_evaluacion
FROM evaluacion_respuesta er
         INNER JOIN evaluacion_pregunta ep ON er.id_evaluacion_pregunta = ep.id_evaluacion_pregunta
         LEFT JOIN evaluacion e ON ep.id_evaluacion = e.id_evaluacion
         LEFT JOIN consulta c ON e.id_consulta = c.id_consulta
         LEFT JOIN paciente pac ON c.id_paciente = pac.id_paciente
         LEFT JOIN personal per ON c.id_personal = per.id_personal
WHERE er.label_evaluacion_respuesta = 'SUICIDAL'
  AND er.confidence_score > 0.7
  AND er.nivel_riesgo = 'ALTO'
ORDER BY er.confidence_score DESC, er.created_at DESC;

-- View: Staff members and their system user accounts
CREATE OR REPLACE VIEW vw_personal_con_usuario AS
SELECT
    p.id_personal,
    p.doc_personal,
    p.nombre_personal,
    p.email_personal,
    p.telefono_personal,
    p.estatus_personal,
    p.id_usuario,
    u.nombre_usuario,
    GROUP_CONCAT(ur.permisos_roles SEPARATOR ', ') AS roles,
    CASE
        WHEN p.id_usuario IS NULL THEN 'Sin acceso al sistema'
        ELSE 'Con acceso al sistema'
        END AS estado_acceso
FROM personal p
         LEFT JOIN usuario u ON p.id_usuario = u.id_usuario
         LEFT JOIN usuario_roles_mapping urm ON u.id_usuario = urm.id_usuario
         LEFT JOIN usuario_roles ur ON urm.id_roles = ur.id_roles
GROUP BY p.id_personal, p.doc_personal, p.nombre_personal, p.email_personal,
         p.telefono_personal, p.estatus_personal, p.id_usuario, u.nombre_usuario;

-- ============================================================================
-- SECTION 8: USEFUL QUERIES (as comments for reference)
-- ============================================================================

/*
-- Query 1: Find all staff WITHOUT system access
SELECT * FROM personal WHERE id_usuario IS NULL;

-- Query 2: Find all staff WITH system access and their roles
SELECT
    p.nombre_personal,
    p.email_personal,
    u.nombre_usuario,
    GROUP_CONCAT(ur.permisos_roles) AS roles
FROM personal p
INNER JOIN usuario u ON p.id_usuario = u.id_usuario
LEFT JOIN usuario_roles_mapping urm ON u.id_usuario = urm.id_usuario
LEFT JOIN usuario_roles ur ON urm.id_roles = ur.id_roles
GROUP BY p.id_personal, u.id_usuario;

-- Query 3: Check 1:1 relationship integrity
SELECT
    u.id_usuario,
    u.nombre_usuario,
    COUNT(p.id_personal) AS linked_staff_count,
    CASE
        WHEN COUNT(p.id_personal) = 0 THEN 'Usuario sin personal'
        WHEN COUNT(p.id_personal) = 1 THEN 'OK - 1:1'
        ELSE 'ERROR - 1:N (should not happen)'
    END AS relationship_status
FROM usuario u
LEFT JOIN personal p ON u.id_usuario = p.id_usuario
GROUP BY u.id_usuario;

-- Query 4: High-risk responses in last 7 days
SELECT * FROM vw_alertas_alto_riesgo
WHERE fecha_respuesta >= DATE_SUB(NOW(), INTERVAL 7 DAY);

-- Query 5: Sentiment distribution by patient
SELECT
    pac.nombre_paciente,
    er.label_evaluacion_respuesta,
    COUNT(*) AS total,
    AVG(er.confidence_score) AS avg_confidence
FROM evaluacion_respuesta er
INNER JOIN evaluacion_pregunta ep ON er.id_evaluacion_pregunta = ep.id_evaluacion_pregunta
LEFT JOIN evaluacion e ON ep.id_evaluacion = e.id_evaluacion
LEFT JOIN consulta c ON e.id_consulta = c.id_consulta
LEFT JOIN paciente pac ON c.id_paciente = pac.id_paciente
WHERE er.label_evaluacion_respuesta IS NOT NULL
GROUP BY pac.id_paciente, er.label_evaluacion_respuesta
ORDER BY pac.nombre_paciente, total DESC;
*/

-- ============================================================================
-- END OF SCHEMA
-- ============================================================================

-- ╔════════════════════════════════════════════════════════════════════════╗
-- ║                         KEY FEATURES SUMMARY                            ║
-- ╚════════════════════════════════════════════════════════════════════════╝
--
-- ✅ 1:1 RELATIONSHIP: personal.id_usuario -> usuario.id_usuario
--    - UNIQUE constraint enforces one-to-one
--    - NULL allowed (not all staff need system access)
--    - ON DELETE SET NULL (preserve staff record if user deleted)
--    - ON UPDATE CASCADE (auto-update if user ID changes)
--
-- ✅ CONTACT INFORMATION: email_personal and telefono_personal added
--
-- ✅ RISK CLASSIFICATION: nivel_riesgo field (BAJO, MEDIO, ALTO)
--
-- ✅ RNTN INTEGRATION: Full support for 5-class sentiment analysis
--    - Class 0: ANXIETY
--    - Class 1: SUICIDAL (⚠️ HIGH RISK)
--    - Class 2: ANGER
--    - Class 3: SADNESS
--    - Class 4: FRUSTRATION
--
-- ✅ PERFORMANCE: Comprehensive indexes for fast queries
--
-- ✅ VIEWS: Pre-built views for common queries and reports
--
-- ✅ MASTER DATA: Roles, statuses, and sample data included
--
-- ╔════════════════════════════════════════════════════════════════════════╗
-- ║                         USAGE EXAMPLES                                  ║
-- ╚════════════════════════════════════════════════════════════════════════╝
--
-- Create staff WITH system access:
--   1. INSERT INTO usuario (nombre_usuario, pass_usuario) VALUES (?, ?);
--   2. INSERT INTO personal (id_usuario, doc_personal, ...) VALUES (LAST_INSERT_ID(), ?, ...);
--
-- Create staff WITHOUT system access:
--   INSERT INTO personal (id_usuario, doc_personal, ...) VALUES (NULL, ?, ...);
--
-- Link existing staff to user:
--   UPDATE personal SET id_usuario = ? WHERE id_personal = ?;
--
-- Unlink staff from user:
--   UPDATE personal SET id_usuario = NULL WHERE id_personal = ?;
--
-- ╔════════════════════════════════════════════════════════════════════════╗
-- ║                         SECURITY NOTES                                  ║
-- ╚════════════════════════════════════════════════════════════════════════╝
--
-- ⚠️ Change default admin password in production
-- ⚠️ Use BCrypt for password hashing (strength 10+)
-- ⚠️ Implement prepared statements to prevent SQL injection
-- ⚠️ Regular backups recommended
-- ⚠️ Monitor vw_alertas_alto_riesgo view for patient safety
-- ⚠️ Implement row-level security in application layer
--
-- ============================================================================


