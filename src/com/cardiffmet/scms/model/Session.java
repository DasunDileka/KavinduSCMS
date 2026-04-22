package com.cardiffmet.scms.model;

import java.util.Objects;

/**
 * Logged-in user context (prototype — in production use secure session tokens).
 */
public final class Session {
    private final String username;
    private final String displayName;
    private final UserRole role;

    public Session(String username, String displayName, UserRole role) {
        this.username = Objects.requireNonNull(username).trim();
        this.displayName = Objects.requireNonNullElse(displayName, username).trim();
        this.role = Objects.requireNonNull(role);
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public UserRole getRole() {
        return role;
    }
}
