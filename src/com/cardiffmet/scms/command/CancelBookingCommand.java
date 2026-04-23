package com.cardiffmet.scms.command;

import com.cardiffmet.scms.service.CampusRepository;

/**
 * Command for a user cancelling their own booking (staff/student dashboards).
 */
public final class CancelBookingCommand implements CampusCommand {

    private final CampusRepository repository;
    private final long bookingId;
    private final String username;

    public CancelBookingCommand(CampusRepository repository, long bookingId, String username) {
        this.repository = repository;
        this.bookingId = bookingId;
        this.username = username;
    }

    @Override
    public void execute() {
        repository.cancelBookingForUser(bookingId, username);
    }
}
