# Password Hashing Implementation - UsuarioService

## Date: 2025-12-31

## Issue Found

The `UsuarioService` was storing user passwords in **plain text** without hashing, which is a critical security vulnerability.

### Before (INSECURE):
```java
// ❌ Password stored as plain text
Usuario usuario = Usuario.builder()
    .nombreUsuario(request.getNombreUsuario())
    .passUsuario(request.getPassUsuario()) // Plain text password!
    .roles(roles)
    .build();
```

## Fix Applied

### Changes Made:

#### 1. Added PasswordEncoder Dependency
```java
@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder; // ✅ Added
}
```

#### 2. Updated `crearUsuario()` Method
```java
public UsuarioResponse crearUsuario(UsuarioRequest request) {
    // ... validation code ...
    
    // ✅ Hash password with BCrypt
    String hashedPassword = passwordEncoder.encode(request.getPassUsuario());
    log.debug("Password hashed successfully for user: {}", request.getNombreUsuario());

    Usuario usuario = Usuario.builder()
        .nombreUsuario(request.getNombreUsuario())
        .passUsuario(hashedPassword) // ✅ Hashed password
        .roles(roles)
        .build();
    
    // ... save and return ...
}
```

#### 3. Updated `actualizarUsuario()` Method
```java
public UsuarioResponse actualizarUsuario(Integer id, UsuarioRequest request) {
    // ... validation code ...
    
    // ✅ Hash password if it's being changed
    String hashedPassword = passwordEncoder.encode(request.getPassUsuario());
    usuario.setPassUsuario(hashedPassword);
    log.debug("Password updated and hashed for user: {}", usuario.getNombreUsuario());
    
    // ... save and return ...
}
```

## Security Configuration

The `PasswordEncoder` bean is already configured in `SecurityConfig.java`:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(); // ✅ BCrypt hashing algorithm
}
```

## BCrypt Password Hashing

### What is BCrypt?
- **Industry-standard** password hashing algorithm
- **Adaptive**: Can be configured to be slower as computers get faster
- **Salt included**: Automatically generates and includes a unique salt per password
- **One-way function**: Impossible to reverse (decrypt) the hash

### Example Hash:
```
Plain text: "password123"
BCrypt hash: "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
```

### Hash Structure:
```
$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
│  │  │  └─────────────────────────────────────────────┬──────────┘
│  │  │                                                  │
│  │  │                                            Actual hash
│  │  │
│  │  └─ Salt (22 characters)
│  │
│  └─ Cost factor (10 = 2^10 = 1024 rounds)
│
└─ Algorithm identifier (2a = BCrypt)
```

## Verification

### How Authentication Works Now:

1. **User Registration** (POST `/api/v1/usuarios`):
   - Password: `"password123"`
   - Stored in DB: `"$2a$10$N9qo8u...lhWy"` (hashed)

2. **User Login** (POST `/api/v1/auth/login`):
   - User sends: `"password123"`
   - System fetches stored hash from DB
   - BCrypt compares: `passwordEncoder.matches("password123", storedHash)`
   - Returns: `true` ✅ or `false` ❌

3. **Password Update** (PUT `/api/v1/usuarios/{id}`):
   - New password: `"newPassword456"`
   - Stored in DB: `"$2a$10$X7yp9m...aB3c"` (new hash)

## Security Best Practices ✅

- ✅ **Never store plain text passwords**
- ✅ **Use BCrypt or Argon2 for hashing**
- ✅ **Hash is done on server-side**
- ✅ **Each password gets unique salt**
- ✅ **Hash is irreversible**
- ✅ **Cost factor makes brute force impractical**

## Testing the Fix

### Create a new user:
```bash
curl -X POST http://localhost:8080/api/v1/usuarios \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin-token>" \
  -d '{
    "nombreUsuario": "testuser",
    "passUsuario": "MySecurePassword123!",
    "rolesIds": [2]
  }'
```

### Check in database:
```sql
SELECT id_usuario, nombre_usuario, pass_usuario 
FROM usuario 
WHERE nombre_usuario = 'testuser';

-- Result should show:
-- pass_usuario: $2a$10$... (60 character BCrypt hash)
-- NOT: MySecurePassword123! (plain text)
```

### Login with the user:
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "MySecurePassword123!"
  }'

# Should return JWT token if password matches
```

## Related Files Modified

1. ✅ `src/main/java/com/example/rntn/service/UsuarioService.java`
   - Added `PasswordEncoder` dependency
   - Updated `crearUsuario()` method
   - Updated `actualizarUsuario()` method

2. ✅ `src/main/java/com/example/rntn/security/SecurityConfig.java`
   - Already had `BCryptPasswordEncoder` bean configured

## Build Status

✅ **Compilation successful** - No errors

## Important Notes

⚠️ **Existing users with plain text passwords:**
If there are existing users in the database with plain text passwords, they will need to:
1. Have their passwords reset by an admin, OR
2. Run a migration script to hash existing passwords

⚠️ **Password in transit:**
Always use HTTPS in production to encrypt passwords during transmission from client to server.

## Recommendations

1. ✅ **Implemented**: BCrypt password hashing
2. ✅ **Configured**: Proper security configuration
3. 🔄 **TODO**: Update existing plain text passwords if any
4. 🔄 **TODO**: Implement password strength validation
5. 🔄 **TODO**: Add password change endpoint (separate from update user)
6. 🔄 **TODO**: Implement password reset functionality

## Summary

The password security vulnerability has been **FIXED**:
- ❌ Before: Passwords stored as plain text
- ✅ After: Passwords hashed with BCrypt
- ✅ Cost factor: 10 (1024 rounds)
- ✅ Automatic salting
- ✅ Secure authentication

The application now follows security best practices for password storage! 🔒

