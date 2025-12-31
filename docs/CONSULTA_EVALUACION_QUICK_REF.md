# Quick Reference: Consulta-Evaluacion N:1 Relationship

## Date: 2025-12-31

## What Changed?

**Before:** One Consulta → Many Evaluaciones (1:N)  
**After:** Many Consultas → One Evaluacion (N:1)

## Database Changes

### Consulta Table (NEW)
```sql
ALTER TABLE consulta 
ADD COLUMN id_evaluacion INT NULL;

ADD FOREIGN KEY (id_evaluacion) 
REFERENCES evaluacion(id_evaluacion) 
ON DELETE SET NULL;
```

### Evaluacion Table (REMOVED)
```sql
ALTER TABLE evaluacion 
DROP COLUMN id_consulta;
```

## Java Entity Changes

### Before
```java
// Evaluacion.java
@ManyToOne
@JoinColumn(name = "id_consulta", nullable = false)
private Consulta consulta;

// Consulta.java
@OneToMany(mappedBy = "consulta")
private List<Evaluacion> evaluaciones;
```

### After
```java
// Evaluacion.java
// No consulta reference

// Consulta.java
@ManyToOne
@JoinColumn(name = "id_evaluacion")
private Evaluacion evaluacion;
```

## API Usage Examples

### Creating Evaluacion (Standalone)
```json
POST /api/v1/evaluaciones
{
  "tituloEvaluacion": "Evaluación Inicial",
  "nombreEvaluacion": "Evaluación de Riesgo Suicida",
  "areaEvaluacion": "SALUD_MENTAL"
}
```

### Creating Consulta with Evaluacion
```json
POST /api/v1/consultas
{
  "idPaciente": 1,
  "idPersonal": 1,
  "idEvaluacion": 1,  // Optional - links to existing evaluacion
  "fechahoraConsulta": "2025-12-31T10:00:00",
  "estatusConsulta": 1
}
```

### Updating Consulta's Evaluacion
```json
PUT /api/v1/consultas/1
{
  "idEvaluacion": 2  // Change to different evaluacion
}
```

## Response Examples

### Evaluacion Response (Simplified)
```json
{
  "idEvaluacion": 1,
  "tituloEvaluacion": "Evaluación Inicial",
  "nombreEvaluacion": "Evaluación de Riesgo Suicida",
  "fechaEvaluacion": "2025-12-31T10:00:00",
  "areaEvaluacion": "SALUD_MENTAL",
  "createdAt": "2025-12-31T08:00:00",
  "updatedAt": "2025-12-31T08:00:00"
}
```

### Consulta Response (Enhanced)
```json
{
  "idConsulta": 1,
  "idEvaluacion": 1,
  "evaluacion": {
    "idEvaluacion": 1,
    "nombreEvaluacion": "Evaluación de Riesgo Suicida",
    "tituloEvaluacion": "Evaluación Inicial"
  },
  "paciente": { ... },
  "personal": { ... },
  "fechahoraConsulta": "2025-12-31T10:00:00",
  "estatusConsulta": 1
}
```

## Migration Checklist

- [x] Update database schema file
- [x] Create Flyway migration script (V6)
- [x] Update Evaluacion entity
- [x] Update Consulta entity
- [x] Update DTOs (Request/Response)
- [x] Update EvaluacionCrudService
- [x] Update ConsultaService
- [x] Update ReporteService
- [x] Build successful
- [ ] Run application (execute migration)
- [ ] Test API endpoints
- [ ] Update frontend code

## Testing Commands

```bash
# Build
mvn clean compile -DskipTests

# Run application (migration will execute automatically)
mvn spring-boot:run

# Test endpoints
curl -X POST http://localhost:8080/api/v1/evaluaciones \
  -H "Content-Type: application/json" \
  -d '{"nombreEvaluacion":"Test","areaEvaluacion":"TEST"}'

curl -X POST http://localhost:8080/api/v1/consultas \
  -H "Content-Type: application/json" \
  -d '{"idPaciente":1,"idPersonal":1,"idEvaluacion":1,"fechahoraConsulta":"2025-12-31T10:00:00","estatusConsulta":1}'
```

## Common Queries

### Find consultas using specific evaluacion
```sql
SELECT c.*, e.nombre_evaluacion
FROM consulta c
LEFT JOIN evaluacion e ON c.id_evaluacion = e.id_evaluacion
WHERE c.id_evaluacion = 1;
```

### Find consultas without evaluacion
```sql
SELECT * FROM consulta
WHERE id_evaluacion IS NULL;
```

### Count consultas per evaluacion
```sql
SELECT 
  e.id_evaluacion,
  e.nombre_evaluacion,
  COUNT(c.id_consulta) as total_consultas
FROM evaluacion e
LEFT JOIN consulta c ON c.id_evaluacion = e.id_evaluacion
GROUP BY e.id_evaluacion;
```

## Benefits

✅ **Flexibility**: Create evaluaciones independently  
✅ **Reusability**: Same evaluacion for multiple consultas  
✅ **Simplicity**: Cleaner service layer code  
✅ **Performance**: Better query optimization  
✅ **Maintainability**: Less coupling between entities

## Important Notes

⚠️ **Breaking Changes**: None! All API endpoints remain compatible  
⚠️ **Data Migration**: Automatically preserves existing relationships  
⚠️ **Null Safety**: Consulta.id_evaluacion can be NULL  
⚠️ **Cascade Delete**: Deleting evaluacion sets consulta.id_evaluacion to NULL

## Support

For issues or questions:
- Check: `docs/FK_CONSULTA_EVALUACION_CONVERSION.md`
- Migration script: `src/main/resources/db/migration/V6__convert_consulta_evaluacion_relationship.sql`
- Build logs: Review compilation output

