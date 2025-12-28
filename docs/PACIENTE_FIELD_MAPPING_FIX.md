# Paciente Field Name Mapping Fix

**Date:** 2025-12-28  
**Issue:** Frontend field names don't match backend field names  
**Status:** ✅ **FIXED**

---

## 🔍 Problem Identified

Frontend is sending a payload with different field names than expected by the backend:

### Frontend Payload
```json
{
  "nombrePaciente": "Javier Costa",
  "docPaciente": "1234",
  "fechanacPaciente": "2025-12-03",           // ❌ Backend expects fechaPaciente
  "generoPaciente": "MASCULINO",
  "direccionPaciente": "Amenábar 2438",
  "telefonoPaciente": "11111111",
  "emailPaciente": "aaa@aaa.com",
  "contactoemergenciaPaciente": "pepe",       // ❌ Backend expects contactoPaciente
  "telefonoemergenciaPaciente": "55554444",   // ❌ Backend expects telefonoContactoPaciente
  "estatusPaciente": "ACTIVO"
}
```

### Field Name Mismatches

| Frontend Field Name | Backend Field Name | Status |
|---------------------|-------------------|--------|
| `fechanacPaciente` | `fechaPaciente` | ❌ Mismatch |
| `contactoemergenciaPaciente` | `contactoPaciente` | ❌ Mismatch |
| `telefonoemergenciaPaciente` | `telefonoContactoPaciente` | ❌ Mismatch |

---

## ✅ Solution Applied

Used `@JsonAlias` annotation to accept alternative field names from frontend while keeping backend field names consistent.

### Changes Made

**File:** `src/main/java/com/example/rntn/dto/request/PacienteRequest.java`

```java
import com.fasterxml.jackson.annotation.JsonAlias;

// ...

@JsonAlias({"fechanacPaciente", "fechaNacPaciente"})
@JsonFormat(pattern = "yyyy-MM-dd")
@Schema(description = "Fecha de nacimiento del paciente", example = "1990-05-15")
private LocalDate fechaPaciente;

@JsonAlias({"contactoemergenciaPaciente", "contactoEmergenciaPaciente"})
@Size(max = 100, message = "El nombre del contacto no puede exceder 100 caracteres")
@Schema(description = "Nombre del contacto de emergencia", example = "María Pérez")
private String contactoPaciente;

@JsonAlias({"telefonoemergenciaPaciente", "telefonoEmergenciaPaciente"})
@Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "...")
@Size(max = 20, message = "El teléfono del contacto no puede exceder 20 caracteres")
@Schema(description = "Teléfono del contacto de emergencia", example = "+34 655 444 333")
private String telefonoContactoPaciente;
```

---

## 🎯 How @JsonAlias Works

The `@JsonAlias` annotation allows Jackson to deserialize JSON using alternative field names:

```java
@JsonAlias({"alternativeName1", "alternativeName2"})
private String fieldName;
```

**Behavior:**
- **Deserialization (JSON → Java):** Accepts `fieldName`, `alternativeName1`, or `alternativeName2`
- **Serialization (Java → JSON):** Always uses `fieldName`

This provides **backward compatibility** - the backend can accept both old and new field names from frontend.

---

## 📊 Now Supported Field Name Variations

### Birth Date
✅ `fechaPaciente` (backend standard)  
✅ `fechanacPaciente` (frontend variant)  
✅ `fechaNacPaciente` (camelCase variant)  

### Emergency Contact Name
✅ `contactoPaciente` (backend standard)  
✅ `contactoemergenciaPaciente` (frontend variant)  
✅ `contactoEmergenciaPaciente` (camelCase variant)  

### Emergency Contact Phone
✅ `telefonoContactoPaciente` (backend standard)  
✅ `telefonoemergenciaPaciente` (frontend variant)  
✅ `telefonoEmergenciaPaciente` (camelCase variant)  

---

## ✅ Testing

### Test Payload 1 (Frontend Format)
```json
POST /api/v1/pacientes

{
  "nombrePaciente": "Javier Costa",
  "docPaciente": "1234",
  "fechanacPaciente": "2025-12-03",
  "generoPaciente": "MASCULINO",
  "direccionPaciente": "Amenábar 2438",
  "telefonoPaciente": "11111111",
  "emailPaciente": "aaa@aaa.com",
  "contactoemergenciaPaciente": "pepe",
  "telefonoemergenciaPaciente": "55554444",
  "estatusPaciente": "ACTIVO"
}
```

✅ **Expected Result:** Patient created successfully with all fields mapped correctly

### Test Payload 2 (Backend Format)
```json
POST /api/v1/pacientes

{
  "nombrePaciente": "María García",
  "docPaciente": "5678",
  "fechaPaciente": "1990-05-15",
  "generoPaciente": "FEMENINO",
  "direccionPaciente": "Calle Principal 123",
  "telefonoPaciente": "22222222",
  "emailPaciente": "maria@email.com",
  "contactoPaciente": "Juan García",
  "telefonoContactoPaciente": "33333333",
  "estatusPaciente": "ACTIVO"
}
```

✅ **Expected Result:** Patient created successfully - backward compatible

---

## 🔄 Update Payload

Both field name formats work for updates too:

```json
PUT /api/v1/pacientes/1

{
  "docPaciente": "1234",
  "nombrePaciente": "Javier Costa Updated",
  "fechanacPaciente": "1985-06-20",           // ✅ Works
  "contactoemergenciaPaciente": "Ana Costa",  // ✅ Works
  "telefonoemergenciaPaciente": "66666666"    // ✅ Works
}
```

---

## 📝 Database Fields

Database columns remain unchanged:
- `fecha_paciente` (DATE)
- `contacto_paciente` (VARCHAR(100))
- `telefono_contacto_paciente` (VARCHAR(20))

---

## 🎉 Benefits

✅ **Backward Compatible:** Old frontend code still works  
✅ **Forward Compatible:** New frontend code with corrected names also works  
✅ **No Breaking Changes:** Existing API consumers not affected  
✅ **Clean Backend Code:** Internal field names remain consistent  
✅ **Flexible Integration:** Multiple naming conventions supported  

---

## 🔍 Verification Steps

1. ✅ **Build successful:** `mvn clean compile`
2. ✅ **Application running:** Port 8080
3. ✅ **Flyway disabled:** Manual SQL changes applied
4. ✅ **@JsonAlias added:** All three mismatched fields
5. 🔄 **Testing required:** Send actual frontend payload

---

## 📚 Related Files

- **Entity:** `Paciente.java` - Database column names
- **Request DTO:** `PacienteRequest.java` - **MODIFIED** with @JsonAlias
- **Response DTO:** `PacienteResponse.java` - Returns backend field names
- **Service:** `PacienteService.java` - No changes needed
- **Controller:** `PacienteController.java` - No changes needed

---

## 🚀 Next Steps

1. **Test Create Patient** with frontend payload format
2. **Test Update Patient** with frontend payload format
3. **Verify Database** - Check data is persisted correctly
4. **Frontend Verification** - Confirm changes work with actual frontend

---

## 📖 Jackson @JsonAlias Documentation

**Purpose:** Allows multiple names for a property during deserialization

**Use Case:** Field name compatibility between frontend and backend

**Alternatives:**
- `@JsonProperty` - Single name mapping (we need multiple)
- Custom deserializer - More complex, overkill for this case
- **@JsonAlias** - Perfect for this scenario ✅

---

**Status:** ✅ **READY FOR TESTING**  
**Build:** ✅ **SUCCESS**  
**Application:** 🔄 **RESTARTED**  

Now the backend will accept both frontend and backend field naming conventions!

