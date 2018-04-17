package com.popularpenguin.runapp.data;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/** Data tracked into the session */
public class Session {
    private long id;
    private Challenge challenge;
    private long time;
    private List<LatLng> path;
    private boolean isCompleted;

    public Session(long id, Challenge challenge, long time, List<LatLng> path, boolean isCompleted) {
        this.id = id;
        this.challenge = challenge;
        this.time = time;
        this.path = path;
        this.isCompleted = isCompleted;
    }

}
