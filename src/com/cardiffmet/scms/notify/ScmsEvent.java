package com.cardiffmet.scms.notify;

import com.cardiffmet.scms.model.NotificationCategory;

import java.util.List;

/**
 * Event broadcast to observers when bookings or maintenance change.
 *
 * @param message    Human-readable notification text shown to users.
 * @param category   BOOKING / MAINTENANCE / SYSTEM.
 * @param recipients Usernames that should receive an in-app notification.
 */
public record ScmsEvent(String message, NotificationCategory category, List<String> recipients) {
}
