package com.cardiffmet.scms.notify;

import com.cardiffmet.scms.auth.UserDirectory;
import com.cardiffmet.scms.model.BookingStatus;
import com.cardiffmet.scms.model.NotificationCategory;
import com.cardiffmet.scms.service.CampusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Observer / notification: events published when bookings and maintenance change.
 */
class NotificationTriggersTest {

    private CampusRepository repo;
    private final List<ScmsEvent> captured = new ArrayList<>();

    @BeforeEach
    void setUp() {
        UserDirectory users = UserDirectory.withDemoAccounts();
        repo = new CampusRepository(users);
        captured.clear();
        repo.getNotificationCenter().subscribe(event -> captured.add(event));
    }

    @Test
    void approvedBookingEmitsEventWithBookingCategory() {
        repo.addBooking("ROOM-L101", "staff", LocalDate.now().plusDays(3),
                LocalTime.of(10, 0), LocalTime.of(11, 0), "Meeting", BookingStatus.APPROVED);

        assertFalse(captured.isEmpty());
        assertTrue(captured.stream().anyMatch(e ->
                e.category() == NotificationCategory.BOOKING && e.message().contains("confirmed")));
    }

    @Test
    void maintenanceCreationNotifiesAdminsAndReporter() {
        repo.addMaintenance("ROOM-M305", "Leak", "Ceiling stain", com.cardiffmet.scms.model.Urgency.CRITICAL, "student");

        assertTrue(captured.stream().anyMatch(e -> e.message().contains("New maintenance")));
        assertTrue(captured.stream().anyMatch(e -> e.message().contains("received")));
        assertTrue(captured.stream().anyMatch(e -> e.recipients().contains("admin")));
        assertTrue(captured.stream().anyMatch(e -> e.recipients().contains("student")));
    }

    @Test
    void assignMaintenanceBroadcastsMaintenanceCategory() {
        long id = repo.addMaintenance("ROOM-S001", "Door", "Sticks", com.cardiffmet.scms.model.Urgency.NORMAL, "staff").getId();

        captured.clear();
        repo.assignMaintenance(id, "Team B");

        assertFalse(captured.isEmpty());
        assertTrue(captured.stream().anyMatch(e ->
                e.category() == NotificationCategory.MAINTENANCE && e.message().contains("assigned")));
    }
}
