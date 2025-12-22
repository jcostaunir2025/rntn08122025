# Checklist de Implementación: RNTN Sentiment API con MySQL

**Proyecto:** Refactorización de RNTN CLI a REST API con persistencia MySQL  
**Fecha de actualización:** 21 de Diciembre de 2025  
**Estado:** Documentación completa - Lista para implementación

---

## 📋 Resumen de Cambios Realizados

### ✅ Documentación Actualizada

1. **REFACTOR_TO_REST_API_PROMPT.md** - Actualizado con:
   - ✅ Sección completa de modelo entidad-relación (10 tablas)
   - ✅ Endpoints CRUD para todas las entidades
   - ✅ Configuración de MySQL + Spring Data JPA + Flyway
   - ✅ Arquitectura de capas completa (entities, repositories, services, controllers)
   - ✅ Ejemplos de endpoints de integración (análisis + persistencia)
   - ✅ Implementación de entidades JPA con relaciones
   - ✅ Repositorios con queries personalizados
   - ✅ Scripts de migración SQL (Flyway V1, V2, V3)

2. **DATABASE_INTEGRATION_SUMMARY.md** - Nuevo documento con:
   - ✅ Diagrama entidad-relación en ASCII
   - ✅ Flujo de datos completo
   - ✅ Mapeo de labels RNTN a niveles de riesgo
   - ✅ Queries SQL importantes
   - ✅ Ejemplos de endpoints
   - ✅ Comandos útiles para setup

3. **CODE_EXAMPLES_SERVICES_CONTROLLERS.md** - Nuevo documento con:
   - ✅ EvaluacionService completo
   - ✅ SentimentService con integración RNTN
   - ✅ EvaluacionController con Swagger
   - ✅ ConsultaController con dashboard
   - ✅ DTOs Request/Response
   - ✅ MapStruct mappers
   - ✅ GlobalExceptionHandler
   - ✅ Configuración Swagger

4. **ARCHITECTURE_COMPARISON_ANALYSIS.md** - Existente
   - Comparación con arquitectura books-catalogue

---

## 📊 Modelo de Datos - Resumen

### Entidades Principales

| Entidad | Tabla | Descripción | Relaciones |
|---------|-------|-------------|------------|
| **Paciente** | `paciente` | Información de pacientes | 1:N con Consulta |
| **Personal** | `personal` | Personal médico | 1:N con Consulta |
| **Usuario** | `usuario` | Usuarios del sistema | N:M con UsuarioRoles, 1:N con Reporte |
| **UsuarioRoles** | `usuario_roles` | Roles y permisos | N:M con Usuario |
| **Consulta** | `consulta` | Consultas médicas | N:1 con Paciente/Personal, 1:N con Evaluacion |
| **ConsultaEstatus** | `consulta_estatus` | Estados de consulta | Catálogo |
| **Evaluacion** | `evaluacion` | Evaluaciones de consulta | N:1 con Consulta, 1:N con Reporte |
| **EvaluacionPregunta** | `evaluacion_pregunta` | Preguntas estándar | 1:N con EvaluacionRespuesta |
| **EvaluacionRespuesta** | `evaluacion_respuesta` | **⭐ Respuestas con análisis RNTN** | N:1 con EvaluacionPregunta |
| **Reporte** | `reporte` | Reportes generados | N:1 con Usuario/Evaluacion |

### Integración RNTN - Campo Clave

```sql
-- Tabla: evaluacion_respuesta
label_evaluacion_respuesta VARCHAR(50),  -- ANXIETY, SUICIDAL, ANGER, SADNESS, FRUSTRATION
confidence_score DOUBLE                  -- 0.0 - 1.0
```

---

## 🎯 Plan de Implementación Completo

### FASE 1: Setup Base de Datos (2-3 horas)

- [ ] **1.1** Instalar MySQL 8.0+ localmente o Docker
  ```bash
  docker run --name mysql-rntn \
    -e MYSQL_ROOT_PASSWORD=rootpass \
    -e MYSQL_DATABASE=rntn_db \
    -e MYSQL_USER=rntn_user \
    -e MYSQL_PASSWORD=rntn_password \
    -p 3306:3306 -d mysql:8.0
  ```

- [ ] **1.2** Crear base de datos y usuario
  ```sql
  CREATE DATABASE rntn_db CHARACTER SET utf8mb4;
  CREATE USER 'rntn_user'@'localhost' IDENTIFIED BY 'rntn_password';
  GRANT ALL PRIVILEGES ON rntn_db.* TO 'rntn_user'@'localhost';
  ```

- [ ] **1.3** Configurar variables de entorno
  - Crear archivo `.env` con credenciales
  - Actualizar `application.yml` con variables

### FASE 2: Actualizar pom.xml (30 minutos)

- [ ] **2.1** Añadir dependencias Spring Data JPA
- [ ] **2.2** Añadir MySQL Connector
- [ ] **2.3** Añadir Flyway Core + Flyway MySQL
- [ ] **2.4** Añadir MapStruct + annotation processor
- [ ] **2.5** Añadir iText PDF para reportes
- [ ] **2.6** Configurar maven-compiler-plugin con annotation processors

### FASE 3: Migraciones Flyway (1-2 horas)

- [ ] **3.1** Crear directorio `src/main/resources/db/migration`
- [ ] **3.2** Crear `V1__create_initial_schema.sql` (10 tablas)
- [ ] **3.3** Crear `V2__insert_master_data.sql` (roles, estados, preguntas)
- [ ] **3.4** Crear `V3__create_indexes.sql` (optimización)
- [ ] **3.5** Ejecutar migraciones: `mvn flyway:migrate`
- [ ] **3.6** Verificar en MySQL: `SHOW TABLES;`

### FASE 4: Entidades JPA (3-4 horas)

- [ ] **4.1** Crear paquete `com.example.rntn.entity`
- [ ] **4.2** Implementar `Paciente.java` con anotaciones JPA
- [ ] **4.3** Implementar `Personal.java`
- [ ] **4.4** Implementar `Usuario.java` + `UsuarioRoles.java`
- [ ] **4.5** Implementar `Consulta.java` con relaciones
- [ ] **4.6** Implementar `ConsultaEstatus.java`
- [ ] **4.7** Implementar `Evaluacion.java`
- [ ] **4.8** Implementar `EvaluacionPregunta.java`
- [ ] **4.9** Implementar `EvaluacionRespuesta.java` ⭐ (integración RNTN)
- [ ] **4.10** Implementar `Reporte.java`
- [ ] **4.11** Verificar relaciones (OneToMany, ManyToOne, ManyToMany)

### FASE 5: Repositorios JPA (2-3 horas)

- [ ] **5.1** Crear paquete `com.example.rntn.repository`
- [ ] **5.2** Implementar `PacienteRepository` con queries personalizados
- [ ] **5.3** Implementar `PersonalRepository`
- [ ] **5.4** Implementar `UsuarioRepository` + `UsuarioRolesRepository`
- [ ] **5.5** Implementar `ConsultaRepository` con filtros de fecha
- [ ] **5.6** Implementar `ConsultaEstatusRepository`
- [ ] **5.7** Implementar `EvaluacionRepository`
- [ ] **5.8** Implementar `EvaluacionPreguntaRepository`
- [ ] **5.9** Implementar `EvaluacionRespuestaRepository` ⭐
- [ ] **5.10** Implementar `ReporteRepository` con queries agregadas
- [ ] **5.11** Probar repositories con tests unitarios

### FASE 6: DTOs (1-2 horas)

- [ ] **6.1** Crear paquete `com.example.rntn.dto.request`
- [ ] **6.2** Crear paquete `com.example.rntn.dto.response`
- [ ] **6.3** Implementar DTOs de Request con validaciones (@NotNull, @Size, etc.)
  - [ ] PacienteRequest
  - [ ] PersonalRequest
  - [ ] UsuarioRequest
  - [ ] ConsultaRequest
  - [ ] EvaluacionRequest
  - [ ] EvaluacionRespuestaRequest ⭐
  - [ ] ReporteRequest
- [ ] **6.4** Implementar DTOs de Response
  - [ ] PacienteResponse
  - [ ] ConsultaResponse
  - [ ] EvaluacionRespuestaResponse ⭐
  - [ ] AnalisisSentimientoResponse ⭐
  - [ ] ReporteResponse

### FASE 7: MapStruct Mappers (1 hora)

- [ ] **7.1** Crear paquete `com.example.rntn.mapper`
- [ ] **7.2** Implementar `PacienteMapper`
- [ ] **7.3** Implementar `ConsultaMapper`
- [ ] **7.4** Implementar `EvaluacionMapper` ⭐
- [ ] **7.5** Implementar `ReporteMapper`
- [ ] **7.6** Compilar y verificar clases generadas en `target/generated-sources`

### FASE 8: SentimentService (3-4 horas) ⭐

- [ ] **8.1** Refactorizar `SentimentPredictor` para quitar `main()`
- [ ] **8.2** Crear `SentimentService` como @Service
- [ ] **8.3** Cargar modelo RNTN en @PostConstruct
- [ ] **8.4** Implementar mapeo de índices a labels personalizados
  ```java
  0 → ANXIETY
  1 → SUICIDAL
  2 → ANGER
  3 → SADNESS
  4 → FRUSTRATION
  ```
- [ ] **8.5** Implementar método `analizarTexto(String texto)`
- [ ] **8.6** Implementar método `analizarLote(List<String> textos)` asíncrono
- [ ] **8.7** Implementar método `determinarNivelRiesgo(String label)`
- [ ] **8.8** Añadir logging detallado
- [ ] **8.9** Probar con textos de ejemplo

### FASE 9: Servicios de Negocio (5-6 horas)

- [ ] **9.1** Crear paquete `com.example.rntn.service`
- [ ] **9.2** Implementar `PacienteService` (CRUD básico)
- [ ] **9.3** Implementar `PersonalService` (CRUD básico)
- [ ] **9.4** Implementar `UsuarioService` (CRUD + autenticación)
- [ ] **9.5** Implementar `ConsultaService`
  - [ ] CRUD de consultas
  - [ ] Filtrado por paciente/personal/fecha
  - [ ] Método `generarDashboard(id)`
  - [ ] Método `analizarConsultaCompleta(id, textos)`
- [ ] **9.6** Implementar `EvaluacionService` ⭐
  - [ ] CRUD de evaluaciones
  - [ ] Método `registrarRespuestaConAnalisis()` ⭐
  - [ ] Método `obtenerAnalisisCompleto()` ⭐
  - [ ] Integración con `SentimentService`
  - [ ] Detección de alertas de riesgo
- [ ] **9.7** Implementar `ReporteService`
  - [ ] Generación de reportes
  - [ ] Cálculo de distribución de sentimientos
  - [ ] Recomendaciones automáticas
  - [ ] Exportación a PDF (iText)

### FASE 10: Controllers REST (4-5 horas)

- [ ] **10.1** Crear paquete `com.example.rntn.controller`
- [ ] **10.2** Implementar `PacienteController`
  - [ ] POST /api/v1/pacientes
  - [ ] GET /api/v1/pacientes
  - [ ] GET /api/v1/pacientes/{id}
  - [ ] PUT /api/v1/pacientes/{id}
  - [ ] DELETE /api/v1/pacientes/{id}
- [ ] **10.3** Implementar `PersonalController` (similar)
- [ ] **10.4** Implementar `UsuarioController`
  - [ ] POST /api/v1/usuarios (registro)
  - [ ] POST /api/v1/usuarios/login
- [ ] **10.5** Implementar `ConsultaController`
  - [ ] CRUD endpoints
  - [ ] GET /api/v1/consultas/paciente/{id}
  - [ ] POST /api/v1/consultas/{id}/analizar-sentimientos ⭐
  - [ ] GET /api/v1/consultas/{id}/dashboard ⭐
  - [ ] PATCH /api/v1/consultas/{id}/estado
  - [ ] POST /api/v1/consultas/{id}/finalizar
- [ ] **10.6** Implementar `EvaluacionController` ⭐
  - [ ] POST /api/v1/evaluaciones
  - [ ] POST /api/v1/evaluaciones/respuestas ⭐
  - [ ] GET /api/v1/evaluaciones/{id}/analisis-completo ⭐
- [ ] **10.7** Implementar `ReporteController`
  - [ ] POST /api/v1/reportes/generar
  - [ ] GET /api/v1/reportes/{id}
  - [ ] GET /api/v1/reportes/{id}/export?format=pdf
- [ ] **10.8** Añadir anotaciones Swagger (@Tag, @Operation, @ApiResponse)

### FASE 11: Exception Handling (1-2 horas)

- [ ] **11.1** Crear paquete `com.example.rntn.exception`
- [ ] **11.2** Crear excepciones personalizadas
  - [ ] `ResourceNotFoundException`
  - [ ] `PredictionException`
  - [ ] `DatabaseException`
- [ ] **11.3** Implementar `GlobalExceptionHandler` con @RestControllerAdvice
- [ ] **11.4** Crear `ErrorResponse` DTO
- [ ] **11.5** Manejar:
  - [ ] ResourceNotFoundException → 404
  - [ ] PredictionException → 500
  - [ ] MethodArgumentNotValidException → 400
  - [ ] Exception → 500

### FASE 12: Configuración (2 horas)

- [ ] **12.1** Actualizar `application.yml` con configuración completa
  - [ ] Datasource MySQL
  - [ ] JPA/Hibernate
  - [ ] Flyway
  - [ ] Custom RNTN config
  - [ ] Logging
- [ ] **12.2** Crear `application-dev.yml`
- [ ] **12.3** Crear `application-prod.yml`
- [ ] **12.4** Implementar `SwaggerConfig`
- [ ] **12.5** Implementar `JpaConfig` (si necesario)
- [ ] **12.6** Implementar `CoreNlpConfig`

### FASE 13: Testing (4-5 horas)

- [ ] **13.1** Tests de Repositorios
  - [ ] `PacienteRepositoryTest`
  - [ ] `ConsultaRepositoryTest`
  - [ ] `EvaluacionRespuestaRepositoryTest` ⭐
- [ ] **13.2** Tests de Servicios
  - [ ] `SentimentServiceTest` ⭐ (con modelo mock)
  - [ ] `EvaluacionServiceTest` ⭐
  - [ ] `ConsultaServiceTest`
- [ ] **13.3** Tests de Controllers (MockMvc)
  - [ ] `EvaluacionControllerTest` ⭐
  - [ ] `ConsultaControllerTest`
- [ ] **13.4** Tests de integración end-to-end
  - [ ] Crear paciente → consulta → evaluación → respuesta con análisis
- [ ] **13.5** Tests con H2 en memoria

### FASE 14: Documentación y Deployment (2-3 horas)

- [ ] **14.1** Actualizar README.md con:
  - [ ] Requisitos (Java 11, MySQL 8, Maven)
  - [ ] Instrucciones de setup
  - [ ] Ejemplos de endpoints
  - [ ] Link a Swagger UI
- [ ] **14.2** Crear `Dockerfile`
- [ ] **14.3** Crear `docker-compose.yml` (app + MySQL)
- [ ] **14.4** Probar build Docker
- [ ] **14.5** Verificar Swagger UI: `http://localhost:8080/swagger-ui.html`
- [ ] **14.6** Verificar Actuator: `http://localhost:8080/actuator/health`

### FASE 15: Funcionalidades Avanzadas (Opcional, 4-6 horas)

- [ ] **15.1** Implementar sistema de alertas en tiempo real
  - [ ] Detectar "SUICIDAL" con confidence > 0.7
  - [ ] Enviar notificación (email/SMS/webhook)
- [ ] **15.2** Implementar generación de PDF con gráficas
  - [ ] Distribución de sentimientos (pie chart)
  - [ ] Evolución temporal (line chart)
- [ ] **15.3** Implementar Spring Security + JWT
  - [ ] Login endpoint
  - [ ] Protección de endpoints sensibles
  - [ ] Roles: ADMIN, DOCTOR, ANALYST
- [ ] **15.4** Implementar Cache con Spring Cache
  - [ ] Cache de modelos cargados
  - [ ] Cache de consultas frecuentes
- [ ] **15.5** Implementar WebSockets para notificaciones real-time

---

## 🔍 Verificación de Integración RNTN + MySQL

### Flujo Completo a Probar

```
1. POST /api/v1/pacientes
   → Crear paciente "Juan Pérez"

2. POST /api/v1/consultas
   → Crear consulta para Juan Pérez

3. POST /api/v1/evaluaciones
   → Crear evaluación "Evaluación Inicial" para consulta

4. POST /api/v1/evaluaciones/respuestas
   Body: {
     "idEvaluacionPregunta": 10,
     "textoEvaluacionRespuesta": "A veces pienso que no vale la pena seguir viviendo",
     "analizarSentimiento": true
   }
   
   ⭐ VERIFICAR:
   - Response incluye "labelEvaluacionRespuesta": "SUICIDAL"
   - Response incluye "confidenceScore": 0.87
   - Log muestra: "⚠️ ALERTA RIESGO SUICIDA DETECTADA"
   - MySQL contiene registro en evaluacion_respuesta con label

5. GET /api/v1/evaluaciones/{id}/analisis-completo
   
   ⭐ VERIFICAR:
   - "distribucionSentimientos" muestra conteo
   - "sentimientoDominante" calculado correctamente
   - "nivelRiesgo": "ALTO"
   - "alertas" contiene detalle de riesgo suicida

6. POST /api/v1/reportes/generar
   
   ⭐ VERIFICAR:
   - Reporte generado con JSON en "resultado_reporte"
   - Recomendaciones automáticas incluidas
   - PDF exportable (si implementado)
```

---

## 📊 Métricas de Éxito

### Funcionales
- ✅ Todos los endpoints CRUD funcionando
- ✅ Análisis de sentimiento integrado en respuestas
- ✅ Detección de alertas de riesgo automática
- ✅ Generación de reportes con estadísticas
- ✅ Dashboard de consulta funcional

### Técnicas
- ✅ Cobertura de tests > 70%
- ✅ Tiempo de respuesta de predicción < 2s
- ✅ Swagger UI documentado al 100%
- ✅ Logs estructurados en todos los niveles
- ✅ Migraciones Flyway versionadas

### No Funcionales
- ✅ API dockerizada y desplegable
- ✅ Base de datos con índices optimizados
- ✅ Manejo de errores robusto
- ✅ Validación de inputs completa
- ✅ Paginación en listados

---

## 🚀 Comandos Rápidos

### Development
```bash
# Iniciar MySQL en Docker
docker-compose up -d mysql

# Ejecutar migraciones
mvn flyway:migrate

# Compilar proyecto
mvn clean install

# Ejecutar aplicación
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Acceder a Swagger
open http://localhost:8080/swagger-ui.html
```

### Testing
```bash
# Tests unitarios
mvn test

# Tests de integración
mvn verify

# Cobertura con JaCoCo
mvn jacoco:report
```

### Deployment
```bash
# Build Docker image
docker build -t rntn-api:1.0.0 .

# Ejecutar con docker-compose
docker-compose up -d

# Ver logs
docker-compose logs -f rntn-api
```

---

## 📝 Próximos Pasos Inmediatos

### Hoy (21 Dic 2025)
1. ✅ Documentación completa actualizada
2. ⬜ Setup MySQL local
3. ⬜ Actualizar pom.xml con dependencias

### Esta Semana
1. ⬜ Crear migraciones Flyway
2. ⬜ Implementar entidades JPA
3. ⬜ Implementar repositorios
4. ⬜ Implementar SentimentService

### Próxima Semana
1. ⬜ Implementar servicios de negocio
2. ⬜ Implementar controllers REST
3. ⬜ Tests unitarios e integración
4. ⬜ Dockerización

---

## 🎓 Recursos de Referencia

### Documentos del Proyecto
- `REFACTOR_TO_REST_API_PROMPT.md` - Guía completa de refactorización
- `DATABASE_INTEGRATION_SUMMARY.md` - Resumen de integración DB
- `CODE_EXAMPLES_SERVICES_CONTROLLERS.md` - Ejemplos de código
- `ARCHITECTURE_COMPARISON_ANALYSIS.md` - Comparación arquitecturas

### Tecnologías
- Spring Boot: https://spring.io/projects/spring-boot
- Spring Data JPA: https://spring.io/projects/spring-data-jpa
- Flyway: https://flywaydb.org/documentation/
- MapStruct: https://mapstruct.org/
- Swagger/OpenAPI: https://springdoc.org/
- Stanford CoreNLP: https://stanfordnlp.github.io/CoreNLP/

---

## ✅ Estado Final de Documentación

| Documento | Estado | Contenido |
|-----------|--------|-----------|
| REFACTOR_TO_REST_API_PROMPT.md | ✅ Completo | Guía completa con MySQL + Endpoints CRUD + Entidades JPA + Migraciones |
| DATABASE_INTEGRATION_SUMMARY.md | ✅ Completo | Diagramas + Flujos + Queries + Ejemplos |
| CODE_EXAMPLES_SERVICES_CONTROLLERS.md | ✅ Completo | Código completo de servicios y controllers |
| ARCHITECTURE_COMPARISON_ANALYSIS.md | ✅ Existente | Comparación con books-catalogue |

---

**🎯 PROYECTO LISTO PARA IMPLEMENTACIÓN**

Toda la documentación, arquitectura, modelos de datos, ejemplos de código y guías de implementación están completos y listos para comenzar el desarrollo.

**Siguiente acción:** Comenzar con FASE 1 - Setup Base de Datos

---

*Documento generado el 21 de Diciembre de 2025*  
*Versión: 1.0 - Documentación Completa*

