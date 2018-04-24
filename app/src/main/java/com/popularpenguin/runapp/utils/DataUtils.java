package com.popularpenguin.runapp.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.popularpenguin.runapp.data.RunContract.SessionsEntry;
import com.popularpenguin.runapp.data.Session;

import java.util.Date;

public class DataUtils {

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

    public static String getCurrentDateString() {
        Date date = new Date();

        return date.toString();
    }
}
