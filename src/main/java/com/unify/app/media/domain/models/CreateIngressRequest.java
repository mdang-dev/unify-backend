package com.unify.app.media.domain.models;

public record CreateIngressRequest(
    String participantIdentity, String participantName, String inputType) {}
