package ru.loolzaaa.telegram.servicebot.core.bot.pojo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class User {

    private Long id;

    private LocalDateTime lastActivity;

    private List<TrackEntry> trackHistory;

    public User() {
    }

    public User(Long id) {
        this.id = id;
        this.trackHistory = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(LocalDateTime lastActivity) {
        this.lastActivity = lastActivity;
    }

    public List<TrackEntry> getTrackHistory() {
        return trackHistory;
    }

    public void setTrackHistory(List<TrackEntry> trackHistory) {
        this.trackHistory = trackHistory;
    }
}
