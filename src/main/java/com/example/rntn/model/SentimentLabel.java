package com.example.rntn.model;

/**
 * Enum que representa las 5 clases de sentimiento del modelo RNTN
 *
 * Mapeo personalizado para análisis de salud mental:
 * - 0: ANXIETY (Ansiedad)
 * - 1: SUICIDAL (Pensamientos suicidas) - RIESGO ALTO
 * - 2: ANGER (Enojo)
 * - 3: SADNESS (Tristeza)
 * - 4: FRUSTRATION (Frustración)
 */
public enum SentimentLabel {
    ANXIETY(0, "Anxiety", "Anxious or worried state", "MEDIO"),
    SUICIDAL(1, "Suicidal", "Suicidal thoughts or expressions", "ALTO"),
    ANGER(2, "Anger", "Angry or frustrated state", "MEDIO"),
    SADNESS(3, "Sadness", "Sad or depressed state", "MEDIO"),
    FRUSTRATION(4, "Frustration", "Frustrated state", "BAJO");

    private final int index;
    private final String name;
    private final String description;
    private final String riskLevel;

    SentimentLabel(int index, String name, String description, String riskLevel) {
        this.index = index;
        this.name = name;
        this.description = description;
        this.riskLevel = riskLevel;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    /**
     * Obtiene el SentimentLabel a partir del índice de clase predicho por RNTN
     *
     * @param index Índice de clase (0-4)
     * @return SentimentLabel correspondiente
     * @throws IllegalArgumentException si el índice no es válido
     */
    public static SentimentLabel fromIndex(int index) {
        for (SentimentLabel label : values()) {
            if (label.index == index) {
                return label;
            }
        }
        throw new IllegalArgumentException("Invalid sentiment index: " + index);
    }
}

