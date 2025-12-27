# 🛡️ Exception Handler Quick Reference

## 📋 When to Use Each Exception

### ✅ ResourceNotFoundException
**Use when:** A requested resource doesn't exist

```java
// Example 1: Simple not found
throw new ResourceNotFoundException("Paciente", id);
// Response: "Paciente not found with id: 123"

// Example 2: Search by field
throw new ResourceNotFoundException("Usuario", "username", "john");
// Response: "Usuario not found with username: 'john'"

// Example 3: Custom message
throw new ResourceNotFoundException("The requested evaluation does not exist");
```

---

### ✅ BusinessException
**Use when:** Business rules are violated

```java
// Example 1: Simple business error
throw new BusinessException("Cannot delete patient with active consultations");

// Example 2: With HTTP status
throw new BusinessException(
    "Document number already registered",
    HttpStatus.CONFLICT
);

// Example 3: With details and suggestions
Map<String, Object> details = Map.of(
    "reason", "Duplicate document",
    "documentNumber", docPaciente,
    "suggestion", "Use a different document number"
);
throw new BusinessException(
    "Document number already exists in the system",
    "Duplicate Document",
    HttpStatus.CONFLICT,
    details
);
```

---

### ✅ IllegalArgumentException
**Use when:** Method receives invalid arguments

```java
// Example: Invalid parameter value
if (edad < 0 || edad > 150) {
    throw new IllegalArgumentException("Age must be between 0 and 150");
}

if (StringUtils.isBlank(nombre)) {
    throw new IllegalArgumentException("Name cannot be empty");
}
```

---

### ✅ IllegalStateException
**Use when:** Object is in wrong state for operation

```java
// Example: Invalid state transition
if (consulta.getEstatus().equals("COMPLETADA")) {
    throw new IllegalStateException(
        "Cannot modify a completed consultation"
    );
}

if (!evaluacion.hasAllRequiredResponses()) {
    throw new IllegalStateException(
        "Evaluation must have all required responses before completion"
    );
}
```

---

## 🎯 Common Scenarios

### Scenario 1: CRUD Operations

```java
@Service
public class PacienteService {
    
    // GET - Not Found
    public PacienteResponse obtener(Integer id) {
        return pacienteRepository.findById(id)
            .map(this::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Paciente", id));
    }
    
    // POST - Duplicate
    public PacienteResponse crear(PacienteRequest request) {
        if (pacienteRepository.existsByDocPaciente(request.getDocPaciente())) {
            throw new BusinessException(
                "Patient with document " + request.getDocPaciente() + " already exists",
                HttpStatus.CONFLICT
            );
        }
        // ... create logic
    }
    
    // PUT - Not Found
    public PacienteResponse actualizar(Integer id, PacienteRequest request) {
        Paciente paciente = pacienteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Paciente", id));
        // ... update logic
    }
    
    // DELETE - Constraint violation
    public void eliminar(Integer id) {
        Paciente paciente = pacienteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Paciente", id));
            
        if (consultaRepository.existsByPaciente(paciente)) {
            throw new BusinessException(
                "Cannot delete patient with existing consultations",
                HttpStatus.CONFLICT
            );
        }
        pacienteRepository.delete(paciente);
    }
}
```

---

### Scenario 2: Validation

```java
@Service
public class EvaluacionService {
    
    public void validarEvaluacion(EvaluacionRequest request) {
        // Business validation
        if (request.getPreguntas().isEmpty()) {
            throw new IllegalArgumentException(
                "Evaluation must have at least one question"
            );
        }
        
        // State validation
        Consulta consulta = consultaRepository.findById(request.getIdConsulta())
            .orElseThrow(() -> new ResourceNotFoundException("Consulta", request.getIdConsulta()));
            
        if (!consulta.getEstatus().equals("EN_PROGRESO")) {
            throw new IllegalStateException(
                "Cannot create evaluation for consultation in status: " + 
                consulta.getEstatus()
            );
        }
    }
}
```

---

### Scenario 3: Permission Checks

```java
@Service
public class ReporteService {
    
    @Autowired
    private PermissionService permissionService;
    
    public ReporteResponse generar(Integer idEvaluacion, String username) {
        // Check permission programmatically
        if (!permissionService.hasPermission(username, "reporte:create")) {
            throw new AccessDeniedException(
                "User does not have permission to create reports"
            );
        }
        
        // Verify ownership
        Evaluacion evaluacion = evaluacionRepository.findById(idEvaluacion)
            .orElseThrow(() -> new ResourceNotFoundException("Evaluacion", idEvaluacion));
            
        if (!evaluacion.getProfesional().getUsername().equals(username)) {
            throw new AccessDeniedException(
                "You can only create reports for your own evaluations"
            );
        }
        
        // ... generate report
    }
}
```

---

## 🔥 Quick Tips

### ✅ DO:
```java
// Clear, specific messages
throw new ResourceNotFoundException("Paciente", "document", docNum);

// Include helpful details
Map<String, Object> details = Map.of(
    "reason", "Insufficient funds",
    "currentBalance", balance,
    "requiredAmount", amount
);
throw new BusinessException("Cannot process payment", details);

// Use appropriate HTTP status
throw new BusinessException("Duplicate entry", HttpStatus.CONFLICT);
```

### ❌ DON'T:
```java
// Generic messages
throw new Exception("Error"); // ❌ Too generic

// Exposing internal details
throw new RuntimeException("SQL error: " + sqlException); // ❌ Security risk

// Wrong exception type
throw new NullPointerException("User not found"); // ❌ Use ResourceNotFoundException
```

---

## 📊 Exception → HTTP Status Mapping

| Exception | HTTP Status | Use Case |
|-----------|-------------|----------|
| `ResourceNotFoundException` | 404 Not Found | Resource doesn't exist |
| `BusinessException` | 400/409 | Business rule violation |
| `IllegalArgumentException` | 400 Bad Request | Invalid input |
| `IllegalStateException` | 409 Conflict | Wrong state |
| `AccessDeniedException` | 403 Forbidden | No permission |
| `AuthenticationException` | 401 Unauthorized | Not authenticated |
| `MethodArgumentNotValidException` | 400 Bad Request | Validation failure |

---

## 🎨 Error Response Examples

### Example 1: Validation Error
```json
{
  "timestamp": "2025-12-26T19:00:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid input data. Please check the fields and try again.",
  "details": {
    "nombre": "Name is required",
    "edad": "Age must be positive"
  },
  "path": "/api/v1/pacientes"
}
```

### Example 2: Permission Denied
```json
{
  "timestamp": "2025-12-26T19:00:00",
  "status": 403,
  "error": "Access Denied",
  "message": "You don't have permission to access this resource. Required permission: paciente:delete",
  "details": {
    "reason": "Insufficient permissions",
    "suggestion": "Contact your administrator"
  },
  "path": "/api/v1/pacientes/123"
}
```

### Example 3: Business Rule Violation
```json
{
  "timestamp": "2025-12-26T19:00:00",
  "status": 409,
  "error": "Duplicate Document",
  "message": "Patient with document 12345678 already exists",
  "details": {
    "reason": "Duplicate document",
    "documentNumber": "12345678",
    "suggestion": "Use a different document number"
  },
  "path": "/api/v1/pacientes"
}
```

---

## ✅ Testing Exceptions

```java
@Test
void shouldThrowResourceNotFoundException_whenPatientNotFound() {
    // Given
    Integer nonExistentId = 999;
    
    // When & Then
    assertThrows(ResourceNotFoundException.class, () -> {
        pacienteService.obtener(nonExistentId);
    });
}

@Test
void shouldThrowBusinessException_whenDuplicateDocument() {
    // Given
    PacienteRequest request = new PacienteRequest();
    request.setDocPaciente("12345678");
    
    when(pacienteRepository.existsByDocPaciente("12345678"))
        .thenReturn(true);
    
    // When & Then
    assertThrows(BusinessException.class, () -> {
        pacienteService.crear(request);
    });
}
```

---

## 🎊 Summary

**4 Custom Exceptions:**
1. `ResourceNotFoundException` - For missing resources
2. `BusinessException` - For business rule violations
3. `IllegalArgumentException` - For invalid inputs
4. `IllegalStateException` - For invalid states

**10+ Handled Scenarios:**
- Validation errors
- Permission denied
- Authentication failed
- Resource not found
- Duplicate entries
- Type mismatches
- And more...

**All errors return structured JSON with:**
- ✅ Timestamp
- ✅ HTTP status
- ✅ Error title
- ✅ Detailed message
- ✅ Helpful details
- ✅ Request path

**🚀 Your API now provides professional, detailed error responses!**


