# 🚀 Quick Start - Aggregate Analysis Endpoints

## Fast Reference Card for New Features

### 1️⃣ Real-Time Batch Analysis with Aggregates

**Endpoint:** `POST /api/v1/sentiment/predict/batch/aggregate`

**Use when:** You want both individual predictions AND summary statistics in one call

```bash
curl -X POST http://localhost:8080/api/v1/sentiment/predict/batch/aggregate \
  -H "Content-Type: application/json" \
  -d '{
    "texts": [
      "I feel anxious",
      "I am so angry", 
      "Everything makes me sad"
    ]
  }'
```

**Returns:**
- ✅ Individual predictions for each text
- ✅ Aggregate stats (distribution, avg confidence, dominant sentiment, alerts)
- ✅ No database required

---

### 2️⃣ Database-Backed Aggregate Stats

**Endpoint:** `POST /api/v1/sentiment/aggregate/stats`

**Use when:** You have saved response IDs and want aggregate statistics from the database

```bash
curl -X POST http://localhost:8080/api/v1/sentiment/aggregate/stats \
  -H "Content-Type: application/json" \
  -d '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10]'
```

**Returns:**
- ✅ Sentiment distribution across all responses
- ✅ Average/min/max confidence
- ✅ Dominant sentiment and highest risk level
- ✅ High-risk alert count
- ⚡ Uses optimized stored procedures

---

### 3️⃣ Evaluation Session Summary

**Endpoint:** `GET /api/v1/sentiment/aggregate/evaluation/{idEvaluacion}`

**Use when:** You want a complete overview of an evaluation session

```bash
curl -X GET http://localhost:8080/api/v1/sentiment/aggregate/evaluation/5
```

**Returns:**
- ✅ Patient and professional information
- ✅ Complete sentiment distribution for the session
- ✅ Total responses analyzed
- ✅ Dominant sentiment and risk alerts
- 📊 Perfect for post-session reports

---

### 4️⃣ High-Risk Alerts Monitor

**Endpoint:** `GET /api/v1/sentiment/alerts/high-risk?daysBack=7`

**Use when:** You need to monitor critical cases (SUICIDAL responses)

```bash
curl -X GET "http://localhost:8080/api/v1/sentiment/alerts/high-risk?daysBack=7"
```

**Returns:**
- ⚠️ All SUICIDAL responses with confidence > 0.7
- ✅ Full patient context (name, phone, email)
- ✅ Professional assigned
- ✅ Consultation and evaluation details
- 🚨 **CRITICAL for patient safety monitoring**

---

## 🎯 Use Case Matrix

| Scenario | Best Endpoint | Why |
|----------|---------------|-----|
| **Analyzing new evaluation session** | `POST /predict/batch/aggregate` | Real-time, no DB needed |
| **Generating historical report** | `POST /aggregate/stats` | Optimized for saved data |
| **Post-session clinician review** | `GET /aggregate/evaluation/{id}` | Complete session overview |
| **Safety monitoring dashboard** | `GET /alerts/high-risk` | Proactive risk detection |
| **Patient progress tracking** | `POST /aggregate/stats` + multiple calls | Compare over time |

---

## 📊 Response Structure Examples

### Batch Aggregate Response

```json
{
  "individualResults": [ /* array of individual analyses */ ],
  "aggregateAnalysis": {
    "sentimentDistribution": {
      "ANXIETY": 2,
      "SUICIDAL": 1,
      "ANGER": 1,
      "SADNESS": 1,
      "FRUSTRATION": 0
    },
    "dominantSentiment": "ANXIETY",
    "averageConfidence": 0.887,
    "minConfidence": 0.75,
    "maxConfidence": 0.95,
    "highestRiskLevel": "ALTO",
    "highRiskAlerts": 1,
    "totalResponses": 5
  },
  "processedCount": 5,
  "timestamp": "2025-12-21T17:30:00"
}
```

### High-Risk Alert Response

```json
{
  "alerts": [
    {
      "id_evaluacion_respuesta": 42,
      "texto_evaluacion_respuesta": "I don't want to live anymore",
      "label_evaluacion_respuesta": "SUICIDAL",
      "confidence_score": 0.95,
      "nivel_riesgo": "ALTO",
      "nombre_paciente": "Juan Pérez García",
      "telefono_paciente": "+34 666 777 888",
      "nombre_personal": "Dra. María González",
      "fecha_respuesta": "2025-12-20T14:30:00"
    }
  ],
  "totalAlerts": 1,
  "daysBack": 7,
  "timestamp": "2025-12-21T17:30:00"
}
```

---

## 🔧 Integration Tips

### Frontend (React)

```typescript
// Analyze evaluation session
const analyzeSession = async (texts: string[]) => {
  const response = await fetch('/api/v1/sentiment/predict/batch/aggregate', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ texts })
  });
  const data = await response.json();
  
  // Check for high-risk alerts
  if (data.aggregateAnalysis.highRiskAlerts > 0) {
    showWarning(`⚠️ ${data.aggregateAnalysis.highRiskAlerts} high-risk alert(s) detected!`);
  }
  
  return data;
};

// Monitor dashboard
const loadAlerts = async () => {
  const response = await fetch('/api/v1/sentiment/alerts/high-risk?daysBack=7');
  const data = await response.json();
  
  if (data.totalAlerts > 0) {
    notifyAdmins(data.alerts);
  }
  
  return data.alerts;
};
```

### Backend (Java Service)

```java
@Service
public class DashboardService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    public DashboardStats getDailyStats() {
        // Get today's alerts
        String url = "http://localhost:8080/api/v1/sentiment/alerts/high-risk?daysBack=1";
        AlertsResponse alerts = restTemplate.getForObject(url, AlertsResponse.class);
        
        // Generate dashboard
        return DashboardStats.builder()
            .totalAlerts(alerts.getTotalAlerts())
            .urgentCases(alerts.getAlerts())
            .build();
    }
}
```

---

## 🚨 Important Notes

### Performance
- **Batch aggregate:** Fast for 1-100 texts
- **DB aggregate:** Optimized for 1000s of IDs
- **Alerts:** Indexed queries, very fast

### Security
- High-risk alerts contain sensitive data
- Implement proper access control
- Use HTTPS in production
- Log access to alert endpoints

### Best Practices
1. Use batch aggregate for real-time analysis
2. Use DB aggregate for historical reports
3. Check alerts endpoint regularly (e.g., every hour)
4. Set up notifications for high-risk alerts
5. Cache evaluation summaries for dashboard performance

---

## 📚 Full Documentation

For complete details, see [AGGREGATE_ANALYSIS_FEATURE.md](./AGGREGATE_ANALYSIS_FEATURE.md)

For all endpoints, see [API_ENDPOINTS_IMPLEMENTED.md](./API_ENDPOINTS_IMPLEMENTED.md)

For Swagger UI, visit: http://localhost:8080/swagger-ui.html

---

## 🆘 Troubleshooting

### Issue: Stored procedure not found
**Solution:** Run Flyway migration: `mvn flyway:migrate` or restart application

### Issue: Empty results from DB aggregate
**Solution:** Ensure response IDs exist and have sentiment analysis data

### Issue: No alerts showing up
**Solution:** Check date range (daysBack parameter) and confidence threshold (> 0.7)

---

**Version:** 1.1.0  
**Last Updated:** 2025-12-21  
**Status:** ✅ Production Ready

