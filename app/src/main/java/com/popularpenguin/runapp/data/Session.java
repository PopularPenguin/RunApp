package com.popularpenguin.runapp.data;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Data tracked into the session */
public class Session {
    private static final String TAG = Session.class.getSimpleName();

    public static final String LAT_LNG_EXTRA = "session_lat_lng";
    public static final String DESCRIPTION_EXTRA = "session_description";
    public static final String TIME_EXTRA = "session_time";
    public static final String FASTEST_TIME_EXTRA = "session_fastest_time";

    private long id; // id from the database
    private Challenge challenge; // the challenge associated with this session
    private String date; // the date and time of the session
    private long time; // the user's run time
    private String pathString; // the serialized path of the run to display on the map
    private boolean isCompleted; // was the challenge successfully completed?

    public Session(Challenge challenge,
                   String date,
                   long time,
                   List<LatLng> path,
                   boolean isCompleted) {

        this.challenge = challenge;
        this.date = date;
        this.time = time;
        this.pathString = getPathString(path);
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

    /**
     * Get a formatted date and time to display in the session list
     * @return formatted String date/time
     */
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

    /**
     * Turns a serialized path String into a List of LatLng objects to create map polylines
     * @param pathString Serialized path
     * @return list of LatLng objects
     */
    public static List<LatLng> getPathLatLng(String pathString) {
        if (pathString == null || pathString.isEmpty()) {
            return new ArrayList<>();
        }

        Log.d(TAG, "String pathString: " + pathString);

        String[] points = pathString.split("!");

        List<LatLng> locationList = new ArrayList<>();

        for (String point : points) {
            String[] latLngString = point.split(",");

            Log.d(TAG, "String point: " + point);

            if (latLngString.length != 2) {
                throw new RuntimeException("Invalid string format for a LatLng point");
            }

            double lat = Double.valueOf(latLngString[0]);
            double lng = Double.valueOf(latLngString[1]);

            LatLng latLng = new LatLng(lat, lng);
            locationList.add(latLng);
        }

        return locationList;
    }

    public String getPath() {
        return pathString;
    }

    public void setPath(List<LatLng> path) {
        this.pathString = getPathString(path);
    }

    /**
     * Serialize the user's run path
     * @param path a list of LatLng points
     * @return String representation of the user's path
     */
    public static String getPathString(List<LatLng> path) {
        if (path == null || path.size() == 0) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (LatLng latLng : path) {
            sb.append(String.format(Locale.US, "%f,%f!", latLng.latitude, latLng.longitude));
        }
        // delete the last "-" to prevent parsing from trying to add another element after last
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
