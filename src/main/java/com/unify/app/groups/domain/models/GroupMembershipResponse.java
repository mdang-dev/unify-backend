package com.unify.app.groups.domain.models;

public record GroupMembershipResponse(boolean isMember, boolean isOwner, String role) {}
