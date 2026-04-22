package com.cardiffmet.scms.model;

/**
 * User roles for the Smart Campus Management System.
 */
public enum UserRole {
    ADMINISTRATOR("Administrator"),
    STAFF_MEMBER("Staff Member"),
    STUDENT("Student");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
