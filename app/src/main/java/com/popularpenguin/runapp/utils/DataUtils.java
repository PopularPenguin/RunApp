package com.popularpenguin.runapp.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.popularpenguin.runapp.data.AppDatabase;
import com.popularpenguin.runapp.data.Challenge;
import com.popularpenguin.runapp.data.Session;

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
     * Updates the fastest time for a challenge in the database
     *
     * @param context         the app's context
     * @param challenge       an instance of the challenge
     * @param time            the time to set as fastest
     */
    public static void updateFastestTime(@NonNull Context context,
                                         @NonNull Challenge challenge,
                                         long time) {

        challenge.setFastestTime(time);

        // TODO: Replace
        new Thread() {
            public void run() {
                AppDatabase.get(context).dao().insertChallenge(challenge);
            }
        }.start();

    }

    /** Get the current system time as a date */
    public static String getCurrentDateString() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.US);

        return formatter.format(date);
    }
}
