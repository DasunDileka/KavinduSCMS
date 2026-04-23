package com.cardiffmet.scms.model;

import java.time.LocalDateTime;
import java.util.Objects;

public final class UserNotification {
    private final long id;
    private final String username;
    private final String message;
    private final LocalDateTime createdAt;
    private final NotificationCategory category;
    private boolean read;

    public UserNotification(long id, String username, String message, LocalDateTime createdAt,
                            NotificationCategory category, boolean read) {
        this.id = id;
        this.username = Objects.requireNonNull(username);
        this.message = Objects.requireNonNull(message);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.category = Objects.requireNonNull(category);
        this.read = read;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public NotificationCategory getCategory() {
        return category;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
