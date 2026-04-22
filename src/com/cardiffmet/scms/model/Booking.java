package com.cardiffmet.scms.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * A room booking or booking request.
 */
public final class Booking {
    private final long id;
    private final String roomId;
    private final String requestedByUsername;
    private final LocalDate date;
    private final LocalTime start;
    private final LocalTime end;
    private final String purpose;
    private BookingStatus status;

    public Booking(long id, String roomId, String requestedByUsername, LocalDate date,
                   LocalTime start, LocalTime end, String purpose, BookingStatus status) {
        this.id = id;
        this.roomId = Objects.requireNonNull(roomId);
        this.requestedByUsername = Objects.requireNonNull(requestedByUsername);
        this.date = Objects.requireNonNull(date);
        this.start = Objects.requireNonNull(start);
        this.end = Objects.requireNonNull(end);
        this.purpose = Objects.requireNonNullElse(purpose, "").trim();
        this.status = Objects.requireNonNull(status);
    }

    public long getId() {
        return id;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getRequestedByUsername() {
        return requestedByUsername;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStart() {
        return start;
    }

    public LocalTime getEnd() {
        return end;
    }

    public String getPurpose() {
        return purpose;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = Objects.requireNonNull(status);
    }
}
