package com.cardiffmet.scms.model;

import java.util.Objects;

/**
 * A bookable campus room (prototype data may be edited by administrators).
 */
public final class Room {
    private final String id;
    private String name;
    private String building;
    private int capacity;
    private String equipmentSummary;

    public Room(String id, String name, String building, int capacity, String equipmentSummary) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.building = Objects.requireNonNull(building);
        this.capacity = capacity;
        this.equipmentSummary = Objects.requireNonNullElse(equipmentSummary, "");
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

    @Override
    public String toString() {
        return name + " (" + building + ")";
    }
}
