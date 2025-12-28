# 🚀 Paciente API - New Fields Quick Reference

## New Fields Added (2025-12-28)

| Field | Type | Required | Format/Values | Example |
|-------|------|----------|---------------|---------|
| **fechaPaciente** | Date | No | `yyyy-MM-dd` | `1990-05-15` |
| **generoPaciente** | String | No | MASCULINO, FEMENINO, OTRO, NO_ESPECIFICA | `MASCULINO` |
| **contactoPaciente** | String | No | Max 100 chars | `María Pérez` |
| **telefonoContactoPaciente** | String | No | Numbers, +, -, (), spaces | `+34 655 444 333` |

---

## Request Example

```json
POST /api/v1/pacientes

{
  "docPaciente": "12345678",
  "nombrePaciente": "Juan Pérez García",
  "fechaPaciente": "1985-03-15",
  "generoPaciente": "MASCULINO",
  "contactoPaciente": "María Pérez",
  "telefonoContactoPaciente": "+34 655 444 333"
}
```

---

## Gender Values

- `MASCULINO` - Male
- `FEMENINO` - Female
- `OTRO` - Other
- `NO_ESPECIFICA` - Not specified

---

## Database Migration

### Automatic (Flyway)
Migration runs on app startup: `V10__add_paciente_new_fields.sql`

### Manual
Run script: `docs/db_update_paciente_new_fields.sql`

---

## Frontend Integration

```typescript
interface Paciente {
  fechaPaciente?: string;              // "1990-05-15"
  generoPaciente?: string;             // "MASCULINO"
  contactoPaciente?: string;           // "María Pérez"
  telefonoContactoPaciente?: string;   // "+34 655 444 333"
}
```

---

## All Fields Optional ✅
No breaking changes - existing API calls still work without new fields.

---

**Last Updated:** 2025-12-28

