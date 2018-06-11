package com.popularpenguin.runapp.loader;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import com.popularpenguin.runapp.data.AppDatabase;
import com.popularpenguin.runapp.data.Challenge;

import java.util.List;

/**
 * Load Challenges from the database using a ContentProvider
 */
public class ChallengeLoader extends AsyncTaskLoader<List<Challenge>> {

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
        return AppDatabase.get(getContext()).dao().getChallenges();
    }
}
