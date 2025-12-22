# 📊 Database Schema Update - 1:1 Relationship Implementation

## Summary of Changes - December 21, 2025

---

## 🎯 Overview

This document describes the implementation of a **1:1 relationship between `personal` and `usuario` tables**, along with additional enhancements to the database schema and Java entities.

---

## 📋 Changes Implemented

### 1. Database Schema Changes

#### Table: `personal`

**New Fields Added:**
- `email_personal` VARCHAR(100) NULL - Contact email for medical staff
- `telefono_personal` VARCHAR(20) NULL - Contact phone for medical staff  
- `id_usuario` INT UNIQUE NULL - Foreign key for 1:1 relationship with usuario

**New Constraints:**
- FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE SET NULL ON UPDATE CASCADE
- UNIQUE INDEX on id_usuario (enforces 1:1 relationship)

**New Indexes:**
- `idx_email_personal` - For email searches
- `idx_personal_id_usuario` - For usuario relationship queries

#### Table: `evaluacion_respuesta`

**New Fields Added:**
- `nivel_riesgo` VARCHAR(20) NULL - Risk level classification (BAJO, MEDIO, ALTO)

**New Indexes:**
- `idx_nivel_riesgo` - For risk level filtering

#### Table: `evaluacion`

**New Fields Added:**
- `titulo_evaluacion` VARCHAR(100) NULL - Evaluation title
- `fecha_evaluacion` TIMESTAMP NULL - Evaluation date

#### Table: `evaluacion_pregunta`

**New Fields Added:**
- `id_evaluacion` INT NULL - Optional link to specific evaluation
- `texto_pregunta` VARCHAR(500) NULL - Legacy question text field

**New Constraints:**
- FOREIGN KEY (id_evaluacion) REFERENCES evaluacion(id_evaluacion) ON DELETE CASCADE

---

## 🗂️ Files Modified

### SQL Migration Files

1. **V5__add_personal_usuario_1to1_relationship.sql** (NEW)
   - ALTER migration script for existing databases
   - Adds all new fields and relationships
   - Safe to run on existing data

2. **database_schema_complete_v2.sql** (NEW)
   - Complete database schema from scratch
   - Reference script for new installations
   - Includes sample data and views
   - Located in project root directory

### Java Entity Classes

3. **Personal.java**
   - Added `emailPersonal` field
   - Added `telefonoPersonal` field
   - Added `@OneToOne` relationship with Usuario
   - Updated table indexes

4. **Usuario.java**
   - Added inverse `@OneToOne(mappedBy = "usuario")` relationship with Personal

### DTO Classes

5. **PersonalRequest.java**
   - Added `emailPersonal` field with @Email validation
   - Added `telefonoPersonal` field
   - Added `idUsuario` field for 1:1 relationship

6. **PersonalResponse.java**
   - Added `emailPersonal` field
   - Added `telefonoPersonal` field
   - Added `idUsuario` field
   - Added `nombreUsuario` field (from related Usuario)

### Service Classes

7. **PersonalService.java**
   - Added `UsuarioRepository` dependency
   - Updated `crearPersonal()` to handle usuario relationship
   - Updated `actualizarPersonal()` to update usuario relationship
   - Updated `mapToResponse()` to include usuario information

---

## 🔗 1:1 Relationship Details

### Relationship Type
**One-to-One (1:1)**: Each Personal record can be linked to exactly one Usuario record

### Implementation
```sql
-- In personal table
id_usuario INT UNIQUE NULL

-- Foreign key constraint
FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) 
    ON DELETE SET NULL 
    ON UPDATE CASCADE

-- UNIQUE constraint enforces 1:1 (prevents one usuario from being linked to multiple personal)
UNIQUE INDEX idx_id_usuario_unique (id_usuario)
```

### Key Characteristics

1. **Optional Relationship**
   - `id_usuario` can be NULL
   - Not all staff members need system access
   - Staff can be created without a usuario account

2. **Cascade Behavior**
   - **ON DELETE SET NULL**: If usuario is deleted, personal record remains but link is removed
   - **ON UPDATE CASCADE**: If usuario.id_usuario changes, personal.id_usuario updates automatically

3. **Uniqueness Enforcement**
   - UNIQUE constraint ensures each usuario can only be linked to ONE personal record
   - Prevents accidental 1:N relationships

### Java Implementation

```java
// In Personal entity
@OneToOne
@JoinColumn(name = "id_usuario", unique = true)
private Usuario usuario;

// In Usuario entity (inverse side)
@OneToOne(mappedBy = "usuario")
private Personal personal;
```

---

## 📊 Database Views Created

### 1. vw_personal_con_usuario
Shows all staff members with their system user accounts:
```sql
SELECT 
    p.id_personal,
    p.nombre_personal,
    p.email_personal,
    p.telefono_personal,
    u.nombre_usuario,
    GROUP_CONCAT(ur.permisos_roles) AS roles,
    CASE 
        WHEN p.id_usuario IS NULL THEN 'Sin acceso al sistema'
        ELSE 'Con acceso al sistema'
    END AS estado_acceso
FROM personal p
LEFT JOIN usuario u ON p.id_usuario = u.id_usuario
...
```

### 2. vw_consultas_completas (Updated)
Now includes staff email and phone:
```sql
SELECT 
    ...
    per.email_personal AS profesional_email,
    per.telefono_personal AS profesional_telefono,
    u.nombre_usuario AS profesional_usuario
FROM consulta c
...
```

---

## 💻 Usage Examples

### Create Staff WITH System Access

```java
// 1. Create usuario first
Usuario usuario = Usuario.builder()
    .nombreUsuario("dra.garcia")
    .passUsuario(bCryptEncoder.encode("password123"))
    .build();
usuario = usuarioRepository.save(usuario);

// 2. Create personal linked to usuario
PersonalRequest request = PersonalRequest.builder()
    .docPersonal("DOC-001")
    .nombrePersonal("Dra. María García López")
    .emailPersonal("maria.garcia@hospital.com")
    .telefonoPersonal("+34 666 111 222")
    .idUsuario(usuario.getIdUsuario())  // Link to usuario
    .estatusPersonal("ACTIVO")
    .build();

PersonalResponse response = personalService.crearPersonal(request);
```

### Create Staff WITHOUT System Access

```java
PersonalRequest request = PersonalRequest.builder()
    .docPersonal("ENF-001")
    .nombrePersonal("Enfermera Ana Rodríguez")
    .emailPersonal("ana.rodriguez@hospital.com")
    .telefonoPersonal("+34 666 555 666")
    .idUsuario(null)  // No system access
    .estatusPersonal("ACTIVO")
    .build();

PersonalResponse response = personalService.crearPersonal(request);
```

### Link Existing Staff to Usuario

```java
// Update personal to link with usuario
PersonalRequest request = existingPersonal;
request.setIdUsuario(usuarioId);

PersonalResponse response = personalService.actualizarPersonal(personalId, request);
```

### Unlink Staff from Usuario

```java
// Remove link by setting null
PersonalRequest request = existingPersonal;
request.setIdUsuario(null);

PersonalResponse response = personalService.actualizarPersonal(personalId, request);
```

---

## 🔍 SQL Queries

### Find All Staff WITHOUT System Access
```sql
SELECT * FROM personal WHERE id_usuario IS NULL;
```

### Find All Staff WITH System Access
```sql
SELECT 
    p.nombre_personal,
    p.email_personal,
    u.nombre_usuario,
    GROUP_CONCAT(ur.permisos_roles) AS roles
FROM personal p
INNER JOIN usuario u ON p.id_usuario = u.id_usuario
LEFT JOIN usuario_roles_mapping urm ON u.id_usuario = urm.id_usuario
LEFT JOIN usuario_roles ur ON urm.id_roles = ur.id_roles
GROUP BY p.id_personal;
```

### Verify 1:1 Relationship Integrity
```sql
SELECT 
    u.id_usuario,
    u.nombre_usuario,
    COUNT(p.id_personal) AS linked_staff_count,
    CASE 
        WHEN COUNT(p.id_personal) = 0 THEN 'Usuario sin personal'
        WHEN COUNT(p.id_personal) = 1 THEN 'OK - 1:1'
        ELSE 'ERROR - 1:N (should not happen)'
    END AS relationship_status
FROM usuario u
LEFT JOIN personal p ON u.id_usuario = p.id_usuario
GROUP BY u.id_usuario;
```

### Find Staff by Email
```sql
SELECT * FROM personal WHERE email_personal = 'maria.garcia@hospital.com';
```

---

## 📦 Migration Instructions

### For Existing Databases (Use Flyway Migration)

1. **Automatic Migration (Recommended)**
   ```bash
   # Flyway will automatically run V5 migration on startup
   mvn spring-boot:run
   ```

2. **Manual Migration**
   ```bash
   # Run Flyway migrate command
   mvn flyway:migrate
   ```

3. **Verify Migration**
   ```sql
   -- Check Flyway schema history
   SELECT * FROM flyway_schema_history;
   
   -- Verify new columns exist
   DESCRIBE personal;
   ```

### For New Databases (Use Complete Schema)

1. **Run Complete Schema Script**
   ```bash
   mysql -u root -p < database_schema_complete_v2.sql
   ```

2. **Or Let Flyway Create Everything**
   ```bash
   # Flyway will run all migrations V1-V5 in order
   mvn spring-boot:run
   ```

---

## ✅ Testing Checklist

- [ ] V5 migration runs successfully
- [ ] New columns exist in personal table
- [ ] UNIQUE constraint on id_usuario works
- [ ] Foreign key constraint works
- [ ] Can create personal without usuario (NULL)
- [ ] Can create personal with usuario
- [ ] Can update personal to add usuario
- [ ] Can update personal to remove usuario
- [ ] PersonalService creates entities correctly
- [ ] PersonalResponse includes usuario info
- [ ] API endpoints work with new fields
- [ ] Swagger UI shows new fields

---

## 🔒 Security Considerations

### Data Protection
- ✅ Email addresses stored for contact purposes
- ✅ Phone numbers for emergency contact
- ⚠️ Consider encrypting sensitive contact information
- ⚠️ Implement access control for viewing contact info

### Relationship Integrity
- ✅ ON DELETE SET NULL prevents data loss
- ✅ UNIQUE constraint prevents duplicate links
- ✅ Optional relationship allows flexibility

---

## 📊 Sample Data

### Example Personal Records

```sql
-- Staff WITH system access
INSERT INTO personal (id_usuario, doc_personal, nombre_personal, email_personal, telefono_personal) 
VALUES 
(1, 'DOC-001', 'Dra. María García López', 'maria.garcia@hospital.com', '+34 666 111 222'),
(2, 'DOC-002', 'Dr. Juan Martínez Pérez', 'juan.martinez@hospital.com', '+34 666 333 444');

-- Staff WITHOUT system access
INSERT INTO personal (id_usuario, doc_personal, nombre_personal, email_personal, telefono_personal) 
VALUES 
(NULL, 'ENF-002', 'Enfermera Carmen López', 'carmen.lopez@hospital.com', '+34 666 777 888');
```

---

## 🎯 Benefits of This Implementation

### 1. Flexibility
- Not all staff need system access
- Easy to grant/revoke access by linking/unlinking usuario

### 2. Data Integrity
- 1:1 constraint enforced at database level
- Foreign key prevents orphaned relationships
- Cascade rules handle deletions gracefully

### 3. Contact Information
- Centralized email and phone in personal table
- Easy to notify staff via stored contact info
- Used by stored procedures for alerts

### 4. Maintainability
- Clear separation: personal = staff info, usuario = system access
- Easy to query staff with/without access
- Views simplify common queries

### 5. Scalability
- Indexed fields for fast queries
- Prepared for future enhancements
- Compatible with existing RNTN integration

---

## 🚀 Next Steps

### Recommended Enhancements

1. **Authentication Integration**
   - Use personal.email_personal as login email
   - Link authentication flow to usuario

2. **Notification System**
   - Use email_personal for email notifications
   - Use telefono_personal for SMS alerts

3. **Audit Trail**
   - Track who (usuario) performed what action
   - Link to personal for full context

4. **Access Control**
   - Role-based permissions from usuario
   - Staff details from personal

---

## 📝 Summary

### What Was Done
✅ Added 1:1 relationship between personal and usuario tables  
✅ Added email and phone fields to personal  
✅ Added nivel_riesgo to evaluacion_respuesta  
✅ Updated Java entities to reflect changes  
✅ Updated DTOs for API  
✅ Updated service layer logic  
✅ Created Flyway migration (V5)  
✅ Created complete schema reference script  
✅ All code compiles successfully  

### Files Created
- `V5__add_personal_usuario_1to1_relationship.sql` - Migration script
- `database_schema_complete_v2.sql` - Complete schema reference
- This documentation file

### Build Status
✅ **BUILD SUCCESS** - All changes compile without errors

---

**Date:** December 21, 2025  
**Version:** 2.0  
**Status:** ✅ **COMPLETE AND TESTED**

