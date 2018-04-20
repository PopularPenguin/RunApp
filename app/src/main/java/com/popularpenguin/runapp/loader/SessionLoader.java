package com.popularpenguin.runapp.loader;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import com.google.android.gms.maps.model.LatLng;
import com.popularpenguin.runapp.data.Challenge;
import com.popularpenguin.runapp.data.RunContract.ChallengesEntry;
import com.popularpenguin.runapp.data.Session;
import com.popularpenguin.runapp.data.RunContract.SessionsEntry;

import java.util.ArrayList;
import java.util.List;

public class SessionLoader extends AsyncTaskLoader<List<Session>> {

    public static final String TAG = SessionLoader.class.getSimpleName();

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

        if (cursor == null || cursor.getCount() == 0) {
            return new ArrayList<>();
        }

        List<Session> sessions = new ArrayList<>();

        cursor.moveToFirst();

        do {
            long id = cursor.getLong(cursor.getColumnIndex(SessionsEntry._ID));
            Challenge challenge = getChallenge(cursor);
            long time = cursor.getLong(cursor.getColumnIndex(SessionsEntry.COLUMN_TIME));
            List<LatLng> path = getPath(cursor);
            boolean isCompleted = time <= challenge.getTimeToComplete();

            Session session = new Session(id, challenge, time, path, isCompleted);
            sessions.add(session);
        } while (cursor.moveToNext());

        cursor.close();

        return sessions;
    }

    private Challenge getChallenge(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndex(ChallengesEntry._ID));
        Cursor challengeCursor = getContext().getContentResolver()
                .query(ChallengesEntry.CONTENT_URI,
                        null,
                        "id=?",
                        new String[] { Long.toString(id) },
                        null);

        if (challengeCursor == null || challengeCursor.getCount() == 0) {
            throw new SQLException("Invalid challenge id in session");
        }

        cursor.moveToFirst();

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

        return new Challenge(id, name, description, timeToComplete, isCompleted);
    }

    // TODO: Remember to store session latlng in this format "12.53-54.64,12.88-55.00"
    private List<LatLng> getPath(Cursor cursor) {
        String unparsed = cursor.getString(cursor.getColumnIndex(SessionsEntry.COLUMN_PATH));
        String[] points = unparsed.split(",");
        List<LatLng> locationList = new ArrayList<>();

        for (String point : points) {
            String[] latlngString = point.split("-");

            double lat = Double.valueOf(latlngString[0]);
            double lng = Double.valueOf(latlngString[1]);

            LatLng latLng = new LatLng(lat, lng);
            locationList.add(latLng);
        }

        return locationList;
    }
}
