package com.popularpenguin.runapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.gms.maps.model.LatLng;
import com.popularpenguin.runapp.data.RunContract.ChallengesEntry;
import com.popularpenguin.runapp.data.RunContract.SessionsEntry;

import java.util.ArrayList;
import java.util.List;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "run.db";
    private static final int DB_VERSION = 1;

    DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_CHALLENGES_TABLE = "CREATE TABLE " +
                ChallengesEntry.CHALLENGE_TABLE_NAME + " (" +
                ChallengesEntry._ID + " INTEGER PRIMARY KEY UNIQUE, " +
                ChallengesEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                ChallengesEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                ChallengesEntry.COLUMN_DISTANCE + " INTEGER NOT NULL, " +
                ChallengesEntry.COLUMN_TIME_TO_COMPLETE + " INTEGER NOT NULL, " +
                ChallengesEntry.COLUMN_FASTEST_TIME + " INTEGER NOT NULL, " +
                ChallengesEntry.COLUMN_IS_COMPLETED + " INTEGER NOT NULL" +
                ");";

        final String CREATE_SESSIONS_TABLE = "CREATE TABLE " +
                SessionsEntry.SESSION_TABLE_NAME + " (" +
                SessionsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SessionsEntry.COLUMN_CHALLENGE_ID + " INTEGER NOT NULL, " +
                SessionsEntry.COLUMN_DATE + " TEXT NOT NULL, " +
                SessionsEntry.COLUMN_PATH + " TEXT, " +
                SessionsEntry.COLUMN_TIME + " INTEGER NOT NULL, " +
                SessionsEntry.COLUMN_IS_COMPLETED + " INTEGER NOT NULL" +
                ");";

        db.execSQL(CREATE_CHALLENGES_TABLE);
        db.execSQL(CREATE_SESSIONS_TABLE);

        addChallenges(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ChallengesEntry.CHALLENGE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SessionsEntry.SESSION_TABLE_NAME);

        onCreate(db);
    }

    private void addChallenges(SQLiteDatabase db) {
        // TODO: Add more challenges, change test challenges (like 10 seconds to run a mile)
        Challenge[] challenges = new Challenge[5];
        challenges[0] = new Challenge(0L,
                "Challenge 1",
                "Run an 8 minute mile",
                5280,
                1000 * 60 * 8,
                false);
        challenges[1] = new Challenge(1L,
                "Challenge 2",
                "Run a 6 minute mile",
                5280,
                1000 * 60 * 6,
                false);
        challenges[2] = new Challenge(2L,
                "Challenge 3",
                "Run a 30 second mile",
                5280,
                1000 * 30,
                false);
        challenges[3] = new Challenge(3L,
                "Challenge 4",
                "Run a 10 second mile",
                5280,
                1000 * 10,
                false);
        challenges[4] = new Challenge(4L,
                "Challenge 5",
                "Run 0.1 miles in 4 minutes",
                5280 / 10,
                1000 * 10 * 4,
                false);

        for (Challenge challenge : challenges) {
            ContentValues cv = new ContentValues();
            cv.put(ChallengesEntry.COLUMN_NAME, challenge.getName());
            cv.put(ChallengesEntry.COLUMN_DESCRIPTION, challenge.getDescription());
            cv.put(ChallengesEntry.COLUMN_DISTANCE, challenge.getDistance());
            cv.put(ChallengesEntry.COLUMN_TIME_TO_COMPLETE, challenge.getTimeToComplete());
            cv.put(ChallengesEntry.COLUMN_FASTEST_TIME, 0L);
            cv.put(ChallengesEntry.COLUMN_IS_COMPLETED, challenge.isCompleted());

            db.insert(ChallengesEntry.CHALLENGE_TABLE_NAME, null, cv);
        }
    }
}
