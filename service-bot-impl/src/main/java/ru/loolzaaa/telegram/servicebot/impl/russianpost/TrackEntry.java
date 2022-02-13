package ru.loolzaaa.telegram.servicebot.impl.russianpost;

import java.time.LocalDateTime;
import java.util.Objects;

public class TrackEntry {

    private String number;
    private String description;

    private LocalDateTime lastActivity;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(LocalDateTime lastActivity) {
        this.lastActivity = lastActivity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrackEntry entry = (TrackEntry) o;
        return Objects.equals(number, entry.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }
}
