-- ============================================================================
-- Migration: Add new fields to Paciente table
-- ============================================================================
-- Version: V10
-- Date: 2025-12-28
-- Description: Add fecha_paciente, genero_paciente, contacto_paciente,
--              and telefono_contacto_paciente fields to paciente table
-- ============================================================================

USE rntn_sentiment_db;

-- Add new columns to paciente table (only if they don't exist)
SET @db_name = 'rntn_sentiment_db';

-- Add fecha_paciente column if it doesn't exist
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'paciente' AND COLUMN_NAME = 'fecha_paciente';
SET @query = IF(@col_exists = 0,
    'ALTER TABLE paciente ADD COLUMN fecha_paciente DATE NULL COMMENT ''Fecha de nacimiento del paciente'' AFTER telefono_paciente',
    'SELECT ''Column fecha_paciente already exists'' AS msg');
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add genero_paciente column if it doesn't exist
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'paciente' AND COLUMN_NAME = 'genero_paciente';
SET @query = IF(@col_exists = 0,
    'ALTER TABLE paciente ADD COLUMN genero_paciente VARCHAR(20) NULL COMMENT ''Género del paciente (MASCULINO, FEMENINO, OTRO, NO_ESPECIFICA)'' AFTER fecha_paciente',
    'SELECT ''Column genero_paciente already exists'' AS msg');
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add contacto_paciente column if it doesn't exist
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'paciente' AND COLUMN_NAME = 'contacto_paciente';
SET @query = IF(@col_exists = 0,
    'ALTER TABLE paciente ADD COLUMN contacto_paciente VARCHAR(100) NULL COMMENT ''Nombre del contacto de emergencia'' AFTER genero_paciente',
    'SELECT ''Column contacto_paciente already exists'' AS msg');
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add telefono_contacto_paciente column if it doesn't exist
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'paciente' AND COLUMN_NAME = 'telefono_contacto_paciente';
SET @query = IF(@col_exists = 0,
    'ALTER TABLE paciente ADD COLUMN telefono_contacto_paciente VARCHAR(20) NULL COMMENT ''Teléfono del contacto de emergencia'' AFTER contacto_paciente',
    'SELECT ''Column telefono_contacto_paciente already exists'' AS msg');
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add indexes for better query performance (only if they don't exist)
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'paciente' AND INDEX_NAME = 'idx_paciente_genero';
SET @query = IF(@index_exists = 0,
    'CREATE INDEX idx_paciente_genero ON paciente(genero_paciente)',
    'SELECT ''Index idx_paciente_genero already exists'' AS msg');
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'paciente' AND INDEX_NAME = 'idx_paciente_fecha';
SET @query = IF(@index_exists = 0,
    'CREATE INDEX idx_paciente_fecha ON paciente(fecha_paciente)',
    'SELECT ''Index idx_paciente_fecha already exists'' AS msg');
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================================
-- END OF MIGRATION
-- ============================================================================

