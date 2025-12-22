# 🧠 RNTN Sentiment Analysis API

Sistema de análisis de sentimientos para salud mental usando **Stanford CoreNLP RNTN** (Recursive Neural Tensor Network) con persistencia en MySQL.

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-11-orange.svg)](https://www.oracle.com/java/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

---

## 📋 Tabla de Contenidos

- [Características](#características)
- [Arquitectura](#arquitectura)
- [Requisitos Previos](#requisitos-previos)
- [Instalación](#instalación)
- [Configuración](#configuración)
- [Ejecución](#ejecución)
- [API Endpoints](#api-endpoints)
- [Documentación Swagger](#documentación-swagger)
- [Modelo de Datos](#modelo-de-datos)
- [Análisis de Sentimientos](#análisis-de-sentimientos)
- [Testing](#testing)
- [Contribución](#contribución)

---

## ✨ Características

- ✅ **Análisis de sentimientos en tiempo real** usando modelo RNTN
- ✅ **5 clases de sentimiento** específicas para salud mental:
  - `ANXIETY` (Ansiedad) - Riesgo MEDIO
  - `SUICIDAL` (Pensamientos suicidas) - Riesgo **ALTO**
  - `ANGER` (Enojo) - Riesgo MEDIO
  - `SADNESS` (Tristeza) - Riesgo MEDIO
  - `FRUSTRATION` (Frustración) - Riesgo BAJO
- ✅ **Análisis agregado avanzado** ⭐ **NUEVO**
  - Estadísticas en tiempo real con cálculo en memoria
  - Análisis histórico con stored procedures optimizados
  - Distribución de sentimientos por evaluación
  - Sistema de alertas de alto riesgo
- ✅ **Detección automática de alertas** de riesgo alto
- ✅ **Persistencia en MySQL** con Spring Data JPA
- ✅ **Migraciones versionadas** con Flyway
- ✅ **API REST** con documentación Swagger/OpenAPI
- ✅ **Manejo global de excepciones**
- ✅ **Arquitectura por capas** (Controller → Service → Repository)
- ✅ **Logging estructurado** con SLF4J
- ✅ **Health checks** con Spring Boot Actuator

---

## 🏗️ Arquitectura

```
┌─────────────────────────────────────────────────────────┐
│                    Cliente (HTTP)                       │
└────────────────┬────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────┐
│              CAPA DE PRESENTACIÓN                       │
│  Controllers (REST Endpoints + Swagger)                 │
│  - EvaluacionController ⭐                              │
└────────────────┬────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────┐
│              CAPA DE NEGOCIO                            │
│  Services (Lógica + Validaciones)                       │
│  - EvaluacionService                                    │
│  - SentimentService ⭐ (integra RNTN)                   │
└────────────────┬────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────┐
│              CAPA DE PERSISTENCIA                       │
│  Repositories (Spring Data JPA)                         │
│  - EvaluacionRespuestaRepository ⭐                     │
└────────────────┬────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────┐
│              BASE DE DATOS                              │
│  MySQL 8.0 (rntn_db)                                    │
│  - 10 tablas principales                                │
│  - Migraciones Flyway                                   │
└─────────────────────────────────────────────────────────┘
```

---

## 📦 Requisitos Previos

- **Java 11+** ([Descargar](https://adoptium.net/))
- **Maven 3.6+** ([Descargar](https://maven.apache.org/download.cgi))
- **MySQL 8.0+** ([Descargar](https://dev.mysql.com/downloads/))
- **Git** ([Descargar](https://git-scm.com/))

---

## 🚀 Instalación

### 1. Clonar el repositorio

```bash
git clone https://github.com/your-org/rntn-sentiment-api.git
cd rntn-sentiment-api
```

### 2. Configurar MySQL

#### Opción A: MySQL Local

```sql
-- Conectar a MySQL
mysql -u root -p

-- Crear base de datos
CREATE DATABASE rntn_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Crear usuario
CREATE USER 'rntn_user'@'localhost' IDENTIFIED BY 'rntn_password';
GRANT ALL PRIVILEGES ON rntn_db.* TO 'rntn_user'@'localhost';
FLUSH PRIVILEGES;
```

#### Opción B: MySQL en Docker

```bash
docker run --name mysql-rntn \
  -e MYSQL_ROOT_PASSWORD=rootpass \
  -e MYSQL_DATABASE=rntn_db \
  -e MYSQL_USER=rntn_user \
  -e MYSQL_PASSWORD=rntn_password \
  -p 3306:3306 \
  -d mysql:8.0
```

### 3. Compilar el proyecto

```bash
mvn clean install
```

Las migraciones Flyway se ejecutarán automáticamente en el primer arranque.

---

## ⚙️ Configuración

### Variables de Entorno

Puedes configurar la aplicación mediante variables de entorno:

```bash
# Base de datos
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=rntn_db
export DB_USER=rntn_user
export DB_PASSWORD=rntn_password

# Perfil de Spring
export SPRING_PROFILES_ACTIVE=dev
```

### Archivos de Configuración

- `application.yml` - Configuración principal
- `application-dev.yml` - Perfil de desarrollo
- `application-prod.yml` - Perfil de producción

---

## ▶️ Ejecución

### Modo Desarrollo

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Modo Producción

```bash
java -jar target/rntn-sentiment-api-1.0.0.jar --spring.profiles.active=prod
```

La aplicación estará disponible en: **http://localhost:8080**

---

## 🌐 API Endpoints

### 📊 Evaluaciones (Análisis de Sentimientos)

#### ⭐ Registrar Respuesta con Análisis

```http
POST /api/v1/evaluaciones/respuestas
Content-Type: application/json

{
  "idEvaluacionPregunta": 1,
  "textoEvaluacionRespuesta": "Me siento muy ansioso últimamente",
  "analizarSentimiento": true
}
```

**Response:**

```json
{
  "idEvaluacionRespuesta": 1,
  "idEvaluacionPregunta": 1,
  "textoPregunta": "¿Cómo se siente hoy?",
  "textoEvaluacionRespuesta": "Me siento muy ansioso últimamente",
  "labelEvaluacionRespuesta": "ANXIETY",
  "confidenceScore": 0.92,
  "sentimentAnalysis": {
    "texto": "Me siento muy ansioso últimamente",
    "predictedClass": 0,
    "predictedLabel": "ANXIETY",
    "confidence": 0.92,
    "nivelRiesgo": "MEDIO",
    "timestamp": "2025-12-21T15:30:00"
  },
  "createdAt": "2025-12-21T15:30:00"
}
```

#### Análisis Agregado

```http
GET /api/v1/evaluaciones/analisis-agregado?preguntaIds=1,2,3
```

**Response:**

```json
{
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
  "alertas": [
    {
      "tipo": "RIESGO_SUICIDA",
      "nivel": "ALTO",
      "respuesta": "A veces pienso que no tiene sentido seguir",
      "confidence": 0.87
    }
  ]
}
```

### 🏥 Health Check

```http
GET /actuator/health
```

---

## 📖 Documentación Swagger

La documentación interactiva de la API está disponible en:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/api-docs

![Swagger UI](docs/images/swagger-ui-screenshot.png)

---

## 🗄️ Modelo de Datos

### Tablas Principales

| Tabla | Descripción | Relaciones |
|-------|-------------|------------|
| **paciente** | Información de pacientes | 1:N con consulta |
| **personal** | Personal médico | 1:N con consulta |
| **usuario** | Usuarios del sistema | N:M con usuario_roles |
| **consulta** | Consultas médicas | N:1 con paciente/personal |
| **evaluacion** | Evaluaciones de consultas | N:1 con consulta |
| **evaluacion_pregunta** | Preguntas estándar | 1:N con evaluacion_respuesta |
| **evaluacion_respuesta** ⭐ | **Respuestas + Análisis RNTN** | N:1 con evaluacion_pregunta |
| **reporte** | Reportes generados | N:1 con evaluacion/usuario |

### ⭐ Tabla Clave: `evaluacion_respuesta`

```sql
CREATE TABLE evaluacion_respuesta (
    id_evaluacion_respuesta INT PRIMARY KEY AUTO_INCREMENT,
    id_evaluacion_pregunta INT NOT NULL,
    texto_evaluacion_respuesta TEXT NOT NULL,
    -- ⭐ Campos del análisis RNTN
    label_evaluacion_respuesta VARCHAR(50),  -- ANXIETY, SUICIDAL, etc.
    confidence_score DOUBLE,                  -- 0.0 - 1.0
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 🧠 Análisis de Sentimientos

### Modelo RNTN

El sistema utiliza un modelo **Recursive Neural Tensor Network** de Stanford CoreNLP, específicamente entrenado para detectar expresiones emocionales en el contexto de salud mental.

### Clases de Sentimiento

| Índice | Label | Descripción | Nivel de Riesgo |
|--------|-------|-------------|-----------------|
| 0 | `ANXIETY` | Estado de ansiedad o preocupación | 🟡 MEDIO |
| 1 | `SUICIDAL` | Pensamientos suicidas | 🔴 ALTO |
| 2 | `ANGER` | Estado de enojo o ira | 🟠 MEDIO |
| 3 | `SADNESS` | Estado de tristeza o depresión | 🟡 MEDIO |
| 4 | `FRUSTRATION` | Estado de frustración | 🟢 BAJO |

### Sistema de Alertas

Cuando se detecta:
- **`SUICIDAL`** con `confidence > 0.7` → ⚠️ **ALERTA AUTOMÁTICA**
- Se registra en logs con nivel WARN
- Se incluye en el análisis agregado como alerta de RIESGO_ALTO

---

## 🧪 Testing

### Ejecutar todos los tests

```bash
mvn test
```

### Tests de integración

```bash
mvn verify
```

### Cobertura con JaCoCo

```bash
mvn jacoco:report
```

El reporte estará en: `target/site/jacoco/index.html`

---

## 🐳 Docker

### Build

```bash
docker build -t rntn-api:1.0.0 .
```

### Run con docker-compose

```bash
docker-compose up -d
```

Esto levantará:
- MySQL en puerto 3306
- RNTN API en puerto 8080

---

## 📝 Logging

Los logs se escriben en:
- **Consola:** `stdout`
- **Archivo:** `logs/rntn-api.log`

Niveles de log configurables en `application.yml`:

```yaml
logging:
  level:
    com.example.rntn: DEBUG
    edu.stanford.nlp: WARN
    org.hibernate.SQL: DEBUG
```

---

## 🔐 Seguridad

### Usuario Admin Predefinido

**Username:** `admin`  
**Password:** `admin123`

⚠️ **IMPORTANTE:** Cambiar en producción.

### Roles Disponibles

- `ADMIN` - Acceso completo
- `DOCTOR` - Gestión de consultas y evaluaciones
- `ENFERMERO` - Visualización
- `RECEPCIONISTA` - Gestión de pacientes
- `ANALISTA` - Generación de reportes

---

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo [LICENSE](LICENSE) para más detalles.

---

## 👥 Equipo

- **RNTN Team** - Desarrollo inicial

---

## 📞 Soporte

Para reportar bugs o solicitar features, por favor abre un issue en GitHub:
https://github.com/your-org/rntn-sentiment-api/issues

---

## 🙏 Agradecimientos

- [Stanford CoreNLP](https://stanfordnlp.github.io/CoreNLP/) - Modelo RNTN
- [Spring Boot](https://spring.io/projects/spring-boot) - Framework
- [Flyway](https://flywaydb.org/) - Migraciones de BD

---

**Última actualización:** 21 de Diciembre de 2025  
**Versión:** 1.0.0

