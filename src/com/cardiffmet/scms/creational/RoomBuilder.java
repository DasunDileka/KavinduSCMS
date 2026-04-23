package com.cardiffmet.scms.creational;

import com.cardiffmet.scms.model.Room;

import java.util.Objects;

/**
 * Creational <strong>Builder</strong>: constructs {@link Room} instances step-by-step with a fluent API,
 * avoiding long constructor argument lists at call sites (e.g. seeded catalog + generated rooms).
 */
public final class RoomBuilder {

    private String id;
    private String name = "";
    private String building = "";
    private int capacity = 1;
    private String equipmentSummary = "";
    private boolean active = true;

    public RoomBuilder id(String id) {
        this.id = id;
        return this;
    }

    public RoomBuilder name(String name) {
        this.name = Objects.requireNonNullElse(name, "");
        return this;
    }

    public RoomBuilder building(String building) {
        this.building = Objects.requireNonNullElse(building, "");
        return this;
    }

    public RoomBuilder capacity(int capacity) {
        this.capacity = capacity;
        return this;
    }

    public RoomBuilder equipmentSummary(String equipmentSummary) {
        this.equipmentSummary = equipmentSummary;
        return this;
    }

    public RoomBuilder active(boolean active) {
        this.active = active;
        return this;
    }

    public Room build() {
        Objects.requireNonNull(id, "room id");
        if (id.isBlank()) {
            throw new IllegalStateException("room id cannot be blank");
        }
        return new Room(id.trim(), name.trim(), building.trim(), capacity,
                Objects.requireNonNullElse(equipmentSummary, "").trim(), active);
    }
}
