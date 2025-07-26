package com.unify.app;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "unify")
public record ApplicationProperties(
    String livekitHost,
    String livekitApiKey,
    String livekitApiSecret,
    String livekitWsUrl,
    String appUri) {}
