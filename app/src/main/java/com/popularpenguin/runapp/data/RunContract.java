package com.popularpenguin.runapp.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class RunContract {
    public static final String AUTHORITY = "com.popularpenguin.runapp";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final String PATH_CHALLENGES = "challenges";
    public static final String PATH_SESSIONS = "sessions";

    public static final class ChallengesEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_CHALLENGES)
                .build();

        // the challenges table
        public static final String CHALLENGE_TABLE_NAME = "challengeTable";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_DISTANCE = "distance";
        public static final String COLUMN_TIME_TO_COMPLETE = "timeToComplete";
        public static final String COLUMN_FASTEST_TIME = "fastestTime";
        public static final String COLUMN_IS_COMPLETED = "isCompleted";
        public static final String COLUMN_CHALLENGE_RATING = "challengeRating";
    }

    public static final class SessionsEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_SESSIONS)
                .build();

        // the sessions table
        public static final String SESSION_TABLE_NAME = "sessionTable";
        public static final String COLUMN_CHALLENGE_ID = "challengeId";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_TIME = "time";
        public static final String COLUMN_PATH = "path";
        public static final String COLUMN_IS_COMPLETED = "isCompleted";
    }
}
