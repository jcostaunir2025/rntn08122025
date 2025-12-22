# Diagrama Entidad-Relación - RNTN Sentiment API

Este documento contiene el diagrama ER completo del sistema en formato Mermaid para visualización en GitHub y otras plataformas compatibles.

---

## Diagrama ER Completo

```mermaid
erDiagram
    PACIENTE ||--o{ CONSULTA : "tiene"
    PERSONAL ||--o{ CONSULTA : "atiende"
    CONSULTA ||--o{ EVALUACION : "contiene"
    EVALUACION ||--o{ REPORTE : "genera"
    USUARIO ||--o{ REPORTE : "crea"
    USUARIO }o--o{ USUARIO_ROLES : "tiene"
    EVALUACION_PREGUNTA ||--o{ EVALUACION_RESPUESTA : "tiene"
    
    PACIENTE {
        int id_paciente PK
        string doc_paciente UK
        string nombre_paciente
        string direccion_paciente
        string email_paciente
        string telefono_paciente
        string estatus_paciente
        timestamp created_at
        timestamp updated_at
    }
    
    PERSONAL {
        int id_personal PK
        string doc_personal UK
        string nombre_personal
        string estatus_personal
        timestamp created_at
        timestamp updated_at
    }
    
    USUARIO {
        int id_usuario PK
        string nombre_usuario UK
        string pass_usuario
        timestamp created_at
        timestamp updated_at
    }
    
    USUARIO_ROLES {
        int id_roles PK
        string permisos_roles
    }
    
    CONSULTA {
        int id_consulta PK
        int id_paciente FK
        int id_personal FK
        timestamp fechahora_consulta
        timestamp fechafin_consulta
        string estatus_consulta
        timestamp created_at
        timestamp updated_at
    }
    
    CONSULTA_ESTATUS {
        int id_consulta_estatus PK
        string nombre_consulta_estatus UK
    }
    
    EVALUACION {
        int id_evaluacion PK
        int id_consulta FK
        string nombre_evaluacion
        string area_evaluacion
        timestamp created_at
        timestamp updated_at
    }
    
    EVALUACION_PREGUNTA {
        int id_evaluacion_pregunta PK
        text texto_evaluacion_pregunta
        timestamp created_at
    }
    
    EVALUACION_RESPUESTA {
        int id_evaluacion_respuesta PK
        int id_evaluacion_pregunta FK
        text texto_evaluacion_respuesta
        text texto_set_evaluacion_respuesta
        string label_evaluacion_respuesta "RNTN_LABEL"
        double confidence_score "RNTN_CONFIDENCE"
        timestamp created_at
    }
    
    REPORTE {
        int id_reporte PK
        int id_usuario FK
        int id_evaluacion FK
        timestamp fechageneracion_reporte
        string nombre_reporte
        text resultado_reporte "JSON"
        timestamp created_at
        timestamp updated_at
    }
```

---

## Diagrama de Flujo: Análisis de Sentimiento

```mermaid
flowchart TD
    A[Cliente REST API] -->|POST /evaluaciones/respuestas| B[EvaluacionController]
    B --> C[EvaluacionService]
    C --> D{¿Analizar sentimiento?}
    D -->|Sí| E[SentimentService]
    D -->|No| F[Guardar sin análisis]
    E --> G[SentimentPredictor<br/>RNTN Model]
    G --> H[Predecir Clase 0-4]
    H --> I[Mapear a Label<br/>ANXIETY, SUICIDAL, etc]
    I --> J[Calcular Confidence]
    J --> K{¿Riesgo Alto?}
    K -->|SUICIDAL + confidence > 0.7| L[⚠️ Generar Alerta]
    K -->|No| M[Continuar]
    L --> N[EvaluacionRespuestaRepository]
    M --> N
    F --> N
    N --> O[(MySQL Database)]
    O --> P[Response con análisis]
    P --> A
    
    style E fill:#ff9999
    style G fill:#99ccff
    style L fill:#ff6666
    style O fill:#99ff99
```

---

## Diagrama de Secuencia: Registro de Respuesta con Análisis

```mermaid
sequenceDiagram
    participant Client
    participant Controller as EvaluacionController
    participant Service as EvaluacionService
    participant Sentiment as SentimentService
    participant Predictor as SentimentPredictor
    participant Repo as Repository
    participant DB as MySQL

    Client->>Controller: POST /evaluaciones/respuestas
    activate Controller
    
    Controller->>Service: registrarRespuestaConAnalisis(request)
    activate Service
    
    Service->>Repo: findById(idPregunta)
    Repo->>DB: SELECT * FROM evaluacion_pregunta
    DB-->>Repo: Pregunta
    Repo-->>Service: EvaluacionPregunta
    
    alt Analizar Sentimiento
        Service->>Sentiment: analizarTexto(texto)
        activate Sentiment
        
        Sentiment->>Predictor: predictClass(texto)
        activate Predictor
        Note over Predictor: Carga modelo RNTN<br/>Procesa con CoreNLP<br/>Predice clase
        Predictor-->>Sentiment: predictedClass (0-4)
        deactivate Predictor
        
        Sentiment->>Sentiment: Mapear a Label
        Note over Sentiment: 0→ANXIETY<br/>1→SUICIDAL<br/>2→ANGER<br/>3→SADNESS<br/>4→FRUSTRATION
        
        Sentiment->>Sentiment: calcularConfidence()
        Sentiment->>Sentiment: determinarNivelRiesgo()
        
        Sentiment-->>Service: AnalisisSentimientoResponse
        deactivate Sentiment
        
        alt Riesgo Alto Detectado
            Service->>Service: ⚠️ Log Alerta SUICIDAL
            Note over Service: TODO: Enviar notificación
        end
    end
    
    Service->>Service: Crear EvaluacionRespuesta
    Note over Service: Incluir label y confidence
    
    Service->>Repo: save(respuesta)
    Repo->>DB: INSERT INTO evaluacion_respuesta
    DB-->>Repo: Respuesta guardada
    Repo-->>Service: EvaluacionRespuesta
    
    Service->>Service: Mapear a DTO
    Service-->>Controller: EvaluacionRespuestaResponse
    deactivate Service
    
    Controller-->>Client: 201 Created + Response JSON
    deactivate Controller
```

---

## Diagrama de Componentes: Arquitectura por Capas

```mermaid
graph TB
    subgraph "Capa de Presentación"
        PC[PacienteController]
        CC[ConsultaController]
        EC[EvaluacionController]
        RC[ReporteController]
    end
    
    subgraph "Capa de Negocio"
        PS[PacienteService]
        CS[ConsultaService]
        ES[EvaluacionService]
        SS[SentimentService]
        RS[ReporteService]
        
        SS --> SP[SentimentPredictor<br/>RNTN Model]
    end
    
    subgraph "Capa de Persistencia"
        PR[PacienteRepository]
        CR[ConsultaRepository]
        ER[EvaluacionRepository]
        ERR[EvaluacionRespuestaRepo]
        RR[ReporteRepository]
    end
    
    subgraph "Base de Datos"
        DB[(MySQL 8.0<br/>rntn_db)]
    end
    
    PC --> PS
    CC --> CS
    EC --> ES
    RC --> RS
    
    PS --> PR
    CS --> CR
    ES --> ER
    ES --> ERR
    ES --> SS
    RS --> RR
    
    PR --> DB
    CR --> DB
    ER --> DB
    ERR --> DB
    RR --> DB
    
    style SS fill:#ff9999
    style SP fill:#99ccff
    style DB fill:#99ff99
```

---

## Diagrama de Actividades: Generación de Reporte

```mermaid
flowchart TD
    A[Inicio: Generar Reporte] --> B[Obtener Evaluación por ID]
    B --> C[Obtener todas las Respuestas]
    C --> D{¿Respuestas con análisis?}
    D -->|No| E[Error: Sin datos para analizar]
    D -->|Sí| F[Calcular Distribución<br/>de Sentimientos]
    F --> G[Contar por Label:<br/>ANXIETY, SUICIDAL, etc]
    G --> H[Determinar<br/>Sentimiento Dominante]
    H --> I[Calcular Nivel de Riesgo]
    
    I --> J{¿Contiene SUICIDAL?}
    J -->|Sí| K[Riesgo = ALTO]
    J -->|No| L{¿> 50% sentimientos negativos?}
    L -->|Sí| M[Riesgo = MEDIO]
    L -->|No| N[Riesgo = BAJO]
    
    K --> O[Detectar Alertas Específicas]
    M --> O
    N --> O
    
    O --> P[Generar Recomendaciones<br/>Automáticas]
    P --> Q[Crear JSON de Resultado]
    Q --> R[Crear Entidad Reporte]
    R --> S[Guardar en Base de Datos]
    S --> T{¿Exportar PDF?}
    T -->|Sí| U[Generar PDF con iText]
    T -->|No| V[Fin: Retornar Response]
    U --> V
    E --> W[Fin: Error]
    
    style K fill:#ff6666
    style M fill:#ffcc66
    style N fill:#66ff66
```

---

## Diagrama de Estados: Consulta

```mermaid
stateDiagram-v2
    [*] --> PENDIENTE: Crear consulta
    PENDIENTE --> EN_PROGRESO: Iniciar consulta
    EN_PROGRESO --> COMPLETADA: Finalizar consulta
    EN_PROGRESO --> CANCELADA: Cancelar
    PENDIENTE --> CANCELADA: Cancelar
    PENDIENTE --> REPROGRAMADA: Reprogramar
    REPROGRAMADA --> PENDIENTE: Confirmar nueva fecha
    COMPLETADA --> [*]
    CANCELADA --> [*]
    
    COMPLETADA: Se pueden crear evaluaciones
    COMPLETADA: Se pueden generar reportes
```

---

## Relaciones y Cardinalidades

| Relación | Cardinalidad | Descripción |
|----------|--------------|-------------|
| **PACIENTE ← CONSULTA** | 1:N | Un paciente puede tener múltiples consultas |
| **PERSONAL ← CONSULTA** | 1:N | Un personal atiende múltiples consultas |
| **CONSULTA ← EVALUACION** | 1:N | Una consulta puede tener múltiples evaluaciones |
| **EVALUACION_PREGUNTA ← EVALUACION_RESPUESTA** | 1:N | Una pregunta tiene múltiples respuestas |
| **EVALUACION ← REPORTE** | 1:N | Una evaluación puede generar múltiples reportes |
| **USUARIO ← REPORTE** | 1:N | Un usuario crea múltiples reportes |
| **USUARIO ↔ USUARIO_ROLES** | N:M | Relación muchos a muchos (tabla intermedia) |

---

## Índices Importantes

```sql
-- Performance crítica
CREATE INDEX idx_consulta_paciente_estatus ON consulta(id_paciente, estatus_consulta);
CREATE INDEX idx_respuesta_label_confidence ON evaluacion_respuesta(label_evaluacion_respuesta, confidence_score);

-- Búsquedas frecuentes
CREATE INDEX idx_consulta_personal_fecha ON consulta(id_personal, fechahora_consulta);
CREATE INDEX idx_reporte_usuario_fecha ON reporte(id_usuario, fechageneracion_reporte);

-- Fulltext search
CREATE FULLTEXT INDEX idx_fulltext_respuesta ON evaluacion_respuesta(texto_evaluacion_respuesta);
```

---

## Vistas Útiles (Opcional)

### Vista: Distribución de Sentimientos por Paciente

```sql
CREATE VIEW v_sentimientos_paciente AS
SELECT 
    p.id_paciente,
    p.nombre_paciente,
    er.label_evaluacion_respuesta,
    COUNT(*) as total,
    AVG(er.confidence_score) as confianza_promedio
FROM paciente p
JOIN consulta c ON p.id_paciente = c.id_paciente
JOIN evaluacion e ON c.id_consulta = e.id_consulta
-- Aquí se necesitaría una tabla de relación evaluacion_respuesta -> evaluacion
GROUP BY p.id_paciente, p.nombre_paciente, er.label_evaluacion_respuesta;
```

### Vista: Alertas de Riesgo Alto

```sql
CREATE VIEW v_alertas_riesgo_alto AS
SELECT 
    p.id_paciente,
    p.nombre_paciente,
    p.telefono_paciente,
    c.id_consulta,
    c.fechahora_consulta,
    per.nombre_personal,
    er.texto_evaluacion_respuesta,
    er.label_evaluacion_respuesta,
    er.confidence_score
FROM evaluacion_respuesta er
JOIN evaluacion_pregunta ep ON er.id_evaluacion_pregunta = ep.id_evaluacion_pregunta
-- Relaciones a completar según modelo final
WHERE er.label_evaluacion_respuesta = 'SUICIDAL'
    AND er.confidence_score > 0.7
ORDER BY er.confidence_score DESC, c.fechahora_consulta DESC;
```

---

## Triggers Útiles (Opcional)

### Trigger: Notificar Riesgo Alto

```sql
DELIMITER $$

CREATE TRIGGER trg_alerta_riesgo_suicida
AFTER INSERT ON evaluacion_respuesta
FOR EACH ROW
BEGIN
    IF NEW.label_evaluacion_respuesta = 'SUICIDAL' AND NEW.confidence_score > 0.7 THEN
        -- Insertar en tabla de alertas
        INSERT INTO alertas (tipo, nivel, id_respuesta, fecha_alerta)
        VALUES ('RIESGO_SUICIDA', 'ALTO', NEW.id_evaluacion_respuesta, NOW());
        
        -- TODO: Llamar procedimiento de notificación externa
    END IF;
END$$

DELIMITER ;
```

---

## Stored Procedures Útiles (Opcional)

### Procedimiento: Obtener Dashboard de Consulta

```sql
DELIMITER $$

CREATE PROCEDURE sp_dashboard_consulta(IN p_id_consulta INT)
BEGIN
    -- Información básica de consulta
    SELECT 
        c.id_consulta,
        p.nombre_paciente,
        per.nombre_personal,
        c.fechahora_consulta,
        c.estatus_consulta
    FROM consulta c
    JOIN paciente p ON c.id_paciente = p.id_paciente
    JOIN personal per ON c.id_personal = per.id_personal
    WHERE c.id_consulta = p_id_consulta;
    
    -- Distribución de sentimientos
    -- (Query a completar según relaciones finales)
    
    -- Alertas activas
    -- (Query a completar)
END$$

DELIMITER ;
```

---

## Formato JSON del campo `resultado_reporte`

Ejemplo de estructura JSON almacenada en `reporte.resultado_reporte`:

```json
{
  "paciente": "Juan Pérez",
  "fecha": "2025-12-21",
  "evaluacion": "Evaluación Inicial",
  "totalRespuestas": 10,
  "sentimientoDominante": "ANXIETY",
  "distribucion": {
    "ANXIETY": 3,
    "SUICIDAL": 1,
    "ANGER": 2,
    "SADNESS": 3,
    "FRUSTRATION": 1
  },
  "nivelRiesgo": "ALTO",
  "alertas": [
    {
      "tipo": "RIESGO_SUICIDA",
      "severidad": "ALTA",
      "respuesta": "A veces pienso que no tiene sentido seguir",
      "confidence": 0.87
    }
  ],
  "recomendaciones": [
    "⚠️ URGENTE: Contactar inmediatamente con el paciente",
    "Programar seguimiento diario",
    "Considerar derivación a psiquiatría de urgencia"
  ],
  "estadisticas": {
    "confidencePromedio": 0.84,
    "respuestasAnalizadas": 10,
    "respuestasSinAnalisis": 0
  }
}
```

---

## Visualización del Modelo en Herramientas

### MySQL Workbench
1. Abrir MySQL Workbench
2. Database → Reverse Engineer
3. Seleccionar base de datos `rntn_db`
4. Generar diagrama ER automáticamente

### DBeaver
1. Conectar a base de datos
2. Click derecho en `rntn_db` → ER Diagram
3. Ver diagrama interactivo

### dbdiagram.io
Importar el siguiente código DBML:

```dbml
Table paciente {
  id_paciente int [pk, increment]
  doc_paciente varchar(20) [unique, not null]
  nombre_paciente varchar(100) [not null]
  direccion_paciente varchar(255)
  email_paciente varchar(100)
  telefono_paciente varchar(20)
  estatus_paciente varchar(20) [default: 'ACTIVO']
  created_at timestamp
  updated_at timestamp
}

Table personal {
  id_personal int [pk, increment]
  doc_personal varchar(20) [unique, not null]
  nombre_personal varchar(100) [not null]
  estatus_personal varchar(20)
  created_at timestamp
  updated_at timestamp
}

Table consulta {
  id_consulta int [pk, increment]
  id_paciente int [ref: > paciente.id_paciente]
  id_personal int [ref: > personal.id_personal]
  fechahora_consulta timestamp
  fechafin_consulta timestamp
  estatus_consulta varchar(50)
  created_at timestamp
  updated_at timestamp
}

Table evaluacion {
  id_evaluacion int [pk, increment]
  id_consulta int [ref: > consulta.id_consulta]
  nombre_evaluacion varchar(100)
  area_evaluacion varchar(100)
  created_at timestamp
  updated_at timestamp
}

Table evaluacion_respuesta {
  id_evaluacion_respuesta int [pk, increment]
  id_evaluacion_pregunta int
  texto_evaluacion_respuesta text
  label_evaluacion_respuesta varchar(50) [note: 'RNTN Label']
  confidence_score double [note: 'RNTN Confidence']
  created_at timestamp
}
```

---

**Documento generado el 21 de Diciembre de 2025**  
**Versión: 1.0 - Diagramas Completos**

