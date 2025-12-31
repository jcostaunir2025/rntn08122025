-- ============================================================================
-- MANUAL DATABASE FIX: Remove id_consulta from evaluacion table
-- ============================================================================
-- Run this script manually to fix the database structure
-- ============================================================================

USE rntn_sentiment_db;

-- Step 1: Check if id_consulta column exists in evaluacion table
SELECT COLUMN_NAME
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'rntn_sentiment_db'
  AND TABLE_NAME = 'evaluacion'
  AND COLUMN_NAME = 'id_consulta';

-- Step 2: Drop the foreign key constraint (if exists)
SET @drop_fk_query = (
    SELECT CONCAT('ALTER TABLE evaluacion DROP FOREIGN KEY ', CONSTRAINT_NAME, ';')
    FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = 'rntn_sentiment_db'
      AND TABLE_NAME = 'evaluacion'
      AND COLUMN_NAME = 'id_consulta'
      AND REFERENCED_TABLE_NAME IS NOT NULL
    LIMIT 1
);

-- Execute the drop FK query if it exists
SET @drop_fk_query = IFNULL(@drop_fk_query, 'SELECT "No FK to drop" AS Info');
PREPARE stmt FROM @drop_fk_query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 3: Drop the index (if exists)
SET @drop_idx_query = (
    SELECT CONCAT('ALTER TABLE evaluacion DROP INDEX ', INDEX_NAME, ';')
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = 'rntn_sentiment_db'
      AND TABLE_NAME = 'evaluacion'
      AND COLUMN_NAME = 'id_consulta'
    LIMIT 1
);

-- Execute the drop index query if it exists
SET @drop_idx_query = IFNULL(@drop_idx_query, 'SELECT "No index to drop" AS Info');
PREPARE stmt FROM @drop_idx_query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 4: Drop the id_consulta column
ALTER TABLE evaluacion DROP COLUMN IF EXISTS id_consulta;

-- Step 5: Verify the change
DESCRIBE evaluacion;

-- Step 6: Verify consulta table has id_evaluacion
DESCRIBE consulta;

-- Step 7: Show the relationship
SELECT
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'rntn_sentiment_db'
  AND (
    (TABLE_NAME = 'consulta' AND COLUMN_NAME = 'id_evaluacion')
    OR
    (TABLE_NAME = 'evaluacion' AND COLUMN_NAME = 'id_consulta')
  );

SELECT '✅ Database structure updated successfully!' AS Status;

