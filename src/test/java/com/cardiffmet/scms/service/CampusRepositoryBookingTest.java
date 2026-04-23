package test.java.com.cardiffmet.scms.service;

import com.cardiffmet.scms.auth.UserDirectory;
import com.cardiffmet.scms.model.BookingStatus;
import com.cardiffmet.scms.service.CampusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for booking rules: approval, conflicts, inactive rooms, weekly series counts.
 */
class CampusRepositoryBookingTest {

    private static final String ROOM = "ROOM-L101";

    private CampusRepository repo;

    @BeforeEach
    void setUp() {
        UserDirectory users = UserDirectory.withDemoAccounts();
        repo = new CampusRepository(users);
    }

    @Test
    void approvedBookingBlocksOverlappingSecondBooking() {
        LocalDate d = LocalDate.now().plusDays(7);
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(11, 0);

        repo.addBooking(ROOM, "staff", d, start, end, "First", BookingStatus.APPROVED);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                repo.addBooking(ROOM, "staff", d, start, end, "Clash", BookingStatus.APPROVED));
        assertTrue(ex.getMessage().toLowerCase().contains("overlap"));
    }

    @Test
    void pendingBookingDoesNotBlockAnotherPendingSameSlot() {
        LocalDate d = LocalDate.now().plusDays(14);
        LocalTime start = LocalTime.of(14, 0);
        LocalTime end = LocalTime.of(15, 0);

        repo.addBooking(ROOM, "student", d, start, end, "A", BookingStatus.PENDING);
        repo.addBooking(ROOM, "student", d, start, end, "B", BookingStatus.PENDING);

        assertEquals(2, repo.getBookings().stream().filter(b -> b.getDate().equals(d)).count());
    }

    @Test
    void cannotBookDeactivatedRoom() {
        repo.setRoomActive("ROOM-M305", false);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                repo.addBooking("ROOM-M305", "staff", LocalDate.now().plusDays(1),
                        LocalTime.of(9, 0), LocalTime.of(10, 0), "x", BookingStatus.APPROVED));
        assertTrue(ex.getMessage().toLowerCase().contains("deactivated"));
    }

    @Test
    void invalidTimeRangeRejected() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                repo.addBooking(ROOM, "staff", LocalDate.now().plusDays(1),
                        LocalTime.of(12, 0), LocalTime.of(11, 0), "bad", BookingStatus.APPROVED));
        assertTrue(ex.getMessage().toLowerCase().contains("before"));
    }

    @Test
    void weeklySeriesSkipsConflictsWhenSecondWeekOverlapsApproved() {
        LocalDate firstMonday = LocalDate.now().plusWeeks(1);
        while (firstMonday.getDayOfWeek().getValue() != 1) {
            firstMonday = firstMonday.plusDays(1);
        }
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(10, 0);

        repo.addBooking(ROOM, "staff", firstMonday.plusWeeks(1), start, end, "Block", BookingStatus.APPROVED);

        com.cardiffmet.scms.service.BookingSeriesResult result = repo.addWeeklyBookingSeries(ROOM, "staff", firstMonday, start, end,
                "Series", BookingStatus.APPROVED, 3);

        assertTrue(result.createdCount() >= 1);
        assertTrue(result.skippedCount() >= 1);
    }
}
