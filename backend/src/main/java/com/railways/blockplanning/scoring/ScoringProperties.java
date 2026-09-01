package com.railways.blockplanning.scoring;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Externalized scoring weights — bound from application.yml under `scoring:`.
 * Weights can be changed at runtime via the /api/v1/config endpoints
 * (for live demo tuning without restart).
 */
@Configuration
@ConfigurationProperties(prefix = "scoring")
@Data
public class ScoringProperties {

    /** RULE_BASED or ML */
    private String mode = "RULE_BASED";

    /** URL of the ML microservice */
    private String mlServiceUrl = "http://localhost:8000";

    private Weights weights = new Weights();

    @Data
    public static class Weights {
        private Severity severity = new Severity();
        private double overdueFactor = 0.5;
        private int maxOverdueDays = 30;
        private SafetyRisk safetyRisk = new SafetyRisk();

        @Data
        public static class Severity {
            private double critical = 10.0;
            private double major = 5.0;
            private double minor = 1.0;
        }

        @Data
        public static class SafetyRisk {
            private double high = 3.0;
            private double medium = 2.0;
            private double low = 1.0;
        }
    }
}
