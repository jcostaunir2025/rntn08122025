-- Migración de datos maestros
-- Versión: V2
-- Descripción: Insertar datos maestros (roles, estados, preguntas estándar, usuario admin)

-- Insertar roles predefinidos
INSERT INTO usuario_roles (permisos_roles) VALUES
('ADMIN'),
('DOCTOR'),
('ENFERMERO'),
('RECEPCIONISTA'),
('ANALISTA');

-- Insertar estados de consulta
INSERT INTO consulta_estatus (nombre_consulta_estatus) VALUES
('PENDIENTE'),
('EN_PROGRESO'),
('COMPLETADA'),
('CANCELADA'),
('REPROGRAMADA');

-- Insertar usuario administrador (password: admin123 - debe ser hasheado en producción)
-- Password hash generado con BCrypt
INSERT INTO usuario (nombre_usuario, pass_usuario) VALUES
('admin', '$2a$10$slYQmyNdGzTn7ZLBXBChFOR.vY0QJLx2z9q3OXZ2.rLQfKVRKxTZ6');

-- Asignar rol ADMIN al usuario admin
INSERT INTO usuario_roles_mapping (id_usuario, id_roles)
SELECT u.id_usuario, r.id_roles
FROM usuario u, usuario_roles r
WHERE u.nombre_usuario = 'admin' AND r.permisos_roles = 'ADMIN';

-- Datos de ejemplo: Personal médico
INSERT INTO personal (doc_personal, nombre_personal, estatus_personal) VALUES
('DOC-001', 'Dra. María García López', 'ACTIVO'),
('DOC-002', 'Dr. Juan Martínez Pérez', 'ACTIVO'),
('ENF-001', 'Enfermera Ana Rodríguez', 'ACTIVO');

-- Preguntas de evaluación estándar para análisis de sentimientos
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
('¿Ha tenido pensamientos de hacerse daño?');

