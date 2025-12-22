# ✅ Endpoints de EvaluacionRespuesta Completados

## 📊 Endpoint Agregado

### GET `/api/v1/evaluaciones/respuestas` - Listar todas las respuestas

**Descripción:** Retorna una lista paginada de todas las respuestas de evaluación con sus análisis de sentimiento.

**Parámetros:**
- `page` (opcional): Número de página (default: 0)
- `size` (opcional): Tamaño de página (default: 20)
- `sort` (opcional): Campo y dirección de ordenamiento

**Ejemplo de uso:**
```bash
# Listar primera página
curl -X GET "http://localhost:8080/api/v1/evaluaciones/respuestas?page=0&size=20"

# Ordenar por fecha de creación descendente
curl -X GET "http://localhost:8080/api/v1/evaluaciones/respuestas?sort=createdAt,desc"

# Filtrar y ordenar
curl -X GET "http://localhost:8080/api/v1/evaluaciones/respuestas?page=0&size=10&sort=confidenceScore,desc"
```

**Response:**
```json
{
  "content": [
    {
      "idEvaluacionRespuesta": 1,
      "idEvaluacionPregunta": 1,
      "textoPregunta": "¿Cómo se siente hoy?",
      "textoEvaluacionRespuesta": "Me siento muy ansioso",
      "labelEvaluacionRespuesta": "ANXIETY",
      "confidenceScore": 0.92,
      "createdAt": "2025-12-21T10:00:00"
    },
    {
      "idEvaluacionRespuesta": 2,
      "idEvaluacionPregunta": 1,
      "textoEvaluacionRespuesta": "A veces pienso que no tiene sentido",
      "labelEvaluacionRespuesta": "SUICIDAL",
      "confidenceScore": 0.87,
      "createdAt": "2025-12-21T10:15:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 45,
  "totalPages": 3
}
```

---

## 📊 Resumen de Todos los Endpoints de EvaluacionRespuesta

### Endpoints Implementados (8 total)

| # | Método | Endpoint | Descripción |
|---|--------|----------|-------------|
| 1 | POST | `/respuestas` | Registrar respuesta con análisis RNTN ⭐ |
| 2 | **GET** | `/respuestas` | **Listar todas con paginación** ⭐ **NUEVO** |
| 3 | GET | `/respuestas/{id}` | Obtener respuesta por ID |
| 4 | GET | `/respuestas/label/{label}` | Buscar por label de sentimiento |
| 5 | GET | `/respuestas/alto-riesgo` | Detectar alto riesgo |
| 6 | PUT | `/respuestas/{id}` | Actualizar respuesta |
| 7 | DELETE | `/respuestas/{id}` | Eliminar respuesta |
| 8 | GET | `/analisis-agregado` | Análisis agregado |

---

## ✨ Funcionalidades Completas

### CRUD Completo ✅
- ✅ **C**reate - POST `/respuestas`
- ✅ **R**ead - GET `/respuestas`, GET `/respuestas/{id}`
- ✅ **U**pdate - PUT `/respuestas/{id}`
- ✅ **D**elete - DELETE `/respuestas/{id}`

### Búsquedas y Filtros ✅
- ✅ Listar todas las respuestas (paginado)
- ✅ Buscar por label de sentimiento
- ✅ Filtrar respuestas de alto riesgo
- ✅ Análisis agregado de múltiples preguntas

### Características Avanzadas ✅
- ✅ Análisis RNTN automático
- ✅ Re-análisis al actualizar
- ✅ Detección de alertas de riesgo
- ✅ Paginación en listados
- ✅ Ordenamiento configurable
- ✅ Confidence score tracking

---

## 🎯 Casos de Uso

### 1. Dashboard de Monitoreo

```bash
# Obtener últimas respuestas ordenadas por fecha
curl -X GET "http://localhost:8080/api/v1/evaluaciones/respuestas?sort=createdAt,desc&size=10"
```

### 2. Análisis de Tendencias

```bash
# Obtener respuestas con alto confidence score
curl -X GET "http://localhost:8080/api/v1/evaluaciones/respuestas?sort=confidenceScore,desc&size=50"
```

### 3. Sistema de Alertas

```bash
# Detectar respuestas de alto riesgo
curl -X GET "http://localhost:8080/api/v1/evaluaciones/respuestas/alto-riesgo?umbral=0.8"
```

### 4. Búsqueda por Sentimiento

```bash
# Encontrar todas las respuestas con ansiedad
curl -X GET "http://localhost:8080/api/v1/evaluaciones/respuestas/label/ANXIETY"
```

### 5. Exportación de Datos

```bash
# Obtener gran cantidad de respuestas para análisis
curl -X GET "http://localhost:8080/api/v1/evaluaciones/respuestas?size=100&page=0"
```

---

## 📊 Comparación: Antes vs Ahora

### Antes (7 endpoints)
```
POST   /respuestas
GET    /respuestas/{id}
GET    /respuestas/label/{label}
GET    /respuestas/alto-riesgo
PUT    /respuestas/{id}
DELETE /respuestas/{id}
GET    /analisis-agregado
```

### Ahora (8 endpoints) ✅
```
POST   /respuestas
GET    /respuestas                  ⭐ NUEVO
GET    /respuestas/{id}
GET    /respuestas/label/{label}
GET    /respuestas/alto-riesgo
PUT    /respuestas/{id}
DELETE /respuestas/{id}
GET    /analisis-agregado
```

---

## 🔄 Flujo de Trabajo Completo

### Flujo: Gestión de Respuestas

1. **Listar todas las respuestas**
   ```
   GET /api/v1/evaluaciones/respuestas?page=0&size=20
   ```

2. **Filtrar por sentimiento**
   ```
   GET /api/v1/evaluaciones/respuestas/label/ANXIETY
   ```

3. **Ver detalle de una respuesta**
   ```
   GET /api/v1/evaluaciones/respuestas/1
   ```

4. **Actualizar si es necesario**
   ```
   PUT /api/v1/evaluaciones/respuestas/1
   ```

5. **Verificar alto riesgo**
   ```
   GET /api/v1/evaluaciones/respuestas/alto-riesgo
   ```

---

## 📈 Estadísticas

### Archivos Modificados: 2
- ✅ `EvaluacionController.java` - 1 endpoint agregado
- ✅ `EvaluacionService.java` - 1 método agregado

### Endpoints Totales en la API

| Controller | Endpoints |
|-----------|-----------|
| SentimentController | 4 |
| **EvaluacionController** | **12** (+1) |
| PacienteController | 5 |
| PersonalController | 5 |
| ConsultaController | 6 |
| UsuarioController | 7 |
| EvaluacionPreguntaController | 6 |
| ReporteController | 7 |
| **TOTAL** | **52** |

---

## ✅ Estado de Completitud

### EvaluacionRespuesta - 100% Completo ✅

| Funcionalidad | Estado |
|--------------|--------|
| CRUD Básico | ✅ Completo |
| Paginación | ✅ Implementada |
| Filtros | ✅ Múltiples filtros |
| Búsquedas | ✅ Por label, riesgo |
| Análisis RNTN | ✅ Integrado |
| Alertas | ✅ Automáticas |
| Documentación | ✅ Swagger |

---

## 🎉 Resultado Final

### ✅ Endpoints de EvaluacionRespuesta Completos

- **8 endpoints** totales para EvaluacionRespuesta
- **CRUD completo** implementado
- **Paginación** agregada
- **Búsquedas avanzadas** disponibles
- **Análisis RNTN** integrado en todos los niveles
- **Sistema de alertas** funcional

---

**Fecha:** 21 de Diciembre de 2025  
**Endpoint Agregado:** GET `/api/v1/evaluaciones/respuestas`  
**Total Endpoints API:** 52  
**Estado:** ✅ **COMPLETADO**

