package com.popularpenguin.runapp.loader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import com.popularpenguin.runapp.data.Challenge;
import com.popularpenguin.runapp.data.RunContract;
import com.popularpenguin.runapp.data.RunContract.ChallengesEntry;

import java.util.ArrayList;
import java.util.List;

public class ChallengeLoader extends AsyncTaskLoader<List<Challenge>> {

    public static final String TAG = ChallengeLoader.class.getSimpleName();

    public ChallengeLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Nullable
    @Override
    public List<Challenge> loadInBackground() {
        Cursor cursor = getContext().getContentResolver().query(ChallengesEntry.CONTENT_URI,
                null, null, null, null);

        if (cursor == null || cursor.getCount() == 0) {
            return new ArrayList<>();
        }

        List<Challenge> challenges = new ArrayList<>();

        cursor.moveToFirst();

        do {
            int id = cursor.getInt(cursor.getColumnIndex(ChallengesEntry._ID));
            String name = cursor.getString(cursor.getColumnIndex(ChallengesEntry.COLUMN_NAME));
            String description =
                    cursor.getString(cursor.getColumnIndex(ChallengesEntry.COLUMN_DESCRIPTION));
            long distance =
                    cursor.getLong(cursor.getColumnIndex(ChallengesEntry.COLUMN_DISTANCE));
            long timeToComplete =
                    cursor.getLong(cursor.getColumnIndex(ChallengesEntry.COLUMN_TIME_TO_COMPLETE));
            long fastestTime =
                    cursor.getLong(cursor.getColumnIndex(ChallengesEntry.COLUMN_FASTEST_TIME));
            boolean isCompleted =
                    cursor.getInt(cursor.getColumnIndex(ChallengesEntry.COLUMN_IS_COMPLETED)) == 1;

            Challenge challenge = new Challenge(id,
                    name,
                    description,
                    distance,
                    timeToComplete,
                    isCompleted);
            challenge.setFastestTime(fastestTime);
            challenges.add(challenge);
        } while (cursor.moveToNext());

        cursor.close();

        return challenges;
    }
}
