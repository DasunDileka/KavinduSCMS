package com.cardiffmet.scms.command;

import com.cardiffmet.scms.model.BookingStatus;
import com.cardiffmet.scms.service.CampusRepository;

/**
 * Command to approve / decline booking requests (typically from the administrator dashboard).
 */
public final class SetBookingStatusCommand implements CampusCommand {

    private final CampusRepository repository;
    private final long bookingId;
    private final BookingStatus newStatus;

    public SetBookingStatusCommand(CampusRepository repository, long bookingId, BookingStatus newStatus) {
        this.repository = repository;
        this.bookingId = bookingId;
        this.newStatus = newStatus;
    }

    @Override
    public void execute() {
        repository.setBookingStatus(bookingId, newStatus);
    }
}
