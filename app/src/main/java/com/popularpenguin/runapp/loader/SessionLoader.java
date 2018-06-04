package com.popularpenguin.runapp.loader;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.popularpenguin.runapp.data.Challenge;
import com.popularpenguin.runapp.data.RunContract.ChallengesEntry;
import com.popularpenguin.runapp.data.Session;
import com.popularpenguin.runapp.data.RunContract.SessionsEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Load a list of sessions from the database
 */
public class SessionLoader extends AsyncTaskLoader<List<Session>> {

    public SessionLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Nullable
    @Override
    public List<Session> loadInBackground() {
        Cursor cursor = getContext().getContentResolver().query(SessionsEntry.CONTENT_URI,
                null, null, null, null);

        if (cursor.getCount() == 0) {
            return new ArrayList<>();
        }

        List<Session> sessions = new ArrayList<>();

        cursor.moveToFirst();

        do {
            Challenge challenge = getChallenge(cursor);

            long id = cursor.getLong(cursor.getColumnIndex(SessionsEntry._ID));
            String date = cursor.getString(cursor.getColumnIndex(SessionsEntry.COLUMN_DATE));
            long time = cursor.getLong(cursor.getColumnIndex(SessionsEntry.COLUMN_TIME));
            List<LatLng> path = getPath(cursor);
            boolean isCompleted = time <= challenge.getTimeToComplete();

            Session session = new Session(challenge, date, time, path, isCompleted);
            session.setId(id);
            sessions.add(session);
        } while (cursor.moveToNext());

        cursor.close();

        return sessions;
    }

    private Challenge getChallenge(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndex(SessionsEntry.COLUMN_CHALLENGE_ID));
        Cursor challengeCursor = getContext().getContentResolver()
                .query(ChallengesEntry.CONTENT_URI,
                        null,
                        ChallengesEntry._ID + "=?",
                        new String[] { Long.toString(id) },
                        null);

        if (challengeCursor == null || challengeCursor.getCount() == 0) {
            throw new SQLException("Invalid challenge id in session");
        }

        challengeCursor.moveToFirst();

        String name =
                challengeCursor.getString(
                        challengeCursor.getColumnIndex(ChallengesEntry.COLUMN_NAME));
        String description =
                challengeCursor.getString(
                        challengeCursor.getColumnIndex(ChallengesEntry.COLUMN_DESCRIPTION));
        long distance =
                challengeCursor.getLong(
                        challengeCursor.getColumnIndex(ChallengesEntry.COLUMN_DISTANCE));
        long timeToComplete =
                challengeCursor.getLong(
                        challengeCursor.getColumnIndex(ChallengesEntry.COLUMN_TIME_TO_COMPLETE));
        long fastestTime =
                challengeCursor.getLong(
                        challengeCursor.getColumnIndex(ChallengesEntry.COLUMN_FASTEST_TIME));
        boolean isCompleted =
                challengeCursor.getInt(
                        challengeCursor.getColumnIndex(ChallengesEntry.COLUMN_IS_COMPLETED)) == 1;
        int challengeRating =
                challengeCursor.getInt(
                        challengeCursor.getColumnIndex(ChallengesEntry.COLUMN_CHALLENGE_RATING));

        challengeCursor.close();

        Challenge challenge =
                new Challenge(id, name, description, distance, timeToComplete, isCompleted, challengeRating);

        if (fastestTime != 0L) {
            challenge.setFastestTime(fastestTime);
        }

        return challenge;
    }

    /**
     * Get the user's run path as a list of coordinates
     * @param cursor the cursor to extract the data from
     * @return the list of LatLng coordinates
     */
    private List<LatLng> getPath(Cursor cursor) {
        String pathString = cursor.getString(cursor.getColumnIndex(SessionsEntry.COLUMN_PATH));
        if (pathString == null) {
            return new ArrayList<>();
        }

        return Session.getPathLatLng(pathString);
    }
}
