# Separación de Roles y Permisos en Login Response

**Fecha:** 2025-12-27  
**Cambio:** Login endpoint ahora retorna roles y permisos en arrays separados  
**Estado:** ✅ **IMPLEMENTADO Y COMPILADO**

---

## 🎯 Objetivo del Cambio

Separar los roles y permisos en el response del endpoint de login para facilitar el manejo en el frontend y proporcionar una estructura de datos más clara.

---

## 📦 Archivos Modificados

### 1. **AuthResponse.java** ✅
**Ubicación:** `src/main/java/com/example/rntn/dto/response/AuthResponse.java`

**Cambio:**
- Se agregó campo `permissions` para almacenar los permisos del usuario
- Se actualizó la documentación de Swagger

**Antes:**
```java
private List<String> roles;  // Contenía roles y permisos mezclados
```

**Después:**
```java
private List<String> roles;        // Solo roles: ["ADMIN", "DOCTOR"]
private List<String> permissions;  // Solo permisos: ["paciente:read", "consulta:create"]
```

### 2. **AuthController.java** ✅
**Ubicación:** `src/main/java/com/example/rntn/controller/AuthController.java`

**Cambios:**
- Se inyectó `UsuarioRepository` para acceder a los datos completos del usuario
- Se extrae la lista de roles sin el prefijo "ROLE_"
- Se extraen los permisos de todos los roles del usuario
- Ambas listas se ordenan y eliminan duplicados

**Lógica implementada:**
```java
// Extraer roles (sin el prefijo "ROLE_")
List<String> roles = usuario.getRoles().stream()
        .map(UsuarioRoles::getPermisosRoles)
        .distinct()
        .sorted()
        .collect(Collectors.toList());

// Extraer permisos de todos los roles
List<String> permissions = usuario.getRoles().stream()
        .flatMap(rol -> rol.getPermissions().stream())
        .map(Permission::getPermissionName)
        .distinct()
        .sorted()
        .collect(Collectors.toList());
```

---

## 📊 Estructura del Response

### Antes del Cambio
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "admin",
  "roles": [
    "ROLE_ADMIN",
    "paciente:read",
    "paciente:create",
    "paciente:update",
    "consulta:read"
  ],
  "expiresIn": 3600000
}
```

### Después del Cambio ✅
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "admin",
  "roles": [
    "ADMIN"
  ],
  "permissions": [
    "consulta:create",
    "consulta:delete",
    "consulta:read",
    "consulta:update",
    "evaluacion:create",
    "evaluacion:read",
    "evaluacion:update",
    "paciente:create",
    "paciente:delete",
    "paciente:read",
    "paciente:update",
    "personal:create",
    "personal:delete",
    "personal:read",
    "personal:update",
    "reporte:create",
    "reporte:read",
    "sentiment:analyze",
    "usuario:create",
    "usuario:delete",
    "usuario:read",
    "usuario:update"
  ],
  "expiresIn": 3600000
}
```

---

## 🔑 Características

### 1. Arrays Separados
- **roles**: Lista de nombres de roles (sin prefijo "ROLE_")
- **permissions**: Lista de permisos en formato "recurso:acción"

### 2. Sin Duplicados
- Ambas listas usan `.distinct()` para eliminar duplicados
- Un usuario con múltiples roles puede tener los mismos permisos

### 3. Ordenación Alfabética
- Ambas listas están ordenadas con `.sorted()`
- Facilita la lectura y debugging

### 4. Logging Mejorado
```java
log.info("Login exitoso para usuario: {} - Roles: {} - Permisos: {}", 
        request.getUsername(), roles.size(), permissions.size());
```

---

## 💻 Uso desde Frontend

### React/Next.js
```javascript
const login = async (username, password) => {
  const response = await fetch('http://localhost:8080/api/v1/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  });
  
  const data = await response.json();
  
  // Acceder a roles y permisos por separado
  console.log('Roles:', data.roles);           // ["ADMIN"]
  console.log('Permisos:', data.permissions);  // ["paciente:read", "consulta:create", ...]
  
  // Guardar en localStorage o state
  localStorage.setItem('token', data.token);
  localStorage.setItem('roles', JSON.stringify(data.roles));
  localStorage.setItem('permissions', JSON.stringify(data.permissions));
  
  return data;
};

// Verificar si el usuario tiene un permiso específico
const hasPermission = (permission) => {
  const permissions = JSON.parse(localStorage.getItem('permissions') || '[]');
  return permissions.includes(permission);
};

// Verificar si el usuario tiene un rol específico
const hasRole = (role) => {
  const roles = JSON.parse(localStorage.getItem('roles') || '[]');
  return roles.includes(role);
};

// Uso en componentes
if (hasPermission('paciente:create')) {
  // Mostrar botón "Crear Paciente"
}

if (hasRole('ADMIN')) {
  // Mostrar menú de administración
}
```

### Angular
```typescript
// auth.service.ts
export interface AuthResponse {
  token: string;
  type: string;
  username: string;
  roles: string[];
  permissions: string[];
  expiresIn: number;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private rolesSubject = new BehaviorSubject<string[]>([]);
  private permissionsSubject = new BehaviorSubject<string[]>([]);
  
  roles$ = this.rolesSubject.asObservable();
  permissions$ = this.permissionsSubject.asObservable();
  
  login(username: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/auth/login`, 
      { username, password }
    ).pipe(
      tap(response => {
        localStorage.setItem('token', response.token);
        this.rolesSubject.next(response.roles);
        this.permissionsSubject.next(response.permissions);
      })
    );
  }
  
  hasPermission(permission: string): boolean {
    return this.permissionsSubject.value.includes(permission);
  }
  
  hasRole(role: string): boolean {
    return this.rolesSubject.value.includes(role);
  }
}

// En componente
export class PacienteListComponent {
  canCreate$ = this.authService.permissions$.pipe(
    map(permissions => permissions.includes('paciente:create'))
  );
}
```

### Vue.js
```javascript
// composables/useAuth.js
import { ref, computed } from 'vue';

const roles = ref([]);
const permissions = ref([]);

export function useAuth() {
  const login = async (username, password) => {
    const response = await fetch('http://localhost:8080/api/v1/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });
    
    const data = await response.json();
    
    roles.value = data.roles;
    permissions.value = data.permissions;
    
    localStorage.setItem('token', data.token);
    localStorage.setItem('roles', JSON.stringify(data.roles));
    localStorage.setItem('permissions', JSON.stringify(data.permissions));
    
    return data;
  };
  
  const hasPermission = (permission) => {
    return permissions.value.includes(permission);
  };
  
  const hasRole = (role) => {
    return roles.value.includes(role);
  };
  
  const isAdmin = computed(() => hasRole('ADMIN'));
  
  return {
    roles,
    permissions,
    login,
    hasPermission,
    hasRole,
    isAdmin
  };
}

// En componente
<template>
  <button v-if="hasPermission('paciente:create')" @click="createPatient">
    Crear Paciente
  </button>
</template>

<script setup>
import { useAuth } from '@/composables/useAuth';
const { hasPermission } = useAuth();
</script>
```

---

## 🧪 Ejemplo de Response Completo

### Usuario "admin" con rol ADMIN
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTczNTM1MTU2MCwiZXhwIjoxNzM1MzU1MTYwfQ.signature",
  "type": "Bearer",
  "username": "admin",
  "roles": ["ADMIN"],
  "permissions": [
    "consulta:create",
    "consulta:delete",
    "consulta:read",
    "consulta:update",
    "evaluacion:create",
    "evaluacion:read",
    "evaluacion:update",
    "paciente:create",
    "paciente:delete",
    "paciente:read",
    "paciente:update",
    "personal:create",
    "personal:delete",
    "personal:read",
    "personal:update",
    "reporte:create",
    "reporte:read",
    "sentiment:analyze",
    "usuario:create",
    "usuario:delete",
    "usuario:read",
    "usuario:update"
  ],
  "expiresIn": 3600000
}
```

### Usuario "doctor" con rol DOCTOR
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "doctor",
  "roles": ["DOCTOR"],
  "permissions": [
    "consulta:create",
    "consulta:read",
    "consulta:update",
    "evaluacion:create",
    "evaluacion:read",
    "paciente:read",
    "reporte:read",
    "sentiment:analyze"
  ],
  "expiresIn": 3600000
}
```

---

## ✅ Beneficios

### 1. **Claridad en el Frontend**
- Los desarrolladores frontend ven claramente qué son roles y qué son permisos
- No hay confusión entre "ROLE_ADMIN" y permisos

### 2. **Facilita Validaciones**
```javascript
// Verificar rol
if (user.roles.includes('ADMIN')) { }

// Verificar permiso específico
if (user.permissions.includes('paciente:delete')) { }
```

### 3. **UI/UX Mejorado**
- Mostrar/ocultar elementos basado en permisos granulares
- Deshabilitar botones si falta permiso específico
- Menús dinámicos según permisos

### 4. **Compatible con RBAC**
- Soporta Role-Based Access Control
- Los permisos son la base de la autorización
- Los roles agrupan permisos relacionados

---

## 🔒 Seguridad

### Importante
⚠️ **El frontend NO debe ser la única validación de seguridad**

- Los permisos en el response son informativos para UI/UX
- El backend SIEMPRE valida permisos en cada endpoint
- Los tokens JWT incluyen los permisos en los claims
- Spring Security valida cada petición

### Validación en Backend
```java
@PreAuthorize("hasPermission(null, 'paciente:delete')")
@DeleteMapping("/{id}")
public ResponseEntity<Void> deletePatient(@PathVariable Integer id) {
    // Código del endpoint
}
```

---

## 📚 Documentación Swagger

El endpoint `/api/v1/auth/login` ahora documenta claramente:

```yaml
responses:
  200:
    description: Autenticación exitosa
    content:
      application/json:
        schema:
          type: object
          properties:
            token:
              type: string
              example: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            type:
              type: string
              example: "Bearer"
            username:
              type: string
              example: "admin"
            roles:
              type: array
              items:
                type: string
              example: ["ADMIN", "DOCTOR"]
            permissions:
              type: array
              items:
                type: string
              example: ["paciente:read", "paciente:create"]
            expiresIn:
              type: integer
              example: 3600000
```

---

## ✅ Verificación

### Build Status
```
[INFO] Compiling 93 source files
[INFO] BUILD SUCCESS
[INFO] Total time:  5.958 s
```

✅ **0 Errores**  
✅ **93 Archivos Compilados**  
✅ **Listo para Usar**

---

## 🎉 Resumen

| Aspecto | Antes | Después |
|---------|-------|---------|
| Estructura | Roles y permisos mezclados | Arrays separados |
| Claridad | ❌ Confuso | ✅ Claro |
| Uso en Frontend | ❌ Difícil | ✅ Fácil |
| Formato Roles | ROLE_ADMIN | ADMIN |
| Formato Permisos | Mezclado | recurso:acción |
| Ordenación | ❌ No | ✅ Sí |
| Duplicados | ❌ Posibles | ✅ Eliminados |

---

**Estado:** ✅ **COMPLETADO**  
**Fecha:** 2025-12-27  
**Version:** 1.0.0

