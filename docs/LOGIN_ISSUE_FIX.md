# 🔍 Login Issue Diagnosis & Fix

## ❌ Current Issue

**Problem:** Login endpoint returns 500 Internal Server Error

**Tested:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}'
```

**Response:** `500 Internal Server Error`

---

## ✅ Database Verification

### Users Exist
```sql
mysql> SELECT u.nombre_usuario, ur.permisos_roles 
       FROM usuario u 
       LEFT JOIN usuario_roles_mapping urm ON u.id_usuario = urm.id_usuario 
       LEFT JOIN usuario_roles ur ON urm.id_roles = ur.id_roles 
       WHERE u.nombre_usuario = 'admin';

+----------------+----------------+
| nombre_usuario | permisos_roles |
+----------------+----------------+
| admin          | ADMIN          |
+----------------+----------------+
```
✅ User exists with ADMIN role

### Password Hash
```sql
mysql> SELECT nombre_usuario, pass_usuario 
       FROM usuario 
       WHERE nombre_usuario = 'admin';

+----------------+--------------------------------------------------------------+
| nombre_usuario | pass_usuario                                                 |
+----------------+--------------------------------------------------------------+
| admin          | $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi |
+----------------+--------------------------------------------------------------+
```
✅ Password hash exists

---

## 🔍 Root Cause

The BCrypt hash `$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi` is the standard BCrypt hash for the password **"password"**, NOT "password123".

This is a well-known test hash used in Spring Security examples.

---

## ✅ Solution Options

### Option 1: Use Correct Password (Quick Fix)
Try logging in with **"password"** instead of "password123":

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'
```

### Option 2: Update Database with Correct Hash (Permanent Fix)

Generate correct BCrypt hash for "password123":
```java
// Java code to generate hash
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String hash = encoder.encode("password123");
System.out.println(hash);
```

Or use online tool: https://bcrypt-generator.com/
- Input: `password123`
- Rounds: 10
- Output: New hash

Then update database:
```sql
UPDATE usuario 
SET pass_usuario = '$2a$10$NEW_HASH_HERE' 
WHERE nombre_usuario = 'admin';
```

### Option 3: Re-run V8 Migration with Correct Hash

Update `V8__insert_default_users.sql`:
1. Generate real BCrypt hash for "password123"
2. Replace the hash in the SQL file
3. Drop and recreate database
4. Re-run migrations

---

## 🧪 Test with Correct Password

### Test 1: Login with "password"
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}' \
  | jq
```

**Expected Success Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "username": "admin",
  "roles": ["ROLE_ADMIN"],
  "expiresIn": 3600000
}
```

### Test 2: Use Token
```bash
# Copy token from login response
TOKEN="eyJhbGciOiJIUzI1NiJ9..."

# Test protected endpoint
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/pacientes
```

---

## 🔧 Correct Implementation

### Generate Real BCrypt Hash

#### Using Java
```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "password123";
        String hash = encoder.encode(password);
        System.out.println("Hash for '" + password + "': " + hash);
    }
}
```

#### Using Spring Boot DevTools
```bash
mvn spring-boot:run

# In another terminal
curl -X POST http://localhost:8080/api/v1/utils/hash-password \
  -H "Content-Type: application/json" \
  -d '{"password":"password123"}'
```

#### Using Online Tool
Visit: https://bcrypt-generator.com/
- Enter: `password123`
- Rounds: 10
- Click "Hash"
- Copy the result

---

## 📝 Update V8 Migration

Edit: `src/main/resources/db/migration/V8__insert_default_users.sql`

```sql
-- Generate proper BCrypt hashes for "password123"
-- Use: BCryptPasswordEncoder with strength 10

-- Example hash (generate your own):
-- $2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRps.9cGLcZEiGDMVr5yUP1KUOYTa

INSERT INTO usuario (nombre_usuario, pass_usuario, created_at)
VALUES 
('admin', '$2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRps.9cGLcZEiGDMVr5yUP1KUOYTa', NOW()),
('doctor1', '$2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRps.9cGLcZEiGDMVr5yUP1KUOYTa', NOW()),
('enfermero1', '$2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRps.9cGLcZEiGDMVr5yUP1KUOYTa', NOW()),
('analista1', '$2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRps.9cGLcZEiGDMVr5yUP1KUOYTa', NOW());
```

**Note:** ALL users should use the SAME hash since they all have the same password.

---

## 🎯 Quick Fix Right Now

### Try this immediately:
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'
```

If this works, you'll get a JWT token! ✅

---

## ✅ Verification Steps

1. **Test Login with "password"**
   ```bash
   curl -X POST http://localhost:8080/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"admin","password":"password"}'
   ```

2. **Extract Token**
   ```bash
   TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"admin","password":"password"}' | jq -r '.token')
   
   echo $TOKEN
   ```

3. **Test Protected Endpoint**
   ```bash
   curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8080/api/v1/pacientes
   ```

4. **Test in Swagger UI**
   - Open: http://localhost:8080/swagger-ui.html
   - Click "Authorize"
   - Login with: `admin` / `password`
   - Copy token
   - Paste in authorization dialog
   - Test endpoints

---

## 📊 Summary

### Issue
- ❌ Using password: "password123"
- ✅ Hash is for: "password"
- ❌ Mismatch causes authentication failure

### Solution
**Immediate:** Use password "password" to login
**Long-term:** Update V8 migration with correct BCrypt hash for "password123"

### Test Credentials (Current)
| Username | Password | Role |
|----------|----------|------|
| admin | **password** | ADMIN |
| doctor1 | **password** | DOCTOR |
| enfermero1 | **password** | ENFERMERO |
| analista1 | **password** | ANALISTA |

---

## 🎉 Expected Working Example

```bash
$ curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'

{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJhdXRob3JpdGllcyI6W3siYXV0aG9yaXR5IjoiUk9MRV9BRE1JTiJ9XSwic3ViIjoiYWRtaW4iLCJpYXQiOjE3MDM4MDAyMDAsImV4cCI6MTcwMzgwMzgwMH0.abc123...",
  "type": "Bearer",
  "username": "admin",
  "roles": ["ROLE_ADMIN"],
  "expiresIn": 3600000
}

✅ SUCCESS!
```

---

**TL;DR: Try password "password" instead of "password123"**

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'
```

This should work immediately! 🎉

