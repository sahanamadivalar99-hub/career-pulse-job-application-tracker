package com.tracker.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

@Order(Ordered.LOWEST_PRECEDENCE)
public class OAuth2EnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String clientId = environment.getProperty("spring.security.oauth2.client.registration.google.client-id");
        String clientSecret = environment.getProperty("spring.security.oauth2.client.registration.google.client-secret");

        Map<String, Object> fallbacks = new HashMap<>();
        if (clientId == null || clientId.isBlank()) {
            fallbacks.put("spring.security.oauth2.client.registration.google.client-id", "not-configured");
        }
        if (clientSecret == null || clientSecret.isBlank()) {
            fallbacks.put("spring.security.oauth2.client.registration.google.client-secret", "not-configured");
        }
        if (!fallbacks.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource("oauth2ClientSafeFallbacks", fallbacks));
        }
    }
}
