package com.popularpenguin.runapp.data;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Locale;

/** Data tracked into the session */
public class Session {
    private long id;
    private Challenge challenge;
    private String date;
    private long time;
    private List<LatLng> path;
    private boolean isCompleted;

    public Session(long id,
                   Challenge challenge,
                   String date,
                   long time,
                   List<LatLng> path,
                   boolean isCompleted) {

        this.id = id;
        this.challenge = challenge;
        this.date = date;
        this.time = time;
        this.path = path;
        this.isCompleted = isCompleted;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public void setChallenge(Challenge challenge) {
        this.challenge = challenge;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getTime() {
        return time;
    }

    public String getTimeString() {
        if (time == 0L) {
            return "-:--:--";
        }

        long seconds = time / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds %= 60;
        minutes %= 60;

        return String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds);
    }

    public void setTime(long time) {
        this.time = time;
    }

    public List<LatLng> getPath() {
        return path;
    }

    public void setPath(List<LatLng> path) {
        this.path = path;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
