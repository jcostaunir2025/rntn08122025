# Database Fix: Remove id_consulta from evaluacion table

## Problem

The `evaluacion` table still has the `id_consulta` column in the database even though:
- ✅ The Java entity `Evaluacion.java` was updated (removed `consulta` reference)
- ✅ The Java entity `Consulta.java` was updated (added `evaluacion` reference)
- ✅ All code was refactored
- ❌ **The database was NOT updated**

## Why This Happened

When we ran the application, Flyway showed:
```
Current version of schema `rntn_sentiment_db`: 10
Schema `rntn_sentiment_db` is up to date. No migration necessary.
```

This means:
1. Flyway saw we're at version 10
2. Our new migration is V6 (which is OLDER than version 10)
3. Flyway skipped it because it only runs migrations with versions HIGHER than the current version

Then Hibernate ran and:
- ✅ Added new columns: `id_evaluacion` to `consulta`, `titulo_evaluacion` and `fecha_evaluacion` to `evaluacion`
- ❌ Did NOT remove `id_consulta` from `evaluacion` (Hibernate never drops columns automatically)

## Solution Options

### Option 1: Manual SQL Fix (RECOMMENDED - FASTEST)

**Steps:**

1. Open MySQL Workbench or your preferred MySQL client

2. Connect to `rntn_sentiment_db` database

3. Run this SQL script:

```sql
USE rntn_sentiment_db;

-- Drop foreign key constraint (if exists)
ALTER TABLE evaluacion DROP FOREIGN KEY evaluacion_ibfk_1;

-- Drop index (if exists)
ALTER TABLE evaluacion DROP INDEX idx_id_consulta;

-- Drop the column
ALTER TABLE evaluacion DROP COLUMN id_consulta;

-- Verify
DESCRIBE evaluacion;
DESCRIBE consulta;
```

4. Restart the application

### Option 2: Run the Fix Script

1. Navigate to project root:
```bash
cd "C:\Users\Javier Costa\Documents\UNIR\CLASES\DWFS\codigo\backend\rntn08122025"
```

2. Run the fix script:
```bash
# If MySQL is in PATH:
mysql -u root -p rntn_sentiment_db < fix_evaluacion_table.sql

# Or connect manually and source the file:
mysql -u root -p
USE rntn_sentiment_db;
SOURCE fix_evaluacion_table.sql;
```

### Option 3: Create New Flyway Migration with Higher Version

Create a new migration file: `V11__remove_id_consulta_from_evaluacion.sql`

```sql
-- V11: Remove id_consulta from evaluacion table

-- Drop FK constraint
SET @drop_fk = (
    SELECT CONCAT('ALTER TABLE evaluacion DROP FOREIGN KEY ', CONSTRAINT_NAME)
    FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = 'rntn_sentiment_db'
      AND TABLE_NAME = 'evaluacion'
      AND COLUMN_NAME = 'id_consulta'
    LIMIT 1
);
PREPARE stmt FROM IFNULL(@drop_fk, 'SELECT 1');
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Drop index
ALTER TABLE evaluacion DROP INDEX IF EXISTS idx_id_consulta;

-- Drop column
ALTER TABLE evaluacion DROP COLUMN IF EXISTS id_consulta;
```

Then run the application - Flyway will execute this automatically.

## Verification Commands

After applying the fix, verify the changes:

```sql
-- Check evaluacion table structure (should NOT have id_consulta)
DESCRIBE evaluacion;

-- Check consulta table structure (should have id_evaluacion)
DESCRIBE consulta;

-- Check foreign keys
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'rntn_sentiment_db'
  AND TABLE_NAME IN ('consulta', 'evaluacion')
  AND REFERENCED_TABLE_NAME IS NOT NULL;
```

Expected results:
- `evaluacion` table: NO `id_consulta` column
- `consulta` table: HAS `id_evaluacion` column with FK to `evaluacion.id_evaluacion`

## Quick MySQL Commands Reference

```bash
# Windows (if MySQL in PATH)
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p

# Commands inside MySQL:
USE rntn_sentiment_db;
SHOW TABLES;
DESCRIBE evaluacion;
DESCRIBE consulta;
```

## What Happens After Fix

Once you apply the fix:
1. ✅ Database structure matches Java entities
2. ✅ Application will start successfully
3. ✅ All endpoints will work correctly
4. ✅ Relationship is correctly N:1 (many consultas → one evaluacion)

## Why V6 Migration Didn't Work

The V6 migration we created has version number 6, but the database is already at version 10. Flyway migrations are applied sequentially and Flyway will NOT run migrations with version numbers lower than the current database version.

**Migration file:** `src/main/resources/db/migration/V6__convert_consulta_evaluacion_relationship.sql`
- Version: 6
- Database version: 10
- Result: ❌ Skipped

**Solution:** Either:
- Use manual SQL fix (fastest)
- Create V11 migration (proper way)

## Recommended Action

**Use Option 1 (Manual SQL Fix)** because:
- ✅ Immediate fix
- ✅ No waiting for Flyway
- ✅ Direct control
- ✅ Can verify immediately

After fixing, document the change so team members know the correct database state.

