package com.popularpenguin.runapp.data;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/** Data tracked into the session */
public class Session {
    long id;
    Challenge challenge;
    long time;
    List<LatLng> route;
    boolean isCompleted;


}
