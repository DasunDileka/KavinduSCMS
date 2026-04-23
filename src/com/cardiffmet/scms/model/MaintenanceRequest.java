package com.cardiffmet.scms.model;

import java.time.LocalDateTime;
import java.util.Objects;

public final class MaintenanceRequest {
    private final long id;
    private final String roomId;
    private final String title;
    private final String description;
    private final Urgency urgency;
    private final String reportedByUsername;
    private final LocalDateTime createdAt;
    private MaintenanceStatus status;
    private String assignedTo;

    public MaintenanceRequest(long id, String roomId, String title, String description, Urgency urgency,
                              String reportedByUsername, LocalDateTime createdAt, MaintenanceStatus status) {
        this.id = id;
        this.roomId = Objects.requireNonNull(roomId).trim();
        this.title = Objects.requireNonNull(title).trim();
        this.description = Objects.requireNonNull(description).trim();
        this.urgency = Objects.requireNonNull(urgency);
        this.reportedByUsername = Objects.requireNonNull(reportedByUsername).trim();
        this.createdAt = Objects.requireNonNull(createdAt);
        this.status = Objects.requireNonNull(status);
        this.assignedTo = "";
    }

    public long getId() {
        return id;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Urgency getUrgency() {
        return urgency;
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

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = Objects.requireNonNullElse(assignedTo, "").trim();
    }
}
