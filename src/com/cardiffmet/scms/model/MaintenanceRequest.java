package com.cardiffmet.scms.model;

import java.time.LocalDateTime;
import java.util.Objects;

public final class MaintenanceRequest {
    private final long id;
    private final String title;
    private final String description;
    private final String reportedByUsername;
    private final LocalDateTime createdAt;
    private MaintenanceStatus status;

    public MaintenanceRequest(long id, String title, String description, String reportedByUsername,
                              LocalDateTime createdAt, MaintenanceStatus status) {
        this.id = id;
        this.title = Objects.requireNonNull(title).trim();
        this.description = Objects.requireNonNull(description).trim();
        this.reportedByUsername = Objects.requireNonNull(reportedByUsername);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.status = Objects.requireNonNull(status);
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getReportedByUsername() {
        return reportedByUsername;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public MaintenanceStatus getStatus() {
        return status;
    }

    public void setStatus(MaintenanceStatus status) {
        this.status = Objects.requireNonNull(status);
    }
}
