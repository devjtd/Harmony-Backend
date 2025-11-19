package com.harmony.sistema.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        System.out.println("🔧 [CORS CONFIG] Configurando CORS para Angular");
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:4200")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
        System.out.println("✅ [CORS CONFIG] CORS configurado correctamente");
    }

    /**
     * ✅ SIMPLIFICACIÓN: Se eliminó extendMessageConverters
     * 
     * ¿Por qué? Spring Boot 3.x maneja automáticamente los conversores JSON
     * y configurarlos manualmente causa conflictos con el Content-Type.
     * 
     * Dejamos que Spring maneje esto por defecto, que es lo correcto.
     */
}