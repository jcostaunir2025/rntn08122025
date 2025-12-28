# 🚀 Login API - Quick Reference

## Endpoint
```
POST /api/v1/auth/login
```

## Request
```json
{
  "username": "admin",
  "password": "password123"
}
```

## Response
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "admin",
  "roles": ["ADMIN"],
  "permissions": [
    "consulta:create",
    "consulta:delete",
    "consulta:read",
    "evaluacion:create",
    "paciente:create",
    "paciente:delete",
    "paciente:read",
    "sentiment:analyze"
  ],
  "expiresIn": 3600000
}
```

## Frontend Usage

### Store Token & Data
```javascript
const { token, roles, permissions } = response.data;
localStorage.setItem('token', token);
localStorage.setItem('roles', JSON.stringify(roles));
localStorage.setItem('permissions', JSON.stringify(permissions));
```

### Check Permission
```javascript
const hasPermission = (perm) => {
  const perms = JSON.parse(localStorage.getItem('permissions') || '[]');
  return perms.includes(perm);
};

// Usage
if (hasPermission('paciente:create')) {
  // Show "Create Patient" button
}
```

### Check Role
```javascript
const hasRole = (role) => {
  const roles = JSON.parse(localStorage.getItem('roles') || '[]');
  return roles.includes(role);
};

// Usage
if (hasRole('ADMIN')) {
  // Show admin menu
}
```

### Make Authenticated Request
```javascript
const token = localStorage.getItem('token');

fetch('/api/v1/pacientes', {
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});
```

## Permission Format
```
resource:action

Examples:
- paciente:read
- paciente:create
- consulta:update
- evaluacion:delete
- sentiment:analyze
```

## Available Roles
- **ADMIN**: Full system access
- **DOCTOR**: Medical professional
- **ENFERMERO**: Nurse
- **ANALISTA**: Data analyst

---

**Last Updated:** 2025-12-27

