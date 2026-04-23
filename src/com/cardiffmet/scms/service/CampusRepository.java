package com.cardiffmet.scms.service;

import com.cardiffmet.scms.auth.UserDirectory;
import com.cardiffmet.scms.creational.RoomBuilder;
import com.cardiffmet.scms.model.Announcement;
import com.cardiffmet.scms.model.Booking;
import com.cardiffmet.scms.model.BookingStatus;
import com.cardiffmet.scms.model.MaintenanceRequest;
import com.cardiffmet.scms.model.MaintenanceStatus;
import com.cardiffmet.scms.model.NotificationCategory;
import com.cardiffmet.scms.model.Room;
import com.cardiffmet.scms.model.Urgency;
import com.cardiffmet.scms.model.UserNotification;
import com.cardiffmet.scms.model.UserRole;
import com.cardiffmet.scms.notify.NotificationCenter;
import com.cardiffmet.scms.notify.ScmsEvent;
import com.cardiffmet.scms.notify.ScmsObserver;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Central prototype data store; publishes {@link ScmsEvent}s through {@link NotificationCenter} (Observer).
 */
public final class CampusRepository {

    private final UserDirectory userDirectory;
    private final NotificationCenter notificationCenter = new NotificationCenter();

    private final List<Room> rooms = new ArrayList<>();
    private final List<Booking> bookings = new ArrayList<>();
    private final List<MaintenanceRequest> maintenanceRequests = new ArrayList<>();
    private final List<Announcement> announcements = new ArrayList<>();

    /** username(lowercase) → inbox notifications */
    private final Map<String, List<UserNotification>> inboxByUser = new LinkedHashMap<>();

    private final AtomicLong bookingId = new AtomicLong(1);
    private final AtomicLong maintenanceId = new AtomicLong(1);
    private final AtomicLong announcementId = new AtomicLong(1);
    private final AtomicLong notificationId = new AtomicLong(1);
    private final AtomicLong roomSeq = new AtomicLong(1);

    public CampusRepository(UserDirectory userDirectory) {
        this.userDirectory = Objects.requireNonNull(userDirectory);
        seedRooms();
        seedAnnouncements();
        notificationCenter.subscribe(new InboxObserver());
    }

    public NotificationCenter getNotificationCenter() {
        return notificationCenter;
    }

    public UserDirectory getUserDirectory() {
        return userDirectory;
    }

    private void seedRooms() {
        rooms.add(new RoomBuilder().id("ROOM-L101").name("Lecture Theatre L101").building("Llandaff Campus")
                .capacity(120).equipmentSummary("Projector, PA, wheelchair access").active(true).build());
        rooms.add(new RoomBuilder().id("ROOM-C204").name("Computer Lab C204").building("Cyncoed Campus")
                .capacity(40).equipmentSummary("35 PCs, dual monitors, AV lectern").active(true).build());
        rooms.add(new RoomBuilder().id("ROOM-M305").name("Seminar Room M305").building("Llandaff Campus")
                .capacity(25).equipmentSummary("Display screen, whiteboards, hybrid camera").active(true).build());
        rooms.add(new RoomBuilder().id("ROOM-S001").name("Collaborative Study Pod S001").building("Cyncoed Campus")
                .capacity(8).equipmentSummary("Screen share, writable walls").active(true).build());
        rooms.add(new RoomBuilder().id("ROOM-HALLA").name("Sports Hall — Main Court").building("Cyncoed Campus")
                .capacity(200).equipmentSummary("Markings, bleachers (by arrangement)").active(true).build());
    }

    private void seedAnnouncements() {
        postAnnouncement("Campus Wi-Fi maintenance", "Wireless upgrades in Cyncoed H block Sat 06:00–09:00.",
                "system");
        postAnnouncement("Exam period quiet zones", "Llandaff library levels 2–3 are quiet study 08:00–20:00.",
                "system");
    }

    /** All rooms (including inactive) — for administrators. */
    public List<Room> getRooms() {
        synchronized (rooms) {
            return List.copyOf(rooms);
        }
    }

    /** Active rooms only — for booking pickers. */
    public List<Room> getActiveRooms() {
        synchronized (rooms) {
            return rooms.stream().filter(Room::isActive).collect(Collectors.toUnmodifiableList());
        }
    }

    public Room addRoom(String name, String building, int capacity, String equipmentSummary) {
        String id = "ROOM-GEN-" + roomSeq.getAndIncrement();
        Room r = new RoomBuilder().id(id).name(name).building(building).capacity(capacity)
                .equipmentSummary(Objects.requireNonNullElse(equipmentSummary, "")).active(true).build();
        synchronized (rooms) {
            rooms.add(r);
        }
        return r;
    }

    public void setRoomActive(String roomId, boolean active) {
        synchronized (rooms) {
            Room r = rooms.stream().filter(x -> x.getId().equals(roomId)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown room: " + roomId));
            r.setActive(active);
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
        Room room = findRoom(roomId).orElseThrow(() -> new IllegalArgumentException("Unknown room."));
        if (!room.isActive()) {
            throw new IllegalStateException("This room is deactivated and cannot be booked.");
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
        String who = requestedByUsername.trim().toLowerCase(Locale.ROOT);
        if (initialStatus == BookingStatus.APPROVED) {
            publish(new ScmsEvent(
                    "Booking confirmed: " + room.getName() + " on " + date + " " + start + "–" + end + ".",
                    NotificationCategory.BOOKING,
                    List.of(who)));
        } else if (initialStatus == BookingStatus.PENDING) {
            publish(new ScmsEvent(
                    "Booking request pending: " + room.getName() + " on " + date + " " + start + "–" + end + ".",
                    NotificationCategory.BOOKING,
                    List.of(who)));
        }
        return b;
    }

    /**
     * Weekly recurrence: same weekday and time for {@code weekCount} consecutive weeks starting at firstOccurrence.
     */
    public BookingSeriesResult addWeeklyBookingSeries(String roomId, String requestedByUsername,
                                                      LocalDate firstOccurrence, LocalTime start, LocalTime end,
                                                      String purpose, BookingStatus initialStatus, int weekCount) {
        if (weekCount < 1 || weekCount > 52) {
            throw new IllegalArgumentException("Week count must be between 1 and 52.");
        }
        int created = 0;
        int skipped = 0;
        for (int w = 0; w < weekCount; w++) {
            LocalDate d = firstOccurrence.plusWeeks(w);
            try {
                addBooking(roomId, requestedByUsername, d, start, end, purpose, initialStatus);
                created++;
            } catch (IllegalStateException | IllegalArgumentException ex) {
                skipped++;
            }
        }
        String summary = created + " session(s) booked" + (skipped > 0 ? "; " + skipped + " skipped (clash or inactive room)." : ".");
        return new BookingSeriesResult(created, skipped, summary);
    }

    public void setBookingStatus(long bookingIdValue, BookingStatus newStatus) {
        Booking previous;
        synchronized (bookings) {
            Booking found = null;
            for (Booking b : bookings) {
                if (b.getId() == bookingIdValue) {
                    found = b;
                    break;
                }
            }
            if (found == null) {
                throw new IllegalArgumentException("Booking not found: " + bookingIdValue);
            }
            previous = found;
            if (newStatus == BookingStatus.APPROVED) {
                if (hasConflict(found.getRoomId(), found.getDate(), found.getStart(), found.getEnd(), found.getId())) {
                    throw new IllegalStateException("Approval would overlap another approved booking.");
                }
            }
            found.setStatus(newStatus);
        }
        String user = previous.getRequestedByUsername().toLowerCase(Locale.ROOT);
        Room room = findRoom(previous.getRoomId()).orElse(null);
        String roomLabel = room != null ? room.getName() : previous.getRoomId();
        switch (newStatus) {
            case APPROVED -> publish(new ScmsEvent(
                    "Booking approved: " + roomLabel + " on " + previous.getDate() + " "
                            + previous.getStart() + "–" + previous.getEnd() + ".",
                    NotificationCategory.BOOKING, List.of(user)));
            case DECLINED -> publish(new ScmsEvent(
                    "Booking declined: " + roomLabel + " on " + previous.getDate() + ".",
                    NotificationCategory.BOOKING, List.of(user)));
            case CANCELLED -> publish(new ScmsEvent(
                    "Booking cancelled: " + roomLabel + " on " + previous.getDate() + ".",
                    NotificationCategory.BOOKING, List.of(user)));
            case PENDING -> {
            }
        }
    }

    /**
     * Cancels a booking owned by the given user (Pending or Approved).
     */
    public void cancelBookingForUser(long bookingIdValue, String username) {
        String u = username.toLowerCase(Locale.ROOT);
        synchronized (bookings) {
            for (Booking b : bookings) {
                if (b.getId() == bookingIdValue) {
                    if (!b.getRequestedByUsername().equalsIgnoreCase(u)) {
                        throw new IllegalStateException("You can only cancel your own bookings.");
                    }
                    if (b.getStatus() != BookingStatus.PENDING && b.getStatus() != BookingStatus.APPROVED) {
                        throw new IllegalStateException("Only pending or approved bookings can be cancelled.");
                    }
                    b.setStatus(BookingStatus.CANCELLED);
                    publish(new ScmsEvent(
                            "You cancelled booking #" + b.getId() + " (" + b.getDate() + ").",
                            NotificationCategory.BOOKING, List.of(u)));
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

    public MaintenanceRequest addMaintenance(String roomId, String title, String description,
                                             Urgency urgency, String reportedByUsername) {
        findRoom(roomId).orElseThrow(() -> new IllegalArgumentException("Unknown room."));
        long id = maintenanceId.getAndIncrement();
        MaintenanceRequest m = new MaintenanceRequest(id, roomId, title, description, urgency,
                reportedByUsername.trim(), LocalDateTime.now(), MaintenanceStatus.PENDING);
        synchronized (maintenanceRequests) {
            maintenanceRequests.add(m);
        }
        List<String> admins = userDirectory.listUsernamesWithRole(UserRole.ADMINISTRATOR);
        publish(new ScmsEvent(
                "New maintenance #" + id + ": \"" + title + "\" (" + urgency.getLabel() + ").",
                NotificationCategory.MAINTENANCE, admins));
        String reporter = reportedByUsername.trim().toLowerCase(Locale.ROOT);
        publish(new ScmsEvent(
                "Your maintenance request #" + id + " was received (Pending).",
                NotificationCategory.MAINTENANCE, List.of(reporter)));
        return m;
    }

    /**
     * Assigns a maintainer or team label and moves status to Assigned.
     */
    public void assignMaintenance(long requestId, String assignedToLabel) {
        String label = assignedToLabel == null ? "" : assignedToLabel.trim();
        if (label.isEmpty()) {
            throw new IllegalArgumentException("Assignee name or team is required.");
        }
        MaintenanceRequest updated;
        synchronized (maintenanceRequests) {
            MaintenanceRequest m = maintenanceRequests.stream().filter(x -> x.getId() == requestId).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Maintenance request not found: " + requestId));
            m.setAssignedTo(label);
            m.setStatus(MaintenanceStatus.ASSIGNED);
            updated = m;
        }
        notifyMaintenanceStakeholders(updated, "Maintenance #" + requestId + " assigned to " + label + ".");
    }

    public void setMaintenanceStatus(long requestId, MaintenanceStatus newStatus) {
        MaintenanceRequest previous;
        synchronized (maintenanceRequests) {
            MaintenanceRequest m = maintenanceRequests.stream().filter(x -> x.getId() == requestId).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Maintenance request not found: " + requestId));
            previous = m;
            m.setStatus(newStatus);
        }
        notifyMaintenanceStakeholders(previous,
                "Maintenance #" + requestId + " is now " + newStatus.getLabel() + ".");
    }

    private void notifyMaintenanceStakeholders(MaintenanceRequest m, String message) {
        List<String> recipients = new ArrayList<>();
        recipients.add(m.getReportedByUsername().toLowerCase(Locale.ROOT));
        recipients.addAll(userDirectory.listUsernamesWithRole(UserRole.ADMINISTRATOR));
        publish(new ScmsEvent(message, NotificationCategory.MAINTENANCE, recipients.stream().distinct().toList()));
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

    public List<UserNotification> getUserNotifications(String username) {
        String key = username.toLowerCase(Locale.ROOT);
        synchronized (inboxByUser) {
            List<UserNotification> list = inboxByUser.get(key);
            if (list == null) {
                return List.of();
            }
            List<UserNotification> copy = new ArrayList<>(list);
            copy.sort(Comparator.comparing(UserNotification::getCreatedAt).reversed());
            return List.copyOf(copy);
        }
    }

    public void markNotificationRead(long notificationId, String username) {
        String key = username.toLowerCase(Locale.ROOT);
        synchronized (inboxByUser) {
            List<UserNotification> list = inboxByUser.get(key);
            if (list == null) {
                return;
            }
            for (UserNotification n : list) {
                if (n.getId() == notificationId) {
                    n.setRead(true);
                    return;
                }
            }
        }
    }

    public void markAllNotificationsRead(String username) {
        String key = username.toLowerCase(Locale.ROOT);
        synchronized (inboxByUser) {
            List<UserNotification> list = inboxByUser.get(key);
            if (list == null) {
                return;
            }
            for (UserNotification n : list) {
                n.setRead(true);
            }
        }
    }

    private void publish(ScmsEvent event) {
        if (event.recipients().isEmpty()) {
            return;
        }
        notificationCenter.publish(event);
    }

    private final class InboxObserver implements ScmsObserver {
        @Override
        public void onEvent(ScmsEvent event) {
            LocalDateTime now = LocalDateTime.now();
            for (String recipient : event.recipients()) {
                String key = recipient.toLowerCase(Locale.ROOT);
                synchronized (inboxByUser) {
                    long nid = notificationId.getAndIncrement();
                    inboxByUser.computeIfAbsent(key, k -> new ArrayList<>()).add(
                            0,
                            new UserNotification(nid, key, event.message(), now, event.category(), false));
                }
            }
        }
    }
}
