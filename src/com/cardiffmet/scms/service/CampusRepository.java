package com.cardiffmet.scms.service;

import com.cardiffmet.scms.model.Announcement;
import com.cardiffmet.scms.model.Booking;
import com.cardiffmet.scms.model.BookingStatus;
import com.cardiffmet.scms.model.MaintenanceRequest;
import com.cardiffmet.scms.model.MaintenanceStatus;
import com.cardiffmet.scms.model.Room;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Central prototype data store for rooms, bookings, maintenance, and announcements.
 */
public final class CampusRepository {

    private final List<Room> rooms = new ArrayList<>();
    private final List<Booking> bookings = new ArrayList<>();
    private final List<MaintenanceRequest> maintenanceRequests = new ArrayList<>();
    private final List<Announcement> announcements = new ArrayList<>();

    private final AtomicLong bookingId = new AtomicLong(1);
    private final AtomicLong maintenanceId = new AtomicLong(1);
    private final AtomicLong announcementId = new AtomicLong(1);

    public CampusRepository() {
        seedRooms();
        seedAnnouncements();
    }

    private void seedRooms() {
        rooms.add(new Room("ROOM-L101", "Lecture Theatre L101", "Llandaff Campus", 120,
                "Projector, PA, wheelchair access"));
        rooms.add(new Room("ROOM-C204", "Computer Lab C204", "Cyncoed Campus", 40,
                "35 PCs, dual monitors, AV lectern"));
        rooms.add(new Room("ROOM-M305", "Seminar Room M305", "Llandaff Campus", 25,
                "Display screen, whiteboards, hybrid camera"));
        rooms.add(new Room("ROOM-S001", "Collaborative Study Pod S001", "Cyncoed Campus", 8,
                "Screen share, writable walls"));
        rooms.add(new Room("ROOM-HALLA", "Sports Hall — Main Court", "Cyncoed Campus", 200,
                "Markings, bleachers (by arrangement)"));
    }

    private void seedAnnouncements() {
        postAnnouncement("Campus Wi-Fi maintenance", "Wireless upgrades in Cyncoed H block Sat 06:00–09:00.",
                "system");
        postAnnouncement("Exam period quiet zones", "Llandaff library levels 2–3 are quiet study 08:00–20:00.",
                "system");
    }

    public List<Room> getRooms() {
        synchronized (rooms) {
            return List.copyOf(rooms);
        }
    }

    public Optional<Room> findRoom(String roomId) {
        synchronized (rooms) {
            return rooms.stream().filter(r -> r.getId().equals(roomId)).findFirst();
        }
    }

    public void updateRoom(Room updated) {
        synchronized (rooms) {
            for (int i = 0; i < rooms.size(); i++) {
                if (rooms.get(i).getId().equals(updated.getId())) {
                    rooms.set(i, updated);
                    return;
                }
            }
        }
        throw new IllegalArgumentException("Unknown room: " + updated.getId());
    }

    /**
     * Replaces mutable fields on the existing room instance (table model may hold references).
     */
    public void updateRoomFields(String roomId, String name, String building, int capacity, String equipment) {
        synchronized (rooms) {
            Optional<Room> opt = rooms.stream().filter(r -> r.getId().equals(roomId)).findFirst();
            if (opt.isEmpty()) {
                throw new IllegalArgumentException("Unknown room: " + roomId);
            }
            Room r = opt.get();
            r.setName(name);
            r.setBuilding(building);
            r.setCapacity(capacity);
            r.setEquipmentSummary(equipment);
        }
    }

    public List<Booking> getBookings() {
        synchronized (bookings) {
            return List.copyOf(bookings);
        }
    }

    public List<Booking> bookingsForUser(String username) {
        String u = username.toLowerCase(Locale.ROOT);
        synchronized (bookings) {
            return bookings.stream()
                    .filter(b -> b.getRequestedByUsername().equalsIgnoreCase(u))
                    .collect(Collectors.toList());
        }
    }

    public Booking addBooking(String roomId, String requestedByUsername, LocalDate date,
                              LocalTime start, LocalTime end, String purpose, BookingStatus initialStatus) {
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }
        if (hasConflict(roomId, date, start, end, null)) {
            throw new IllegalStateException("That slot overlaps another approved booking for this room.");
        }
        long id = bookingId.getAndIncrement();
        Booking b = new Booking(id, roomId, requestedByUsername.trim(), date, start, end,
                purpose, initialStatus);
        synchronized (bookings) {
            bookings.add(b);
        }
        return b;
    }

    public void setBookingStatus(long bookingIdValue, BookingStatus status) {
        synchronized (bookings) {
            for (Booking b : bookings) {
                if (b.getId() == bookingIdValue) {
                    if (status == BookingStatus.APPROVED) {
                        if (hasConflict(b.getRoomId(), b.getDate(), b.getStart(), b.getEnd(), b.getId())) {
                            throw new IllegalStateException("Approval would overlap another approved booking.");
                        }
                    }
                    b.setStatus(status);
                    return;
                }
            }
        }
        throw new IllegalArgumentException("Booking not found: " + bookingIdValue);
    }

    private boolean hasConflict(String roomId, LocalDate date, LocalTime start, LocalTime end,
                               Long ignoreBookingId) {
        synchronized (bookings) {
            for (Booking b : bookings) {
                if (b.getStatus() != BookingStatus.APPROVED) {
                    continue;
                }
                if (ignoreBookingId != null && b.getId() == ignoreBookingId) {
                    continue;
                }
                if (!b.getRoomId().equals(roomId) || !b.getDate().equals(date)) {
                    continue;
                }
                if (start.isBefore(b.getEnd()) && end.isAfter(b.getStart())) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<MaintenanceRequest> getMaintenanceRequests() {
        synchronized (maintenanceRequests) {
            return List.copyOf(maintenanceRequests);
        }
    }

    public MaintenanceRequest addMaintenance(String title, String description, String reportedBy) {
        long id = maintenanceId.getAndIncrement();
        MaintenanceRequest m = new MaintenanceRequest(id, title, description, reportedBy.trim(),
                LocalDateTime.now(), MaintenanceStatus.OPEN);
        synchronized (maintenanceRequests) {
            maintenanceRequests.add(m);
        }
        return m;
    }

    public void setMaintenanceStatus(long requestId, MaintenanceStatus status) {
        synchronized (maintenanceRequests) {
            for (MaintenanceRequest m : maintenanceRequests) {
                if (m.getId() == requestId) {
                    m.setStatus(status);
                    return;
                }
            }
        }
        throw new IllegalArgumentException("Maintenance request not found: " + requestId);
    }

    public List<Announcement> getAnnouncementsDescending() {
        synchronized (announcements) {
            List<Announcement> copy = new ArrayList<>(announcements);
            copy.sort((a, b) -> b.getPostedAt().compareTo(a.getPostedAt()));
            return List.copyOf(copy);
        }
    }

    public Announcement postAnnouncement(String title, String body, String authorUsername) {
        long id = announcementId.getAndIncrement();
        Announcement a = new Announcement(id, title, body, authorUsername.trim(), LocalDateTime.now());
        synchronized (announcements) {
            announcements.add(a);
        }
        return a;
    }
}
