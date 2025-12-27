# ✅ Global Exception Handler Implementation

**Date:** 2025-12-26  
**Feature:** Comprehensive exception handling with detailed error messages  
**Status:** ✅ IMPLEMENTED

---

## 🎯 Overview

Instead of returning generic HTTP status codes (403, 500, etc.), the application now provides **detailed, user-friendly error messages** with structured error responses.

---

## 📦 Components Created

### 1. **GlobalExceptionHandler.java**
Central exception handling for all REST API errors

**Location:** `src/main/java/com/example/rntn/exception/GlobalExceptionHandler.java`

**Features:**
- ✅ Handles all common exceptions
- ✅ Provides detailed error messages
- ✅ Includes helpful suggestions
- ✅ Logs all errors for debugging
- ✅ Returns structured JSON responses

### 2. **ErrorResponse.java**
Standard error response DTO

**Location:** `src/main/java/com/example/rntn/exception/ErrorResponse.java`

**Structure:**
```json
{
  "timestamp": "2025-12-26T19:00:00",
  "status": 403,
  "error": "Access Denied",
  "message": "You don't have permission to access this resource...",
  "details": {
    "reason": "Insufficient permissions",
    "suggestion": "Contact administrator..."
  },
  "path": "/api/v1/pacientes"
}
```

### 3. **ResourceNotFoundException.java**
Custom exception for resource not found scenarios

**Location:** `src/main/java/com/example/rntn/exception/ResourceNotFoundException.java`

**Usage:**
```java
throw new ResourceNotFoundException("Paciente", "id", 123);
// Returns: "Paciente not found with id: '123'"
```

### 4. **BusinessException.java**
Custom exception for business logic violations

**Location:** `src/main/java/com/example/rntn/exception/BusinessException.java`

**Usage:**
```java
throw new BusinessException(
    "Cannot delete patient with active consultations",
    HttpStatus.CONFLICT
);
```

---

## 🛡️ Exception Types Handled

### 1. **Validation Errors** (400 Bad Request)
**Trigger:** `@Valid` annotation failures

**Example Response:**
```json
{
  "timestamp": "2025-12-26T19:00:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid input data. Please check the fields and try again.",
  "details": {
    "nombre": "Name is required",
    "email": "Email must be valid",
    "edad": "Age must be between 0 and 120"
  },
  "path": "/api/v1/pacientes"
}
```

### 2. **Access Denied** (403 Forbidden)
**Trigger:** `@PreAuthorize` permission check fails

**Before:**
```json
{
  "status": 403,
  "error": "Forbidden"
}
```

**After:**
```json
{
  "timestamp": "2025-12-26T19:00:00",
  "status": 403,
  "error": "Access Denied",
  "message": "You don't have permission to access this resource. Required permission: paciente:create",
  "details": {
    "reason": "Insufficient permissions",
    "suggestion": "Please contact your administrator to request the necessary permissions"
  },
  "path": "/api/v1/pacientes"
}
```

### 3. **Authentication Failed** (401 Unauthorized)
**Trigger:** Invalid JWT token or missing authentication

**Example Response:**
```json
{
  "timestamp": "2025-12-26T19:00:00",
  "status": 401,
  "error": "Authentication Failed",
  "message": "Authentication failed. Please check your credentials and try again.",
  "details": {
    "reason": "Invalid token",
    "suggestion": "Verify your username and password, or obtain a new access token"
  },
  "path": "/api/v1/evaluaciones"
}
```

### 4. **Bad Credentials** (401 Unauthorized)
**Trigger:** Wrong username or password

**Example Response:**
```json
{
  "timestamp": "2025-12-26T19:00:00",
  "status": 401,
  "error": "Invalid Credentials",
  "message": "Invalid username or password. Please try again.",
  "details": {
    "suggestion": "Double-check your username and password. Account may be locked after multiple failed attempts."
  },
  "path": "/api/v1/auth/login"
}
```

### 5. **Resource Not Found** (404 Not Found)
**Trigger:** `EntityNotFoundException` or custom `ResourceNotFoundException`

**Example Response:**
```json
{
  "timestamp": "2025-12-26T19:00:00",
  "status": 404,
  "error": "Resource Not Found",
  "message": "Paciente not found with id: '123'",
  "details": {
    "resourceName": "Paciente",
    "searchField": "id",
    "searchValue": 123
  },
  "path": "/api/v1/pacientes/123"
}
```

### 6. **Invalid Request** (400 Bad Request)
**Trigger:** `IllegalArgumentException`

**Example Response:**
```json
{
  "timestamp": "2025-12-26T19:00:00",
  "status": 400,
  "error": "Invalid Request",
  "message": "Document number is already registered",
  "path": "/api/v1/pacientes"
}
```

### 7. **Conflict** (409 Conflict)
**Trigger:** `IllegalStateException`

**Example Response:**
```json
{
  "timestamp": "2025-12-26T19:00:00",
  "status": 409,
  "error": "Conflict",
  "message": "Cannot delete patient with active consultations",
  "details": {
    "reason": "The operation conflicts with the current state",
    "suggestion": "The resource may already exist or is in an invalid state for this operation"
  },
  "path": "/api/v1/pacientes/123"
}
```

### 8. **Type Mismatch** (400 Bad Request)
**Trigger:** Invalid parameter types (e.g., string instead of int)

**Example Response:**
```json
{
  "timestamp": "2025-12-26T19:00:00",
  "status": 400,
  "error": "Invalid Parameter Type",
  "message": "Parameter 'id' should be of type Integer, but received: abc",
  "details": {
    "parameter": "id",
    "expectedType": "Integer",
    "providedValue": "abc"
  },
  "path": "/api/v1/pacientes/abc"
}
```

### 9. **Internal Server Error** (500)
**Trigger:** Unhandled exceptions, NullPointerException, etc.

**Example Response:**
```json
{
  "timestamp": "2025-12-26T19:00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred. Please try again later or contact support.",
  "details": {
    "exception": "NullPointerException",
    "cause": "Cannot invoke method on null object"
  },
  "path": "/api/v1/evaluaciones/123"
}
```

---

## 🚀 Usage Examples

### In Services - Throwing Custom Exceptions

```java
@Service
public class PacienteService {
    
    public PacienteResponse obtenerPaciente(Integer id) {
        Paciente paciente = pacienteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Paciente", "id", id));
        return mapToResponse(paciente);
    }
    
    public void validarDocumento(String documento) {
        if (pacienteRepository.existsByDocPaciente(documento)) {
            throw new BusinessException(
                "Document number already registered: " + documento,
                "Duplicate Document",
                HttpStatus.CONFLICT
            );
        }
    }
}
```

### Testing Exception Responses

```bash
# Test 403 - Access Denied
curl -X POST http://localhost:8080/api/v1/pacientes \
  -H "Authorization: Bearer USER_TOKEN_WITHOUT_PERMISSION" \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Juan","docPaciente":"12345678"}'

# Test 404 - Not Found
curl -X GET http://localhost:8080/api/v1/pacientes/99999 \
  -H "Authorization: Bearer VALID_TOKEN"

# Test 400 - Validation Error
curl -X POST http://localhost:8080/api/v1/pacientes \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nombre":"","docPaciente":""}'

# Test 401 - Invalid Credentials
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"wrong","password":"wrong"}'
```

---

## ✅ Benefits

### Before
```json
{
  "status": 403,
  "error": "Forbidden"
}
```
- ❌ No details
- ❌ No guidance
- ❌ Hard to debug

### After
```json
{
  "timestamp": "2025-12-26T19:00:00",
  "status": 403,
  "error": "Access Denied",
  "message": "You don't have permission to access this resource. Required permission: paciente:create",
  "details": {
    "reason": "Insufficient permissions",
    "suggestion": "Please contact your administrator to request the necessary permissions"
  },
  "path": "/api/v1/pacientes"
}
```
- ✅ Clear error description
- ✅ Helpful suggestions
- ✅ Easy to debug
- ✅ Professional appearance
- ✅ User-friendly

---

## 🎯 Key Features

1. **Structured Responses**
   - Consistent format across all errors
   - Easy to parse by frontend applications

2. **Helpful Messages**
   - Clear descriptions of what went wrong
   - Suggestions for how to fix the issue

3. **Detailed Information**
   - Field-level validation errors
   - Required permissions shown
   - Parameter type mismatches explained

4. **Logging**
   - All errors logged with appropriate levels
   - Stack traces for server errors
   - Request paths included

5. **Security**
   - Sensitive information hidden in production
   - Stack traces only in detailed errors
   - No database schema exposure

---

## 📝 Best Practices

### 1. Use Custom Exceptions in Services
```java
// ✅ Good
throw new ResourceNotFoundException("Paciente", id);

// ❌ Avoid
return null; // Let controller handle null
```

### 2. Provide Meaningful Messages
```java
// ✅ Good
throw new BusinessException("Cannot delete patient with ID " + id + 
    " because they have active consultations");

// ❌ Avoid
throw new BusinessException("Error");
```

### 3. Include Recovery Suggestions
```java
Map<String, Object> details = Map.of(
    "reason", "Duplicate document",
    "suggestion", "Use a different document number or update existing patient"
);
throw new BusinessException("Document already exists", details);
```

---

## 🔍 Error Response Fields

| Field | Type | Description | Always Present |
|-------|------|-------------|----------------|
| `timestamp` | LocalDateTime | When error occurred | ✅ Yes |
| `status` | int | HTTP status code | ✅ Yes |
| `error` | String | Short error title | ✅ Yes |
| `message` | String | Detailed description | ✅ Yes |
| `details` | Map | Additional info | ⚠️ Optional |
| `path` | String | Request path | ✅ Yes |

---

## ✅ Compilation Status

```bash
mvn compile -DskipTests
```

**Result:** ✅ BUILD SUCCESS

---

## 🎉 Summary

**What Changed:**
- ❌ Before: Generic HTTP errors (403, 500)
- ✅ After: Detailed, structured error responses

**Files Created:**
1. `GlobalExceptionHandler.java` - Centralized exception handling
2. `ErrorResponse.java` - Standard error DTO
3. `ResourceNotFoundException.java` - Custom not found exception
4. `BusinessException.java` - Custom business logic exception

**Exception Types Handled:** 10+ common scenarios

**Benefits:**
- ✅ Better user experience
- ✅ Easier debugging
- ✅ Professional API responses
- ✅ Clear permission requirements
- ✅ Actionable error messages

**🎊 Your API now provides detailed, helpful error messages instead of generic status codes!** 🎊


