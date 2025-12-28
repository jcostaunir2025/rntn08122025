package com.example.rntn.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Configuración CORS para permitir acceso desde frontend
 * Permite peticiones desde diferentes orígenes (localhost, producción, etc.)
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Orígenes permitidos - ajustar según el entorno
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",      // React/Next.js development
            "http://localhost:4200",      // Angular development
            "http://localhost:5173",      // Vite development
            "http://localhost:8081",      // Alternative frontend port
            "http://127.0.0.1:3000",
            "http://127.0.0.1:4200",
            "http://127.0.0.1:5173",
            "http://127.0.0.1:8081"
            // Agregar URLs de producción aquí cuando se despliegue
            // "https://tu-dominio-frontend.com"
        ));

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
            "GET",
            "POST",
            "PUT",
            "DELETE",
            "PATCH",
            "OPTIONS"
        ));

        // Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept",
            "Origin",
            "X-Requested-With",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));

        // Headers expuestos al cliente
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Content-Disposition",
            "X-Total-Count"
        ));

        // Permitir credenciales (cookies, headers de autenticación)
        configuration.setAllowCredentials(true);

        // Tiempo de cache de la configuración CORS (en segundos)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Aplicar configuración CORS a todos los endpoints
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}

