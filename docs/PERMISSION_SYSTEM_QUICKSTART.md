# Permission System - Quick Start Guide 🚀

**Date:** 2025-12-26  
**Version:** 1.0

---

## ✅ Implementation Complete!

The enhanced RBAC permission system with 45 granular permissions is now fully implemented and ready to use.

---

## 🎯 What You Need to Know

### 1. Two Security Models Work Together

#### ✅ Old Way (Still Works)
```java
@PreAuthorize("hasRole('ADMIN')")
@PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
```

#### ✅ New Way (Recommended)
```java
@PreAuthorize("hasPermission(null, 'paciente:create')")
@PreAuthorize("hasPermission(null, 'evaluacion:read')")
```

**Both work!** No need to change existing code. Gradually migrate when needed.

---

## 🚀 Quick Start

### Step 1: Start the Application

```bash
mvn spring-boot:run
```

Flyway will automatically:
- Create `permissions` table (45 permissions)
- Create `role_permissions` table
- Assign permissions to all 7 roles

### Step 2: Login as Admin

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "admin",
  "roles": ["ADMIN"]
}
```

### Step 3: Check Your Permissions

```bash
curl -X GET http://localhost:8080/api/v1/permissions/my-permissions \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected:** 45 permissions for admin user

---

## 📊 Permission Quick Reference

### Pattern: `resource:action`

| Permission Format | Example | Description |
|------------------|---------|-------------|
| `resource:create` | `paciente:create` | Create new record |
| `resource:read` | `evaluacion:read` | View/list records |
| `resource:update` | `consulta:update` | Modify records |
| `resource:delete` | `reporte:delete` | Delete records |
| `resource:execute` | `sentiment:analyze` | Execute operation |
| `resource:manage` | `usuario:manage` | Full management |

---

## 🎓 Common Use Cases

### Check if User Can Create Patients
```java
@PreAuthorize("hasPermission(null, 'paciente:create')")
@PostMapping("/api/v1/pacientes")
public ResponseEntity<?> crearPaciente(@RequestBody PacienteRequest request) {
    // Your code
}
```

### Check Multiple Permissions in Service
```java
@Service
public class MyService {
    @Autowired
    private PermissionService permissionService;
    
    public void processEvaluation(String username) {
        if (!permissionService.hasAllPermissions(username, 
                "evaluacion:read", "evaluacion:update")) {
            throw new AccessDeniedException("Insufficient permissions");
        }
        // Process
    }
}
```

### Check User Permissions Dynamically
```bash
# Check if user has permission
curl -X GET http://localhost:8080/api/v1/permissions/check/paciente:create \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Response:
{
  "permission": "paciente:create",
  "granted": true
}
```

---

## 👥 Who Has What?

### ADMIN (45 permissions)
- ✅ Everything

### DOCTOR (28 permissions)
- ✅ Full CRUD: Paciente, Consulta, Evaluacion
- ✅ All sentiment analysis
- ✅ Create/read/export reports
- ❌ User/role management

### ENFERMERO (8 permissions)
- ✅ Read/update: Paciente, Consulta
- ✅ Read: Evaluacion, responses
- ❌ Create/delete anything

### PSICOLOGO (21 permissions)
- ✅ Full CRUD: Evaluacion (all aspects)
- ✅ Create/update: Paciente, Consulta
- ✅ Sentiment analysis
- ❌ Staff/user management

### TERAPEUTA (14 permissions)
- ✅ Update: Paciente, Consulta
- ✅ Create/read/update: Evaluation responses
- ✅ Basic sentiment analysis
- ❌ Delete anything

### ANALISTA (8 permissions)
- ✅ All sentiment analysis operations
- ✅ Full report management
- ✅ Read: Evaluations
- ❌ Patient/consultation data

### RECEPCIONISTA (8 permissions)
- ✅ Create/read/update: Paciente, Consulta
- ✅ Read: Staff information
- ❌ Evaluations, reports

---

## 🔧 Admin Operations

### List All Permissions
```bash
curl -X GET http://localhost:8080/api/v1/permissions \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

### View Permission Summary by Role
```bash
curl -X GET http://localhost:8080/api/v1/role-permissions/summary \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

### Get Permissions for Specific Role
```bash
# Get DOCTOR permissions (role ID = 2)
curl -X GET http://localhost:8080/api/v1/role-permissions/role/2 \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

### Add Permissions to Role
```bash
# Add permissions 10, 11, 12 to ENFERMERO (role ID = 3)
curl -X POST http://localhost:8080/api/v1/role-permissions/role/3/add \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '[10, 11, 12]'
```

### Remove Permissions from Role
```bash
# Remove permissions 5, 6 from role
curl -X DELETE http://localhost:8080/api/v1/role-permissions/role/3/remove \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '[5, 6]'
```

### Replace All Permissions for a Role
```bash
curl -X PUT http://localhost:8080/api/v1/role-permissions/assign \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "roleId": 3,
    "permissionIds": [1, 2, 3, 10, 11, 12]
  }'
```

---

## 🎨 Frontend Integration Examples

### React/Vue - Check Permission Before Showing Button

```javascript
// Get user permissions on login
const userPermissions = await fetch('/api/v1/permissions/my-permissions', {
  headers: { 'Authorization': `Bearer ${token}` }
}).then(r => r.json());

// Store in state/context
const [permissions, setPermissions] = useState(userPermissions);

// Component
function CreatePatientButton() {
  const canCreate = permissions.includes('paciente:create');
  
  return canCreate ? (
    <button onClick={createPatient}>Create Patient</button>
  ) : null;
}
```

### Check Permission Before API Call

```javascript
async function deleteReport(reportId) {
  // Check permission first
  const canDelete = await checkPermission('reporte:delete');
  
  if (!canDelete) {
    alert('You do not have permission to delete reports');
    return;
  }
  
  // Proceed with delete
  await fetch(`/api/v1/reportes/${reportId}`, {
    method: 'DELETE',
    headers: { 'Authorization': `Bearer ${token}` }
  });
}

async function checkPermission(permissionName) {
  const response = await fetch(`/api/v1/permissions/check/${permissionName}`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  const data = await response.json();
  return data.granted;
}
```

---

## 📱 Swagger UI Testing

Once the app is running, go to: **http://localhost:8080/swagger-ui.html**

1. **Authorize** with JWT token from login
2. Navigate to **Permissions** section
3. Test endpoints:
   - GET `/api/v1/permissions/my-permissions`
   - GET `/api/v1/permissions/by-resource`
   - GET `/api/v1/permissions/check/{permissionName}`

---

## 🐛 Troubleshooting

### "Access Denied" Error
**Problem:** Getting 403 Forbidden  
**Solution:** Check if user has required permission:
```bash
curl -X GET http://localhost:8080/api/v1/permissions/check/paciente:create \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Permissions Not Loading
**Problem:** User has role but no permissions  
**Solution:** Check role-permission assignments:
```bash
# Check role permissions (admin only)
curl -X GET http://localhost:8080/api/v1/role-permissions/summary \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

### Migration Not Running
**Problem:** Tables not created  
**Solution:** Check Flyway status:
- Look for `V9__create_permission_system.sql` execution in logs
- Verify database connection in `application.properties`
- Check `flyway_schema_history` table

---

## ✅ Verification Checklist

After starting the app:

- [ ] Application starts without errors
- [ ] V9 migration executed successfully (check logs)
- [ ] `permissions` table created with 45 records
- [ ] `role_permissions` table created with ~140 records
- [ ] Admin user can login
- [ ] GET `/api/v1/permissions/my-permissions` returns permissions
- [ ] GET `/api/v1/permissions` returns all permissions (admin only)
- [ ] GET `/api/v1/role-permissions/summary` returns role summary

---

## 📚 Key Endpoints Reference

| Endpoint | Method | Access | Purpose |
|----------|--------|--------|---------|
| `/api/v1/permissions/my-permissions` | GET | User | My permissions |
| `/api/v1/permissions/check/{perm}` | GET | User | Check if I have permission |
| `/api/v1/permissions` | GET | ADMIN | List all permissions |
| `/api/v1/permissions/by-resource` | GET | ADMIN | Permissions by resource |
| `/api/v1/role-permissions/summary` | GET | ADMIN | Role permission summary |
| `/api/v1/role-permissions/role/{id}` | GET | ADMIN | Get role permissions |
| `/api/v1/role-permissions/assign` | PUT | ADMIN | Assign permissions to role |
| `/api/v1/role-permissions/role/{id}/add` | POST | ADMIN | Add permissions to role |
| `/api/v1/role-permissions/role/{id}/remove` | DELETE | ADMIN | Remove permissions from role |

---

## 🎉 You're Ready!

The permission system is fully operational. Start the application and begin using granular permissions in your API!

For detailed documentation, see:
- `docs/PERMISSIONS_STRATEGY_ANALYSIS.md` - Complete analysis
- `docs/PERMISSION_SYSTEM_IMPLEMENTATION.md` - Full implementation details

**Happy coding! 🚀**

