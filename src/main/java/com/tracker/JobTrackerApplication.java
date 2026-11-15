package com.tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class JobTrackerApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(JobTrackerApplication.class);
        app.addInitializers(new ApplicationContextInitializer<ConfigurableApplicationContext>() {
            @Override
            public void initialize(ConfigurableApplicationContext applicationContext) {
                ConfigurableEnvironment env = applicationContext.getEnvironment();
                String clientId = env.getProperty("spring.security.oauth2.client.registration.google.client-id");
                String clientSecret = env.getProperty("spring.security.oauth2.client.registration.google.client-secret");

                Map<String, Object> fallbacks = new HashMap<>();
                if (clientId == null || clientId.isBlank()) {
                    fallbacks.put("spring.security.oauth2.client.registration.google.client-id", "not-configured");
                }
                if (clientSecret == null || clientSecret.isBlank()) {
                    fallbacks.put("spring.security.oauth2.client.registration.google.client-secret", "not-configured");
                }
                if (!fallbacks.isEmpty()) {
                    env.getPropertySources().addFirst(new MapPropertySource("oauth2ClientFallbacks", fallbacks));
                }
            }
        });
        app.run(args);
    }
}
