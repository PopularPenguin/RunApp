package com.popularpenguin.runapp.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.popularpenguin.runapp.data.Challenge;
import com.popularpenguin.runapp.data.RunContract.ChallengesEntry;
import com.popularpenguin.runapp.data.RunContract.SessionsEntry;
import com.popularpenguin.runapp.data.Session;

import java.util.Date;
import java.util.Locale;

public class DataUtils {

    private static final String TAG = DataUtils.class.getSimpleName();

    private DataUtils() { } // this class shouldn't be instantiated

    /**
     * Insert a session into the session table in the database
     * @param contentResolver app's content resolver
     * @param session the session to insert
     * @return uri of the inserted session in the database
     */
    public static Uri insertSession(@NonNull ContentResolver contentResolver,
                                    @NonNull Session session) {

        Log.d(TAG, "Session LatLng: " + session.getPath());

        ContentValues cv = new ContentValues();
        cv.put(SessionsEntry.COLUMN_CHALLENGE_ID, session.getChallenge().getId());
        cv.put(SessionsEntry.COLUMN_DATE, session.getDate());
        cv.put(SessionsEntry.COLUMN_TIME, session.getTime());
        cv.put(SessionsEntry.COLUMN_PATH, session.getPath());
        cv.put(SessionsEntry.COLUMN_IS_COMPLETED, session.isCompleted());

        return contentResolver.insert(SessionsEntry.CONTENT_URI, cv);
    }

    /**
     * Updates the fastest time for a challenge in the database
     * @param contentResolver the app's content resolver
     * @param challenge an instance of the challenge
     * @param time the time to set as fastest
     */
    public static void updateFastestTime(@NonNull ContentResolver contentResolver,
                                        @NonNull Challenge challenge,
                                        long time) {

        String id = String.valueOf(challenge.getId());

        Log.d(TAG, "Fastest time to update is: " + time);

        ContentValues cv = new ContentValues();
        cv.put(ChallengesEntry.COLUMN_FASTEST_TIME, time);

        contentResolver.update(ChallengesEntry.CONTENT_URI, cv,"_id=?", new String[]{ id });
    }

    public static String getCurrentDateString() {
        Date date = new Date();

        return date.toString();
    }

    public static String getFormattedTime(long time) {
        int seconds = (int) (time / 1000);
        int minutes = seconds / 60;
        int hours = minutes / 60;

        seconds %= 60;
        minutes %= 60;

        return String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds);
    }
}
