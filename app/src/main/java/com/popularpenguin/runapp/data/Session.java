package com.popularpenguin.runapp.data;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.UUID;

/** Data tracked into the session */
public class Session {
    UUID uuid;
    long time;
    List<LatLng> route;
    boolean isCompleted;
}
