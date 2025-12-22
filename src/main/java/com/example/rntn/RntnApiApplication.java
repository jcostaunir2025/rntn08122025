package com.example.rntn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Aplicación principal de RNTN Sentiment Analysis API
 *
 * Sistema de análisis de sentimientos para salud mental usando
 * Stanford CoreNLP RNTN (Recursive Neural Tensor Network)
 *
 * @author RNTN Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@SpringBootApplication
public class RntnApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(RntnApiApplication.class, args);
    }
}

