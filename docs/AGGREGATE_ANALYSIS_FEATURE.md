# 🎯 Aggregate Sentiment Analysis - Implementation Guide

## Overview

This document describes the new **Aggregate Sentiment Analysis** feature implemented in the RNTN Sentiment API. This feature combines real-time batch predictions with database-backed statistical analysis using stored procedures.

**Implementation Date:** 2025-12-21  
**Version:** 1.0.0  
**Status:** ✅ Implemented and Tested

---

## 📋 Table of Contents

1. [Features Overview](#features-overview)
2. [Architecture](#architecture)
3. [Database Layer](#database-layer)
4. [API Endpoints](#api-endpoints)
5. [Usage Examples](#usage-examples)
6. [Integration Guide](#integration-guide)
7. [Performance Considerations](#performance-considerations)

---

## 🎯 Features Overview

### What Was Implemented

The aggregate analysis feature provides **three complementary approaches** for analyzing sentiment data:

#### 1. **In-Memory Aggregation** (Real-time)
- Calculates statistics from batch predictions in real-time
- No database interaction required
- Ideal for: Live analysis, quick insights, external data

#### 2. **Database-Backed Aggregation** (Stored Procedures)
- Leverages MySQL stored procedures for optimized queries
- Works with persisted EvaluacionRespuesta records
- Ideal for: Reports, dashboards, historical analysis

#### 3. **High-Risk Alert System**
- Proactive detection of critical cases (SUICIDAL with high confidence)
- Temporal filtering (last N days)
- Includes full patient and professional context

---

## 🏗️ Architecture

### Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    SentimentController                      │
│  - POST /predict/batch/aggregate                            │
│  - POST /aggregate/stats                                    │
│  - GET  /aggregate/evaluation/{id}                          │
│  - GET  /alerts/high-risk                                   │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                    SentimentService                         │
│  - calcularEstadisticasAgregadas()      [In-Memory]        │
│  - obtenerEstadisticasAgregadasDesdeBD() [DB-Backed]       │
│  - obtenerDistribucionPorEvaluacion()    [DB-Backed]       │
│  - obtenerAlertasAltoRiesgo()            [DB-Backed]       │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│              EvaluacionRespuestaRepository                  │
│  - getAggregateStats(ids)              [SP Call]           │
│  - getDistributionByEvaluation(id)     [SP Call]           │
│  - getHighRiskAlerts(days)             [SP Call]           │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                    MySQL Database                           │
│  - sp_get_sentiment_aggregate_stats                         │
│  - sp_get_sentiment_distribution_by_evaluation              │
│  - sp_get_high_risk_alerts                                  │
└─────────────────────────────────────────────────────────────┘
```

---

## 🗄️ Database Layer

### Stored Procedures

#### 1. `sp_get_sentiment_aggregate_stats`

**Purpose:** Calculate aggregate statistics for a set of response IDs

**Input:**
- `p_respuesta_ids TEXT` - Comma-separated IDs: "1,2,3,4,5"

**Output:**
```sql
{
  total_respuestas: INT,
  count_anxiety: INT,
  count_suicidal: INT,
  count_anger: INT,
  count_sadness: INT,
  count_frustration: INT,
  avg_confidence: DECIMAL,
  min_confidence: DECIMAL,
  max_confidence: DECIMAL,
  dominant_sentiment: VARCHAR,
  highest_risk_level: VARCHAR,
  high_risk_alerts: INT,
  calculated_at: DATETIME
}
```

**Example Usage:**
```sql
CALL sp_get_sentiment_aggregate_stats('1,2,3,4,5,6,7,8,9,10');
```

---

#### 2. `sp_get_sentiment_distribution_by_evaluation`

**Purpose:** Get sentiment distribution for a complete evaluation session

**Input:**
- `p_id_evaluacion BIGINT` - Evaluation ID

**Output:**
```sql
{
  id_evaluacion: BIGINT,
  fecha_evaluacion: DATETIME,
  nombre_paciente: VARCHAR,
  nombre_profesional: VARCHAR,
  total_respuestas: INT,
  count_anxiety: INT,
  count_suicidal: INT,
  count_anger: INT,
  count_sadness: INT,
  count_frustration: INT,
  avg_confidence: DECIMAL,
  dominant_sentiment: VARCHAR,
  high_risk_alerts: INT
}
```

**Example Usage:**
```sql
CALL sp_get_sentiment_distribution_by_evaluation(1);
```

---

#### 3. `sp_get_high_risk_alerts`

**Purpose:** Find high-risk responses (SUICIDAL with confidence > 0.7) in recent days

**Input:**
- `p_days_back INT` - Days to look back (e.g., 7 for last week)

**Output:**
```sql
{
  id_evaluacion_respuesta: BIGINT,
  texto_evaluacion_respuesta: TEXT,
  label_evaluacion_respuesta: VARCHAR,
  confidence_score: DECIMAL,
  nivel_riesgo: VARCHAR,
  fecha_respuesta: DATETIME,
  id_paciente: BIGINT,
  nombre_paciente: VARCHAR,
  telefono_paciente: VARCHAR,
  email_paciente: VARCHAR,
  id_consulta: BIGINT,
  fechahora_consulta: DATETIME,
  id_personal: BIGINT,
  nombre_personal: VARCHAR,
  email_personal: VARCHAR,
  id_evaluacion: BIGINT,
  titulo_evaluacion: VARCHAR,
  fecha_evaluacion: DATETIME,
  texto_pregunta: TEXT
}
```

**Example Usage:**
```sql
CALL sp_get_high_risk_alerts(7);  -- Last 7 days
```

---

### Database Indexes

The migration also creates optimized indexes:

```sql
-- For label and confidence searches (alerts)
CREATE INDEX idx_evaluacion_respuesta_label_confidence 
ON evaluacion_respuesta(label_evaluacion_respuesta, confidence_score);

-- For temporal queries
CREATE INDEX idx_evaluacion_respuesta_created_at 
ON evaluacion_respuesta(created_at);

-- For aggregate analysis
CREATE INDEX idx_evaluacion_respuesta_label_riesgo 
ON evaluacion_respuesta(label_evaluacion_respuesta, nivel_riesgo);
```

---

## 🌐 API Endpoints

### 1. Batch Predict with Aggregate Analysis

**Endpoint:** `POST /api/v1/sentiment/predict/batch/aggregate`

**Description:** Analyzes multiple texts and returns both individual results and aggregate statistics.

**Request:**
```json
{
  "texts": [
    "I feel very anxious lately",
    "I am so angry at everything",
    "This situation makes me sad",
    "I want to die",
    "I'm just frustrated"
  ]
}
```

**Response:**
```json
{
  "individualResults": [
    {
      "texto": "I feel very anxious lately",
      "predictedClass": 0,
      "predictedLabel": "ANXIETY",
      "confidence": 0.92,
      "nivelRiesgo": "MEDIO",
      "timestamp": "2025-12-21T17:30:00"
    },
    {
      "texto": "I am so angry at everything",
      "predictedClass": 2,
      "predictedLabel": "ANGER",
      "confidence": 0.89,
      "nivelRiesgo": "MEDIO",
      "timestamp": "2025-12-21T17:30:00"
    },
    {
      "texto": "This situation makes me sad",
      "predictedClass": 3,
      "predictedLabel": "SADNESS",
      "confidence": 0.85,
      "nivelRiesgo": "MEDIO",
      "timestamp": "2025-12-21T17:30:00"
    },
    {
      "texto": "I want to die",
      "predictedClass": 1,
      "predictedLabel": "SUICIDAL",
      "confidence": 0.95,
      "nivelRiesgo": "ALTO",
      "timestamp": "2025-12-21T17:30:00"
    },
    {
      "texto": "I'm just frustrated",
      "predictedClass": 4,
      "predictedLabel": "FRUSTRATION",
      "confidence": 0.78,
      "nivelRiesgo": "BAJO",
      "timestamp": "2025-12-21T17:30:00"
    }
  ],
  "aggregateAnalysis": {
    "sentimentDistribution": {
      "ANXIETY": 1,
      "SUICIDAL": 1,
      "ANGER": 1,
      "SADNESS": 1,
      "FRUSTRATION": 1
    },
    "dominantSentiment": "ANXIETY",
    "averageConfidence": 0.878,
    "minConfidence": 0.78,
    "maxConfidence": 0.95,
    "highestRiskLevel": "ALTO",
    "highRiskAlerts": 1,
    "totalResponses": 5
  },
  "processedCount": 5,
  "timestamp": "2025-12-21T17:30:00",
  "savedResponseIds": null
}
```

**Use Cases:**
- ✅ Real-time analysis of evaluation sessions
- ✅ Quick insights without database persistence
- ✅ External data analysis
- ✅ Batch processing from files

---

### 2. Get Aggregate Stats from Database

**Endpoint:** `POST /api/v1/sentiment/aggregate/stats`

**Description:** Calculates aggregate statistics for responses already saved in the database using stored procedures.

**Request:**
```json
[12, 15, 18, 21, 24, 27, 30, 33, 36, 39]
```

**Response:**
```json
{
  "sentimentDistribution": {
    "ANXIETY": 3,
    "SUICIDAL": 1,
    "ANGER": 2,
    "SADNESS": 3,
    "FRUSTRATION": 1
  },
  "dominantSentiment": "ANXIETY",
  "averageConfidence": 0.865,
  "minConfidence": 0.75,
  "maxConfidence": 0.95,
  "highestRiskLevel": "ALTO",
  "highRiskAlerts": 1,
  "totalResponses": 10
}
```

**Use Cases:**
- ✅ Generate reports from historical data
- ✅ Dashboard statistics
- ✅ Patient progress analysis over time
- ✅ Optimized database-level calculations

---

### 3. Get Distribution by Evaluation

**Endpoint:** `GET /api/v1/sentiment/aggregate/evaluation/{idEvaluacion}`

**Description:** Gets complete sentiment distribution for an evaluation session, including patient and professional info.

**Request:**
```bash
GET /api/v1/sentiment/aggregate/evaluation/5
```

**Response:**
```json
{
  "id_evaluacion": 5,
  "fecha_evaluacion": "2025-12-20T10:00:00",
  "nombre_paciente": "Juan Pérez García",
  "nombre_profesional": "Dra. María González",
  "total_respuestas": 10,
  "count_anxiety": 3,
  "count_suicidal": 1,
  "count_anger": 2,
  "count_sadness": 3,
  "count_frustration": 1,
  "avg_confidence": 0.865,
  "dominant_sentiment": "ANXIETY",
  "high_risk_alerts": 1
}
```

**Use Cases:**
- ✅ Session summary for clinicians
- ✅ Post-evaluation reports
- ✅ Patient progress tracking
- ✅ Clinical decision support

---

### 4. Get High-Risk Alerts

**Endpoint:** `GET /api/v1/sentiment/alerts/high-risk?daysBack=7`

**Description:** Retrieves all high-risk responses (SUICIDAL with confidence > 0.7) from recent days with full context.

**Request:**
```bash
GET /api/v1/sentiment/alerts/high-risk?daysBack=7
```

**Response:**
```json
{
  "alerts": [
    {
      "id_evaluacion_respuesta": 42,
      "texto_evaluacion_respuesta": "I don't want to live anymore",
      "label_evaluacion_respuesta": "SUICIDAL",
      "confidence_score": 0.95,
      "nivel_riesgo": "ALTO",
      "fecha_respuesta": "2025-12-20T14:30:00",
      "id_paciente": 8,
      "nombre_paciente": "Juan Pérez García",
      "telefono_paciente": "+34 666 777 888",
      "email_paciente": "juan.perez@example.com",
      "id_consulta": 12,
      "fechahora_consulta": "2025-12-20T14:00:00",
      "id_personal": 3,
      "nombre_personal": "Dra. María González",
      "email_personal": "maria.gonzalez@hospital.com",
      "id_evaluacion": 5,
      "titulo_evaluacion": "Evaluación de Depresión",
      "fecha_evaluacion": "2025-12-20T14:00:00",
      "texto_pregunta": "¿Has tenido pensamientos de hacerte daño?"
    }
  ],
  "totalAlerts": 1,
  "daysBack": 7,
  "timestamp": "2025-12-21T17:30:00"
}
```

**Use Cases:**
- ✅ **CRITICAL:** Patient safety monitoring
- ✅ Emergency response coordination
- ✅ Follow-up prioritization
- ✅ Quality assurance audits

---

## 💻 Usage Examples

### Example 1: Real-time Batch Analysis

```bash
curl -X POST http://localhost:8080/api/v1/sentiment/predict/batch/aggregate \
  -H "Content-Type: application/json" \
  -d '{
    "texts": [
      "I feel anxious all the time",
      "Everything makes me angry",
      "I am so sad lately"
    ]
  }'
```

### Example 2: Generate Report from Saved Responses

```bash
curl -X POST http://localhost:8080/api/v1/sentiment/aggregate/stats \
  -H "Content-Type: application/json" \
  -d '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10]'
```

### Example 3: Evaluation Summary

```bash
curl -X GET http://localhost:8080/api/v1/sentiment/aggregate/evaluation/5
```

### Example 4: Check Recent Alerts

```bash
curl -X GET "http://localhost:8080/api/v1/sentiment/alerts/high-risk?daysBack=7"
```

---

## 🔧 Integration Guide

### Frontend Integration

#### React/TypeScript Example

```typescript
// services/sentimentService.ts
export interface AggregateAnalysisRequest {
  texts: string[];
}

export interface AggregateAnalysisResponse {
  individualResults: SentimentResult[];
  aggregateAnalysis: AggregateStats;
  processedCount: number;
  timestamp: string;
}

export interface AggregateStats {
  sentimentDistribution: Record<string, number>;
  dominantSentiment: string;
  averageConfidence: number;
  minConfidence: number;
  maxConfidence: number;
  highestRiskLevel: string;
  highRiskAlerts: number;
  totalResponses: number;
}

export async function analyzeEvaluationResponses(
  texts: string[]
): Promise<AggregateAnalysisResponse> {
  const response = await fetch(
    '/api/v1/sentiment/predict/batch/aggregate',
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ texts })
    }
  );
  return response.json();
}

export async function getEvaluationStats(
  evaluationId: number
): Promise<any> {
  const response = await fetch(
    `/api/v1/sentiment/aggregate/evaluation/${evaluationId}`
  );
  return response.json();
}

export async function getHighRiskAlerts(
  daysBack: number = 7
): Promise<any> {
  const response = await fetch(
    `/api/v1/sentiment/alerts/high-risk?daysBack=${daysBack}`
  );
  return response.json();
}
```

#### React Component Example

```tsx
// components/EvaluationDashboard.tsx
import React, { useState, useEffect } from 'react';
import { analyzeEvaluationResponses, getHighRiskAlerts } from '../services/sentimentService';

export const EvaluationDashboard: React.FC = () => {
  const [aggregateData, setAggregateData] = useState(null);
  const [alerts, setAlerts] = useState([]);

  useEffect(() => {
    // Load high-risk alerts
    getHighRiskAlerts(7).then(data => setAlerts(data.alerts));
  }, []);

  const analyzeResponses = async (responses: string[]) => {
    const result = await analyzeEvaluationResponses(responses);
    setAggregateData(result.aggregateAnalysis);
  };

  return (
    <div>
      <h2>Sentiment Analysis Dashboard</h2>
      
      {/* Aggregate Stats */}
      {aggregateData && (
        <div className="stats-panel">
          <h3>Session Overview</h3>
          <p>Dominant Sentiment: {aggregateData.dominantSentiment}</p>
          <p>Average Confidence: {aggregateData.averageConfidence}</p>
          <p>Highest Risk: {aggregateData.highestRiskLevel}</p>
          {aggregateData.highRiskAlerts > 0 && (
            <div className="alert-warning">
              ⚠️ {aggregateData.highRiskAlerts} High Risk Alert(s)
            </div>
          )}
        </div>
      )}
      
      {/* Recent Alerts */}
      {alerts.length > 0 && (
        <div className="alerts-panel">
          <h3>Recent High-Risk Alerts</h3>
          {alerts.map(alert => (
            <div key={alert.id_evaluacion_respuesta} className="alert-item">
              <p><strong>{alert.nombre_paciente}</strong></p>
              <p>{alert.texto_evaluacion_respuesta}</p>
              <p>Confidence: {alert.confidence_score}</p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
```

---

### Backend Integration (Service-to-Service)

#### Java Example

```java
@Service
public class ReportService {
    
    private final RestTemplate restTemplate;
    
    public ReportService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    public AggregateStats getEvaluationReport(Long evaluationId) {
        String url = "http://localhost:8080/api/v1/sentiment/aggregate/evaluation/" + evaluationId;
        return restTemplate.getForObject(url, AggregateStats.class);
    }
    
    public List<HighRiskAlert> checkRecentAlerts(int daysBack) {
        String url = "http://localhost:8080/api/v1/sentiment/alerts/high-risk?daysBack=" + daysBack;
        AlertsResponse response = restTemplate.getForObject(url, AlertsResponse.class);
        return response.getAlerts();
    }
}
```

---

## ⚡ Performance Considerations

### In-Memory vs Database-Backed

| Aspect | In-Memory | Database-Backed |
|--------|-----------|----------------|
| **Speed** | ⚡ Very Fast (ms) | 🐢 Slower (10-100ms) |
| **Data Source** | Real-time predictions | Persisted records |
| **Best For** | Live analysis | Historical reports |
| **Memory** | Uses application RAM | Uses DB resources |
| **Scalability** | Limited by app memory | Better for large datasets |

### Optimization Tips

#### 1. **Use Appropriate Method**
```java
// ✅ GOOD: Real-time analysis
List<String> texts = getUserResponses();
SentimentAggregateStats stats = sentimentService.calcularEstadisticasAgregadas(
    texts.stream().map(sentimentService::analizarTexto).collect(Collectors.toList())
);

// ✅ GOOD: Historical analysis
List<Long> savedIds = getSavedResponseIds();
SentimentAggregateStats stats = sentimentService.obtenerEstadisticasAgregadasDesdeBD(savedIds);
```

#### 2. **Batch Size Limits**
- In-memory: Limit to **100-500 texts** per batch
- Database: Can handle **1000s of IDs** efficiently with indexes

#### 3. **Caching Strategy**
```java
@Cacheable(value = "evaluation-stats", key = "#evaluationId")
public Map<String, Object> obtenerDistribucionPorEvaluacion(Long evaluationId) {
    // ... cached for repeated requests
}
```

#### 4. **Async Processing for Large Batches**
```java
@Async
public CompletableFuture<SentimentAggregateStats> analyzeAsync(List<String> texts) {
    return CompletableFuture.completedFuture(calcularEstadisticasAgregadas(texts));
}
```

---

## 🔒 Security Considerations

### Access Control

Ensure proper authorization for sensitive endpoints:

```java
@PreAuthorize("hasRole('CLINICIAN') or hasRole('ADMIN')")
@GetMapping("/alerts/high-risk")
public ResponseEntity<Map<String, Object>> getHighRiskAlerts(...) {
    // Only clinicians and admins can view alerts
}
```

### Data Privacy

High-risk alerts contain sensitive patient data:
- 🔒 Use HTTPS in production
- 🔒 Log access to alert endpoints
- 🔒 Implement data retention policies
- 🔒 Anonymize data in reports when appropriate

---

## 📊 Metrics and Monitoring

### Key Metrics to Track

1. **Aggregate Endpoint Usage**
   - Requests per hour
   - Average response time
   - Error rate

2. **Alert Statistics**
   - High-risk alerts per day
   - Response time to alerts
   - False positive rate

3. **Database Performance**
   - Stored procedure execution time
   - Index hit rate
   - Query optimization opportunities

### Logging

The implementation includes comprehensive logging:

```java
log.info("📊 Calculando estadísticas agregadas para {} resultados", results.size());
log.warn("⚠️ ALERTA RIESGO SUICIDA DETECTADO - Label: {}, Confidence: {}", ...);
log.error("❌ Error al obtener estadísticas agregadas desde BD", e);
```

---

## 🧪 Testing

### Unit Tests Example

```java
@Test
void testCalcularEstadisticasAgregadas() {
    List<AnalisisSentimientoResponse> results = Arrays.asList(
        createResponse("ANXIETY", 0.9, "MEDIO"),
        createResponse("SADNESS", 0.85, "MEDIO"),
        createResponse("SUICIDAL", 0.95, "ALTO")
    );
    
    SentimentAggregateStats stats = sentimentService.calcularEstadisticasAgregadas(results);
    
    assertEquals(3, stats.getTotalResponses());
    assertEquals("ALTO", stats.getHighestRiskLevel());
    assertEquals(1, stats.getHighRiskAlerts());
    assertTrue(stats.getAverageConfidence() > 0.8);
}
```

### Integration Tests

```java
@SpringBootTest
@AutoConfigureMockMvc
class SentimentControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testBatchAggregateEndpoint() throws Exception {
        String request = """
            {
              "texts": ["I feel anxious", "I am sad", "Everything is frustrating"]
            }
            """;
        
        mockMvc.perform(post("/api/v1/sentiment/predict/batch/aggregate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.aggregateAnalysis.totalResponses").value(3))
            .andExpect(jsonPath("$.individualResults").isArray());
    }
}
```

---

## 📚 Additional Resources

- [Database Migration Script](../src/main/resources/db/migration/V4__create_sentiment_aggregate_procedures.sql)
- [SentimentService.java](../src/main/java/com/example/rntn/service/SentimentService.java)
- [SentimentController.java](../src/main/java/com/example/rntn/controller/SentimentController.java)
- [Swagger UI](http://localhost:8080/swagger-ui.html) - Interactive API documentation

---

## 🎉 Summary

The Aggregate Sentiment Analysis feature provides:

✅ **Real-time batch analysis** with in-memory aggregation  
✅ **Database-backed statistics** using optimized stored procedures  
✅ **High-risk alert system** for patient safety  
✅ **Flexible API endpoints** for various use cases  
✅ **Comprehensive documentation** and examples  
✅ **Production-ready implementation** with logging and error handling  

This feature enables clinicians and administrators to gain deeper insights into patient sentiment patterns, track trends over time, and respond proactively to high-risk situations.

---

**Questions or Issues?** Open an issue on the project repository or contact the development team.

