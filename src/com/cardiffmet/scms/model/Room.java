package com.cardiffmet.scms.model;

import java.util.Objects;

/**
 * A campus room; inactive rooms are hidden from booking pickers but visible to administrators.
 */
public final class Room {
    private final String id;
    private String name;
    private String building;
    private int capacity;
    private String equipmentSummary;
    private boolean active;

    public Room(String id, String name, String building, int capacity, String equipmentSummary, boolean active) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.building = Objects.requireNonNull(building);
        this.capacity = capacity;
        this.equipmentSummary = Objects.requireNonNullElse(equipmentSummary, "");
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = Objects.requireNonNull(building);
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getEquipmentSummary() {
        return equipmentSummary;
    }

    public void setEquipmentSummary(String equipmentSummary) {
        this.equipmentSummary = Objects.requireNonNullElse(equipmentSummary, "");
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return name + " (" + building + ")";
    }
}
