package com.example.rntn.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de OpenAPI/Swagger para documentación automática de la API
 */
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
            .description("""
                API REST para análisis de sentimientos en consultas médicas usando modelos RNTN
                (Recursive Neural Tensor Network) de Stanford CoreNLP.

                **Características principales:**
                - Análisis de sentimientos en tiempo real
                - 5 clases de sentimiento: ANXIETY, SUICIDAL, ANGER, SADNESS, FRUSTRATION
                - Detección automática de riesgos altos
                - Persistencia en MySQL
                - Generación de reportes con estadísticas agregadas
                - Autenticación JWT

                **Autenticación:**
                Para usar la API, primero debes autenticarte en /api/v1/auth/login con tus credenciales.
                Obtendrás un token JWT que debes incluir en el header Authorization: Bearer {token}

                **Credenciales de prueba:**
                - admin / password123 (Rol: ADMIN)
                - doctor1 / password123 (Rol: DOCTOR)
                - psicologo1 / password123 (Rol: PSICOLOGO)

                **Modelo RNTN:**
                El sistema utiliza un modelo RNTN entrenado específicamente para análisis de salud mental,
                capaz de detectar expresiones de ansiedad, pensamientos suicidas, enojo, tristeza y frustración.
                """)
            .contact(contact)
            .license(license);

        // Definir el esquema de seguridad JWT
        SecurityScheme securityScheme = new SecurityScheme()
            .name("Bearer Authentication")
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .description("Ingresa el token JWT obtenido desde /api/v1/auth/login");

        SecurityRequirement securityRequirement = new SecurityRequirement()
            .addList("Bearer Authentication");

        return new OpenAPI()
            .info(info)
            .servers(List.of(server))
            .addSecurityItem(securityRequirement)
            .components(new Components().addSecuritySchemes("Bearer Authentication", securityScheme));
    }
}

