package com.railways.blockplanning.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Application-level Spring configuration.
 *
 * CORS is configured to allow all origins (required for decoupled frontend).
 * When the frontend URL is known (cloud deployment), narrow this to that URL.
 */
@Configuration
@ConfigurationPropertiesScan("com.railways.blockplanning")
public class AppConfig implements WebMvcConfigurer {

    /**
     * RestTemplate for calling the ML microservice.
     * Injected into PriorityScoreService.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Global CORS configuration — allows decoupled frontend on any port/domain.
     * Narrow origins in production.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("*")
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .maxAge(3600);
    }

    /**
     * Swagger / OpenAPI metadata for Swagger UI.
     */
    @Bean
    public OpenAPI openApiInfo() {
        return new OpenAPI()
            .info(new Info()
                .title("AI Block Planning System — API")
                .description("""
                    REST API for SIH PS 26027: AI-Powered Automatic Block Planning.
                    
                    **IMPORTANT**: All data is synthetic — TMS/SMMS/TDMS/BDMS/COA access is simulated.
                    The adapter layer is designed for real API substitution without touching core logic.
                    
                    All endpoints are versioned under /api/v1/.
                    The frontend communicates exclusively via these endpoints (decoupled architecture).
                    """)
                .version("1.0.0")
                .contact(new Contact()
                    .name("SIH Team")
                    .email("team@railways-ai.dev"))
                .license(new License().name("MIT")));
    }
}
