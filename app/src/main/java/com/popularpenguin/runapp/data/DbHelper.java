package com.popularpenguin.runapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.popularpenguin.runapp.data.DbContract.ChallengesEntry;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "run.db";
    private static final int DB_VERSION = 1;

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_TABLE = "CREATE TABLE " + ChallengesEntry.CHALLENGE_TABLE_NAME + " (" +
                ChallengesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ChallengesEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                ChallengesEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                ChallengesEntry.COLUMN_TIME_TO_COMPLETE + " INTEGER NOT NULL, " +
                ChallengesEntry.COLUMN_FASTEST_TIME + " INTEGER NOT NULL, " +
                ChallengesEntry.COLUMN_IS_COMPLETED + " INTEGER NOT NULL" +
                ");";

        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ChallengesEntry.CHALLENGE_TABLE_NAME);

        onCreate(db);
    }
}
