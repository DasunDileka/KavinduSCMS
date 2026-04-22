package com.cardiffmet.scms.model;

import java.time.LocalDateTime;
import java.util.Objects;

public final class Announcement {
    private final long id;
    private final String title;
    private final String body;
    private final String authorUsername;
    private final LocalDateTime postedAt;

    public Announcement(long id, String title, String body, String authorUsername, LocalDateTime postedAt) {
        this.id = id;
        this.title = Objects.requireNonNull(title).trim();
        this.body = Objects.requireNonNull(body).trim();
        this.authorUsername = Objects.requireNonNull(authorUsername);
        this.postedAt = Objects.requireNonNull(postedAt);
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public LocalDateTime getPostedAt() {
        return postedAt;
    }
}
