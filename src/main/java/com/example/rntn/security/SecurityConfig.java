package com.example.rntn.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración de seguridad Spring Security con JWT
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos (sin autenticación)
                .requestMatchers(
                    "/api/v1/auth/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-ui.html",
                    "/actuator/health"
                ).permitAll()

                // Endpoints de pacientes - requieren autenticación
                .requestMatchers("/api/v1/pacientes/**").hasAnyRole("ADMIN", "DOCTOR", "ENFERMERO")

                // Endpoints de personal - solo ADMIN
                .requestMatchers(HttpMethod.POST, "/api/v1/personal/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/personal/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/personal/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/personal/**").hasAnyRole("ADMIN", "DOCTOR")

                // Endpoints de consultas
                .requestMatchers("/api/v1/consultas/**").hasAnyRole("ADMIN", "DOCTOR", "ENFERMERO")

                // Endpoints de evaluaciones
                .requestMatchers("/api/v1/evaluaciones/**").hasAnyRole("ADMIN", "DOCTOR")
                .requestMatchers("/api/v1/evaluacion-preguntas/**").hasAnyRole("ADMIN", "DOCTOR")
                .requestMatchers("/api/v1/evaluacion-respuestas/**").hasAnyRole("ADMIN", "DOCTOR")

                // Sentiment analysis - accesible por profesionales de salud
                .requestMatchers("/api/v1/sentiment/**").hasAnyRole("ADMIN", "DOCTOR", "ANALISTA")

                // Reportes - accesible por profesionales
                .requestMatchers("/api/v1/reportes/**").hasAnyRole("ADMIN", "DOCTOR", "ANALISTA")

                // Usuarios y roles - solo ADMIN
                .requestMatchers("/api/v1/usuarios/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/roles/**").hasRole("ADMIN")

                // Cualquier otra petición requiere autenticación
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

