-- Migración inicial: Crear esquema de base de datos RNTN
-- Versión: V1
-- Descripción: Crear tablas para gestión de pacientes, consultas, evaluaciones y reportes

-- Tabla: paciente
CREATE TABLE paciente (
    id_paciente INT PRIMARY KEY AUTO_INCREMENT,
    doc_paciente VARCHAR(20) UNIQUE NOT NULL,
    nombre_paciente VARCHAR(100) NOT NULL,
    direccion_paciente VARCHAR(255),
    email_paciente VARCHAR(100),
    telefono_paciente VARCHAR(20),
    estatus_paciente VARCHAR(20) DEFAULT 'ACTIVO',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_doc_paciente (doc_paciente),
    INDEX idx_estatus_paciente (estatus_paciente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: personal
CREATE TABLE personal (
    id_personal INT PRIMARY KEY AUTO_INCREMENT,
    doc_personal VARCHAR(20) UNIQUE NOT NULL,
    nombre_personal VARCHAR(100) NOT NULL,
    estatus_personal VARCHAR(20) DEFAULT 'ACTIVO',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_doc_personal (doc_personal)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: usuario
CREATE TABLE usuario (
    id_usuario INT PRIMARY KEY AUTO_INCREMENT,
    nombre_usuario VARCHAR(50) UNIQUE NOT NULL,
    pass_usuario VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_nombre_usuario (nombre_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: usuario_roles
CREATE TABLE usuario_roles (
    id_roles INT PRIMARY KEY AUTO_INCREMENT,
    permisos_roles VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla intermedia: usuario_roles_mapping
CREATE TABLE usuario_roles_mapping (
    id_usuario INT NOT NULL,
    id_roles INT NOT NULL,
    PRIMARY KEY (id_usuario, id_roles),
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    FOREIGN KEY (id_roles) REFERENCES usuario_roles(id_roles) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: consulta_estatus
CREATE TABLE consulta_estatus (
    id_consulta_estatus INT PRIMARY KEY AUTO_INCREMENT,
    nombre_consulta_estatus VARCHAR(50) UNIQUE NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: consulta
CREATE TABLE consulta (
    id_consulta INT PRIMARY KEY AUTO_INCREMENT,
    id_paciente INT NOT NULL,
    id_personal INT NOT NULL,
    fechahora_consulta TIMESTAMP NOT NULL,
    fechafin_consulta TIMESTAMP NULL,
    estatus_consulta VARCHAR(50) DEFAULT 'PENDIENTE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_paciente) REFERENCES paciente(id_paciente) ON DELETE CASCADE,
    FOREIGN KEY (id_personal) REFERENCES personal(id_personal) ON DELETE RESTRICT,
    INDEX idx_id_paciente (id_paciente),
    INDEX idx_id_personal (id_personal),
    INDEX idx_fechahora_consulta (fechahora_consulta),
    INDEX idx_estatus_consulta (estatus_consulta)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: evaluacion
CREATE TABLE evaluacion (
    id_evaluacion INT PRIMARY KEY AUTO_INCREMENT,
    id_consulta INT NOT NULL,
    nombre_evaluacion VARCHAR(100) NOT NULL,
    area_evaluacion VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_consulta) REFERENCES consulta(id_consulta) ON DELETE CASCADE,
    INDEX idx_id_consulta (id_consulta)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: evaluacion_pregunta
CREATE TABLE evaluacion_pregunta (
    id_evaluacion_pregunta INT PRIMARY KEY AUTO_INCREMENT,
    texto_evaluacion_pregunta TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: evaluacion_respuesta (⭐ INTEGRACIÓN RNTN)
CREATE TABLE evaluacion_respuesta (
    id_evaluacion_respuesta INT PRIMARY KEY AUTO_INCREMENT,
    id_evaluacion_pregunta INT NOT NULL,
    texto_evaluacion_respuesta TEXT NOT NULL,
    texto_set_evaluacion_respuesta TEXT,
    label_evaluacion_respuesta VARCHAR(50),
    confidence_score DOUBLE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_evaluacion_pregunta) REFERENCES evaluacion_pregunta(id_evaluacion_pregunta) ON DELETE CASCADE,
    INDEX idx_id_evaluacion_pregunta (id_evaluacion_pregunta),
    INDEX idx_label_evaluacion_respuesta (label_evaluacion_respuesta)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: reporte
CREATE TABLE reporte (
    id_reporte INT PRIMARY KEY AUTO_INCREMENT,
    id_usuario INT NOT NULL,
    id_evaluacion INT NOT NULL,
    fechageneracion_reporte TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    nombre_reporte VARCHAR(100) NOT NULL,
    resultado_reporte TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    FOREIGN KEY (id_evaluacion) REFERENCES evaluacion(id_evaluacion) ON DELETE CASCADE,
    INDEX idx_id_usuario (id_usuario),
    INDEX idx_id_evaluacion (id_evaluacion),
    INDEX idx_fechageneracion_reporte (fechageneracion_reporte)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

