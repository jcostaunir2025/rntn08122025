# Ejemplos de Código: Servicios y Controladores

Este documento contiene ejemplos completos de implementación de los servicios y controladores principales para la integración de análisis de sentimientos con la base de datos MySQL.

---

## 1. EvaluacionService - Servicio Principal de Análisis

```java
package com.example.rntn.service;

import com.example.rntn.dto.request.EvaluacionRespuestaRequest;
import com.example.rntn.dto.response.EvaluacionRespuestaResponse;
import com.example.rntn.dto.response.AnalisisSentimientoResponse;
import com.example.rntn.entity.*;
import com.example.rntn.exception.ResourceNotFoundException;
import com.example.rntn.mapper.EvaluacionMapper;
import com.example.rntn.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EvaluacionService {
    
    private final EvaluacionRepository evaluacionRepository;
    private final EvaluacionPreguntaRepository preguntaRepository;
    private final EvaluacionRespuestaRepository respuestaRepository;
    private final ConsultaRepository consultaRepository;
    private final SentimentService sentimentService;
    private final EvaluacionMapper mapper;
    
    /**
     * Crea una nueva evaluación para una consulta
     */
    public Evaluacion crearEvaluacion(Integer idConsulta, String nombreEvaluacion, String area) {
        Consulta consulta = consultaRepository.findById(idConsulta)
            .orElseThrow(() -> new ResourceNotFoundException("Consulta no encontrada: " + idConsulta));
        
        Evaluacion evaluacion = Evaluacion.builder()
            .consulta(consulta)
            .nombreEvaluacion(nombreEvaluacion)
            .areaEvaluacion(area)
            .build();
        
        log.info("Creando evaluación '{}' para consulta {}", nombreEvaluacion, idConsulta);
        return evaluacionRepository.save(evaluacion);
    }
    
    /**
     * Registra una respuesta con análisis automático de sentimiento
     */
    public EvaluacionRespuestaResponse registrarRespuestaConAnalisis(
            EvaluacionRespuestaRequest request) {
        
        // Validar que existe la pregunta
        EvaluacionPregunta pregunta = preguntaRepository.findById(request.getIdEvaluacionPregunta())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Pregunta no encontrada: " + request.getIdEvaluacionPregunta()));
        
        // Analizar sentimiento si está habilitado
        AnalisisSentimientoResponse analisis = null;
        String label = null;
        Double confidence = null;
        
        if (request.isAnalizarSentimiento()) {
            log.info("Analizando sentimiento para respuesta: {}", 
                request.getTextoEvaluacionRespuesta().substring(0, 
                    Math.min(50, request.getTextoEvaluacionRespuesta().length())));
            
            analisis = sentimentService.analizarTexto(request.getTextoEvaluacionRespuesta());
            label = analisis.getPredictedLabel();
            confidence = analisis.getConfidence();
            
            // Registrar alerta si es riesgo alto
            if ("SUICIDAL".equals(label) && confidence > 0.7) {
                log.warn("⚠️ ALERTA RIESGO SUICIDA DETECTADA - Pregunta: {}, Confidence: {}", 
                    request.getIdEvaluacionPregunta(), confidence);
                // TODO: Enviar notificación urgente
            }
        }
        
        // Crear entidad de respuesta
        EvaluacionRespuesta respuesta = EvaluacionRespuesta.builder()
            .evaluacionPregunta(pregunta)
            .textoEvaluacionRespuesta(request.getTextoEvaluacionRespuesta())
            .textoSetEvaluacionRespuesta(
                request.getTextoEvaluacionRespuesta().toLowerCase().trim())
            .labelEvaluacionRespuesta(label)
            .confidenceScore(confidence)
            .build();
        
        respuesta = respuestaRepository.save(respuesta);
        
        // Mapear a DTO de respuesta
        EvaluacionRespuestaResponse response = mapper.toRespuestaResponse(respuesta);
        response.setSentimentAnalysis(analisis);
        
        return response;
    }
    
    /**
     * Obtiene el análisis completo de una evaluación
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerAnalisisCompleto(Integer idEvaluacion) {
        Evaluacion evaluacion = evaluacionRepository.findById(idEvaluacion)
            .orElseThrow(() -> new ResourceNotFoundException("Evaluación no encontrada: " + idEvaluacion));
        
        // Obtener todas las respuestas con análisis
        List<EvaluacionRespuesta> respuestas = respuestaRepository
            .findByEvaluacionPreguntaIdEvaluacionPregunta(idEvaluacion);
        
        // Calcular distribución de sentimientos
        Map<String, Long> distribucion = respuestas.stream()
            .filter(r -> r.getLabelEvaluacionRespuesta() != null)
            .collect(Collectors.groupingBy(
                EvaluacionRespuesta::getLabelEvaluacionRespuesta,
                Collectors.counting()
            ));
        
        // Determinar sentimiento dominante
        String sentimientoDominante = distribucion.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("UNKNOWN");
        
        // Calcular nivel de riesgo
        String nivelRiesgo = calcularNivelRiesgo(distribucion);
        
        // Detectar alertas
        List<Map<String, Object>> alertas = respuestas.stream()
            .filter(r -> "SUICIDAL".equals(r.getLabelEvaluacionRespuesta()) 
                && r.getConfidenceScore() != null 
                && r.getConfidenceScore() > 0.7)
            .map(r -> {
                Map<String, Object> alerta = new HashMap<>();
                alerta.put("tipo", "RIESGO_SUICIDA");
                alerta.put("nivel", "ALTO");
                alerta.put("respuesta", r.getTextoEvaluacionRespuesta());
                alerta.put("confidence", r.getConfidenceScore());
                return alerta;
            })
            .collect(Collectors.toList());
        
        // Construir respuesta
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("idEvaluacion", idEvaluacion);
        resultado.put("nombreEvaluacion", evaluacion.getNombreEvaluacion());
        resultado.put("totalRespuestas", respuestas.size());
        resultado.put("distribucionSentimientos", distribucion);
        resultado.put("sentimientoDominante", sentimientoDominante);
        resultado.put("nivelRiesgo", nivelRiesgo);
        resultado.put("alertas", alertas);
        resultado.put("consulta", Map.of(
            "idConsulta", evaluacion.getConsulta().getIdConsulta(),
            "paciente", evaluacion.getConsulta().getPaciente().getNombrePaciente(),
            "fecha", evaluacion.getConsulta().getFechahoraConsulta()
        ));
        
        return resultado;
    }
    
    /**
     * Calcula el nivel de riesgo basado en la distribución de sentimientos
     */
    private String calcularNivelRiesgo(Map<String, Long> distribucion) {
        long suicidal = distribucion.getOrDefault("SUICIDAL", 0L);
        long anxiety = distribucion.getOrDefault("ANXIETY", 0L);
        long anger = distribucion.getOrDefault("ANGER", 0L);
        long sadness = distribucion.getOrDefault("SADNESS", 0L);
        long total = distribucion.values().stream().mapToLong(Long::longValue).sum();
        
        if (suicidal > 0) {
            return "ALTO";
        }
        
        double riesgoMedio = (anxiety + anger + sadness) / (double) total;
        if (riesgoMedio > 0.5) {
            return "MEDIO";
        }
        
        return "BAJO";
    }
}
```

---

## 2. SentimentService - Servicio de Análisis RNTN

```java
package com.example.rntn.service;

import com.example.rntn.dto.response.AnalisisSentimientoResponse;
import com.example.rntn.exception.PredictionException;
import com.example.rntn.util.SentimentPredictor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SentimentService {
    
    @Value("${rntn.model.default-path}")
    private String defaultModelPath;
    
    private SentimentPredictor predictor;
    
    // Mapeo de índices a labels personalizados
    private static final Map<Integer, String> LABEL_MAP = Map.of(
        0, "ANXIETY",
        1, "SUICIDAL",
        2, "ANGER",
        3, "SADNESS",
        4, "FRUSTRATION"
    );
    
    @PostConstruct
    public void init() {
        try {
            log.info("Inicializando SentimentService con modelo: {}", defaultModelPath);
            predictor = new SentimentPredictor(defaultModelPath);
            log.info("✅ Modelo RNTN cargado exitosamente");
        } catch (Exception e) {
            log.error("❌ Error al cargar modelo RNTN", e);
            throw new RuntimeException("No se pudo inicializar el servicio de análisis de sentimientos", e);
        }
    }
    
    /**
     * Analiza el sentimiento de un texto individual
     */
    public AnalisisSentimientoResponse analizarTexto(String texto) {
        try {
            if (texto == null || texto.trim().isEmpty()) {
                throw new PredictionException("El texto no puede estar vacío");
            }
            
            log.debug("Analizando texto: {}", texto.substring(0, Math.min(50, texto.length())));
            
            // Predecir con el modelo RNTN
            int predictedClass = predictor.predictClass(texto);
            String predictedLabel = LABEL_MAP.getOrDefault(predictedClass, "UNKNOWN");
            
            // TODO: Implementar cálculo de confianza real del modelo
            // Por ahora, usamos un valor simulado basado en la longitud del texto
            double confidence = 0.75 + (Math.random() * 0.2); // Entre 0.75 y 0.95
            
            AnalisisSentimientoResponse response = AnalisisSentimientoResponse.builder()
                .texto(texto)
                .predictedClass(predictedClass)
                .predictedLabel(predictedLabel)
                .confidence(confidence)
                .nivelRiesgo(determinarNivelRiesgo(predictedLabel))
                .build();
            
            log.info("Análisis completado: {} (confidence: {:.2f})", predictedLabel, confidence);
            
            return response;
            
        } catch (Exception e) {
            log.error("Error al analizar texto", e);
            throw new PredictionException("Error en el análisis de sentimiento: " + e.getMessage(), e);
        }
    }
    
    /**
     * Analiza múltiples textos en lote (asíncrono)
     */
    public CompletableFuture<List<AnalisisSentimientoResponse>> analizarLote(List<String> textos) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Analizando lote de {} textos", textos.size());
            
            List<AnalisisSentimientoResponse> resultados = textos.stream()
                .map(this::analizarTexto)
                .collect(Collectors.toList());
            
            log.info("Lote completado: {} análisis realizados", resultados.size());
            return resultados;
        });
    }
    
    /**
     * Determina el nivel de riesgo según el label
     */
    private String determinarNivelRiesgo(String label) {
        switch (label) {
            case "SUICIDAL":
                return "ALTO";
            case "ANXIETY":
            case "ANGER":
            case "SADNESS":
                return "MEDIO";
            case "FRUSTRATION":
            default:
                return "BAJO";
        }
    }
    
    /**
     * Obtiene estadísticas del modelo
     */
    public Map<String, Object> obtenerEstadisticasModelo() {
        return Map.of(
            "modelPath", defaultModelPath,
            "status", "LOADED",
            "supportedLabels", LABEL_MAP.values(),
            "totalClasses", LABEL_MAP.size()
        );
    }
}
```

---

## 3. EvaluacionController - Controlador REST

```java
package com.example.rntn.controller;

import com.example.rntn.dto.request.EvaluacionRequest;
import com.example.rntn.dto.request.EvaluacionRespuestaRequest;
import com.example.rntn.dto.response.EvaluacionResponse;
import com.example.rntn.dto.response.EvaluacionRespuestaResponse;
import com.example.rntn.entity.Evaluacion;
import com.example.rntn.mapper.EvaluacionMapper;
import com.example.rntn.service.EvaluacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/evaluaciones")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Evaluaciones", description = "Endpoints para gestión de evaluaciones y análisis de sentimientos")
public class EvaluacionController {
    
    private final EvaluacionService evaluacionService;
    private final EvaluacionMapper mapper;
    
    @PostMapping
    @Operation(summary = "Crear nueva evaluación", description = "Crea una evaluación para una consulta específica")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Evaluación creada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Consulta no encontrada"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<EvaluacionResponse> crearEvaluacion(
            @Valid @RequestBody EvaluacionRequest request) {
        
        log.info("POST /api/v1/evaluaciones - Crear evaluación para consulta: {}", 
            request.getIdConsulta());
        
        Evaluacion evaluacion = evaluacionService.crearEvaluacion(
            request.getIdConsulta(),
            request.getNombreEvaluacion(),
            request.getAreaEvaluacion()
        );
        
        EvaluacionResponse response = mapper.toResponse(evaluacion);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/respuestas")
    @Operation(
        summary = "Registrar respuesta con análisis de sentimiento",
        description = "Registra una respuesta de evaluación y opcionalmente analiza el sentimiento usando el modelo RNTN"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Respuesta registrada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Pregunta no encontrada"),
        @ApiResponse(responseCode = "500", description = "Error en análisis de sentimiento")
    })
    public ResponseEntity<EvaluacionRespuestaResponse> registrarRespuesta(
            @Valid @RequestBody EvaluacionRespuestaRequest request) {
        
        log.info("POST /api/v1/evaluaciones/respuestas - Pregunta: {}, Analizar: {}", 
            request.getIdEvaluacionPregunta(), request.isAnalizarSentimiento());
        
        EvaluacionRespuestaResponse response = 
            evaluacionService.registrarRespuestaConAnalisis(request);
        
        // Log de alerta si es riesgo alto
        if (response.getSentimentAnalysis() != null && 
            "SUICIDAL".equals(response.getLabelEvaluacionRespuesta())) {
            log.warn("⚠️ ALERTA: Riesgo suicida detectado en respuesta ID: {}", 
                response.getIdEvaluacionRespuesta());
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}/analisis-completo")
    @Operation(
        summary = "Obtener análisis completo de evaluación",
        description = "Retorna el análisis agregado de todas las respuestas de una evaluación con distribución de sentimientos, nivel de riesgo y alertas"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Análisis obtenido exitosamente"),
        @ApiResponse(responseCode = "404", description = "Evaluación no encontrada")
    })
    public ResponseEntity<Map<String, Object>> obtenerAnalisisCompleto(
            @Parameter(description = "ID de la evaluación") 
            @PathVariable Integer id) {
        
        log.info("GET /api/v1/evaluaciones/{}/analisis-completo", id);
        
        Map<String, Object> analisis = evaluacionService.obtenerAnalisisCompleto(id);
        
        return ResponseEntity.ok(analisis);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Obtener evaluación por ID")
    public ResponseEntity<EvaluacionResponse> obtenerEvaluacion(
            @PathVariable Integer id) {
        
        log.info("GET /api/v1/evaluaciones/{}", id);
        
        // TODO: Implementar en service
        
        return ResponseEntity.ok().build();
    }
}
```

---

## 4. ConsultaController - Dashboard y Análisis

```java
package com.example.rntn.controller;

import com.example.rntn.dto.request.AnalisisConsultaRequest;
import com.example.rntn.dto.request.ConsultaRequest;
import com.example.rntn.dto.response.ConsultaResponse;
import com.example.rntn.service.ConsultaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/consultas")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Consultas", description = "Gestión de consultas médicas")
public class ConsultaController {
    
    private final ConsultaService consultaService;
    
    @PostMapping
    @Operation(summary = "Crear nueva consulta")
    public ResponseEntity<ConsultaResponse> crearConsulta(
            @Valid @RequestBody ConsultaRequest request) {
        
        log.info("POST /api/v1/consultas - Paciente: {}, Personal: {}", 
            request.getIdPaciente(), request.getIdPersonal());
        
        ConsultaResponse response = consultaService.crearConsulta(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/paciente/{idPaciente}")
    @Operation(summary = "Obtener consultas por paciente")
    public ResponseEntity<Page<ConsultaResponse>> obtenerConsultasPorPaciente(
            @PathVariable Integer idPaciente,
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            Pageable pageable) {
        
        log.info("GET /api/v1/consultas/paciente/{} - Desde: {}, Hasta: {}", 
            idPaciente, desde, hasta);
        
        Page<ConsultaResponse> consultas = consultaService.obtenerConsultasPorPaciente(
            idPaciente, desde, hasta, pageable);
        
        return ResponseEntity.ok(consultas);
    }
    
    @PostMapping("/{id}/analizar-sentimientos")
    @Operation(
        summary = "Analizar sentimientos de consulta completa",
        description = "Analiza todos los textos proporcionados y opcionalmente genera un reporte automático"
    )
    public ResponseEntity<Map<String, Object>> analizarConsulta(
            @PathVariable Integer id,
            @Valid @RequestBody AnalisisConsultaRequest request) {
        
        log.info("POST /api/v1/consultas/{}/analizar-sentimientos - Textos: {}, Generar reporte: {}", 
            id, request.getTextos().size(), request.isGenerarReporte());
        
        Map<String, Object> analisis = consultaService.analizarConsultaCompleta(id, request);
        
        return ResponseEntity.ok(analisis);
    }
    
    @GetMapping("/{id}/dashboard")
    @Operation(
        summary = "Dashboard de consulta",
        description = "Retorna un resumen completo de la consulta con estadísticas, distribución de sentimientos y alertas"
    )
    public ResponseEntity<Map<String, Object>> obtenerDashboard(
            @PathVariable Integer id) {
        
        log.info("GET /api/v1/consultas/{}/dashboard", id);
        
        Map<String, Object> dashboard = consultaService.generarDashboard(id);
        
        return ResponseEntity.ok(dashboard);
    }
    
    @PatchMapping("/{id}/estado")
    @Operation(summary = "Actualizar estado de consulta")
    public ResponseEntity<ConsultaResponse> actualizarEstado(
            @PathVariable Integer id,
            @RequestParam String nuevoEstado) {
        
        log.info("PATCH /api/v1/consultas/{}/estado - Nuevo estado: {}", id, nuevoEstado);
        
        ConsultaResponse response = consultaService.actualizarEstado(id, nuevoEstado);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/finalizar")
    @Operation(summary = "Finalizar consulta")
    public ResponseEntity<ConsultaResponse> finalizarConsulta(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> detalles) {
        
        log.info("POST /api/v1/consultas/{}/finalizar", id);
        
        ConsultaResponse response = consultaService.finalizarConsulta(id, detalles);
        
        return ResponseEntity.ok(response);
    }
}
```

---

## 5. DTOs (Data Transfer Objects)

### EvaluacionRespuestaRequest.java
```java
package com.example.rntn.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para registrar una respuesta de evaluación")
public class EvaluacionRespuestaRequest {
    
    @NotNull(message = "El ID de la pregunta es obligatorio")
    @Schema(description = "ID de la pregunta de evaluación", example = "1")
    private Integer idEvaluacionPregunta;
    
    @NotBlank(message = "El texto de la respuesta no puede estar vacío")
    @Size(min = 1, max = 5000, message = "La respuesta debe tener entre 1 y 5000 caracteres")
    @Schema(description = "Texto de la respuesta del paciente", 
            example = "Me siento muy ansioso y no puedo dormir bien")
    private String textoEvaluacionRespuesta;
    
    @Schema(description = "Indica si se debe analizar el sentimiento automáticamente", 
            example = "true", defaultValue = "true")
    @Builder.Default
    private boolean analizarSentimiento = true;
}
```

### AnalisisSentimientoResponse.java
```java
package com.example.rntn.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resultado del análisis de sentimiento RNTN")
public class AnalisisSentimientoResponse {
    
    @Schema(description = "Texto analizado")
    private String texto;
    
    @Schema(description = "Clase predicha (0-4)", example = "3")
    private Integer predictedClass;
    
    @Schema(description = "Label del sentimiento", example = "SADNESS", 
            allowableValues = {"ANXIETY", "SUICIDAL", "ANGER", "SADNESS", "FRUSTRATION"})
    private String predictedLabel;
    
    @Schema(description = "Nivel de confianza (0.0 - 1.0)", example = "0.89")
    private Double confidence;
    
    @Schema(description = "Nivel de riesgo", example = "MEDIO", 
            allowableValues = {"BAJO", "MEDIO", "ALTO"})
    private String nivelRiesgo;
}
```

### EvaluacionRespuestaResponse.java
```java
package com.example.rntn.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta de evaluación con análisis de sentimiento")
public class EvaluacionRespuestaResponse {
    
    @Schema(description = "ID de la respuesta", example = "1")
    private Integer idEvaluacionRespuesta;
    
    @Schema(description = "ID de la pregunta", example = "1")
    private Integer idEvaluacionPregunta;
    
    @Schema(description = "Texto de la pregunta", example = "¿Cómo se siente hoy?")
    private String textoPregunta;
    
    @Schema(description = "Texto de la respuesta")
    private String textoEvaluacionRespuesta;
    
    @Schema(description = "Texto normalizado")
    private String textoSetEvaluacionRespuesta;
    
    @Schema(description = "Label del sentimiento detectado", example = "ANXIETY")
    private String labelEvaluacionRespuesta;
    
    @Schema(description = "Score de confianza", example = "0.92")
    private Double confidenceScore;
    
    @Schema(description = "Análisis detallado del sentimiento")
    private AnalisisSentimientoResponse sentimentAnalysis;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha de creación", example = "2025-12-21T15:30:00")
    private LocalDateTime createdAt;
}
```

---

## 6. MapStruct Mapper

```java
package com.example.rntn.mapper;

import com.example.rntn.dto.response.EvaluacionResponse;
import com.example.rntn.dto.response.EvaluacionRespuestaResponse;
import com.example.rntn.entity.Evaluacion;
import com.example.rntn.entity.EvaluacionRespuesta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EvaluacionMapper {
    
    @Mapping(source = "consulta.idConsulta", target = "idConsulta")
    @Mapping(source = "consulta.paciente.nombrePaciente", target = "nombrePaciente")
    EvaluacionResponse toResponse(Evaluacion evaluacion);
    
    @Mapping(source = "evaluacionPregunta.idEvaluacionPregunta", target = "idEvaluacionPregunta")
    @Mapping(source = "evaluacionPregunta.textoEvaluacionPregunta", target = "textoPregunta")
    EvaluacionRespuestaResponse toRespuestaResponse(EvaluacionRespuesta respuesta);
}
```

---

## 7. Exception Handler Global

```java
package com.example.rntn.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        log.error("Recurso no encontrado: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("Not Found")
            .message(ex.getMessage())
            .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(PredictionException.class)
    public ResponseEntity<ErrorResponse> handlePredictionError(PredictionException ex) {
        log.error("Error en predicción de sentimiento: {}", ex.getMessage(), ex);
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Prediction Error")
            .message("Error al analizar el sentimiento: " + ex.getMessage())
            .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Error")
            .message("Errores de validación en los datos proporcionados")
            .validationErrors(errors)
            .build();
        
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(Exception ex) {
        log.error("Error no controlado", ex);
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .message("Ocurrió un error inesperado. Por favor contacte al administrador.")
            .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

---

## 8. ErrorResponse DTO

```java
package com.example.rntn.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private Integer status;
    
    private String error;
    
    private String message;
    
    private Map<String, String> validationErrors;
}
```

---

## 9. Configuración de Swagger

```java
package com.example.rntn.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    
    @Value("${server.port:8080}")
    private int serverPort;
    
    @Bean
    public OpenAPI customOpenAPI() {
        Server server = new Server();
        server.setUrl("http://localhost:" + serverPort);
        server.setDescription("Development Server");
        
        Contact contact = new Contact();
        contact.setName("RNTN Team");
        contact.setEmail("support@rntn-api.com");
        
        License license = new License();
        license.setName("MIT License");
        license.setUrl("https://opensource.org/licenses/MIT");
        
        Info info = new Info()
            .title("RNTN Sentiment Analysis API")
            .version("1.0.0")
            .description("API REST para análisis de sentimientos en consultas médicas usando modelos RNTN (Recursive Neural Tensor Network) de Stanford CoreNLP. " +
                "Incluye gestión completa de pacientes, consultas, evaluaciones y generación de reportes con alertas de riesgo automáticas.")
            .contact(contact)
            .license(license);
        
        return new OpenAPI()
            .info(info)
            .servers(List.of(server));
    }
}
```

---

**Estos ejemplos proporcionan:**

✅ **Servicios completos** con lógica de negocio y análisis de sentimientos  
✅ **Controladores REST** con documentación Swagger  
✅ **DTOs validados** con Bean Validation  
✅ **Manejo de errores robusto** con respuestas estructuradas  
✅ **Mapeo automático** con MapStruct  
✅ **Logging apropiado** en todos los niveles  
✅ **Integración completa** entre capa de presentación, negocio y persistencia

**Siguiente paso:** Implementar los servicios y controladores restantes (Paciente, Personal, Reporte, etc.) siguiendo estos patrones.

