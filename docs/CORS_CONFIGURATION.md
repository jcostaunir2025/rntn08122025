# Configuración CORS - Backend Spring Boot

## 📋 Resumen

Se ha configurado CORS (Cross-Origin Resource Sharing) en el backend Spring Boot para permitir el acceso desde aplicaciones frontend alojadas en diferentes orígenes.

## 🔧 Archivos Modificados/Creados

### 1. **CorsConfig.java** (NUEVO)
**Ubicación**: `src/main/java/com/example/rntn/config/CorsConfig.java`

**Descripción**: Clase de configuración que define las políticas CORS para la API.

**Características Principales**:
- ✅ Orígenes permitidos para desarrollo local (puertos 3000, 4200, 5173, 8081)
- ✅ Métodos HTTP permitidos: GET, POST, PUT, DELETE, PATCH, OPTIONS
- ✅ Headers permitidos: Authorization, Content-Type, Accept, etc.
- ✅ Headers expuestos: Authorization, Content-Type, Content-Disposition, X-Total-Count
- ✅ Credenciales permitidas (cookies, tokens JWT)
- ✅ Cache de configuración CORS: 3600 segundos

### 2. **SecurityConfig.java** (MODIFICADO)
**Ubicación**: `src/main/java/com/example/rntn/security/SecurityConfig.java`

**Cambios realizados**:
- Se inyectó `CorsConfigurationSource` como dependencia
- Se habilitó CORS en el `SecurityFilterChain` con: `.cors(cors -> cors.configurationSource(corsConfigurationSource))`

## 🌐 Orígenes Frontend Permitidos

Por defecto, se permiten los siguientes orígenes para desarrollo local:

```
http://localhost:3000       // React/Next.js
http://localhost:4200       // Angular
http://localhost:5173       // Vite
http://localhost:8081       // Puerto alternativo
http://127.0.0.1:3000
http://127.0.0.1:4200
http://127.0.0.1:5173
http://127.0.0.1:8081
```

### ⚙️ Agregar Orígenes de Producción

Para agregar URLs de producción, edita `CorsConfig.java`:

```java
configuration.setAllowedOrigins(Arrays.asList(
    "http://localhost:3000",
    "http://localhost:4200",
    "https://tu-dominio-frontend.com",      // ← Agregar aquí
    "https://app.tu-empresa.com"            // ← Agregar aquí
));
```

## 🔐 Integración con JWT

La configuración CORS está completamente integrada con el sistema de autenticación JWT:

1. **Authorization Header**: Permitido en peticiones cross-origin
2. **Credenciales**: Habilitadas con `setAllowCredentials(true)`
3. **Preflight Requests**: Manejadas automáticamente por Spring Security

## 🧪 Probar la Configuración CORS

### Desde el navegador (Consola JavaScript):

```javascript
fetch('http://localhost:8080/api/v1/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  credentials: 'include',
  body: JSON.stringify({
    username: 'admin',
    password: 'password123'
  })
})
.then(response => response.json())
.then(data => console.log(data))
.catch(error => console.error('Error:', error));
```

### Desde aplicación React:

```javascript
// Login request
const response = await fetch('http://localhost:8080/api/v1/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({ username, password })
});

const data = await response.json();
const token = data.token;

// Authenticated request
const patientsResponse = await fetch('http://localhost:8080/api/v1/pacientes', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
  }
});
```

### Desde aplicación Angular:

```typescript
// login.service.ts
import { HttpClient, HttpHeaders } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/v1';
  
  constructor(private http: HttpClient) {}
  
  login(username: string, password: string) {
    return this.http.post(`${this.apiUrl}/auth/login`, { username, password });
  }
  
  getPatients(token: string) {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.get(`${this.apiUrl}/pacientes`, { headers });
  }
}
```

### Usando cURL (Preflight Request):

```bash
# OPTIONS request (preflight)
curl -X OPTIONS http://localhost:8080/api/v1/pacientes \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: Authorization" \
  -v
```

## 📊 Headers CORS en las Respuestas

Las respuestas del backend incluirán los siguientes headers CORS:

```
Access-Control-Allow-Origin: http://localhost:3000
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, PATCH, OPTIONS
Access-Control-Allow-Headers: Authorization, Content-Type, Accept, ...
Access-Control-Allow-Credentials: true
Access-Control-Max-Age: 3600
Access-Control-Expose-Headers: Authorization, Content-Type, ...
```

## ⚠️ Consideraciones de Seguridad

### Desarrollo vs Producción

**Desarrollo**: 
- Se permiten múltiples orígenes localhost
- Configuración más permisiva para facilitar desarrollo

**Producción**:
- ⚠️ **IMPORTANTE**: Actualizar la lista de orígenes permitidos
- ⚠️ Solo incluir dominios específicos y confiables
- ⚠️ Nunca usar `"*"` con credenciales habilitadas

### Buenas Prácticas

1. ✅ **Especificar orígenes exactos**: No usar wildcards en producción
2. ✅ **Revisar headers permitidos**: Solo permitir headers necesarios
3. ✅ **Limitar métodos HTTP**: Solo permitir métodos que se usan
4. ✅ **Monitorear peticiones CORS**: Revisar logs de peticiones rechazadas

## 🔄 Configuración por Entorno

Si necesitas diferentes configuraciones CORS por entorno, puedes usar `@Profile`:

```java
@Configuration
public class CorsConfig {

    @Bean
    @Profile("dev")
    public CorsConfigurationSource corsConfigurationSourceDev() {
        // Configuración permisiva para desarrollo
    }
    
    @Bean
    @Profile("prod")
    public CorsConfigurationSource corsConfigurationSourceProd() {
        // Configuración restrictiva para producción
    }
}
```

O usar propiedades en `application.yml`:

```yaml
cors:
  allowed-origins:
    - http://localhost:3000
    - http://localhost:4200
  allowed-methods:
    - GET
    - POST
    - PUT
    - DELETE
```

## 🐛 Troubleshooting

### Error: "CORS policy: No 'Access-Control-Allow-Origin' header"

**Solución**: 
- Verificar que el origen del frontend esté en la lista de `allowedOrigins`
- Verificar que el backend esté corriendo
- Verificar que la configuración CORS esté cargada

### Error: "CORS policy: Response to preflight request doesn't pass"

**Solución**:
- Verificar que el método HTTP esté en `allowedMethods`
- Verificar que los headers estén en `allowedHeaders`
- Verificar que el endpoint no requiera autenticación para OPTIONS

### Error: "Credentials flag is true, but Access-Control-Allow-Credentials is not"

**Solución**:
- Verificar que `setAllowCredentials(true)` esté configurado
- No usar `"*"` en `allowedOrigins` cuando las credenciales están habilitadas

## ✅ Verificación

Para verificar que CORS está funcionando correctamente:

1. **Compilar el proyecto**: 
   ```bash
   mvn clean compile
   ```

2. **Iniciar el servidor**:
   ```bash
   mvn spring-boot:run
   ```

3. **Probar desde el navegador**:
   - Abrir la consola de desarrollador
   - Ejecutar una petición fetch desde una aplicación en puerto diferente
   - Verificar que no haya errores CORS en la consola

4. **Verificar headers**:
   - Usar Network tab en DevTools
   - Verificar que las respuestas incluyan headers `Access-Control-*`

## 📝 Notas Adicionales

- La configuración CORS se aplica a **todos los endpoints** (`/**`)
- Las peticiones OPTIONS se manejan automáticamente
- El cache de preflight (3600s) reduce peticiones OPTIONS repetidas
- La configuración es compatible con JWT y Spring Security

## 🔗 Referencias

- [Spring CORS Documentation](https://docs.spring.io/spring-framework/reference/web/webmvc-cors.html)
- [MDN CORS Guide](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS)
- [Spring Security CORS](https://docs.spring.io/spring-security/reference/servlet/integrations/cors.html)

---

**Fecha de implementación**: 2025-12-27  
**Estado**: ✅ Configuración completada y verificada

