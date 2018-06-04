package com.popularpenguin.runapp.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.popularpenguin.runapp.R;
import com.popularpenguin.runapp.data.Challenge;
import com.popularpenguin.runapp.data.RunContract.ChallengesEntry;
import com.popularpenguin.runapp.data.RunContract.SessionsEntry;
import com.popularpenguin.runapp.data.Session;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Helper methods for various tasks
 */
public class DataUtils {

    private DataUtils() {
    } // this class shouldn't be instantiated

    /**
     * Insert a session into the session table in the database
     *
     * @param contentResolver app's content resolver
     * @param session         the session to insert
     * @return uri of the inserted session in the database
     */
    public static Uri insertSession(@NonNull ContentResolver contentResolver,
                                    @NonNull Session session) {

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
     *
     * @param contentResolver the app's content resolver
     * @param challenge       an instance of the challenge
     * @param time            the time to set as fastest
     */
    public static void updateFastestTime(@NonNull ContentResolver contentResolver,
                                         @NonNull Challenge challenge,
                                         long time) {

        String id = String.valueOf(challenge.getId());

        ContentValues cv = new ContentValues();
        cv.put(ChallengesEntry.COLUMN_FASTEST_TIME, time);

        contentResolver.update(ChallengesEntry.CONTENT_URI, cv, "_id=?", new String[]{id});
    }

    /** Get the current system time as a date */
    public static String getCurrentDateString() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.US);

        return formatter.format(date);
    }
}
