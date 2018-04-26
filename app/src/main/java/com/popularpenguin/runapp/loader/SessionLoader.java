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

public class SessionLoader extends AsyncTaskLoader<List<Session>> {

    private static final String TAG = SessionLoader.class.getSimpleName();

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

        Log.d(TAG, "Cursor count: " + cursor.getCount());

        if (cursor.getCount() == 0) {
            return new ArrayList<>();
        }

        List<Session> sessions = new ArrayList<>();

        cursor.moveToFirst();

        do {
            Challenge challenge = getChallenge(cursor);
            Log.d(TAG, "id = " + challenge.getId() +
                ", name = " + challenge.getName() +
                ", desc = " + challenge.getDescription());
            /*Challenge challenge = new Challenge(0, "Challenge", "Test",
                    1000 * 60 * 8, false); */
            String date = cursor.getString(cursor.getColumnIndex(SessionsEntry.COLUMN_DATE));
            long time = cursor.getLong(cursor.getColumnIndex(SessionsEntry.COLUMN_TIME));
            List<LatLng> path = getPath(cursor);
            boolean isCompleted = time <= challenge.getTimeToComplete();

            Session session = new Session(challenge, date, time, path, isCompleted);
            sessions.add(session);
        } while (cursor.moveToNext());

        cursor.close();

        return sessions;
    }

    private Challenge getChallenge(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndex(SessionsEntry.COLUMN_CHALLENGE_ID));
        Log.d(TAG, "Challenge id: " + id);
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
        long timeToComplete =
                challengeCursor.getLong(
                        challengeCursor.getColumnIndex(ChallengesEntry.COLUMN_TIME_TO_COMPLETE));
        boolean isCompleted =
                challengeCursor.getInt(
                        challengeCursor.getColumnIndex(ChallengesEntry.COLUMN_IS_COMPLETED)) == 1;

        challengeCursor.close();

        return new Challenge(id, name, description, timeToComplete, isCompleted);
    }

    // TODO: Remember to store session latlng in this format "12.53-54.64,12.88-55.00"
    private List<LatLng> getPath(Cursor cursor) {
        String pathString = cursor.getString(cursor.getColumnIndex(SessionsEntry.COLUMN_PATH));
        if (pathString == null) {
            return new ArrayList<>();
        }

        return Session.getPathLatLng(pathString);
    }
}
