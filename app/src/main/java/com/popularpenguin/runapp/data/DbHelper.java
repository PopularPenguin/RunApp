package com.popularpenguin.runapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.gms.maps.model.LatLng;
import com.popularpenguin.runapp.R;
import com.popularpenguin.runapp.data.RunContract.ChallengesEntry;
import com.popularpenguin.runapp.data.RunContract.SessionsEntry;

import java.util.ArrayList;
import java.util.List;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "run.db";
    private static final int DB_VERSION = 1;

    private Context mContext;

    DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);

        mContext = context;
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
        Resources resources = mContext.getResources();

        int one = 5_280;
        int two = 10_560;
        int half = 2_640;
        int quarter = 1_320;
        int fiveK = 16_404;

        Challenge[] challenges = new Challenge[33];
        challenges[0] = new Challenge(0L,
                resources.getString(R.string.n_12_minute_mile),
                resources.getString(R.string.d_12_minute_mile),
                one,
                1000 * 60 * 12,
                false);
        challenges[1] = new Challenge(1L,
                resources.getString(R.string.n_11_minute_mile),
                resources.getString(R.string.d_11_minute_mile),
                one,
                1000 * 60 * 11,
                false);
        challenges[2] = new Challenge(2L,
                resources.getString(R.string.n_10_minute_mile),
                resources.getString(R.string.d_10_minute_mile),
                one,
                1000 * 60 * 10,
                false);
        challenges[3] = new Challenge(3L,
                resources.getString(R.string.n_9_minute_mile),
                resources.getString(R.string.d_9_minute_mile),
                one,
                1000 * 60 * 9,
                false);
        challenges[4] = new Challenge(4L,
                resources.getString(R.string.n_8_minute_mile),
                resources.getString(R.string.d_8_minute_mile),
                one,
                1000 * 60 * 8,
                false);
        challenges[5] = new Challenge(5L,
                resources.getString(R.string.n_7_minute_mile),
                resources.getString(R.string.d_7_minute_mile),
                one,
                1000 * 60 * 7,
                false);
        challenges[6] = new Challenge(6L,
                resources.getString(R.string.n_6_minute_mile),
                resources.getString(R.string.d_6_minute_mile),
                one,
                1000 * 60 * 6,
                false);
        challenges[7] = new Challenge(7L,
                resources.getString(R.string.n_5_minute_mile),
                resources.getString(R.string.d_5_minute_mile),
                one,
                1000 * 60 * 5,
                false);
        challenges[8] = new Challenge(8L,
                resources.getString(R.string.n_4_minute_mile),
                resources.getString(R.string.d_4_minute_mile),
                one,
                1000 * 60 * 4,
                false);
        // half mile challenges
        challenges[9] = new Challenge(9L,
                resources.getString(R.string.n_6_minute_half),
                resources.getString(R.string.d_6_minute_half),
                half,
                1000 * 60 * 6,
                false);
        challenges[10] = new Challenge(10L,
                resources.getString(R.string.n_5_minute_half),
                resources.getString(R.string.d_5_minute_half),
                half,
                1000 * 60 * 5,
                false);
        challenges[11] = new Challenge(11L,
                resources.getString(R.string.n_4_minute_half),
                resources.getString(R.string.d_4_minute_half),
                half,
                1000 * 60 * 4,
                false);
        challenges[12] = new Challenge(12L,
                resources.getString(R.string.n_3_minute_half),
                resources.getString(R.string.d_3_minute_half),
                half,
                1000 * 60 * 3,
                false);
        challenges[13] = new Challenge(13L,
                resources.getString(R.string.n_2_minute_half),
                resources.getString(R.string.d_2_minute_half),
                half,
                1000 * 60 * 2,
                false);
        // quarter mile challenges
        challenges[14] = new Challenge(14L,
                resources.getString(R.string.n_3_minute_quarter),
                resources.getString(R.string.d_3_minute_quarter),
                quarter,
                1000 * 60 * 3,
                false);
        challenges[15] = new Challenge(15L,
                resources.getString(R.string.n_2_minute_quarter),
                resources.getString(R.string.d_2_minute_quarter),
                quarter,
                1000 * 60 * 2,
                false);
        challenges[16] = new Challenge(16L,
                resources.getString(R.string.n_1_minute_quarter),
                resources.getString(R.string.d_1_minute_quarter),
                quarter,
                1000 * 60,
                false);
        // two mile challenges
        challenges[17] = new Challenge(17L,
                resources.getString(R.string.n_24_minute_two),
                resources.getString(R.string.d_24_minute_two),
                two,
                1000 * 60 * 24,
                false);
        challenges[18] = new Challenge(18L,
                resources.getString(R.string.n_22_minute_two),
                resources.getString(R.string.d_22_minute_two),
                two,
                1000 * 60 * 22,
                false);
        challenges[19] = new Challenge(19L,
                resources.getString(R.string.n_20_minute_two),
                resources.getString(R.string.d_20_minute_two),
                two,
                1000 * 60 * 20,
                false);
        challenges[20] = new Challenge(20L,
                resources.getString(R.string.n_18_minute_two),
                resources.getString(R.string.d_18_minute_two),
                two,
                1000 * 60 * 18,
                false);
        challenges[21] = new Challenge(21L,
                resources.getString(R.string.n_16_minute_two),
                resources.getString(R.string.d_16_minute_two),
                two,
                1000 * 60 * 16,
                false);
        challenges[22] = new Challenge(22L,
                resources.getString(R.string.n_14_minute_two),
                resources.getString(R.string.d_14_minute_two),
                two,
                1000 * 60 * 14,
                false);
        challenges[23] = new Challenge(23L,
                resources.getString(R.string.n_12_minute_two),
                resources.getString(R.string.d_12_minute_two),
                two,
                1000 * 60 * 12,
                false);
        challenges[24] = new Challenge(24L,
                resources.getString(R.string.n_10_minute_two),
                resources.getString(R.string.d_10_minute_two),
                two,
                1000 * 60 * 10,
                false);
        // 5K challenges
        challenges[25] = new Challenge(25L,
                resources.getString(R.string.n_60_minute_5k),
                resources.getString(R.string.d_60_minute_5k),
                fiveK,
                1000 * 60 * 60,
                false);
        challenges[26] = new Challenge(26L,
                resources.getString(R.string.n_50_minute_5k),
                resources.getString(R.string.d_50_minute_5k),
                fiveK,
                1000 * 60 * 50,
                false);
        challenges[27] = new Challenge(27L,
                resources.getString(R.string.n_40_minute_5k),
                resources.getString(R.string.d_40_minute_5k),
                fiveK,
                1000 * 60 * 40,
                false);
        challenges[28] = new Challenge(28L,
                resources.getString(R.string.n_30_minute_5k),
                resources.getString(R.string.d_30_minute_5k),
                fiveK,
                1000 * 60 * 30,
                false);
        challenges[29] = new Challenge(29L,
                resources.getString(R.string.n_20_minute_5k),
                resources.getString(R.string.d_20_minute_5k),
                fiveK,
                1000 * 60 * 20,
                false);
        challenges[30] = new Challenge(30L,
                resources.getString(R.string.n_15_minute_5k),
                resources.getString(R.string.d_15_minute_5k),
                fiveK,
                1000 * 60 * 15,
                false);

        // TODO: Remove test challenges
        challenges[31] = new Challenge(4L,
                "Challenge 5",
                "Run 0.1 miles in 4 minutes",
                5280 / 10,
                1000 * 60 * 4,
                false);
        challenges[32] = new Challenge(5L,
                "Challenge 6",
                "Run 0.02 miles in 10 minutes",
                5280 / 100,
                1000 * 60 * 10,
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
