package com.example.rntn.exception;

/**
 * Excepción lanzada cuando hay un error en la predicción de sentimientos
 */
public class PredictionException extends RuntimeException {

    public PredictionException(String message) {
        super(message);
    }

    public PredictionException(String message, Throwable cause) {
        super(message, cause);
    }
}

