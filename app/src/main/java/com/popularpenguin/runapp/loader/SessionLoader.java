package com.popularpenguin.runapp.loader;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import com.popularpenguin.runapp.data.AppDatabase;
import com.popularpenguin.runapp.data.Challenge;
import com.popularpenguin.runapp.data.RunDao;
import com.popularpenguin.runapp.data.Session;

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
        RunDao dao = AppDatabase.get(getContext()).dao();
        List<Session> sessions =  dao.getSessions();

        for (Session session : sessions) {
            Challenge challenge = dao.getChallengeById(session.getChallengeId());
            session.setChallenge(challenge);
        }

        return sessions;
    }
}
