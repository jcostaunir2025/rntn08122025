# Permission Management Strategy Analysis

**Date:** 2025-12-26  
**Version:** 1.0  
**Status:** Proposal

---

## 📋 Executive Summary

This document analyzes the current role-based access control (RBAC) implementation and proposes the best approach for managing user permissions in the RNTN Sentiment Analysis API.

---

## 🔍 Current Implementation Analysis

### Current Architecture

#### Database Structure
```
┌─────────────┐         ┌────────────────────────┐         ┌────────────────┐
│   usuario   │────────►│usuario_roles_mapping  │◄────────│ usuario_roles  │
│             │         │   (Junction Table)     │         │                │
│ id_usuario  │         │ id_usuario             │         │ id_roles       │
│ nombre_...  │         │ id_roles               │         │ permisos_roles │
│ pass_...    │         └────────────────────────┘         └────────────────┘
└─────────────┘                                             
                                                            Current Roles:
                                                            - ADMIN
                                                            - DOCTOR
                                                            - ENFERMERO
                                                            - PSICOLOGO
                                                            - TERAPEUTA
                                                            - RECEPCIONISTA
                                                            - ANALISTA
```

#### Current Permission Model
- **Type:** Role-Based Access Control (RBAC)
- **Relationship:** Many-to-Many (Usuario ↔ UsuarioRoles)
- **Authorization:** Spring Security with `@PreAuthorize` annotations
- **Roles stored as:** Simple strings (ADMIN, DOCTOR, etc.)

### Current Security Configuration

```java
// SecurityConfig.java - Hardcoded role permissions
.requestMatchers("/api/v1/pacientes/**").hasAnyRole("ADMIN", "DOCTOR", "ENFERMERO")
.requestMatchers("/api/v1/personal/**").hasRole("ADMIN")
.requestMatchers("/api/v1/evaluaciones/**").hasAnyRole("ADMIN", "DOCTOR")
.requestMatchers("/api/v1/sentiment/**").hasAnyRole("ADMIN", "DOCTOR", "ANALISTA")
```

### Limitations of Current Approach

❌ **Hardcoded Permissions**: All permissions are hardcoded in `SecurityConfig.java`  
❌ **No Granularity**: Cannot define specific permissions per action (CREATE, READ, UPDATE, DELETE)  
❌ **Difficult to Modify**: Changing permissions requires code changes and redeployment  
❌ **No Permission Auditing**: No way to track what permissions each role has  
❌ **Role Proliferation Risk**: As system grows, number of roles can become unmanageable  

---

## 💡 Proposed Solutions

### Option 1: Enhanced RBAC with Permission Tables (⭐ RECOMMENDED)

**Best for:** Medium to large applications requiring flexibility and granular control

#### Database Schema Enhancement

```sql
-- New table: permissions (master list of all permissions)
CREATE TABLE permissions (
    id_permission INT PRIMARY KEY AUTO_INCREMENT,
    permission_name VARCHAR(100) UNIQUE NOT NULL,
    resource VARCHAR(50) NOT NULL,
    action VARCHAR(20) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_resource_action (resource, action)
);

-- New junction table: role_permissions (which permissions each role has)
CREATE TABLE role_permissions (
    id_role INT NOT NULL,
    id_permission INT NOT NULL,
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_role, id_permission),
    FOREIGN KEY (id_role) REFERENCES usuario_roles(id_roles) ON DELETE CASCADE,
    FOREIGN KEY (id_permission) REFERENCES permissions(id_permission) ON DELETE CASCADE
);

-- Example permissions data
INSERT INTO permissions (permission_name, resource, action, description) VALUES
('paciente:create', 'PACIENTE', 'CREATE', 'Create new patient records'),
('paciente:read', 'PACIENTE', 'READ', 'View patient information'),
('paciente:update', 'PACIENTE', 'UPDATE', 'Modify patient records'),
('paciente:delete', 'PACIENTE', 'DELETE', 'Delete patient records'),
('evaluacion:create', 'EVALUACION', 'CREATE', 'Create evaluations'),
('evaluacion:read', 'EVALUACION', 'READ', 'View evaluations'),
('sentiment:analyze', 'SENTIMENT', 'EXECUTE', 'Run sentiment analysis'),
('reporte:generate', 'REPORTE', 'CREATE', 'Generate reports'),
('usuario:manage', 'USUARIO', 'MANAGE', 'Full user management');

-- Assign permissions to roles
INSERT INTO role_permissions (id_role, id_permission)
SELECT r.id_roles, p.id_permission
FROM usuario_roles r, permissions p
WHERE r.permisos_roles = 'ADMIN'; -- Admin gets all permissions

INSERT INTO role_permissions (id_role, id_permission)
SELECT r.id_roles, p.id_permission
FROM usuario_roles r, permissions p
WHERE r.permisos_roles = 'DOCTOR' 
  AND p.resource IN ('PACIENTE', 'EVALUACION', 'CONSULTA', 'SENTIMENT', 'REPORTE');
```

#### Java Implementation

```java
// Entity: Permission.java
@Entity
@Table(name = "permissions")
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPermission;
    
    @Column(unique = true, nullable = false)
    private String permissionName; // e.g., "paciente:create"
    
    private String resource;      // e.g., "PACIENTE"
    private String action;        // e.g., "CREATE"
    private String description;
    
    @ManyToMany(mappedBy = "permissions")
    private Set<UsuarioRoles> roles = new HashSet<>();
}

// Update UsuarioRoles.java
@Entity
@Table(name = "usuario_roles")
public class UsuarioRoles {
    // ...existing fields...
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "id_role"),
        inverseJoinColumns = @JoinColumn(name = "id_permission")
    )
    private Set<Permission> permissions = new HashSet<>();
}

// Custom Permission Checker
@Service
public class PermissionService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    public boolean hasPermission(String username, String permissionName) {
        Usuario user = usuarioRepository.findByNombreUsuarioWithRoles(username)
            .orElseThrow();
        
        return user.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .anyMatch(perm -> perm.getPermissionName().equals(permissionName));
    }
    
    public boolean hasAnyPermission(String username, String... permissionNames) {
        Set<String> requiredPerms = Set.of(permissionNames);
        Usuario user = usuarioRepository.findByNombreUsuarioWithRoles(username)
            .orElseThrow();
        
        return user.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .anyMatch(perm -> requiredPerms.contains(perm.getPermissionName()));
    }
}

// Custom Permission Evaluator
@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {
    
    @Autowired
    private PermissionService permissionService;
    
    @Override
    public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
        if (auth == null || permission == null) return false;
        return permissionService.hasPermission(auth.getName(), permission.toString());
    }
    
    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId, 
                                String targetType, Object permission) {
        return hasPermission(auth, null, permission);
    }
}

// Usage in Controllers
@PreAuthorize("hasPermission(null, 'paciente:create')")
@PostMapping("/api/v1/pacientes")
public ResponseEntity<PacienteResponse> crearPaciente(@RequestBody PacienteRequest request) {
    // ...
}

@PreAuthorize("hasPermission(null, 'evaluacion:read')")
@GetMapping("/api/v1/evaluaciones/{id}")
public ResponseEntity<EvaluacionResponse> obtenerEvaluacion(@PathVariable Integer id) {
    // ...
}
```

#### ✅ Advantages
- **Granular Control**: Specific permissions for each action
- **Dynamic Management**: Permissions can be modified via admin endpoints without code changes
- **Scalable**: Easy to add new permissions as system grows
- **Auditable**: Clear permission assignment tracking
- **Database-Driven**: All permission logic stored in database
- **Industry Standard**: Follows RBAC best practices

#### ❌ Disadvantages
- Increased database complexity
- Requires initial setup effort
- More tables to maintain

---

### Option 2: Simplified RBAC with Permission Matrix (Current Enhanced)

**Best for:** Small to medium applications with stable permission requirements

Keep the current many-to-many relationship but enhance the `usuario_roles` table:

```sql
-- Enhanced usuario_roles table
ALTER TABLE usuario_roles ADD COLUMN permissions_json JSON;

UPDATE usuario_roles 
SET permissions_json = '["paciente:read", "paciente:create", "consulta:read", "evaluacion:read"]'
WHERE permisos_roles = 'DOCTOR';

UPDATE usuario_roles 
SET permissions_json = '["paciente:read", "consulta:read"]'
WHERE permisos_roles = 'ENFERMERO';

UPDATE usuario_roles 
SET permissions_json = '["*:*"]'  -- All permissions
WHERE permisos_roles = 'ADMIN';
```

#### Java Implementation
```java
@Entity
@Table(name = "usuario_roles")
public class UsuarioRoles {
    @Id
    private Integer idRoles;
    
    private String permisosRoles; // Role name
    
    @Column(columnDefinition = "JSON")
    @Convert(converter = JsonPermissionsConverter.class)
    private Set<String> permissionsJson;
}

// Custom Security Expression
@Component("permissionChecker")
public class PermissionChecker {
    
    public boolean hasPermission(String permission) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        return auth.getAuthorities().stream()
            .anyMatch(grantedAuth -> checkPermission(grantedAuth, permission));
    }
    
    private boolean checkPermission(GrantedAuthority authority, String required) {
        // Implement wildcard matching: "paciente:*", "*:read", etc.
        String roleName = authority.getAuthority().replace("ROLE_", "");
        // Fetch permissions from UsuarioRoles and check
        return false; // Implement logic
    }
}

// Usage
@PreAuthorize("@permissionChecker.hasPermission('paciente:create')")
@PostMapping("/api/v1/pacientes")
public ResponseEntity<?> crearPaciente(...) { }
```

#### ✅ Advantages
- Simpler than Option 1
- No additional junction tables
- JSON flexibility for complex permission structures

#### ❌ Disadvantages
- Less normalized database design
- Harder to query permissions across roles
- JSON querying can be slower

---

### Option 3: Keep Current Simple RBAC (Status Quo)

**Best for:** Very small applications with few roles and stable requirements

Keep the current implementation but improve organization:

```java
// Create Permission Constants
public class Permissions {
    public static final String[] PATIENT_MANAGEMENT = {"ADMIN", "DOCTOR", "ENFERMERO"};
    public static final String[] EVALUATION_MANAGEMENT = {"ADMIN", "DOCTOR"};
    public static final String[] USER_MANAGEMENT = {"ADMIN"};
    public static final String[] SENTIMENT_ANALYSIS = {"ADMIN", "DOCTOR", "ANALISTA"};
}

// Use in SecurityConfig
.requestMatchers("/api/v1/pacientes/**").hasAnyRole(Permissions.PATIENT_MANAGEMENT)
.requestMatchers("/api/v1/evaluaciones/**").hasAnyRole(Permissions.EVALUATION_MANAGEMENT)
```

#### ✅ Advantages
- Simplest approach
- No database changes needed
- Fast authorization checks

#### ❌ Disadvantages
- Still requires code deployment for permission changes
- Limited scalability
- No granular control

---

## 🎯 Recommendation: Option 1 (Enhanced RBAC with Permission Tables)

### Why Option 1 is Best for This Application

1. **Healthcare Context**: Medical applications require strict, auditable access control
2. **Regulatory Compliance**: HIPAA/GDPR may require detailed permission tracking
3. **Multiple User Types**: 7 different roles (ADMIN, DOCTOR, ENFERMERO, etc.) need different access levels
4. **Future Growth**: System likely to expand with new features and permission requirements
5. **Dynamic Administration**: Admin users can manage permissions via UI without code changes

### Implementation Roadmap

#### Phase 1: Database Setup (Week 1)
- [ ] Create `permissions` table
- [ ] Create `role_permissions` junction table
- [ ] Create Flyway migration script
- [ ] Seed initial permissions data
- [ ] Assign permissions to existing roles

#### Phase 2: Java Entity Layer (Week 1-2)
- [ ] Create `Permission` entity
- [ ] Update `UsuarioRoles` entity with permissions relationship
- [ ] Create `PermissionRepository`
- [ ] Update `UsuarioRepository` to fetch permissions

#### Phase 3: Service Layer (Week 2)
- [ ] Create `PermissionService`
- [ ] Implement `CustomPermissionEvaluator`
- [ ] Create permission checking utilities
- [ ] Add permission validation logic

#### Phase 4: Security Configuration (Week 2-3)
- [ ] Register `CustomPermissionEvaluator` in Security Config
- [ ] Enable method security with custom evaluator
- [ ] Update controllers with `@PreAuthorize` permissions
- [ ] Test authorization flows

#### Phase 5: Admin Endpoints (Week 3)
- [ ] Create `PermissionController` for CRUD operations
- [ ] Create `RolePermissionController` for assignment
- [ ] Add DTOs for permission management
- [ ] Create Swagger documentation

#### Phase 6: Testing & Documentation (Week 4)
- [ ] Unit tests for permission service
- [ ] Integration tests for authorization
- [ ] Update API documentation
- [ ] Create permission management guide

---

## 📊 Permission Matrix (Proposed Initial Setup)

| Resource            | Create | Read | Update | Delete | Execute | ADMIN | DOCTOR | ENFERMERO | PSICOLOGO | ANALISTA |
|---------------------|--------|------|--------|--------|---------|-------|--------|-----------|-----------|----------|
| **Paciente**        | ✅     | ✅   | ✅     | ✅     | -       | ✅    | ✅     | ✅        | ✅        | ❌       |
| **Personal**        | ✅     | ✅   | ✅     | ✅     | -       | ✅    | ✅     | ❌        | ❌        | ❌       |
| **Consulta**        | ✅     | ✅   | ✅     | ✅     | -       | ✅    | ✅     | ✅        | ✅        | ❌       |
| **Evaluacion**      | ✅     | ✅   | ✅     | ✅     | -       | ✅    | ✅     | ❌        | ✅        | ❌       |
| **Sentiment**       | -      | -    | -      | -      | ✅      | ✅    | ✅     | ❌        | ✅        | ✅       |
| **Reporte**         | ✅     | ✅   | ❌     | ✅     | -       | ✅    | ✅     | ❌        | ❌        | ✅       |
| **Usuario**         | ✅     | ✅   | ✅     | ✅     | -       | ✅    | ❌     | ❌        | ❌        | ❌       |
| **Roles**           | ✅     | ✅   | ✅     | ✅     | -       | ✅    | ❌     | ❌        | ❌        | ❌       |

---

## 🔒 Security Best Practices

1. **Principle of Least Privilege**: Grant minimum permissions needed
2. **Separation of Duties**: No single role should have all critical permissions
3. **Audit Logging**: Log all permission checks and changes
4. **Regular Reviews**: Periodically review and update permissions
5. **Default Deny**: Deny access by default, explicitly grant permissions
6. **Role Hierarchy**: Consider implementing role inheritance for complex scenarios

---

## 📝 Sample Permission Naming Convention

```
<resource>:<action>[:scope]

Examples:
- paciente:create
- paciente:read:own        (read own patients only)
- paciente:read:all        (read all patients)
- evaluacion:update
- sentiment:analyze:batch  (batch analysis)
- usuario:manage
- reporte:export
```

---

## 🔧 Migration Script for Option 1

```sql
-- V9__create_permission_system.sql

-- Step 1: Create permissions table
CREATE TABLE permissions (
    id_permission INT PRIMARY KEY AUTO_INCREMENT,
    permission_name VARCHAR(100) UNIQUE NOT NULL,
    resource VARCHAR(50) NOT NULL,
    action VARCHAR(20) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_resource_action (resource, action),
    INDEX idx_permission_name (permission_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Step 2: Create role_permissions junction table
CREATE TABLE role_permissions (
    id_role INT NOT NULL,
    id_permission INT NOT NULL,
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_role, id_permission),
    FOREIGN KEY (id_role) REFERENCES usuario_roles(id_roles) ON DELETE CASCADE,
    FOREIGN KEY (id_permission) REFERENCES permissions(id_permission) ON DELETE CASCADE,
    INDEX idx_role (id_role),
    INDEX idx_permission (id_permission)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Step 3: Insert base permissions
INSERT INTO permissions (permission_name, resource, action, description) VALUES
-- Paciente permissions
('paciente:create', 'PACIENTE', 'CREATE', 'Create patient records'),
('paciente:read', 'PACIENTE', 'READ', 'View patient information'),
('paciente:update', 'PACIENTE', 'UPDATE', 'Modify patient records'),
('paciente:delete', 'PACIENTE', 'DELETE', 'Delete patient records'),

-- Personal permissions
('personal:create', 'PERSONAL', 'CREATE', 'Create staff records'),
('personal:read', 'PERSONAL', 'READ', 'View staff information'),
('personal:update', 'PERSONAL', 'UPDATE', 'Modify staff records'),
('personal:delete', 'PERSONAL', 'DELETE', 'Delete staff records'),

-- Consulta permissions
('consulta:create', 'CONSULTA', 'CREATE', 'Create consultations'),
('consulta:read', 'CONSULTA', 'READ', 'View consultations'),
('consulta:update', 'CONSULTA', 'UPDATE', 'Modify consultations'),
('consulta:delete', 'CONSULTA', 'DELETE', 'Delete consultations'),

-- Evaluacion permissions
('evaluacion:create', 'EVALUACION', 'CREATE', 'Create evaluations'),
('evaluacion:read', 'EVALUACION', 'READ', 'View evaluations'),
('evaluacion:update', 'EVALUACION', 'UPDATE', 'Modify evaluations'),
('evaluacion:delete', 'EVALUACION', 'DELETE', 'Delete evaluations'),

-- Sentiment analysis permissions
('sentiment:analyze', 'SENTIMENT', 'EXECUTE', 'Run sentiment analysis'),
('sentiment:batch', 'SENTIMENT', 'EXECUTE', 'Run batch sentiment analysis'),

-- Reporte permissions
('reporte:create', 'REPORTE', 'CREATE', 'Generate reports'),
('reporte:read', 'REPORTE', 'READ', 'View reports'),
('reporte:delete', 'REPORTE', 'DELETE', 'Delete reports'),
('reporte:export', 'REPORTE', 'EXECUTE', 'Export reports'),

-- Usuario permissions
('usuario:create', 'USUARIO', 'CREATE', 'Create users'),
('usuario:read', 'USUARIO', 'READ', 'View users'),
('usuario:update', 'USUARIO', 'UPDATE', 'Modify users'),
('usuario:delete', 'USUARIO', 'DELETE', 'Delete users'),
('usuario:manage', 'USUARIO', 'MANAGE', 'Full user management'),

-- Role permissions
('role:create', 'ROLE', 'CREATE', 'Create roles'),
('role:read', 'ROLE', 'READ', 'View roles'),
('role:update', 'ROLE', 'UPDATE', 'Modify roles'),
('role:delete', 'ROLE', 'DELETE', 'Delete roles'),
('role:assign', 'ROLE', 'MANAGE', 'Assign roles to users');

-- Step 4: Assign permissions to ADMIN role (gets everything)
INSERT INTO role_permissions (id_role, id_permission)
SELECT r.id_roles, p.id_permission
FROM usuario_roles r
CROSS JOIN permissions p
WHERE r.permisos_roles = 'ADMIN';

-- Step 5: Assign permissions to DOCTOR role
INSERT INTO role_permissions (id_role, id_permission)
SELECT r.id_roles, p.id_permission
FROM usuario_roles r, permissions p
WHERE r.permisos_roles = 'DOCTOR'
AND p.permission_name IN (
    'paciente:create', 'paciente:read', 'paciente:update',
    'personal:read',
    'consulta:create', 'consulta:read', 'consulta:update',
    'evaluacion:create', 'evaluacion:read', 'evaluacion:update',
    'sentiment:analyze', 'sentiment:batch',
    'reporte:create', 'reporte:read', 'reporte:export'
);

-- Step 6: Assign permissions to ENFERMERO role
INSERT INTO role_permissions (id_role, id_permission)
SELECT r.id_roles, p.id_permission
FROM usuario_roles r, permissions p
WHERE r.permisos_roles = 'ENFERMERO'
AND p.permission_name IN (
    'paciente:read', 'paciente:update',
    'consulta:read', 'consulta:update'
);

-- Step 7: Assign permissions to ANALISTA role
INSERT INTO role_permissions (id_role, id_permission)
SELECT r.id_roles, p.id_permission
FROM usuario_roles r, permissions p
WHERE r.permisos_roles = 'ANALISTA'
AND p.permission_name IN (
    'sentiment:analyze', 'sentiment:batch',
    'reporte:read', 'reporte:create', 'reporte:export'
);

-- Step 8: Assign permissions to PSICOLOGO role
INSERT INTO role_permissions (id_role, id_permission)
SELECT r.id_roles, p.id_permission
FROM usuario_roles r, permissions p
WHERE r.permisos_roles = 'PSICOLOGO'
AND p.permission_name IN (
    'paciente:read', 'paciente:create', 'paciente:update',
    'consulta:create', 'consulta:read', 'consulta:update',
    'evaluacion:create', 'evaluacion:read', 'evaluacion:update',
    'sentiment:analyze'
);
```

---

## 📚 References

- Spring Security Method Security: https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html
- RBAC Best Practices: https://csrc.nist.gov/projects/role-based-access-control
- OWASP Authorization Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Authorization_Cheat_Sheet.html

---

## ✅ Conclusion

**Recommended Approach: Option 1 - Enhanced RBAC with Permission Tables**

This approach provides:
- **Flexibility**: Easy to modify permissions without code changes
- **Granularity**: Specific control over each action
- **Scalability**: Supports growth and new features
- **Auditability**: Full tracking of permission assignments
- **Best Practice**: Industry-standard RBAC implementation

The implementation will take approximately 3-4 weeks and provides a solid foundation for future access control requirements.


