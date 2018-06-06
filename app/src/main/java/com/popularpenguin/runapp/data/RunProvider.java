package com.popularpenguin.runapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.popularpenguin.runapp.data.RunContract.ChallengesEntry;
import com.popularpenguin.runapp.data.RunContract.SessionsEntry;

public class RunProvider extends ContentProvider {

    private static final int CHALLENGES = 100;
    private static final int CHALLENGE_WITH_ID = 101;

    private static final int SESSIONS = 200;
    private static final int SESSION_WITH_ID = 201;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        matcher.addURI(RunContract.AUTHORITY, RunContract.PATH_CHALLENGES, CHALLENGES);
        matcher.addURI(RunContract.AUTHORITY, RunContract.PATH_CHALLENGES + "/#", CHALLENGE_WITH_ID);

        matcher.addURI(RunContract.AUTHORITY, RunContract.PATH_SESSIONS, SESSIONS);
        matcher.addURI(RunContract.AUTHORITY, RunContract.PATH_SESSIONS + "/#", SESSION_WITH_ID);

        return matcher;
    }

    private DbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new DbHelper(getContext());

        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri,
                        @Nullable String[] projection,
                        @Nullable String selection,
                        @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {

        final SQLiteDatabase db = mDbHelper.getReadableDatabase();

        int match = sUriMatcher.match(uri);
        Cursor returnCursor;

        switch (match) {
            case CHALLENGES:
                returnCursor = db.query(ChallengesEntry.CHALLENGE_TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                break;

            case SESSIONS:
                returnCursor = db.query(SessionsEntry.SESSION_TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                break;

            default:
                throw new IllegalArgumentException("Invalid uri: " + uri);
        }

        returnCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return returnCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("getType hasn't been implemented");
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        Uri returnUri;
        long id;

        switch (match) {
            case CHALLENGES:
                id = db.insert(ChallengesEntry.CHALLENGE_TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(ChallengesEntry.CONTENT_URI, id);
                }
                else {
                    throw new SQLException("Insert failed on uri: " + uri);
                }

                break;

            case SESSIONS:
                id = db.insert(SessionsEntry.SESSION_TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(SessionsEntry.CONTENT_URI, id);
                }
                else {
                    throw new SQLException("Insert failed on uri: " + uri);
                }

                break;

            default:
                throw new IllegalArgumentException("Invalid uri: " + uri);
        }

        // notify resolver that the uri has changed
        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        int entriesDeleted;
        String id;

        switch (match) {
            case CHALLENGE_WITH_ID:
                id = uri.getLastPathSegment();
                entriesDeleted = db.delete(ChallengesEntry.CHALLENGE_TABLE_NAME,
                        ChallengesEntry._ID + "=?",
                        new String[] { id });

                break;

            case SESSIONS:
                entriesDeleted = db.delete(SessionsEntry.SESSION_TABLE_NAME,
                        selection,
                        selectionArgs);

                break;

            case SESSION_WITH_ID:
                id = uri.getLastPathSegment();
                entriesDeleted = db.delete(SessionsEntry.SESSION_TABLE_NAME,
                        SessionsEntry._ID + "=?",
                        new String[] { id });

                break;

            default:
                throw new IllegalArgumentException("Invalid uri: " + uri);
        }

        // set notification if a session was deleted
        if (entriesDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return entriesDeleted;
    }

    @Override
    public int update(@NonNull Uri uri,
                      @Nullable ContentValues contentValues,
                      @Nullable String selection,
                      @Nullable String[] selectionArgs) {

        final SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case CHALLENGES:
                rowsUpdated = db.update(ChallengesEntry.CHALLENGE_TABLE_NAME,
                        contentValues,
                        selection,
                        selectionArgs);

                break;

            default:
                throw new IllegalArgumentException("Invalid uri: " + uri);
        }

        // set notification if a row is updated
        if (rowsUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }
}
