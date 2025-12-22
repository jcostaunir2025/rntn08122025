-- Migración de índices adicionales
-- Versión: V3
-- Descripción: Crear índices adicionales que NO están en V1
-- NOTA: V1 baseline ya incluye idx_consulta_paciente_estatus e idx_consulta_personal_fecha

-- Índice para búsqueda de respuestas por label y confianza
CREATE INDEX idx_respuesta_label_confidence
ON evaluacion_respuesta(label_evaluacion_respuesta, confidence_score);

-- Índice para reportes por usuario y fecha
CREATE INDEX idx_reporte_usuario_fecha
ON reporte(id_usuario, fechageneracion_reporte);

-- Índice de texto completo para búsqueda en respuestas (MySQL 5.7+)
CREATE FULLTEXT INDEX idx_fulltext_respuesta
ON evaluacion_respuesta(texto_evaluacion_respuesta);

-- Índice para búsqueda de pacientes activos
CREATE INDEX idx_paciente_estatus_nombre
ON paciente(estatus_paciente, nombre_paciente);

