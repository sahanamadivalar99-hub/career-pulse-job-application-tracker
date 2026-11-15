package com.tracker.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

@Configuration
public class OAuth2ClientConfig {

    private static final Logger log = LoggerFactory.getLogger(OAuth2ClientConfig.class);

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String configuredClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret:}")
    private String configuredClientSecret;

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        String clientId = resolveValue(configuredClientId, "GOOGLE_CLIENT_ID", null);
        String clientSecret = resolveValue(configuredClientSecret, "GOOGLE_CLIENT_SECRET", null);

        if (clientId == null || clientId.isBlank()) {
            clientId = "not-configured";
        }
        if (clientSecret == null || clientSecret.isBlank()) {
            clientSecret = "not-configured";
        }

        ClientRegistration googleRegistration = CommonOAuth2Provider.GOOGLE.getBuilder("google")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .scope("email", "profile")
                .build();

        log.info("Initialized Google OAuth2 client registration (configured: {})", !"not-configured".equals(clientId));
        return new InMemoryClientRegistrationRepository(googleRegistration);
    }

    private String resolveValue(String propValue, String envName, String altEnvName) {
        if (propValue != null && !propValue.isBlank()) {
            return propValue.trim();
        }
        String env = System.getenv(envName);
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        if (altEnvName != null) {
            String alt = System.getenv(altEnvName);
            if (alt != null && !alt.isBlank()) {
                return alt.trim();
            }
        }
        return null;
    }
}
