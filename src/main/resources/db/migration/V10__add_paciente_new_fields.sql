-- ============================================================================
-- Migration: Add new fields to Paciente table
-- ============================================================================
-- Version: V10
-- Date: 2025-12-28
-- Description: Add fecha_paciente, genero_paciente, contacto_paciente,
--              and telefono_contacto_paciente fields to paciente table
-- ============================================================================

USE rntn_sentiment_db;

-- Add new columns to paciente table
ALTER TABLE paciente
    ADD COLUMN fecha_paciente DATE NULL COMMENT 'Fecha de nacimiento del paciente' AFTER telefono_paciente,
    ADD COLUMN genero_paciente VARCHAR(20) NULL COMMENT 'Género del paciente (MASCULINO, FEMENINO, OTRO, NO_ESPECIFICA)' AFTER fecha_paciente,
    ADD COLUMN contacto_paciente VARCHAR(100) NULL COMMENT 'Nombre del contacto de emergencia' AFTER genero_paciente,
    ADD COLUMN telefono_contacto_paciente VARCHAR(20) NULL COMMENT 'Teléfono del contacto de emergencia' AFTER contacto_paciente;

-- Add indexes for better query performance
CREATE INDEX idx_paciente_genero ON paciente(genero_paciente);
CREATE INDEX idx_paciente_fecha ON paciente(fecha_paciente);

-- ============================================================================
-- END OF MIGRATION
-- ============================================================================

