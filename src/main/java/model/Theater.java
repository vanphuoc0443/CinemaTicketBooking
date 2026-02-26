package model;

import java.sql.Timestamp;

public class Theater {
    private int theaterId;
    private String name;
    private int totalSeats;
    private boolean active;
    private Timestamp createdAt;

    public Theater() {
        this.totalSeats = 80;
        this.active = true;
    }

    public Theater(String name) {
        this.name = name;
        this.totalSeats = 80;
        this.active = true;
    }

    public Theater(int theaterId, String name, int totalSeats, boolean active) {
        this.theaterId = theaterId;
        this.name = name;
        this.totalSeats = totalSeats;
        this.active = active;
    }

    public int getTheaterId() {
        return theaterId;
    }

    public void setTheaterId(int theaterId) {
        this.theaterId = theaterId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return name;
    }
}
