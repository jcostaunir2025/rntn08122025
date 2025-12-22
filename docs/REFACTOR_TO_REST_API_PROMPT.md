# Prompt para Refactorizar Proyecto RNTN como REST API con Persistencia MySQL

## Contexto del Proyecto Actual

Este es un proyecto Maven Java 11 para análisis de sentimientos usando Stanford CoreNLP RNTN (Recursive Neural Tensor Network). Actualmente funciona como aplicaciones de línea de comandos con las siguientes clases principales:

> **⚡ ACTUALIZACIÓN 21/12/2025**: Se añade capa de persistencia MySQL siguiendo modelo entidad-relación para gestión de consultas médicas, pacientes, evaluaciones y reportes de análisis de sentimientos.

### Clases Existentes
### Clases Existentes

- **SentimentPredictor** - Predicción de sentimientos usando modelo entrenado
- **TrainingRunner** - Wrapper para entrenar modelos RNTN
- **CsvToSstConverter** - Convierte CSV a formato SST para entrenamiento
- **TreeConverter** - Genera árboles de análisis sintáctico con etiquetas
- **BinarizeSst** - Binariza árboles SST para compatibilidad
- **MainApp** - Punto de entrada simple actual

### Etiquetas Personalizadas

El modelo usa 5 clases de sentimiento (índices 0-4):

| Índice | Label | Descripción |
|--------|-------|-------------|
| 0 | ANXIETY | Estado de ansiedad |
| 1 | SUICIDAL | Pensamientos suicidas |
| 2 | ANGER | Estado de enojo |
| 3 | SADNESS | Estado de tristeza |
| 4 | FRUSTRATION | Estado de frustración |

### Dependencias Actuales

- Stanford CoreNLP 4.5.4
- Apache Commons CSV 1.10.0
- SLF4J Simple 1.7.36

---

## Objetivo de la Refactorización

Convertir este proyecto en una API REST moderna usando Spring Boot con los siguientes requisitos:

### 1. Framework y Tecnologías
### 1. Framework y Tecnologías

- Spring Boot 3.x (compatible con Java 11+)
- Spring Web para REST endpoints
- **Spring Data JPA para persistencia**
- **MySQL 8.0+ como base de datos**
- **Flyway para migraciones de base de datos**
- Spring Boot Actuator para health checks
- Springdoc OpenAPI (Swagger UI) para documentación
- Lombok para reducir boilerplate
- Validation API para validación de requests
- **MapStruct para mapeo entre entidades y DTOs**
- Mantener Stanford CoreNLP y dependencias existentes

### 2. Arquitectura de la API

#### Estructura de Capas

```
src/main/java/com/example/rntn/
├── RntnApiApplication.java          # Main Spring Boot
├── config/
│   ├── CoreNlpConfig.java           # Configuración de pipelines CoreNLP
│   ├── SwaggerConfig.java           # Configuración OpenAPI/Swagger
│   ├── JpaConfig.java               # Configuración JPA/Hibernate
│   └── MapStructConfig.java         # Configuración MapStruct
├── controller/
│   ├── SentimentController.java     # Endpoints de predicción
│   ├── TrainingController.java      # Endpoints de entrenamiento
│   ├── DataPreparationController.java # Endpoints de conversión/preparación
│   ├── PacienteController.java      # CRUD Pacientes
│   ├── PersonalController.java      # CRUD Personal
│   ├── UsuarioController.java       # CRUD Usuarios
│   ├── ConsultaController.java      # CRUD Consultas
│   ├── ReporteController.java       # CRUD Reportes
│   └── EvaluacionController.java    # CRUD Evaluaciones
├── service/
│   ├── SentimentService.java        # Lógica de negocio predicción
│   ├── TrainingService.java         # Lógica de entrenamiento
│   ├── DataPreparationService.java  # Lógica de conversión datos
│   ├── PacienteService.java         # Lógica negocio pacientes
│   ├── PersonalService.java         # Lógica negocio personal
│   ├── UsuarioService.java          # Lógica negocio usuarios
│   ├── ConsultaService.java         # Lógica negocio consultas
│   ├── ReporteService.java          # Lógica negocio reportes
│   └── EvaluacionService.java       # Lógica negocio evaluaciones
├── repository/
│   ├── PacienteRepository.java      # JPA Repository
│   ├── PersonalRepository.java
│   ├── UsuarioRepository.java
│   ├── UsuarioRolesRepository.java
│   ├── ConsultaRepository.java
│   ├── ConsultaEstatusRepository.java
│   ├── ReporteRepository.java
│   ├── EvaluacionRepository.java
│   ├── EvaluacionPreguntaRepository.java
│   └── EvaluacionRespuestaRepository.java
├── entity/
│   ├── Paciente.java                # Entidad JPA
│   ├── Personal.java
│   ├── Usuario.java
│   ├── UsuarioRoles.java
│   ├── Consulta.java
│   ├── ConsultaEstatus.java
│   ├── Reporte.java
│   ├── Evaluacion.java
│   ├── EvaluacionPregunta.java
│   └── EvaluacionRespuesta.java
├── dto/
│   ├── request/
│   │   ├── PredictRequest.java
│   │   ├── BatchPredictRequest.java
│   │   ├── TrainModelRequest.java
│   │   ├── ConvertDataRequest.java
│   │   ├── PacienteRequest.java
│   │   ├── PersonalRequest.java
│   │   ├── UsuarioRequest.java
│   │   ├── ConsultaRequest.java
│   │   ├── ReporteRequest.java
│   │   └── EvaluacionRequest.java
│   └── response/
│       ├── PredictResponse.java
│       ├── BatchPredictResponse.java
│       ├── TrainingStatusResponse.java
│       ├── ConversionResponse.java
│       ├── PacienteResponse.java
│       ├── PersonalResponse.java
│       ├── UsuarioResponse.java
│       ├── ConsultaResponse.java
│       ├── ReporteResponse.java
│       └── EvaluacionResponse.java
├── mapper/
│   ├── PacienteMapper.java          # MapStruct mappers
│   ├── PersonalMapper.java
│   ├── UsuarioMapper.java
│   ├── ConsultaMapper.java
│   ├── ReporteMapper.java
│   └── EvaluacionMapper.java
├── model/
│   ├── SentimentResult.java
│   └── SentimentLabel.java          # Enum con las 5 etiquetas
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ModelNotFoundException.java
│   ├── PredictionException.java
│   ├── ResourceNotFoundException.java
│   └── DatabaseException.java
└── util/
    ├── SentimentPredictor.java      # Refactorizado (sin main)
    ├── TreeConverter.java           # Refactorizado
    └── BinarizeSst.java             # Refactorizado
```

#### Recursos (`src/main/resources/`)

```
src/main/resources/
├── application.yml
├── application-dev.yml
├── application-prod.yml
└── db/
    └── migration/
        ├── V1__create_initial_schema.sql
        ├── V2__insert_master_data.sql
        └── V3__create_indexes.sql
```

---

## 3. Endpoints Requeridos

### A. Sentiment Prediction Endpoints

#### POST /api/v1/sentiment/predict

**Content-Type:** `application/json`

**Request:**
{
  "text": "I want to die",
  "modelPath": "models/out-model.ser.gz"  // opcional, usa default
}

Response:
{
  "text": "I want to die",
  "predictedClass": 1,
  "predictedLabel": "SUICIDAL",
  "confidence": 0.87,
  "timestamp": "2025-12-20T10:30:45Z"
}
```

#### POST /api/v1/sentiment/predict/batch

**Content-Type:** `application/json`

**Request:**

```json
{
  "texts": [
    "I feel anxious",
    "I am so angry",
    "This makes me sad"
  ],
  "modelPath": "models/out-model.ser.gz"  // opcional
}
```

**Response:**

```json
{
  "results": [
    {
      "text": "I feel anxious",
      "predictedClass": 0,
      "predictedLabel": "ANXIETY",
      "confidence": 0.92
    },
    {
      "text": "I am so angry",
      "predictedClass": 2,
      "predictedLabel": "ANGER",
      "confidence": 0.89
    },
    {
      "text": "This makes me sad",
      "predictedClass": 3,
      "predictedLabel": "SADNESS",
      "confidence": 0.85
    }
  ],
  "processedCount": 3,
  "timestamp": "2025-12-20T10:31:00Z"
}
```

#### GET /api/v1/sentiment/labels

**Response:**

```json
{
  "labels": [
    {"id": 0, "name": "ANXIETY", "description": "Anxious or worried state"},
    {"id": 1, "name": "SUICIDAL", "description": "Suicidal thoughts or expressions"},
    {"id": 2, "name": "ANGER", "description": "Angry or frustrated state"},
    {"id": 3, "name": "SADNESS", "description": "Sad or depressed state"},
    {"id": 4, "name": "FRUSTRATION", "description": "Frustrated state"}
  ]
}
```

### B. Training Endpoints

#### POST /api/v1/training/start

**Content-Type:** `application/json`

**Request:**

```json
{
  "trainDataPath": "data/train.sst",
  "devDataPath": "data/dev.sst",
  "outputModelPath": "models/new-model.ser.gz",
  "numHiddenUnits": 25,
  "numClasses": 5,
  "maxEpochs": 50
}
```

**Response:**

```json
{
  "trainingId": "uuid-12345",
  "status": "STARTED",
  "message": "Training initiated successfully",
  "startTime": "2025-12-20T10:32:00Z"
}
```

#### GET /api/v1/training/status/{trainingId}

**Response:**

```json
{
  "trainingId": "uuid-12345",
  "status": "IN_PROGRESS",  // STARTED, IN_PROGRESS, COMPLETED, FAILED
  "progress": 45,
  "currentEpoch": 23,
  "totalEpochs": 50,
  "startTime": "2025-12-20T10:32:00Z",
  "estimatedCompletion": "2025-12-20T11:15:00Z"
}
```

> **Nota:** El entrenamiento debe ejecutarse de forma asíncrona usando `@Async` o un `ExecutorService`, ya que puede tardar horas.

### C. Data Preparation Endpoints

#### POST /api/v1/data/csv-to-sst

**Content-Type:** `multipart/form-data`

**Request Parameters:**
- `file`: sample.csv (multipart)
- `hasHeader`: true
- `outputFileName`: train.sst

**Response:**

```json
{
  "inputFile": "sample.csv",
  "outputFile": "data/train.sst",
  "recordsProcessed": 1500,
  "status": "SUCCESS",
  "timestamp": "2025-12-20T10:33:00Z"
}
POST /api/v1/data/binarize-sst
Content-Type: application/json

Request:
{
  "inputPath": "data/train.sst",
  "outputPath": "data/train.binarized.sst"
}

Response:
{
  "inputFile": "data/train.sst",
  "outputFile": "data/train.binarized.sst",
  "treesProcessed": 1500,
  "status": "SUCCESS",
  "timestamp": "2025-12-20T10:34:00Z"
}
POST /api/v1/data/parse-tree
Content-Type: application/json

Request:
{
  "sentence": "I feel very sad today",
  "label": 3
}

Response:
{
  "sentence": "I feel very sad today",
  "parseTree": "(ROOT (S (NP (PRP I)) (VP (VBP feel) (ADJP (RB very) (JJ sad)) (NP (NN today)))))",
  "labeledTree": "(3 (3 I) (3 (3 feel) (3 (3 very) (3 sad) (3 today))))",
  "timestamp": "2025-12-20T10:35:00Z"
}
D. Modelo de Datos y Endpoints CRUD

## 3.1. Modelo Entidad-Relación (MySQL)

### Entidades Principales

#### PACIENTE
```sql
CREATE TABLE paciente (
    id_paciente INT PRIMARY KEY AUTO_INCREMENT,
    doc_paciente VARCHAR(20) UNIQUE NOT NULL,
    nombre_paciente VARCHAR(100) NOT NULL,
    direccion_paciente VARCHAR(255),
    email_paciente VARCHAR(100),
    telefono_paciente VARCHAR(20),
    estatus_paciente VARCHAR(20) DEFAULT 'ACTIVO',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### PERSONAL
```sql
CREATE TABLE personal (
    id_personal INT PRIMARY KEY AUTO_INCREMENT,
    doc_personal VARCHAR(20) UNIQUE NOT NULL,
    nombre_personal VARCHAR(100) NOT NULL,
    estatus_personal VARCHAR(20) DEFAULT 'ACTIVO',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### USUARIO
```sql
CREATE TABLE usuario (
    id_usuario INT PRIMARY KEY AUTO_INCREMENT,
    nombre_usuario VARCHAR(50) UNIQUE NOT NULL,
    pass_usuario VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### USUARIO_ROLES
```sql
CREATE TABLE usuario_roles (
    id_roles INT PRIMARY KEY AUTO_INCREMENT,
    permisos_roles VARCHAR(255) NOT NULL
);
```

#### CONSULTA
```sql
CREATE TABLE consulta (
    id_consulta INT PRIMARY KEY AUTO_INCREMENT,
    id_paciente INT NOT NULL,
    id_personal INT NOT NULL,
    fechahora_consulta TIMESTAMP NOT NULL,
    fechafin_consulta TIMESTAMP,
    estatus_consulta VARCHAR(50) DEFAULT 'PENDIENTE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_paciente) REFERENCES paciente(id_paciente),
    FOREIGN KEY (id_personal) REFERENCES personal(id_personal)
);
```

#### CONSULTA_ESTATUS
```sql
CREATE TABLE consulta_estatus (
    id_consulta_estatus INT PRIMARY KEY AUTO_INCREMENT,
    nombre_consulta_estatus VARCHAR(50) UNIQUE NOT NULL
);
```

#### EVALUACION
```sql
CREATE TABLE evaluacion (
    id_evaluacion INT PRIMARY KEY AUTO_INCREMENT,
    id_consulta INT NOT NULL,
    nombre_evaluacion VARCHAR(100) NOT NULL,
    area_evaluacion VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_consulta) REFERENCES consulta(id_consulta)
);
```

#### EVALUACION_PREGUNTA
```sql
CREATE TABLE evaluacion_pregunta (
    id_evaluacion_pregunta INT PRIMARY KEY AUTO_INCREMENT,
    texto_evaluacion_pregunta TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### EVALUACION_RESPUESTA
```sql
CREATE TABLE evaluacion_respuesta (
    id_evaluacion_respuesta INT PRIMARY KEY AUTO_INCREMENT,
    id_evaluacion_pregunta INT NOT NULL,
    texto_evaluacion_respuesta TEXT NOT NULL,
    texto_set_evaluacion_respuesta TEXT,
    label_evaluacion_respuesta VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_evaluacion_pregunta) REFERENCES evaluacion_pregunta(id_evaluacion_pregunta)
);
```

#### REPORTE
```sql
CREATE TABLE reporte (
    id_reporte INT PRIMARY KEY AUTO_INCREMENT,
    id_usuario INT NOT NULL,
    id_evaluacion INT NOT NULL,
    fechageneracion_reporte TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    nombre_reporte VARCHAR(100) NOT NULL,
    resultado_reporte TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    FOREIGN KEY (id_evaluacion) REFERENCES evaluacion(id_evaluacion)
);
```

### Relaciones
- **PACIENTE** 1:N **CONSULTA** (un paciente puede tener múltiples consultas)
- **PERSONAL** 1:N **CONSULTA** (un personal atiende múltiples consultas)
- **CONSULTA** 1:N **EVALUACION** (una consulta puede tener múltiples evaluaciones)
- **EVALUACION_PREGUNTA** 1:N **EVALUACION_RESPUESTA** (una pregunta tiene múltiples respuestas)
- **USUARIO** 1:N **REPORTE** (un usuario genera múltiples reportes)
- **EVALUACION** 1:N **REPORTE** (una evaluación puede generar múltiples reportes)
- **USUARIO** N:M **USUARIO_ROLES** (relación muchos a muchos, requiere tabla intermedia)

## 3.2. Endpoints CRUD - Pacientes

### Crear Paciente
```http
POST /api/v1/pacientes
Content-Type: application/json

Request:
{
  "docPaciente": "12345678",
  "nombrePaciente": "Juan Pérez",
  "direccionPaciente": "Calle Principal 123",
  "emailPaciente": "juan.perez@email.com",
  "telefonoPaciente": "+34600123456",
  "estatusPaciente": "ACTIVO"
}

Response: 201 Created
{
  "idPaciente": 1,
  "docPaciente": "12345678",
  "nombrePaciente": "Juan Pérez",
  "direccionPaciente": "Calle Principal 123",
  "emailPaciente": "juan.perez@email.com",
  "telefonoPaciente": "+34600123456",
  "estatusPaciente": "ACTIVO",
  "createdAt": "2025-12-21T10:00:00Z",
  "updatedAt": "2025-12-21T10:00:00Z"
}
```

### Obtener Todos los Pacientes
```http
GET /api/v1/pacientes
Query Params: ?page=0&size=20&sort=nombrePaciente,asc&estatus=ACTIVO

Response: 200 OK
{
  "content": [
    {
      "idPaciente": 1,
      "docPaciente": "12345678",
      "nombrePaciente": "Juan Pérez",
      "estatusPaciente": "ACTIVO"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

### Obtener Paciente por ID
```http
GET /api/v1/pacientes/{id}

Response: 200 OK
{
  "idPaciente": 1,
  "docPaciente": "12345678",
  "nombrePaciente": "Juan Pérez",
  "direccionPaciente": "Calle Principal 123",
  "emailPaciente": "juan.perez@email.com",
  "telefonoPaciente": "+34600123456",
  "estatusPaciente": "ACTIVO",
  "consultas": [
    {
      "idConsulta": 1,
      "fechahoraConsulta": "2025-12-20T14:00:00Z",
      "estatusConsulta": "COMPLETADA"
    }
  ]
}
```

### Actualizar Paciente
```http
PUT /api/v1/pacientes/{id}
Content-Type: application/json

Request:
{
  "direccionPaciente": "Nueva Dirección 456",
  "telefonoPaciente": "+34600999888"
}

Response: 200 OK
{
  "idPaciente": 1,
  "docPaciente": "12345678",
  "nombrePaciente": "Juan Pérez",
  "direccionPaciente": "Nueva Dirección 456",
  "telefonoPaciente": "+34600999888",
  "updatedAt": "2025-12-21T11:00:00Z"
}
```

### Eliminar Paciente (Soft Delete)
```http
DELETE /api/v1/pacientes/{id}

Response: 204 No Content
```

## 3.3. Endpoints CRUD - Personal

### Crear Personal
```http
POST /api/v1/personal
Content-Type: application/json

Request:
{
  "docPersonal": "DOC-001",
  "nombrePersonal": "Dra. María García",
  "estatusPersonal": "ACTIVO"
}

Response: 201 Created
{
  "idPersonal": 1,
  "docPersonal": "DOC-001",
  "nombrePersonal": "Dra. María García",
  "estatusPersonal": "ACTIVO",
  "createdAt": "2025-12-21T10:00:00Z"
}
```

### Listar Personal
```http
GET /api/v1/personal
Query Params: ?page=0&size=20&estatus=ACTIVO

Response: 200 OK
{
  "content": [
    {
      "idPersonal": 1,
      "docPersonal": "DOC-001",
      "nombrePersonal": "Dra. María García",
      "estatusPersonal": "ACTIVO"
    }
  ]
}
```

## 3.4. Endpoints CRUD - Usuarios

### Crear Usuario
```http
POST /api/v1/usuarios
Content-Type: application/json

Request:
{
  "nombreUsuario": "admin",
  "passUsuario": "securePassword123",
  "roles": [1, 2]
}

Response: 201 Created
{
  "idUsuario": 1,
  "nombreUsuario": "admin",
  "roles": [
    {"idRoles": 1, "permisosRoles": "ADMIN"},
    {"idRoles": 2, "permisosRoles": "USER"}
  ],
  "createdAt": "2025-12-21T10:00:00Z"
}
```

### Autenticar Usuario
```http
POST /api/v1/usuarios/login
Content-Type: application/json

Request:
{
  "nombreUsuario": "admin",
  "passUsuario": "securePassword123"
}

Response: 200 OK
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "idUsuario": 1,
  "nombreUsuario": "admin",
  "roles": ["ADMIN", "USER"],
  "expiresIn": 3600
}
```

## 3.5. Endpoints CRUD - Consultas

### Crear Consulta
```http
POST /api/v1/consultas
Content-Type: application/json

Request:
{
  "idPaciente": 1,
  "idPersonal": 1,
  "fechahoraConsulta": "2025-12-21T15:00:00Z",
  "estatusConsulta": "PENDIENTE"
}

Response: 201 Created
{
  "idConsulta": 1,
  "paciente": {
    "idPaciente": 1,
    "nombrePaciente": "Juan Pérez"
  },
  "personal": {
    "idPersonal": 1,
    "nombrePersonal": "Dra. María García"
  },
  "fechahoraConsulta": "2025-12-21T15:00:00Z",
  "estatusConsulta": "PENDIENTE",
  "createdAt": "2025-12-21T10:00:00Z"
}
```

### Obtener Consultas por Paciente
```http
GET /api/v1/consultas/paciente/{idPaciente}
Query Params: ?page=0&size=20&desde=2025-01-01&hasta=2025-12-31

Response: 200 OK
{
  "content": [
    {
      "idConsulta": 1,
      "fechahoraConsulta": "2025-12-21T15:00:00Z",
      "fechafinConsulta": "2025-12-21T16:00:00Z",
      "estatusConsulta": "COMPLETADA",
      "personal": {
        "nombrePersonal": "Dra. María García"
      },
      "evaluaciones": [
        {
          "idEvaluacion": 1,
          "nombreEvaluacion": "Evaluación Inicial"
        }
      ]
    }
  ]
}
```

### Actualizar Estado de Consulta
```http
PATCH /api/v1/consultas/{id}/estado
Content-Type: application/json

Request:
{
  "estatusConsulta": "EN_PROGRESO",
  "fechafinConsulta": null
}

Response: 200 OK
{
  "idConsulta": 1,
  "estatusConsulta": "EN_PROGRESO",
  "updatedAt": "2025-12-21T15:05:00Z"
}
```

### Finalizar Consulta
```http
POST /api/v1/consultas/{id}/finalizar
Content-Type: application/json

Request:
{
  "fechafinConsulta": "2025-12-21T16:00:00Z",
  "notas": "Consulta completada satisfactoriamente"
}

Response: 200 OK
{
  "idConsulta": 1,
  "estatusConsulta": "COMPLETADA",
  "fechafinConsulta": "2025-12-21T16:00:00Z",
  "updatedAt": "2025-12-21T16:00:00Z"
}
```

## 3.6. Endpoints CRUD - Evaluaciones

### Crear Evaluación
```http
POST /api/v1/evaluaciones
Content-Type: application/json

Request:
{
  "idConsulta": 1,
  "nombreEvaluacion": "Evaluación de Sentimientos - Sesión 1",
  "areaEvaluacion": "SALUD_MENTAL"
}

Response: 201 Created
{
  "idEvaluacion": 1,
  "idConsulta": 1,
  "nombreEvaluacion": "Evaluación de Sentimientos - Sesión 1",
  "areaEvaluacion": "SALUD_MENTAL",
  "createdAt": "2025-12-21T15:30:00Z"
}
```

### Agregar Pregunta a Evaluación
```http
POST /api/v1/evaluaciones/preguntas
Content-Type: application/json

Request:
{
  "textoEvaluacionPregunta": "¿Cómo te sientes hoy?"
}

Response: 201 Created
{
  "idEvaluacionPregunta": 1,
  "textoEvaluacionPregunta": "¿Cómo te sientes hoy?",
  "createdAt": "2025-12-21T15:30:00Z"
}
```

### Registrar Respuesta con Análisis de Sentimiento
```http
POST /api/v1/evaluaciones/respuestas
Content-Type: application/json

Request:
{
  "idEvaluacionPregunta": 1,
  "textoEvaluacionRespuesta": "Me siento muy triste y sin esperanza",
  "analizarSentimiento": true
}

Response: 201 Created
{
  "idEvaluacionRespuesta": 1,
  "idEvaluacionPregunta": 1,
  "textoEvaluacionRespuesta": "Me siento muy triste y sin esperanza",
  "textoSetEvaluacionRespuesta": "me siento muy triste y sin esperanza",
  "labelEvaluacionRespuesta": "SADNESS",
  "sentimentAnalysis": {
    "predictedClass": 3,
    "predictedLabel": "SADNESS",
    "confidence": 0.89
  },
  "createdAt": "2025-12-21T15:35:00Z"
}
```

### Obtener Evaluación Completa con Respuestas
```http
GET /api/v1/evaluaciones/{id}/completa

Response: 200 OK
{
  "idEvaluacion": 1,
  "nombreEvaluacion": "Evaluación de Sentimientos - Sesión 1",
  "consulta": {
    "idConsulta": 1,
    "paciente": {
      "nombrePaciente": "Juan Pérez"
    }
  },
  "preguntas": [
    {
      "idPregunta": 1,
      "textoPregunta": "¿Cómo te sientes hoy?",
      "respuesta": {
        "textoRespuesta": "Me siento muy triste y sin esperanza",
        "labelSentimiento": "SADNESS",
        "confidence": 0.89
      }
    }
  ],
  "analisisSentimientos": {
    "anxiety": 0,
    "suicidal": 0,
    "anger": 0,
    "sadness": 1,
    "frustration": 0,
    "dominante": "SADNESS"
  }
}
```

## 3.7. Endpoints CRUD - Reportes

### Generar Reporte de Evaluación
```http
POST /api/v1/reportes/generar
Content-Type: application/json

Request:
{
  "idUsuario": 1,
  "idEvaluacion": 1,
  "nombreReporte": "Reporte de Análisis de Sentimientos - Juan Pérez",
  "incluirDetalles": true
}

Response: 201 Created
{
  "idReporte": 1,
  "nombreReporte": "Reporte de Análisis de Sentimientos - Juan Pérez",
  "fechageneracionReporte": "2025-12-21T16:00:00Z",
  "usuario": {
    "nombreUsuario": "admin"
  },
  "evaluacion": {
    "idEvaluacion": 1,
    "nombreEvaluacion": "Evaluación de Sentimientos - Sesión 1"
  },
  "resultadoReporte": {
    "paciente": "Juan Pérez",
    "fecha": "2025-12-21",
    "sentimientoDominante": "SADNESS",
    "distribucion": {
      "ANXIETY": 0,
      "SUICIDAL": 0,
      "ANGER": 0,
      "SADNESS": 1,
      "FRUSTRATION": 0
    },
    "recomendaciones": [
      "Seguimiento cercano recomendado",
      "Considerar terapia especializada"
    ],
    "nivelRiesgo": "MEDIO"
  }
}
```

### Obtener Reporte por ID
```http
GET /api/v1/reportes/{id}

Response: 200 OK
{
  "idReporte": 1,
  "nombreReporte": "Reporte de Análisis de Sentimientos - Juan Pérez",
  "fechageneracionReporte": "2025-12-21T16:00:00Z",
  "resultadoReporte": { ... },
  "evaluacion": {
    "consulta": {
      "paciente": {
        "nombrePaciente": "Juan Pérez"
      }
    }
  }
}
```

### Listar Reportes por Usuario
```http
GET /api/v1/reportes/usuario/{idUsuario}
Query Params: ?page=0&size=20&desde=2025-01-01&hasta=2025-12-31

Response: 200 OK
{
  "content": [
    {
      "idReporte": 1,
      "nombreReporte": "Reporte de Análisis...",
      "fechageneracionReporte": "2025-12-21T16:00:00Z"
    }
  ]
}
```

### Exportar Reporte (PDF)
```http
GET /api/v1/reportes/{id}/export
Query Params: ?format=pdf

Response: 200 OK
Content-Type: application/pdf
Content-Disposition: attachment; filename="reporte_1.pdf"

[Binary PDF content]
```

## 3.8. Endpoints Especiales de Integración

### Analizar Consulta Completa
```http
POST /api/v1/consultas/{id}/analizar-sentimientos
Content-Type: application/json

Request:
{
  "textos": [
    "Me siento muy ansioso últimamente",
    "A veces pienso que no tiene sentido seguir",
    "Estoy enojado con todo el mundo"
  ],
  "generarReporte": true
}

Response: 200 OK
{
  "idConsulta": 1,
  "analisisCompleto": [
    {
      "texto": "Me siento muy ansioso últimamente",
      "sentimiento": "ANXIETY",
      "confidence": 0.91
    },
    {
      "texto": "A veces pienso que no tiene sentido seguir",
      "sentimiento": "SUICIDAL",
      "confidence": 0.87,
      "alertaRiesgo": true
    },
    {
      "texto": "Estoy enojado con todo el mundo",
      "sentimiento": "ANGER",
      "confidence": 0.85
    }
  ],
  "resumen": {
    "sentimientoDominante": "ANXIETY",
    "nivelRiesgo": "ALTO",
    "requiereAtencionInmediata": true
  },
  "reporteGenerado": {
    "idReporte": 2,
    "nombreReporte": "Análisis Automático - Consulta 1"
  }
}
```

### Dashboard de Consulta
```http
GET /api/v1/consultas/{id}/dashboard

Response: 200 OK
{
  "consulta": {
    "idConsulta": 1,
    "paciente": "Juan Pérez",
    "personal": "Dra. María García",
    "fecha": "2025-12-21T15:00:00Z"
  },
  "evaluaciones": 3,
  "respuestasAnalizadas": 15,
  "distribucionSentimientos": {
    "ANXIETY": 3,
    "SUICIDAL": 1,
    "ANGER": 2,
    "SADNESS": 6,
    "FRUSTRATION": 3
  },
  "alertas": [
    {
      "tipo": "RIESGO_SUICIDA",
      "nivel": "ALTO",
      "mensaje": "Detectada expresión suicida en respuesta #7"
    }
  ],
  "reportes": [
    {
      "idReporte": 1,
      "nombreReporte": "Reporte Inicial"
    }
  ]
}
```

E. Health & Info Endpoints
GET /actuator/health
Response:
{
  "status": "UP",
  "components": {
    "diskSpace": {"status": "UP"},
    "corenlp": {"status": "UP", "model": "loaded"},
    "sentiment": {"status": "UP", "modelPath": "models/out-model.ser.gz"}
  }
}
GET /api/v1/info
Response:
{
  "applicationName": "RNTN Sentiment Analysis API",
  "version": "1.0.0",
  "availableModels": [
    "models/out-model.ser.gz",
    "models/unir08122025.ser.gz"
  ],
  "supportedLabels": 5,
  "coreNlpVersion": "4.5.4"
}
4. Configuración (application.yml)
```yaml
server:
  port: 8080
  servlet:
    context-path: /

spring:
  application:
    name: rntn-sentiment-api
    
  # Configuración de base de datos MySQL
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:rntn_db}?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: ${DB_USER:root}
    password: ${DB_PASSWORD:password}
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      
  # Configuración JPA/Hibernate
  jpa:
    hibernate:
      ddl-auto: validate  # validate en producción, update en desarrollo
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
        use_sql_comments: true
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
    open-in-view: false
    
  # Configuración Flyway para migraciones
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    validate-on-migrate: true
    
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB
      
  task:
    execution:
      pool:
        core-size: 2
        max-size: 5
        queue-capacity: 100

# Configuración custom RNTN
rntn:
  model:
    default-path: models/out-model.ser.gz
    directory: models/
  data:
    directory: data/
  training:
    max-memory: 6g
    default-hidden-units: 25
    default-classes: 5
  sentiment:
    labels:
      - name: ANXIETY
        index: 0
        description: "Anxious or worried state"
        risk-level: MEDIO
      - name: SUICIDAL
        index: 1
        description: "Suicidal thoughts or expressions"
        risk-level: ALTO
      - name: ANGER
        index: 2
        description: "Angry or frustrated state"
        risk-level: MEDIO
      - name: SADNESS
        index: 3
        description: "Sad or depressed state"
        risk-level: MEDIO
      - name: FRUSTRATION
        index: 4
        description: "Frustrated state"
        risk-level: BAJO

# Configuración de logging
logging:
  level:
    com.example.rntn: DEBUG
    edu.stanford.nlp: WARN
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
    org.springframework.jdbc.core: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/rntn-api.log
    max-size: 10MB
    max-history: 30

# Actuator endpoints
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
  health:
    db:
      enabled: true
    diskspace:
      enabled: true

# OpenAPI/Swagger configuración
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    operationsSorter: method
    tagsSorter: alpha
  show-actuator: true
```

### application-dev.yml
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/rntn_db_dev?useSSL=false
    username: dev_user
    password: dev_password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
  flyway:
    clean-disabled: false

logging:
  level:
    com.example.rntn: DEBUG
    org.hibernate.SQL: DEBUG
```

### application-prod.yml
```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=true&requireSSL=true
    hikari:
      maximum-pool-size: 20
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  flyway:
    clean-disabled: true

logging:
  level:
    com.example.rntn: INFO
    edu.stanford.nlp: WARN
    org.hibernate: WARN
```
5. Mejoras Adicionales
A. Singleton para SentimentPredictor
Cargar el modelo una sola vez al inicio de la aplicación
Usar @PostConstruct en un @Configuration o @Service
Cache de modelos cargados para evitar recargas
B. Manejo de Errores Robusto
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ModelNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleModelNotFound(...)
    
    @ExceptionHandler(PredictionException.class)
    public ResponseEntity<ErrorResponse> handlePredictionError(...)
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(...)
}
C. Validación de Requests
public class PredictRequest {
    @NotBlank(message = "Text cannot be empty")
    @Size(min = 1, max = 5000, message = "Text must be between 1 and 5000 characters")
    private String text;
    
    @Pattern(regexp = ".*\\.ser\\.gz$", message = "Model path must end with .ser.gz")
    private String modelPath;
}
D. Documentación OpenAPI/Swagger
Anotar controllers con @Tag, @Operation, @ApiResponse
Generar especificación OpenAPI automática
Swagger UI disponible en /swagger-ui.html
E. Testing
Unit tests para services usando Mockito
Integration tests para controllers usando MockMvc
Test del endpoint de predicción con modelo mock
6. Consideraciones de Seguridad
A. Rate Limiting
Implementar rate limiting con Bucket4j o Spring Cloud Gateway
Limitar requests por IP/usuario
B. CORS
Configurar CORS para permitir acceso desde frontend
@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        // configuración CORS
    }
}
C. Authentication (Opcional)
Spring Security con JWT o API Keys
Proteger endpoints de entrenamiento (solo admin)
7. Dockerización
Crear Dockerfile:

FROM openjdk:11-jre-slim
WORKDIR /app
COPY target/rntn-api.jar app.jar
COPY models/ models/
COPY data/ data/
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx6g", "-jar", "app.jar"]
Crear docker-compose.yml:

version: '3.8'
services:
  rntn-api:
    build: .
    ports:
      - "8080:8080"
    volumes:
      - ./models:/app/models
      - ./data:/app/data
    environment:
      - SPRING_PROFILES_ACTIVE=prod
8. Plan de Implementación
Fase 1: Setup Base (2-3 horas)
Actualizar pom.xml con dependencias Spring Boot
Crear estructura de paquetes
Crear DTOs básicos y enums
Configurar application.yml
Fase 2: Servicio de Predicción (3-4 horas)
Refactorizar SentimentPredictor como @Service
Implementar SentimentService con cache de modelos
Crear SentimentController con endpoints básicos
Implementar manejo de errores
Fase 3: Servicios de Datos (2-3 horas)
Refactorizar CsvToSstConverter, TreeConverter, BinarizeSst
Crear DataPreparationService
Implementar DataPreparationController
Soporte para multipart/form-data (file upload)
Fase 4: Servicio de Entrenamiento (4-5 horas)
Refactorizar TrainingRunner
Implementar TrainingService con ejecución asíncrona
Crear sistema de tracking de estado de entrenamiento
Implementar TrainingController
Fase 5: Documentación y Testing (3-4 horas)
Añadir anotaciones OpenAPI/Swagger
Crear tests unitarios para services
Crear tests de integración para controllers
Actualizar README con documentación API
Fase 6: Producción (2-3 horas)
Configurar profiles (dev, prod)
Crear Dockerfile y docker-compose
Implementar health checks personalizados
Configurar logging apropiado
9. Dependencias POM.xml Adicionales
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version>
</parent>

<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    
    <!-- OpenAPI/Swagger -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.3.0</version>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <scope>provided</scope>
    </dependency>
    
    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- Mantener las existentes: CoreNLP, Commons CSV, SLF4J -->
</dependencies>
Resultado Esperado
Una API REST profesional y moderna que:

✅ Expone endpoints RESTful para todas las funcionalidades
✅ Usa arquitectura por capas (Controller → Service → Utility)
✅ Tiene documentación automática con Swagger UI
✅ Maneja errores de forma robusta
✅ Valida inputs automáticamente
✅ Soporta operaciones asíncronas para entrenamiento
✅ Es fácilmente desplegable con Docker
✅ Tiene health checks y monitoring
✅ Es testeable y mantenible
✅ Mantiene compatibilidad con modelos entrenados existentes
Notas Importantes
Memoria: El modelo CoreNLP requiere mucha RAM. Configurar -Xmx6g mínimo.
Modelos Pre-cargados: Cargar modelo default al inicio para respuestas rápidas.
Entrenamiento Asíncrono: NO bloquear el thread principal durante entrenamiento.
File Upload: Validar tamaño y tipo de archivos CSV subidos.
Seguridad: Validar rutas de archivos para evitar path traversal attacks.
Logging: Usar SLF4J con Logback para logging estructurado.
Health Checks: Verificar que el modelo esté cargado y CoreNLP funcional.
Preguntas para el Desarrollador
¿Quieres incluir autenticación JWT desde el inicio o dejarlo para fase posterior?
¿Prefieres H2/PostgreSQL para tracking de entrenamientos o simplemente en memoria?
¿Necesitas websockets para updates en tiempo real del progreso de entrenamiento?
¿Quieres soporte para múltiples modelos simultáneos o uno default?
¿Necesitas endpoints de administración (listar/eliminar modelos)?
Este prompt proporciona una guía completa para transformar el proyecto CLI en una API REST production-ready manteniendo toda la funcionalidad existente y añadiendo capacidades enterprise-grade.

---

# SECCIÓN ADICIONAL: IMPLEMENTACIÓN DE CAPA DE PERSISTENCIA

## 10. Entidades JPA (Entity Classes)

### 10.1. Paciente.java
```java
package com.example.rntn.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "paciente", indexes = {
    @Index(name = "idx_doc_paciente", columnList = "doc_paciente"),
    @Index(name = "idx_estatus_paciente", columnList = "estatus_paciente")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paciente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paciente")
    private Integer idPaciente;
    
    @Column(name = "doc_paciente", unique = true, nullable = false, length = 20)
    private String docPaciente;
    
    @Column(name = "nombre_paciente", nullable = false, length = 100)
    private String nombrePaciente;
    
    @Column(name = "direccion_paciente", length = 255)
    private String direccionPaciente;
    
    @Column(name = "email_paciente", length = 100)
    private String emailPaciente;
    
    @Column(name = "telefono_paciente", length = 20)
    private String telefonoPaciente;
    
    @Column(name = "estatus_paciente", length = 20)
    @Builder.Default
    private String estatusPaciente = "ACTIVO";
    
    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Consulta> consultas = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Helper methods
    public void addConsulta(Consulta consulta) {
        consultas.add(consulta);
        consulta.setPaciente(this);
    }
    
    public void removeConsulta(Consulta consulta) {
        consultas.remove(consulta);
        consulta.setPaciente(null);
    }
}
```

### 10.2. Personal.java
```java
package com.example.rntn.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "personal", indexes = {
    @Index(name = "idx_doc_personal", columnList = "doc_personal")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Personal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_personal")
    private Integer idPersonal;
    
    @Column(name = "doc_personal", unique = true, nullable = false, length = 20)
    private String docPersonal;
    
    @Column(name = "nombre_personal", nullable = false, length = 100)
    private String nombrePersonal;
    
    @Column(name = "estatus_personal", length = 20)
    @Builder.Default
    private String estatusPersonal = "ACTIVO";
    
    @OneToMany(mappedBy = "personal", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Consulta> consultas = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

### 10.3. Usuario.java
```java
package com.example.rntn.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "usuario", indexes = {
    @Index(name = "idx_nombre_usuario", columnList = "nombre_usuario")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;
    
    @Column(name = "nombre_usuario", unique = true, nullable = false, length = 50)
    private String nombreUsuario;
    
    @Column(name = "pass_usuario", nullable = false, length = 255)
    private String passUsuario;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "usuario_roles_mapping",
        joinColumns = @JoinColumn(name = "id_usuario"),
        inverseJoinColumns = @JoinColumn(name = "id_roles")
    )
    @Builder.Default
    private Set<UsuarioRoles> roles = new HashSet<>();
    
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Reporte> reportes = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

### 10.4. UsuarioRoles.java
```java
package com.example.rntn.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuario_roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioRoles {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_roles")
    private Integer idRoles;
    
    @Column(name = "permisos_roles", nullable = false, length = 255)
    private String permisosRoles;
    
    @ManyToMany(mappedBy = "roles")
    @Builder.Default
    private Set<Usuario> usuarios = new HashSet<>();
}
```

### 10.5. Consulta.java
```java
package com.example.rntn.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "consulta", indexes = {
    @Index(name = "idx_id_paciente", columnList = "id_paciente"),
    @Index(name = "idx_id_personal", columnList = "id_personal"),
    @Index(name = "idx_fechahora_consulta", columnList = "fechahora_consulta"),
    @Index(name = "idx_estatus_consulta", columnList = "estatus_consulta")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consulta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_consulta")
    private Integer idConsulta;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_personal", nullable = false)
    private Personal personal;
    
    @Column(name = "fechahora_consulta", nullable = false)
    private LocalDateTime fechahoraConsulta;
    
    @Column(name = "fechafin_consulta")
    private LocalDateTime fechafinConsulta;
    
    @Column(name = "estatus_consulta", length = 50)
    @Builder.Default
    private String estatusConsulta = "PENDIENTE";
    
    @OneToMany(mappedBy = "consulta", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Evaluacion> evaluaciones = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Helper methods
    public void addEvaluacion(Evaluacion evaluacion) {
        evaluaciones.add(evaluacion);
        evaluacion.setConsulta(this);
    }
}
```

### 10.6. ConsultaEstatus.java
```java
package com.example.rntn.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "consulta_estatus")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultaEstatus {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_consulta_estatus")
    private Integer idConsultaEstatus;
    
    @Column(name = "nombre_consulta_estatus", unique = true, nullable = false, length = 50)
    private String nombreConsultaEstatus;
}
```

### 10.7. Evaluacion.java
```java
package com.example.rntn.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "evaluacion", indexes = {
    @Index(name = "idx_id_consulta", columnList = "id_consulta")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evaluacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evaluacion")
    private Integer idEvaluacion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_consulta", nullable = false)
    private Consulta consulta;
    
    @Column(name = "nombre_evaluacion", nullable = false, length = 100)
    private String nombreEvaluacion;
    
    @Column(name = "area_evaluacion", length = 100)
    private String areaEvaluacion;
    
    @OneToMany(mappedBy = "evaluacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Reporte> reportes = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

### 10.8. EvaluacionPregunta.java
```java
package com.example.rntn.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "evaluacion_pregunta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluacionPregunta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evaluacion_pregunta")
    private Integer idEvaluacionPregunta;
    
    @Column(name = "texto_evaluacion_pregunta", nullable = false, columnDefinition = "TEXT")
    private String textoEvaluacionPregunta;
    
    @OneToMany(mappedBy = "evaluacionPregunta", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EvaluacionRespuesta> respuestas = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

### 10.9. EvaluacionRespuesta.java
```java
package com.example.rntn.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "evaluacion_respuesta", indexes = {
    @Index(name = "idx_id_evaluacion_pregunta", columnList = "id_evaluacion_pregunta"),
    @Index(name = "idx_label_evaluacion_respuesta", columnList = "label_evaluacion_respuesta")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluacionRespuesta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evaluacion_respuesta")
    private Integer idEvaluacionRespuesta;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_evaluacion_pregunta", nullable = false)
    private EvaluacionPregunta evaluacionPregunta;
    
    @Column(name = "texto_evaluacion_respuesta", nullable = false, columnDefinition = "TEXT")
    private String textoEvaluacionRespuesta;
    
    @Column(name = "texto_set_evaluacion_respuesta", columnDefinition = "TEXT")
    private String textoSetEvaluacionRespuesta;
    
    @Column(name = "label_evaluacion_respuesta", length = 50)
    private String labelEvaluacionRespuesta;
    
    @Column(name = "confidence_score")
    private Double confidenceScore;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

### 10.10. Reporte.java
```java
package com.example.rntn.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reporte", indexes = {
    @Index(name = "idx_id_usuario", columnList = "id_usuario"),
    @Index(name = "idx_id_evaluacion", columnList = "id_evaluacion"),
    @Index(name = "idx_fechageneracion_reporte", columnList = "fechageneracion_reporte")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reporte {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reporte")
    private Integer idReporte;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_evaluacion", nullable = false)
    private Evaluacion evaluacion;
    
    @Column(name = "fechageneracion_reporte", nullable = false)
    @Builder.Default
    private LocalDateTime fechageneracionReporte = LocalDateTime.now();
    
    @Column(name = "nombre_reporte", nullable = false, length = 100)
    private String nombreReporte;
    
    @Column(name = "resultado_reporte", columnDefinition = "TEXT")
    private String resultadoReporte;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

## 11. Repositorios JPA

### 11.1. PacienteRepository.java
```java
package com.example.rntn.repository;

import com.example.rntn.entity.Paciente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Integer> {
    
    Optional<Paciente> findByDocPaciente(String docPaciente);
    
    Page<Paciente> findByEstatusPaciente(String estatus, Pageable pageable);
    
    @Query("SELECT p FROM Paciente p WHERE " +
           "LOWER(p.nombrePaciente) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.docPaciente) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Paciente> searchPacientes(@Param("search") String search, Pageable pageable);
    
    boolean existsByDocPaciente(String docPaciente);
}
```

### 11.2. PersonalRepository.java
```java
package com.example.rntn.repository;

import com.example.rntn.entity.Personal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonalRepository extends JpaRepository<Personal, Integer> {
    
    Optional<Personal> findByDocPersonal(String docPersonal);
    
    Page<Personal> findByEstatusPersonal(String estatus, Pageable pageable);
    
    boolean existsByDocPersonal(String docPersonal);
}
```

### 11.3. UsuarioRepository.java
```java
package com.example.rntn.repository;

import com.example.rntn.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);
    
    boolean existsByNombreUsuario(String nombreUsuario);
    
    @Query("SELECT u FROM Usuario u JOIN FETCH u.roles WHERE u.nombreUsuario = :nombreUsuario")
    Optional<Usuario> findByNombreUsuarioWithRoles(String nombreUsuario);
}
```

### 11.4. ConsultaRepository.java
```java
package com.example.rntn.repository;

import com.example.rntn.entity.Consulta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Integer> {
    
    Page<Consulta> findByPacienteIdPaciente(Integer idPaciente, Pageable pageable);
    
    Page<Consulta> findByPersonalIdPersonal(Integer idPersonal, Pageable pageable);
    
    Page<Consulta> findByEstatusConsulta(String estatus, Pageable pageable);
    
    @Query("SELECT c FROM Consulta c WHERE " +
           "c.paciente.idPaciente = :idPaciente AND " +
           "c.fechahoraConsulta BETWEEN :desde AND :hasta")
    Page<Consulta> findByPacienteAndDateRange(
        @Param("idPaciente") Integer idPaciente,
        @Param("desde") LocalDateTime desde,
        @Param("hasta") LocalDateTime hasta,
        Pageable pageable
    );
    
    @Query("SELECT c FROM Consulta c JOIN FETCH c.evaluaciones WHERE c.idConsulta = :id")
    Consulta findByIdWithEvaluaciones(@Param("id") Integer id);
    
    List<Consulta> findByFechahoraConsultaBetween(LocalDateTime desde, LocalDateTime hasta);
}
```

### 11.5. EvaluacionRepository.java
```java
package com.example.rntn.repository;

import com.example.rntn.entity.Evaluacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluacionRepository extends JpaRepository<Evaluacion, Integer> {
    
    List<Evaluacion> findByConsultaIdConsulta(Integer idConsulta);
    
    @Query("SELECT e FROM Evaluacion e JOIN FETCH e.reportes WHERE e.idEvaluacion = :id")
    Evaluacion findByIdWithReportes(@Param("id") Integer id);
}
```

### 11.6. EvaluacionPreguntaRepository.java
```java
package com.example.rntn.repository;

import com.example.rntn.entity.EvaluacionPregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluacionPreguntaRepository extends JpaRepository<EvaluacionPregunta, Integer> {
}
```

### 11.7. EvaluacionRespuestaRepository.java
```java
package com.example.rntn.repository;

import com.example.rntn.entity.EvaluacionRespuesta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluacionRespuestaRepository extends JpaRepository<EvaluacionRespuesta, Integer> {
    
    List<EvaluacionRespuesta> findByEvaluacionPreguntaIdEvaluacionPregunta(Integer idPregunta);
    
    @Query("SELECT er FROM EvaluacionRespuesta er WHERE er.labelEvaluacionRespuesta = :label")
    List<EvaluacionRespuesta> findByLabel(@Param("label") String label);
    
    @Query("SELECT er.labelEvaluacionRespuesta, COUNT(er) FROM EvaluacionRespuesta er " +
           "WHERE er.evaluacionPregunta.idEvaluacionPregunta IN :preguntaIds " +
           "GROUP BY er.labelEvaluacionRespuesta")
    List<Object[]> countByLabelForPreguntas(@Param("preguntaIds") List<Integer> preguntaIds);
}
```

### 11.8. ReporteRepository.java
```java
package com.example.rntn.repository;

import com.example.rntn.entity.Reporte;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Integer> {
    
    Page<Reporte> findByUsuarioIdUsuario(Integer idUsuario, Pageable pageable);
    
    Page<Reporte> findByEvaluacionIdEvaluacion(Integer idEvaluacion, Pageable pageable);
    
    @Query("SELECT r FROM Reporte r WHERE " +
           "r.usuario.idUsuario = :idUsuario AND " +
           "r.fechageneracionReporte BETWEEN :desde AND :hasta")
    Page<Reporte> findByUsuarioAndDateRange(
        @Param("idUsuario") Integer idUsuario,
        @Param("desde") LocalDateTime desde,
        @Param("hasta") LocalDateTime hasta,
        Pageable pageable
    );
    
    @Query("SELECT r FROM Reporte r JOIN FETCH r.evaluacion e JOIN FETCH e.consulta c " +
           "WHERE r.idReporte = :id")
    Reporte findByIdWithFullDetails(@Param("id") Integer id);
}
```

### 11.9. UsuarioRolesRepository.java
```java
package com.example.rntn.repository;

import com.example.rntn.entity.UsuarioRoles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRolesRepository extends JpaRepository<UsuarioRoles, Integer> {
    
    Optional<UsuarioRoles> findByPermisosRoles(String permisos);
}
```

### 11.10. ConsultaEstatusRepository.java
```java
package com.example.rntn.repository;

import com.example.rntn.entity.ConsultaEstatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConsultaEstatusRepository extends JpaRepository<ConsultaEstatus, Integer> {
    
    Optional<ConsultaEstatus> findByNombreConsultaEstatus(String nombre);
}
```

## 12. Migraciones Flyway (SQL Scripts)

### 12.1. V1__create_initial_schema.sql
```sql
-- filepath: src/main/resources/db/migration/V1__create_initial_schema.sql

-- Tabla: paciente
CREATE TABLE paciente (
    id_paciente INT PRIMARY KEY AUTO_INCREMENT,
    doc_paciente VARCHAR(20) UNIQUE NOT NULL,
    nombre_paciente VARCHAR(100) NOT NULL,
    direccion_paciente VARCHAR(255),
    email_paciente VARCHAR(100),
    telefono_paciente VARCHAR(20),
    estatus_paciente VARCHAR(20) DEFAULT 'ACTIVO',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_doc_paciente (doc_paciente),
    INDEX idx_estatus_paciente (estatus_paciente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: personal
CREATE TABLE personal (
    id_personal INT PRIMARY KEY AUTO_INCREMENT,
    doc_personal VARCHAR(20) UNIQUE NOT NULL,
    nombre_personal VARCHAR(100) NOT NULL,
    estatus_personal VARCHAR(20) DEFAULT 'ACTIVO',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_doc_personal (doc_personal)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: usuario
CREATE TABLE usuario (
    id_usuario INT PRIMARY KEY AUTO_INCREMENT,
    nombre_usuario VARCHAR(50) UNIQUE NOT NULL,
    pass_usuario VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_nombre_usuario (nombre_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: usuario_roles
CREATE TABLE usuario_roles (
    id_roles INT PRIMARY KEY AUTO_INCREMENT,
    permisos_roles VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla intermedia: usuario_roles_mapping
CREATE TABLE usuario_roles_mapping (
    id_usuario INT NOT NULL,
    id_roles INT NOT NULL,
    PRIMARY KEY (id_usuario, id_roles),
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    FOREIGN KEY (id_roles) REFERENCES usuario_roles(id_roles) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: consulta_estatus
CREATE TABLE consulta_estatus (
    id_consulta_estatus INT PRIMARY KEY AUTO_INCREMENT,
    nombre_consulta_estatus VARCHAR(50) UNIQUE NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: consulta
CREATE TABLE consulta (
    id_consulta INT PRIMARY KEY AUTO_INCREMENT,
    id_paciente INT NOT NULL,
    id_personal INT NOT NULL,
    fechahora_consulta TIMESTAMP NOT NULL,
    fechafin_consulta TIMESTAMP NULL,
    estatus_consulta VARCHAR(50) DEFAULT 'PENDIENTE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_paciente) REFERENCES paciente(id_paciente) ON DELETE CASCADE,
    FOREIGN KEY (id_personal) REFERENCES personal(id_personal) ON DELETE RESTRICT,
    INDEX idx_id_paciente (id_paciente),
    INDEX idx_id_personal (id_personal),
    INDEX idx_fechahora_consulta (fechahora_consulta),
    INDEX idx_estatus_consulta (estatus_consulta)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: evaluacion
CREATE TABLE evaluacion (
    id_evaluacion INT PRIMARY KEY AUTO_INCREMENT,
    id_consulta INT NOT NULL,
    nombre_evaluacion VARCHAR(100) NOT NULL,
    area_evaluacion VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_consulta) REFERENCES consulta(id_consulta) ON DELETE CASCADE,
    INDEX idx_id_consulta (id_consulta)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: evaluacion_pregunta
CREATE TABLE evaluacion_pregunta (
    id_evaluacion_pregunta INT PRIMARY KEY AUTO_INCREMENT,
    texto_evaluacion_pregunta TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: evaluacion_respuesta
CREATE TABLE evaluacion_respuesta (
    id_evaluacion_respuesta INT PRIMARY KEY AUTO_INCREMENT,
    id_evaluacion_pregunta INT NOT NULL,
    texto_evaluacion_respuesta TEXT NOT NULL,
    texto_set_evaluacion_respuesta TEXT,
    label_evaluacion_respuesta VARCHAR(50),
    confidence_score DOUBLE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_evaluacion_pregunta) REFERENCES evaluacion_pregunta(id_evaluacion_pregunta) ON DELETE CASCADE,
    INDEX idx_id_evaluacion_pregunta (id_evaluacion_pregunta),
    INDEX idx_label_evaluacion_respuesta (label_evaluacion_respuesta)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: reporte
CREATE TABLE reporte (
    id_reporte INT PRIMARY KEY AUTO_INCREMENT,
    id_usuario INT NOT NULL,
    id_evaluacion INT NOT NULL,
    fechageneracion_reporte TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    nombre_reporte VARCHAR(100) NOT NULL,
    resultado_reporte TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    FOREIGN KEY (id_evaluacion) REFERENCES evaluacion(id_evaluacion) ON DELETE CASCADE,
    INDEX idx_id_usuario (id_usuario),
    INDEX idx_id_evaluacion (id_evaluacion),
    INDEX idx_fechageneracion_reporte (fechageneracion_reporte)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 12.2. V2__insert_master_data.sql
```sql
-- filepath: src/main/resources/db/migration/V2__insert_master_data.sql

-- Insertar roles predefinidos
INSERT INTO usuario_roles (permisos_roles) VALUES
('ADMIN'),
('DOCTOR'),
('ENFERMERO'),
('RECEPCIONISTA'),
('ANALISTA');

-- Insertar estados de consulta
INSERT INTO consulta_estatus (nombre_consulta_estatus) VALUES
('PENDIENTE'),
('EN_PROGRESO'),
('COMPLETADA'),
('CANCELADA'),
('REPROGRAMADA');

-- Insertar usuario administrador (password: admin123 - debe ser hasheado en producción)
INSERT INTO usuario (nombre_usuario, pass_usuario) VALUES
('admin', '$2a$10$slYQmyNdGzTn7ZLBXBChFOR.vY0QJLx2z9q3OXZ2.rLQfKVRKxTZ6');

-- Asignar rol ADMIN al usuario admin
INSERT INTO usuario_roles_mapping (id_usuario, id_roles)
SELECT u.id_usuario, r.id_roles
FROM usuario u, usuario_roles r
WHERE u.nombre_usuario = 'admin' AND r.permisos_roles = 'ADMIN';

-- Datos de ejemplo: Personal
INSERT INTO personal (doc_personal, nombre_personal, estatus_personal) VALUES
('DOC-001', 'Dra. María García López', 'ACTIVO'),
('DOC-002', 'Dr. Juan Martínez Pérez', 'ACTIVO'),
('ENF-001', 'Enfermera Ana Rodríguez', 'ACTIVO');

-- Preguntas de evaluación estándar
INSERT INTO evaluacion_pregunta (texto_evaluacion_pregunta) VALUES
('¿Cómo se siente hoy?'),
('¿Ha experimentado cambios en su estado de ánimo recientemente?'),
('¿Tiene pensamientos que le preocupan?'),
('¿Cómo describiría su nivel de energía?'),
('¿Ha tenido problemas para dormir?'),
('¿Se siente ansioso o nervioso?'),
('¿Ha experimentado cambios en su apetito?'),
('¿Cómo es su relación con las personas cercanas?'),
('¿Tiene dificultades para concentrarse?'),
('¿Ha tenido pensamientos de hacerse daño?');
```

### 12.3. V3__create_indexes.sql
```sql
-- filepath: src/main/resources/db/migration/V3__create_indexes.sql

-- Índices adicionales para optimización de consultas frecuentes

-- Índice compuesto para búsqueda de consultas por paciente y estado
CREATE INDEX idx_consulta_paciente_estatus 
ON consulta(id_paciente, estatus_consulta);

-- Índice compuesto para búsqueda de consultas por personal y fecha
CREATE INDEX idx_consulta_personal_fecha 
ON consulta(id_personal, fechahora_consulta);

-- Índice para búsqueda de respuestas por label y confianza
CREATE INDEX idx_respuesta_label_confidence 
ON evaluacion_respuesta(label_evaluacion_respuesta, confidence_score);

-- Índice para reportes por usuario y fecha
CREATE INDEX idx_reporte_usuario_fecha 
ON reporte(id_usuario, fechageneracion_reporte);

-- Índice de texto completo para búsqueda en respuestas (MySQL 5.7+)
CREATE FULLTEXT INDEX idx_fulltext_respuesta 
ON evaluacion_respuesta(texto_evaluacion_respuesta);

-- Índice para búsqueda de pacientes activos
CREATE INDEX idx_paciente_estatus_nombre 
ON paciente(estatus_paciente, nombre_paciente);
```

## 13. Actualización de Dependencias Maven

Añadir al pom.xml existente:

```xml
<!-- Spring Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- MySQL Connector -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
    <scope>runtime</scope>
</dependency>

<!-- Flyway Core -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>

<!-- Flyway MySQL -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>

<!-- MapStruct -->
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>

<!-- H2 Database (para testing) -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>

<!-- iText PDF (para generación de reportes) -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext7-core</artifactId>
    <version>7.2.5</version>
    <type>pom</type>
</dependency>
```

## 14. Instrucciones de Setup de Base de Datos

### 14.1. Crear Base de Datos MySQL

```sql
-- Conectar a MySQL
mysql -u root -p

-- Crear base de datos
CREATE DATABASE rntn_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Crear usuario para la aplicación
CREATE USER 'rntn_user'@'localhost' IDENTIFIED BY 'rntn_password';
GRANT ALL PRIVILEGES ON rntn_db.* TO 'rntn_user'@'localhost';
FLUSH PRIVILEGES;

-- Verificar creación
SHOW DATABASES;
USE rntn_db;
```

### 14.2. Configurar Variables de Entorno

Crear archivo `.env` en la raíz del proyecto:

```properties
DB_HOST=localhost
DB_PORT=3306
DB_NAME=rntn_db
DB_USER=rntn_user
DB_PASSWORD=rntn_password
```

### 14.3. Ejecutar Migraciones

Las migraciones Flyway se ejecutarán automáticamente al iniciar la aplicación Spring Boot.

Para ejecutar manualmente:

```bash
mvn flyway:migrate
```

Para limpiar la base de datos (solo desarrollo):

```bash
mvn flyway:clean
```

## 15. Puntos Clave de Integración

### 15.1. Integración con SentimentPredictor

Al guardar una respuesta de evaluación, el sistema:
1. Recibe el texto de la respuesta
2. Lo procesa con `SentimentPredictor.predictClass()`
3. Obtiene el label y confidence
4. Guarda en `evaluacion_respuesta` con el label y confidence

### 15.2. Generación de Reportes

Cuando se genera un reporte:
1. Se obtienen todas las respuestas de una evaluación
2. Se analizan los labels predominantes
3. Se calcula el nivel de riesgo basado en:
   - SUICIDAL: Riesgo ALTO
   - ANXIETY, ANGER, SADNESS: Riesgo MEDIO
   - FRUSTRATION: Riesgo BAJO
4. Se genera recomendaciones automáticas
5. Se guarda el reporte con el resultado en formato JSON

### 15.3. Alertas Automáticas

El sistema debe generar alertas cuando:
- Se detecta label "SUICIDAL" con confidence > 0.7
- Se detectan múltiples respuestas con labels de riesgo alto
- Cambios significativos en patrones de sentimientos entre consultas

---

**FIN DEL DOCUMENTO DE REFACTORIZACIÓN**

Este documento proporciona una guía completa para:
✅ Convertir el proyecto CLI en REST API
✅ Integrar capa de persistencia MySQL
✅ Implementar CRUD completo para todas las entidades
✅ Mantener funcionalidad de análisis de sentimientos
✅ Añadir sistema de reportes y alertas
✅ Seguir mejores prácticas de arquitectura Spring Boot
