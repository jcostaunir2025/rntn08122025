# ✅ FK Reference Changed to Primary Key

## Date: December 21, 2025
## Status: COMPLETE

---

## 🎯 Change Summary

Modified the `consulta.estatus_consulta` foreign key to reference the **primary key** (`id_consulta_estatus`) instead of the unique field (`nombre_consulta_estatus`) in the `consulta_estatus` table.

---

## 📝 What Changed

### Before (VARCHAR referencing unique field)
```sql
CREATE TABLE consulta (
    ...
    estatus_consulta VARCHAR(50) NOT NULL DEFAULT 'PENDIENTE',
    ...
    FOREIGN KEY (estatus_consulta) 
        REFERENCES consulta_estatus(nombre_consulta_estatus)
        ON DELETE RESTRICT ON UPDATE CASCADE
);
```

### After (INT referencing primary key)
```sql
CREATE TABLE consulta (
    ...
    estatus_consulta INT NOT NULL,
    ...
    FOREIGN KEY (estatus_consulta) 
        REFERENCES consulta_estatus(id_consulta_estatus)
        ON DELETE RESTRICT ON UPDATE CASCADE
);
```

---

## ✅ Benefits of This Change

### 1. **Better Performance** ⚡
- INT comparison is **faster** than VARCHAR comparison
- 4 bytes (INT) vs 50 bytes (VARCHAR) per record
- Smaller indexes, faster JOIN operations

### 2. **Standard Database Design** 📐
- **Best practice**: Foreign keys should reference primary keys
- More maintainable and predictable
- Follows normalization principles

### 3. **Data Stability** 🔒
- Status IDs never change (stable references)
- Status names might be renamed (e.g., localization)
- No issues with character encoding or case sensitivity

### 4. **Storage Efficiency** 💾
- Smaller table size (especially with many consultations)
- Smaller backup files
- Faster table scans

### 5. **Cleaner Application Code** 💻
```java
// After: Use simple integers
consulta.setEstatusConsulta(1); // PENDIENTE

// Before: Use strings (prone to typos)
consulta.setEstatusConsulta("PENDIENTE");
```

---

## 📊 Status ID Mapping

Based on the master data insertion order:

| ID | Status Name | Description |
|----|-------------|-------------|
| 1 | PENDIENTE | Pending consultation |
| 2 | EN_PROGRESO | In progress |
| 3 | COMPLETADA | Completed |
| 4 | CANCELADA | Cancelled |
| 5 | REPROGRAMADA | Rescheduled |
| 6 | NO_ASISTIO | No-show |

---

## 📂 Files Modified

### 1. ✅ database_schema_complete_v2.sql
**Changes:**
- `estatus_consulta` column type: `VARCHAR(50)` → `INT`
- FK reference: `nombre_consulta_estatus` → `id_consulta_estatus`
- Updated comments to reflect PK reference
- Removed DEFAULT 'PENDIENTE' (will use ID 1 instead in application)

### 2. ✅ V7__change_consulta_estatus_to_pk_reference.sql (NEW)
**Purpose:** Migration script for existing databases

**Steps:**
1. Adds new INT column `estatus_consulta_id`
2. Migrates data: maps status names → status IDs
3. Drops old VARCHAR column
4. Renames new column to `estatus_consulta`
5. Adds FK constraint to `id_consulta_estatus`
6. Verifies constraint

---

## 🔄 Data Migration Strategy

### For Existing Databases

The V7 migration handles data conversion:

```sql
-- Maps: 'PENDIENTE' → 1, 'EN_PROGRESO' → 2, etc.
UPDATE consulta c
INNER JOIN consulta_estatus ce 
    ON c.estatus_consulta = ce.nombre_consulta_estatus
SET c.estatus_consulta_id = ce.id_consulta_estatus;
```

### For New Databases

Use the updated `database_schema_complete_v2.sql` directly - no migration needed.

---

## 💻 Application Code Changes Needed

### Java Entity (Consulta.java)

**Before:**
```java
@Column(name = "estatus_consulta", length = 50)
private String estatusConsulta = "PENDIENTE";
```

**After:**
```java
@Column(name = "estatus_consulta")
private Integer estatusConsulta; // Will hold ID, not name

// Optional: Add relationship to ConsultaEstatus entity
@ManyToOne
@JoinColumn(name = "estatus_consulta", insertable = false, updatable = false)
private ConsultaEstatus consultaEstatusObj;
```

### ConsultaEstatus Entity (NEW or UPDATE)

```java
@Entity
@Table(name = "consulta_estatus")
public class ConsultaEstatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_consulta_estatus")
    private Integer idConsultaEstatus;
    
    @Column(name = "nombre_consulta_estatus")
    private String nombreConsultaEstatus;
    
    // Getters and setters
}
```

### Service/Repository Usage

**Before:**
```java
consulta.setEstatusConsulta("PENDIENTE");
```

**After (Option 1 - Use IDs directly):**
```java
consulta.setEstatusConsulta(1); // PENDIENTE
```

**After (Option 2 - Use constants):**
```java
public class ConsultaEstatus {
    public static final Integer PENDIENTE = 1;
    public static final Integer EN_PROGRESO = 2;
    public static final Integer COMPLETADA = 3;
    public static final Integer CANCELADA = 4;
    public static final Integer REPROGRAMADA = 5;
    public static final Integer NO_ASISTIO = 6;
}

// Usage
consulta.setEstatusConsulta(ConsultaEstatus.PENDIENTE);
```

### Queries with Status Name

**Before (direct field):**
```java
SELECT c FROM Consulta c WHERE c.estatusConsulta = 'PENDIENTE'
```

**After (requires JOIN):**
```java
SELECT c FROM Consulta c 
JOIN c.consultaEstatusObj ce 
WHERE ce.nombreConsultaEstatus = 'PENDIENTE'

// OR use ID directly
SELECT c FROM Consulta c WHERE c.estatusConsulta = 1
```

---

## 🧪 Testing

### SQL Tests

```sql
-- Test 1: Insert with valid status ID (should succeed)
INSERT INTO consulta (id_paciente, id_personal, fechahora_consulta, estatus_consulta) 
VALUES (1, 1, NOW(), 1);
-- ✅ Success

-- Test 2: Insert with invalid status ID (should fail)
INSERT INTO consulta (id_paciente, id_personal, fechahora_consulta, estatus_consulta) 
VALUES (1, 1, NOW(), 999);
-- ❌ Error: foreign key constraint fails

-- Test 3: Query with status name (requires JOIN)
SELECT 
    c.id_consulta,
    ce.nombre_consulta_estatus as estatus,
    c.fechahora_consulta
FROM consulta c
INNER JOIN consulta_estatus ce ON c.estatus_consulta = ce.id_consulta_estatus;
-- ✅ Returns consultations with readable status names

-- Test 4: Delete status in use (should fail)
DELETE FROM consulta_estatus WHERE id_consulta_estatus = 1;
-- ❌ Error: Cannot delete, foreign key constraint

-- Test 5: Performance comparison
EXPLAIN SELECT * FROM consulta WHERE estatus_consulta = 1;
-- Shows index usage, should be faster than VARCHAR comparison
```

---

## 📋 Deployment Checklist

### For New Databases
- [x] Updated `database_schema_complete_v2.sql` with INT FK to PK
- [x] Run complete schema script
- [ ] Update Java entities (Consulta, ConsultaEstatus)
- [ ] Update DTOs if needed
- [ ] Update service layer code
- [ ] Update tests

### For Existing Databases
- [x] Created V7 migration script
- [ ] Backup database before migration
- [ ] Run V7 migration: `mvn flyway:migrate`
- [ ] Verify migration success
- [ ] Update Java entities
- [ ] Update application code
- [ ] Test thoroughly
- [ ] Deploy updated application

---

## 🔧 Rollback Plan (If Needed)

If issues occur after migration, you can rollback:

```sql
-- 1. Drop FK constraint
ALTER TABLE consulta DROP FOREIGN KEY fk_consulta_estatus;

-- 2. Add VARCHAR column back
ALTER TABLE consulta ADD COLUMN estatus_consulta_old VARCHAR(50);

-- 3. Restore status names
UPDATE consulta c
INNER JOIN consulta_estatus ce ON c.estatus_consulta = ce.id_consulta_estatus
SET c.estatus_consulta_old = ce.nombre_consulta_estatus;

-- 4. Drop INT column
ALTER TABLE consulta DROP COLUMN estatus_consulta;

-- 5. Rename VARCHAR column
ALTER TABLE consulta CHANGE estatus_consulta_old estatus_consulta VARCHAR(50) NOT NULL;

-- 6. Re-add old FK constraint
ALTER TABLE consulta
ADD CONSTRAINT fk_consulta_estatus
FOREIGN KEY (estatus_consulta) 
REFERENCES consulta_estatus(nombre_consulta_estatus);
```

---

## ⚠️ Important Notes

### 1. **Breaking Change**
This is a **breaking change** for existing code. All code that sets or reads `consulta.estatus_consulta` must be updated.

### 2. **API Responses**
DTOs should include status name for API consumers:

```java
@Data
public class ConsultaResponse {
    private Integer idConsulta;
    private Integer estatusConsulta; // ID
    private String estatusConsultaNombre; // Name for display
    // ... other fields
}
```

### 3. **Database Views**
Update any database views that reference `estatus_consulta` to include JOIN with `consulta_estatus`.

---

## 📊 Impact Analysis

### High Impact ⚠️
- **Java entities**: Must update Consulta entity
- **Service layer**: Must update status assignment logic
- **Queries**: Must update queries that filter by status
- **Tests**: Must update test data

### Medium Impact ⚡
- **DTOs**: Should add status name field
- **Controllers**: May need mapping logic
- **Views**: Database views need updates

### Low Impact ✅
- **Database performance**: Improved (positive)
- **Storage**: Reduced (positive)
- **Indexes**: More efficient (positive)

---

## ✅ Summary

### What Was Done
- ✅ Changed `consulta.estatus_consulta` from VARCHAR to INT
- ✅ Updated FK to reference `id_consulta_estatus` (PK) instead of `nombre_consulta_estatus`
- ✅ Created V7 migration for data conversion
- ✅ Updated `database_schema_complete_v2.sql`

### Why This Change
- ✅ Better performance (INT vs VARCHAR)
- ✅ Standard database pattern (FK to PK)
- ✅ Smaller storage footprint
- ✅ Stable references (IDs don't change)

### Next Steps
1. Run V7 migration on existing databases
2. Update Java entities and application code
3. Test thoroughly
4. Deploy updated application

---

**Change Date:** December 21, 2025  
**Migration:** V7__change_consulta_estatus_to_pk_reference.sql  
**Type:** Schema Enhancement  
**Impact:** Medium (requires code updates)  
**Status:** ✅ **COMPLETE**

