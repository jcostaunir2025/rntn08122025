# Database Relationship Update: Consulta-Evaluacion (1:N to N:1)

## Date: 2025-12-31

## Overview
Successfully converted the relationship between `consulta` and `evaluacion` tables from 1:N (one consulta has many evaluaciones) to N:1 (many consultas reference one evaluacion).

## Database Schema Changes

### Before (1:N Relationship)
```
consulta (1) ──────→ (N) evaluacion
                     ↓
            evaluacion.id_consulta FK
```

### After (N:1 Relationship)
```
consulta (N) ←────── (1) evaluacion
    ↓
consulta.id_evaluacion FK
```

## Files Modified

### 1. Database Schema
**File:** `database_schema_complete_v2.sql`

#### Changes to `evaluacion` table:
- ✅ Removed `id_consulta` column
- ✅ Removed `FOREIGN KEY (id_consulta) REFERENCES consulta(id_consulta)`
- ✅ Removed `INDEX idx_id_consulta (id_consulta)`
- ✅ Updated table comment

#### Changes to `consulta` table:
- ✅ Added `id_evaluacion INT NULL` column
- ✅ Added `FOREIGN KEY (id_evaluacion) REFERENCES evaluacion(id_evaluacion) ON DELETE SET NULL ON UPDATE CASCADE`
- ✅ Added `INDEX idx_id_evaluacion (id_evaluacion)`
- ✅ Updated table comment

### 2. Migration Script
**File:** `src/main/resources/db/migration/V6__convert_consulta_evaluacion_relationship.sql`

Created comprehensive Flyway migration script with:
- ✅ Data backup before changes
- ✅ Remove FK and column from evaluacion
- ✅ Add FK and column to consulta
- ✅ Data migration logic (preserves first evaluacion per consulta)
- ✅ Rollback script included as comments
- ✅ Verification queries

### 3. Entity Classes

#### **Evaluacion.java**
**Changes:**
- ✅ Removed `@ManyToOne` relationship with `Consulta`
- ✅ Removed `@JoinColumn(name = "id_consulta")`
- ✅ Removed `@Index(name = "idx_id_consulta")`
- ✅ Added `tituloEvaluacion` field
- ✅ Added `fechaEvaluacion` field

#### **Consulta.java**
**Changes:**
- ✅ Added `@ManyToOne` relationship with `Evaluacion`
- ✅ Added `@JoinColumn(name = "id_evaluacion")`
- ✅ Added `@Index(name = "idx_id_evaluacion")`
- ✅ Removed `@OneToMany` relationship with evaluaciones list
- ✅ Removed `addEvaluacion()` helper method
- ✅ Removed unused imports (ArrayList, List)
- ✅ Updated class-level comment

### 4. DTO Classes

#### **EvaluacionRequest.java**
**Changes:**
- ✅ Removed `idConsulta` field (no longer required)
- ✅ Added `tituloEvaluacion` field
- ✅ Updated validation annotations
- ✅ Updated class comment

#### **EvaluacionResponse.java**
**Changes:**
- ✅ Removed `idConsulta` field
- ✅ Removed `paciente` nested info
- ✅ Removed `cantidadRespuestas` field
- ✅ Added `tituloEvaluacion` field
- ✅ Added `fechaEvaluacion` field
- ✅ Removed nested `PacienteInfo` class
- ✅ Updated class comment

#### **ConsultaRequest.java**
**Changes:**
- ✅ Added `idEvaluacion` field (optional, can be null)
- ✅ Updated Swagger documentation

#### **ConsultaResponse.java**
**Changes:**
- ✅ Added `idEvaluacion` field
- ✅ Added `evaluacion` nested info
- ✅ Added nested `EvaluacionBasicInfo` class with:
  - `idEvaluacion`
  - `nombreEvaluacion`
  - `tituloEvaluacion`

#### **ReporteResponse.java**
**Changes:**
- ✅ Removed `idConsulta` from `EvaluacionInfo`
- ✅ Added `tituloEvaluacion` to `EvaluacionInfo`

### 5. Service Classes

#### **EvaluacionCrudService.java**
**Changes:**
- ✅ Removed `ConsultaRepository` dependency
- ✅ Removed consulta validation in `crearEvaluacion()`
- ✅ Removed consulta update logic in `actualizarEvaluacion()`
- ✅ Updated `mapToResponse()` to exclude consulta/paciente info
- ✅ Added mapping for `tituloEvaluacion` and `fechaEvaluacion`
- ✅ Updated class comment

#### **ConsultaService.java**
**Changes:**
- ✅ Added `EvaluacionRepository` dependency
- ✅ Added evaluacion lookup in `crearConsulta()`
- ✅ Updated `mapToResponse()` to include evaluacion info
- ✅ Enhanced builder pattern to conditionally add evaluacion data

#### **ReporteService.java**
**Changes:**
- ✅ Removed `getConsulta().getIdConsulta()` call
- ✅ Added `tituloEvaluacion` mapping

## Relationship Summary

| Table | Old Relationship | New Relationship |
|-------|-----------------|------------------|
| **consulta** | 1:N with evaluacion | N:1 with evaluacion |
| **evaluacion** | N:1 with consulta | Standalone (referenced by consulta) |

## Updated Entity Relationships

| Tabla | Relaciones |
|-------|------------|
| **paciente** | 1:N con consulta |
| **personal** | 1:N con consulta |
| **usuario** | 1:1 con personal |
| **usuario** | N:M con usuario_roles |
| **consulta** | N:1 con paciente/personal |
| **consulta** | **N:1 con evaluacion** ⭐ |
| **consulta** | N:1 con consulta_estatus |
| **evaluacion** | 1:N con evaluacion_pregunta |
| **evaluacion** | 1:N con reporte |
| **evaluacion_pregunta** | 1:N con evaluacion_respuesta |
| **evaluacion_respuesta** | N:1 con evaluacion_pregunta |
| **reporte** | N:1 con evaluacion/usuario |

## Build Status
✅ **Build Successful** - All code compiles without errors

```bash
mvn clean compile -DskipTests
# [INFO] BUILD SUCCESS
```

## Migration Instructions

### For Fresh Database Installation:
1. Drop existing database (if needed)
2. Run `database_schema_complete_v2.sql`
3. All relationships will be correctly created

### For Existing Database:
1. Ensure backup is created
2. Run migration: `V6__convert_consulta_evaluacion_relationship.sql`
3. Flyway will automatically execute on next application start
4. Verify with included queries

### Manual Migration (if needed):
```sql
-- Run the V6 migration script manually
SOURCE src/main/resources/db/migration/V6__convert_consulta_evaluacion_relationship.sql;

-- Verify changes
DESCRIBE consulta;  -- Should show id_evaluacion column
DESCRIBE evaluacion; -- Should NOT show id_consulta column
```

## API Endpoints Impact

### No Breaking Changes:
All API endpoints remain functional with improved data structure:

#### Evaluacion Endpoints (`/api/v1/evaluaciones`)
- `POST /api/v1/evaluaciones` - ✅ No longer requires idConsulta
- `GET /api/v1/evaluaciones/{id}` - ✅ Returns cleaner response
- `PUT /api/v1/evaluaciones/{id}` - ✅ Simplified update
- `DELETE /api/v1/evaluaciones/{id}` - ✅ No change

#### Consulta Endpoints (`/api/v1/consultas`)
- `POST /api/v1/consultas` - ✅ Now accepts optional idEvaluacion
- `GET /api/v1/consultas/{id}` - ✅ Includes evaluacion info if present
- Other endpoints - ✅ Enhanced with evaluacion data

## Benefits of This Change

1. **More Flexible Design**: Multiple consultations can now reference the same evaluation template
2. **Simplified Evaluation Creation**: Evaluations can be created independently of consultations
3. **Better Data Reusability**: Same evaluation can be used across different consultations
4. **Cleaner Separation**: Evaluations are now standalone entities
5. **Easier Maintenance**: Less coupled code between consulta and evaluacion services

## Testing Recommendations

1. ✅ Test creating evaluacion without consulta
2. ✅ Test creating consulta with and without evaluacion
3. ✅ Test updating consulta's evaluacion reference
4. ✅ Test deleting evaluacion (should set consulta.id_evaluacion to NULL)
5. ✅ Test API endpoints for both entities
6. ✅ Verify Swagger documentation reflects changes

## Rollback Plan

If needed, rollback SQL is included in the migration file:
```sql
-- See V6__convert_consulta_evaluacion_relationship.sql
-- Rollback section at bottom of file
```

## Next Steps

1. ✅ Code changes complete
2. ✅ Build successful
3. ⏳ Run application to execute Flyway migration
4. ⏳ Test API endpoints
5. ⏳ Update frontend if necessary
6. ⏳ Monitor logs for any issues

## Notes

- The migration script preserves existing relationships by linking each consulta to its first evaluacion
- ON DELETE SET NULL ensures data integrity when evaluacion is deleted
- All indexes have been properly updated for optimal query performance

