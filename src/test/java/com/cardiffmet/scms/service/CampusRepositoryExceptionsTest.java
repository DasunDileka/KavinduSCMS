package test.java.com.cardiffmet.scms.service;

import com.cardiffmet.scms.auth.UserDirectory;
import com.cardiffmet.scms.model.BookingStatus;
import com.cardiffmet.scms.model.MaintenanceStatus;
import com.cardiffmet.scms.service.CampusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exception paths: illegal arguments, illegal state (ownership, overlaps, missing entities).
 */
class CampusRepositoryExceptionsTest {

    private CampusRepository repo;

    @BeforeEach
    void setUp() {
        repo = new CampusRepository(UserDirectory.withDemoAccounts());
    }

    @Test
    void unknownRoomThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                repo.addBooking("ROOM-NONE", "staff", LocalDate.now().plusDays(1),
                        LocalTime.of(9, 0), LocalTime.of(10, 0), "x", BookingStatus.APPROVED));
    }

    @Test
    void cancelSomeoneElsesBookingThrows() {
        LocalDate d = LocalDate.now().plusDays(20);
        long id = repo.addBooking("ROOM-C204", "staff", d, LocalTime.of(8, 0), LocalTime.of(9, 0),
                "Theirs", BookingStatus.APPROVED).getId();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                repo.cancelBookingForUser(id, "student"));
        assertTrue(ex.getMessage().contains("only cancel your own"));
    }

    @Test
    void approveOverlappingPendingBookingThrowsWhenConflictExists() {
        LocalDate d = LocalDate.now().plusDays(30);
        LocalTime s = LocalTime.of(13, 0);
        LocalTime e = LocalTime.of(14, 0);

        repo.addBooking("ROOM-HALLA", "staff", d, s, e, "Taken", BookingStatus.APPROVED);
        long pendingId = repo.addBooking("ROOM-HALLA", "student", d, s, e, "Ask", BookingStatus.PENDING).getId();

        assertThrows(IllegalStateException.class, () ->
                repo.setBookingStatus(pendingId, BookingStatus.APPROVED));
    }

    @Test
    void setBookingStatusUnknownIdThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                repo.setBookingStatus(999_999L, BookingStatus.APPROVED));
    }

    @Test
    void setMaintenanceStatusUnknownIdThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                repo.setMaintenanceStatus(999_999L, MaintenanceStatus.COMPLETED));
    }

    @Test
    void weeklySeriesInvalidWeekCountThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                repo.addWeeklyBookingSeries("ROOM-L101", "staff", LocalDate.now().plusDays(1),
                        LocalTime.of(9, 0), LocalTime.of(10, 0), "x", BookingStatus.APPROVED, 0));
    }

    @Test
    void duplicateUsernameRejectedInDirectory() {
        UserDirectory users = UserDirectory.withDemoAccounts();
        assertFalse(users.addAccount("admin", "x", "dup", com.cardiffmet.scms.model.UserRole.STUDENT));
    }
}
