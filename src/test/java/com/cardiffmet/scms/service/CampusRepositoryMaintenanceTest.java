package com.cardiffmet.scms.service;

import com.cardiffmet.scms.auth.UserDirectory;
import com.cardiffmet.scms.model.MaintenanceStatus;
import com.cardiffmet.scms.model.Urgency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for maintenance lifecycle: create, assign, status updates.
 */
class CampusRepositoryMaintenanceTest {

    private CampusRepository repo;

    @BeforeEach
    void setUp() {
        repo = new CampusRepository(UserDirectory.withDemoAccounts());
    }

    @Test
    void newRequestIsPendingWithRoomAndUrgency() {
        var m = repo.addMaintenance("ROOM-C204", "Projector fault", "No signal", Urgency.HIGH, "student");

        assertEquals(MaintenanceStatus.PENDING, m.getStatus());
        assertEquals("ROOM-C204", m.getRoomId());
        assertEquals(Urgency.HIGH, m.getUrgency());
    }

    @Test
    void assignSetsAssignedStatusAndAssignee() {
        long id = repo.addMaintenance("ROOM-S001", "Chair broken", "Leg loose", Urgency.NORMAL, "staff").getId();

        repo.assignMaintenance(id, "Estates Team A");

        var found = repo.getMaintenanceRequests().stream().filter(x -> x.getId() == id).findFirst().orElseThrow();
        assertEquals(MaintenanceStatus.ASSIGNED, found.getStatus());
        assertEquals("Estates Team A", found.getAssignedTo());
    }

    @Test
    void completedStatusApplied() {
        long id = repo.addMaintenance("ROOM-L101", "Lights", "Flicker", Urgency.LOW, "staff").getId();

        repo.setMaintenanceStatus(id, MaintenanceStatus.COMPLETED);

        var found = repo.getMaintenanceRequests().stream().filter(x -> x.getId() == id).findFirst().orElseThrow();
        assertEquals(MaintenanceStatus.COMPLETED, found.getStatus());
    }

    @Test
    void unknownRoomRejected() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                repo.addMaintenance("ROOM-UNKNOWN", "x", "y", Urgency.NORMAL, "staff"));
        assertTrue(ex.getMessage().toLowerCase().contains("unknown"));
    }

    @Test
    void assignRequiresNonBlankAssignee() {
        long id = repo.addMaintenance("ROOM-HALLA", "Floor", "Scuff", Urgency.NORMAL, "student").getId();

        assertThrows(IllegalArgumentException.class, () -> repo.assignMaintenance(id, "   "));
    }
}
