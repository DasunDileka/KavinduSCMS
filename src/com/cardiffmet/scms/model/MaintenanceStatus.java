package com.cardiffmet.scms.model;

/**
 * Maintenance workflow: Pending → Assigned → Completed.
 */
public enum MaintenanceStatus {
    PENDING("Pending"),
    ASSIGNED("Assigned"),
    COMPLETED("Completed");

    private final String label;

    MaintenanceStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
