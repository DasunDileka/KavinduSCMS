package com.cardiffmet.scms.facade;

import com.cardiffmet.scms.auth.UserDirectory;
import com.cardiffmet.scms.notify.NotificationCenter;
import com.cardiffmet.scms.service.CampusRepository;

import java.util.Objects;

/**
 * Structural <strong>Facade</strong>: one entry point for the “campus backend” subsystem
 * ({@link CampusRepository}, {@link UserDirectory}, notifications) so the UI layer does not
 * construct or orchestrate those components directly.
 */
public final class CampusServicesFacade {

    private final UserDirectory userDirectory;
    private final CampusRepository campusRepository;

    private CampusServicesFacade(UserDirectory userDirectory, CampusRepository campusRepository) {
        this.userDirectory = Objects.requireNonNull(userDirectory);
        this.campusRepository = Objects.requireNonNull(campusRepository);
    }

    /** Wires demo users, repository, and observer-based notifications for the Swing prototype. */
    public static CampusServicesFacade bootstrap() {
        UserDirectory users = UserDirectory.withDemoAccounts();
        CampusRepository campus = new CampusRepository(users);
        return new CampusServicesFacade(users, campus);
    }

    public UserDirectory users() {
        return userDirectory;
    }

    public CampusRepository campus() {
        return campusRepository;
    }

    /** Convenience: notification hub (subject in the Observer pattern). */
    public NotificationCenter notifications() {
        return campusRepository.getNotificationCenter();
    }
}
