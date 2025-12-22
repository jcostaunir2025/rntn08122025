# Resumen de Integración de Base de Datos MySQL - Proyecto RNTN Sentiment API

**Fecha:** 21 de Diciembre de 2025  
**Proyecto:** RNTN Sentiment Analysis API  
**Base de Datos:** MySQL 8.0+  
**Framework:** Spring Boot 3.x + Spring Data JPA + Flyway

---

## 1. Diagrama Entidad-Relación

```
┌─────────────────┐         ┌──────────────────┐         ┌─────────────────┐
│    PACIENTE     │         │     CONSULTA     │         │    PERSONAL     │
├─────────────────┤         ├──────────────────┤         ├─────────────────┤
│ id_paciente (PK)│◄───────┤ id_consulta (PK) ├────────►│ id_personal (PK)│
│ doc_paciente    │  1:N    │ id_paciente (FK) │  N:1    │ doc_personal    │
│ nombre_paciente │         │ id_personal (FK) │         │ nombre_personal │
│ direccion       │         │ fechahora        │         │ estatus         │
│ email           │         │ fechafin         │         └─────────────────┘
│ telefono        │         │ estatus          │
│ estatus         │         ├──────────────────┤
└─────────────────┘         │     1:N          │
                            └────────┬─────────┘
                                     │
                                     ▼
                            ┌─────────────────┐
                            │   EVALUACION    │
                            ├─────────────────┤
                            │ id_evaluacion   │
                            │ id_consulta (FK)│
                            │ nombre          │
                            │ area            │
                            └────────┬────────┘
                                     │ 1:N
                                     ▼
                            ┌─────────────────┐
                            │    REPORTE      │
                            ├─────────────────┤
                            │ id_reporte (PK) │
                            │ id_usuario (FK) ├────┐
                            │ id_evaluacion   │    │
                            │ nombre_reporte  │    │
                            │ resultado       │    │
                            │ fecha_generacion│    │
                            └─────────────────┘    │
                                                   │ N:1
┌─────────────────────┐                           │
│ EVALUACION_PREGUNTA │                           │
├─────────────────────┤                    ┌──────▼──────┐
│ id_pregunta (PK)    │                    │   USUARIO   │
│ texto_pregunta      │                    ├─────────────┤
└──────────┬──────────┘                    │ id_usuario  │
           │ 1:N                           │ nombre_usr  │
           ▼                                │ pass_usr    │
┌─────────────────────┐                    └──────┬──────┘
│ EVALUACION_RESPUESTA│                           │ N:M
├─────────────────────┤                           │
│ id_respuesta (PK)   │                    ┌──────▼──────┐
│ id_pregunta (FK)    │                    │USUARIO_ROLES│
│ texto_respuesta     │                    ├─────────────┤
│ texto_set           │                    │ id_roles    │
│ label_respuesta     │◄─────────────────  │ permisos    │
│ confidence_score    │  (Análisis RNTN)   └─────────────┘
└─────────────────────┘
```

---

## 2. Flujo de Datos: Análisis de Sentimientos + Persistencia

```
┌─────────────────────────────────────────────────────────────────────┐
│                         CAPA DE PRESENTACIÓN                        │
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐          │
│  │ Paciente      │  │ Consulta      │  │ Evaluacion    │          │
│  │ Controller    │  │ Controller    │  │ Controller    │          │
│  └───────┬───────┘  └───────┬───────┘  └───────┬───────┘          │
└──────────┼──────────────────┼──────────────────┼──────────────────┘
           │                  │                  │
           │  REST API        │                  │
           ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        CAPA DE NEGOCIO                               │
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐          │
│  │ Paciente      │  │ Consulta      │  │ Evaluacion    │          │
│  │ Service       │  │ Service       │  │ Service       │          │
│  └───────┬───────┘  └───────┬───────┘  └───────┬───────┘          │
│          │                  │                  │                    │
│          │                  │   ┌──────────────▼────────┐          │
│          │                  │   │ Sentiment Service     │          │
│          │                  │   │ ┌──────────────────┐  │          │
│          │                  │   │ │ SentimentPredictor│  │          │
│          │                  │   │ │    (RNTN Model)   │  │          │
│          │                  │   │ └──────────────────┘  │          │
│          │                  │   └───────────┬───────────┘          │
└──────────┼──────────────────┼───────────────┼──────────────────────┘
           │                  │               │
           │  JPA/Hibernate   │               │ Model Load
           ▼                  ▼               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      CAPA DE PERSISTENCIA                            │
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐          │
│  │ Paciente      │  │ Consulta      │  │ Evaluacion    │          │
│  │ Repository    │  │ Repository    │  │ Repository    │          │
│  └───────┬───────┘  └───────┬───────┘  └───────┬───────┘          │
└──────────┼──────────────────┼──────────────────┼──────────────────┘
           │                  │                  │
           │    JDBC          │                  │
           ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                          MYSQL DATABASE                              │
│  ┌─────────┐  ┌─────────┐  ┌──────────┐  ┌──────────────────┐    │
│  │paciente │  │consulta │  │evaluacion│  │evaluacion_respuesta│   │
│  └─────────┘  └─────────┘  └──────────┘  └──────────────────┘    │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 3. Flujo Completo: Registro de Respuesta con Análisis

```
1. Cliente HTTP POST /api/v1/evaluaciones/respuestas
   {
     "idEvaluacionPregunta": 1,
     "textoEvaluacionRespuesta": "Me siento muy triste y sin esperanza",
     "analizarSentimiento": true
   }
             ↓
2. EvaluacionController → EvaluacionService
             ↓
3. EvaluacionService llama a SentimentService.analyze(texto)
             ↓
4. SentimentService usa SentimentPredictor (RNTN Model)
   - Carga modelo: models/out-model.ser.gz
   - Procesa texto con Stanford CoreNLP
   - Predice clase: 3 (SADNESS)
   - Calcula confianza: 0.89
             ↓
5. EvaluacionService crea EvaluacionRespuesta
   - texto_evaluacion_respuesta: "Me siento muy triste..."
   - label_evaluacion_respuesta: "SADNESS"
   - confidence_score: 0.89
             ↓
6. EvaluacionRespuestaRepository.save() → MySQL
             ↓
7. Respuesta HTTP 201 Created
   {
     "idEvaluacionRespuesta": 1,
     "textoEvaluacionRespuesta": "Me siento muy triste...",
     "labelEvaluacionRespuesta": "SADNESS",
     "confidenceScore": 0.89,
     "sentimentAnalysis": {
       "predictedClass": 3,
       "predictedLabel": "SADNESS"
     }
   }
```

---

## 4. Mapeo de Labels RNTN a Niveles de Riesgo

| Índice | Label       | Descripción                | Nivel de Riesgo | Color Alerta |
|--------|-------------|----------------------------|-----------------|--------------|
| 0      | ANXIETY     | Estado ansioso/preocupado  | MEDIO           | 🟡 Amarillo  |
| 1      | SUICIDAL    | Pensamientos suicidas      | **ALTO**        | 🔴 Rojo      |
| 2      | ANGER       | Estado de enojo            | MEDIO           | 🟠 Naranja   |
| 3      | SADNESS     | Estado triste/deprimido    | MEDIO           | 🟡 Amarillo  |
| 4      | FRUSTRATION | Estado de frustración      | BAJO            | 🟢 Verde     |

---

## 5. Estructura de Tablas - Campos Clave

### 5.1. Tabla: evaluacion_respuesta (Integración RNTN)

```sql
CREATE TABLE evaluacion_respuesta (
    id_evaluacion_respuesta INT PRIMARY KEY AUTO_INCREMENT,
    id_evaluacion_pregunta INT NOT NULL,
    
    -- Texto original del paciente
    texto_evaluacion_respuesta TEXT NOT NULL,
    
    -- Texto procesado/normalizado (para entrenamiento)
    texto_set_evaluacion_respuesta TEXT,
    
    -- ⭐ RESULTADO DEL ANÁLISIS RNTN
    label_evaluacion_respuesta VARCHAR(50),     -- ANXIETY, SUICIDAL, etc.
    confidence_score DOUBLE,                     -- 0.0 - 1.0
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (id_evaluacion_pregunta) 
        REFERENCES evaluacion_pregunta(id_evaluacion_pregunta),
    INDEX idx_label (label_evaluacion_respuesta)
);
```

### 5.2. Tabla: reporte (Almacena resultados agregados)

```sql
CREATE TABLE reporte (
    id_reporte INT PRIMARY KEY AUTO_INCREMENT,
    id_usuario INT NOT NULL,
    id_evaluacion INT NOT NULL,
    
    nombre_reporte VARCHAR(100) NOT NULL,
    
    -- ⭐ RESULTADO DEL ANÁLISIS (JSON)
    resultado_reporte TEXT,  
    /* Ejemplo:
    {
      "sentimientoDominante": "SADNESS",
      "distribucion": {
        "ANXIETY": 2,
        "SUICIDAL": 0,
        "ANGER": 1,
        "SADNESS": 5,
        "FRUSTRATION": 2
      },
      "nivelRiesgo": "MEDIO",
      "recomendaciones": [...]
    }
    */
    
    fechageneracion_reporte TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    FOREIGN KEY (id_evaluacion) REFERENCES evaluacion(id_evaluacion)
);
```

---

## 6. Queries Importantes

### 6.1. Obtener todas las respuestas de una evaluación con análisis

```sql
SELECT 
    er.id_evaluacion_respuesta,
    ep.texto_evaluacion_pregunta AS pregunta,
    er.texto_evaluacion_respuesta AS respuesta,
    er.label_evaluacion_respuesta AS sentimiento,
    er.confidence_score AS confianza,
    er.created_at
FROM evaluacion_respuesta er
INNER JOIN evaluacion_pregunta ep 
    ON er.id_evaluacion_pregunta = ep.id_evaluacion_pregunta
INNER JOIN evaluacion e 
    ON e.id_evaluacion = (
        SELECT id_evaluacion 
        FROM evaluacion 
        WHERE id_evaluacion = ?
    )
ORDER BY er.created_at;
```

### 6.2. Dashboard: Distribución de sentimientos por paciente

```sql
SELECT 
    p.nombre_paciente,
    er.label_evaluacion_respuesta,
    COUNT(*) AS total,
    AVG(er.confidence_score) AS confianza_promedio
FROM paciente p
INNER JOIN consulta c ON p.id_paciente = c.id_paciente
INNER JOIN evaluacion e ON c.id_consulta = e.id_consulta
INNER JOIN evaluacion_pregunta ep ON 1=1
INNER JOIN evaluacion_respuesta er ON ep.id_evaluacion_pregunta = er.id_evaluacion_pregunta
WHERE p.id_paciente = ?
    AND er.label_evaluacion_respuesta IS NOT NULL
GROUP BY p.nombre_paciente, er.label_evaluacion_respuesta
ORDER BY total DESC;
```

### 6.3. Alertas: Detectar respuestas de alto riesgo

```sql
SELECT 
    p.nombre_paciente,
    p.telefono_paciente,
    c.fechahora_consulta,
    per.nombre_personal,
    er.texto_evaluacion_respuesta,
    er.label_evaluacion_respuesta,
    er.confidence_score
FROM evaluacion_respuesta er
INNER JOIN evaluacion_pregunta ep ON er.id_evaluacion_pregunta = ep.id_evaluacion_pregunta
INNER JOIN consulta c ON 1=1  -- Se debe relacionar correctamente
INNER JOIN paciente p ON c.id_paciente = p.id_paciente
INNER JOIN personal per ON c.id_personal = per.id_personal
WHERE er.label_evaluacion_respuesta = 'SUICIDAL'
    AND er.confidence_score > 0.7
    AND c.fechahora_consulta >= DATE_SUB(NOW(), INTERVAL 7 DAY)
ORDER BY er.confidence_score DESC, c.fechahora_consulta DESC;
```

---

## 7. Endpoints de Integración Principal

### 7.1. Analizar Respuesta Individual

**POST** `/api/v1/evaluaciones/respuestas/analizar`

```json
Request:
{
  "texto": "Me siento muy ansioso y no puedo dormir"
}

Response:
{
  "texto": "Me siento muy ansioso y no puedo dormir",
  "analisis": {
    "label": "ANXIETY",
    "confidence": 0.92,
    "nivelRiesgo": "MEDIO",
    "recomendacion": "Seguimiento en 48 horas"
  }
}
```

### 7.2. Analizar Consulta Completa

**POST** `/api/v1/consultas/{id}/analizar`

```json
Response:
{
  "idConsulta": 1,
  "paciente": "Juan Pérez",
  "totalRespuestas": 10,
  "analisis": {
    "distribucion": {
      "ANXIETY": 3,
      "SUICIDAL": 1,
      "ANGER": 2,
      "SADNESS": 3,
      "FRUSTRATION": 1
    },
    "sentimientoDominante": "ANXIETY",
    "nivelRiesgo": "ALTO",
    "alertas": [
      {
        "tipo": "RIESGO_SUICIDA",
        "severidad": "ALTA",
        "respuesta": "A veces pienso que no vale la pena...",
        "confidence": 0.87
      }
    ]
  }
}
```

### 7.3. Generar Reporte Completo

**POST** `/api/v1/reportes/generar`

```json
Request:
{
  "idEvaluacion": 1,
  "idUsuario": 1,
  "incluirRecomendaciones": true,
  "formato": "JSON"
}

Response:
{
  "idReporte": 5,
  "nombreReporte": "Reporte Automático - Evaluación 1",
  "fechaGeneracion": "2025-12-21T16:30:00Z",
  "evaluacion": {
    "id": 1,
    "nombre": "Evaluación Inicial",
    "consulta": {
      "paciente": "Juan Pérez",
      "fecha": "2025-12-21T15:00:00Z"
    }
  },
  "resultado": {
    "totalRespuestas": 10,
    "distribucionSentimientos": {
      "ANXIETY": 3,
      "SUICIDAL": 1,
      "ANGER": 2,
      "SADNESS": 3,
      "FRUSTRATION": 1
    },
    "sentimientoDominante": "ANXIETY",
    "nivelRiesgo": "ALTO",
    "recomendaciones": [
      "⚠️ URGENTE: Detectado riesgo suicida",
      "Contactar inmediatamente con el paciente",
      "Programar seguimiento diario",
      "Considerar derivación a psiquiatría"
    ],
    "graficas": {
      "url": "/api/v1/reportes/5/grafica"
    }
  }
}
```

---

## 8. Configuración de Conexión MySQL

### application.yml

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/rntn_db?useSSL=false&serverTimezone=UTC
    username: rntn_user
    password: rntn_password
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      
  jpa:
    hibernate:
      ddl-auto: validate  # Las migraciones las maneja Flyway
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
        
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
```

---

## 9. Checklist de Implementación

### Fase 1: Base de Datos
- [x] Crear esquema MySQL con todas las tablas
- [x] Implementar migraciones Flyway (V1, V2, V3)
- [x] Insertar datos maestros (roles, estados)
- [x] Crear índices para optimización

### Fase 2: Entidades JPA
- [x] Definir todas las entidades con anotaciones JPA
- [x] Implementar relaciones (OneToMany, ManyToOne, ManyToMany)
- [x] Añadir timestamps automáticos
- [x] Configurar Lombok para reducir boilerplate

### Fase 3: Repositorios
- [x] Crear interfaces Repository para cada entidad
- [x] Implementar queries personalizados con @Query
- [x] Añadir métodos de búsqueda derivados
- [x] Implementar paginación

### Fase 4: Servicios de Negocio
- [ ] PacienteService: CRUD + búsqueda
- [ ] ConsultaService: CRUD + filtros por fecha/estado
- [ ] EvaluacionService: CRUD + integración con SentimentService
- [ ] ReporteService: Generación + exportación PDF
- [ ] SentimentService: Análisis individual y por lote

### Fase 5: Controllers REST
- [ ] PacienteController: Endpoints CRUD
- [ ] ConsultaController: Endpoints CRUD + dashboard
- [ ] EvaluacionController: Endpoints CRUD + análisis
- [ ] ReporteController: Generación + exportación
- [ ] SentimentController: Análisis en tiempo real

### Fase 6: DTOs y Mappers
- [ ] Crear DTOs para Request/Response
- [ ] Implementar MapStruct mappers
- [ ] Validaciones con Bean Validation
- [ ] Documentar con Swagger annotations

### Fase 7: Testing
- [ ] Unit tests para Services
- [ ] Integration tests para Repositories
- [ ] REST API tests para Controllers
- [ ] Tests de carga para análisis de sentimientos

### Fase 8: Seguridad y Producción
- [ ] Implementar Spring Security (JWT)
- [ ] Configurar CORS
- [ ] Rate limiting
- [ ] Logging estructurado
- [ ] Monitoreo con Actuator
- [ ] Dockerización

---

## 10. Comandos Útiles

### Iniciar MySQL en Docker
```bash
docker run --name mysql-rntn \
  -e MYSQL_ROOT_PASSWORD=rootpass \
  -e MYSQL_DATABASE=rntn_db \
  -e MYSQL_USER=rntn_user \
  -e MYSQL_PASSWORD=rntn_password \
  -p 3306:3306 \
  -d mysql:8.0
```

### Ejecutar Migraciones Flyway
```bash
mvn flyway:migrate
mvn flyway:info
mvn flyway:validate
```

### Compilar y Ejecutar Aplicación
```bash
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Acceder a Swagger UI
```
http://localhost:8080/swagger-ui.html
```

### Verificar Health Check
```bash
curl http://localhost:8080/actuator/health
```

---

## 11. Consideraciones de Rendimiento

1. **Índices de Base de Datos**
   - Índice en `label_evaluacion_respuesta` para filtrado rápido
   - Índices compuestos para consultas frecuentes
   - Índice FULLTEXT para búsqueda de texto

2. **Caching**
   - Cache de modelos RNTN cargados (Singleton pattern)
   - Cache de consultas frecuentes con Spring Cache
   - Redis para cache distribuido (opcional)

3. **Paginación**
   - Siempre usar paginación en listados
   - Limitar tamaño máximo de página a 100
   - Implementar scroll infinito en frontend

4. **Lazy Loading**
   - Relaciones JPA configuradas como LAZY por defecto
   - Usar JOIN FETCH solo cuando sea necesario
   - DTOs proyectados para evitar N+1 queries

5. **Batch Processing**
   - Análisis de múltiples respuestas en paralelo
   - Hibernate batch inserts configurado
   - Async processing para reportes pesados

---

## 12. Próximos Pasos

1. **Implementar WebSockets** para notificaciones en tiempo real de alertas de riesgo
2. **Machine Learning Pipeline** para reentrenar modelo RNTN con nuevos datos
3. **Dashboard de Analytics** con gráficas de tendencias de sentimientos
4. **Mobile App** para pacientes con análisis de sentimientos en diario digital
5. **Integración con EHR** (Electronic Health Records) externos

---

**Documento generado el:** 21 de Diciembre de 2025  
**Autor:** Sistema de Documentación Automática  
**Versión:** 1.0

