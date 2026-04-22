package com.cardiffmet.scms.model;

public enum BookingStatus {
    PENDING("Pending"),
    APPROVED("Approved"),
    DECLINED("Declined");

    private final String label;

    BookingStatus(String label) {
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
