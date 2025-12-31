-- ============================================================================
-- MIGRATION SCRIPT: Convert Consulta-Evaluacion Relationship from 1:N to N:1
-- ============================================================================
-- Version: V6
-- Date: 2025-12-31
-- Description: Converts the relationship between consulta and evaluacion tables
--              from 1:N (one consulta -> many evaluaciones)
--              to N:1 (many consultas -> one evaluacion)
--
-- CHANGES:
--   1. Remove id_consulta FK from evaluacion table
--   2. Add id_evaluacion FK to consulta table
--   3. Migrate existing data (preserve relationships where possible)
--
-- ⚠️ IMPORTANT: Review and test on a backup database first!
-- ============================================================================

USE rntn_sentiment_db;

-- ============================================================================
-- STEP 1: Backup existing relationships (for data migration)
-- ============================================================================

-- Create temporary table to store current relationships
CREATE TABLE IF NOT EXISTS _migration_consulta_evaluacion (
    id_consulta INT,
    id_evaluacion INT,
    PRIMARY KEY (id_consulta, id_evaluacion)
) ENGINE=InnoDB;

-- Store current relationships
INSERT INTO _migration_consulta_evaluacion (id_consulta, id_evaluacion)
SELECT id_consulta, id_evaluacion
FROM evaluacion
WHERE id_consulta IS NOT NULL;

-- ============================================================================
-- STEP 2: Modify evaluacion table (remove id_consulta)
-- ============================================================================

-- Drop foreign key constraint
ALTER TABLE evaluacion
DROP FOREIGN KEY evaluacion_ibfk_1;

-- Drop index
ALTER TABLE evaluacion
DROP INDEX idx_id_consulta;

-- Remove id_consulta column
ALTER TABLE evaluacion
DROP COLUMN id_consulta;

-- ============================================================================
-- STEP 3: Modify consulta table (add id_evaluacion)
-- ============================================================================

-- Add id_evaluacion column
ALTER TABLE consulta
ADD COLUMN id_evaluacion INT NULL
COMMENT 'N:1 FK to evaluacion - Many consultations can use same evaluation'
AFTER id_personal;

-- Create index
ALTER TABLE consulta
ADD INDEX idx_id_evaluacion (id_evaluacion);

-- Add foreign key constraint
ALTER TABLE consulta
ADD CONSTRAINT fk_consulta_evaluacion
FOREIGN KEY (id_evaluacion) REFERENCES evaluacion(id_evaluacion)
ON DELETE SET NULL
ON UPDATE CASCADE;

-- ============================================================================
-- STEP 4: Migrate data (use first evaluacion for each consulta)
-- ============================================================================

-- Update consulta table with the first evaluacion from each consultation
UPDATE consulta c
INNER JOIN (
    SELECT id_consulta, MIN(id_evaluacion) as id_evaluacion
    FROM _migration_consulta_evaluacion
    GROUP BY id_consulta
) m ON c.id_consulta = m.id_consulta
SET c.id_evaluacion = m.id_evaluacion;

-- ============================================================================
-- STEP 5: Cleanup temporary table
-- ============================================================================

-- Drop temporary migration table
DROP TABLE IF EXISTS _migration_consulta_evaluacion;

-- ============================================================================
-- VERIFICATION QUERIES
-- ============================================================================

-- Show consultas with their evaluaciones
-- SELECT c.id_consulta, c.id_evaluacion, e.nombre_evaluacion
-- FROM consulta c
-- LEFT JOIN evaluacion e ON c.id_evaluacion = e.id_evaluacion;

-- Count consultas without evaluacion
-- SELECT COUNT(*) as consultas_sin_evaluacion
-- FROM consulta
-- WHERE id_evaluacion IS NULL;

-- Show evaluacion table structure (verify id_consulta is removed)
-- DESCRIBE evaluacion;

-- Show consulta table structure (verify id_evaluacion is added)
-- DESCRIBE consulta;

-- ============================================================================
-- ROLLBACK SCRIPT (if needed)
-- ============================================================================
-- ⚠️ Save this for emergency rollback

/*
-- Rollback: Restore original structure

-- Add id_consulta back to evaluacion
ALTER TABLE evaluacion
ADD COLUMN id_consulta INT NOT NULL AFTER id_evaluacion;

ALTER TABLE evaluacion
ADD INDEX idx_id_consulta (id_consulta);

ALTER TABLE evaluacion
ADD CONSTRAINT evaluacion_ibfk_1
FOREIGN KEY (id_consulta) REFERENCES consulta(id_consulta)
ON DELETE CASCADE;

-- Remove id_evaluacion from consulta
ALTER TABLE consulta
DROP FOREIGN KEY fk_consulta_evaluacion;

ALTER TABLE consulta
DROP INDEX idx_id_evaluacion;

ALTER TABLE consulta
DROP COLUMN id_evaluacion;
*/

-- ============================================================================
-- END OF MIGRATION SCRIPT
-- ============================================================================

