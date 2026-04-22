package com.cardiffmet.scms.model;

import java.util.Objects;

/**
 * Directory user record. Password is stored in plain text for this prototype only.
 */
public final class UserAccount {
    private final String username;
    private String password;
    private String displayName;
    private UserRole role;

    public UserAccount(String username, String password, String displayName, UserRole role) {
        this.username = Objects.requireNonNull(username).trim().toLowerCase();
        this.password = Objects.requireNonNull(password);
        this.displayName = Objects.requireNonNullElse(displayName, username).trim();
        this.role = Objects.requireNonNull(role);
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = Objects.requireNonNull(password);
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = Objects.requireNonNullElse(displayName, username).trim();
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = Objects.requireNonNull(role);
    }
}
