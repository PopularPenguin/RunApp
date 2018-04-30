package com.popularpenguin.runapp.view;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.popularpenguin.runapp.R;
import com.popularpenguin.runapp.adapter.SessionAdapter;
import com.popularpenguin.runapp.data.Session;
import com.popularpenguin.runapp.loader.SessionLoader;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SessionListActivity extends AppCompatActivity implements
        SessionAdapter.SessionAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<List<Session>> {

    @BindView(R.id.rv_session_list) RecyclerView mRecyclerView;
    @BindView(R.id.ad_view_session_list) AdView mAdView;

    private List<Session> mSessionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_list);

        ButterKnife.bind(this);

        getSupportLoaderManager().initLoader(0, null, this);

        setAdView();
    }

    private void setRecyclerView() {
        SessionAdapter adapter = new SessionAdapter(mSessionList, getResources(),this);
        mRecyclerView.setAdapter(adapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
    }

    // TODO: Load real ads after submitting project before uploading to Google Play
    private void setAdView() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);
    }

    @Override
    public void onClick(int position) {
        // TODO: Implement SessionActivity and the Session Info class
        Session session = mSessionList.get(position);

        Intent intent = new Intent(this, SessionActivity.class);
        //intent.putExtra(SessionInfo.SESSION_BUNDLE_KEY, mSessionList.get(position));
        intent.putExtra(Session.DESCRIPTION_EXTRA, session.getChallenge().getDescription());
        intent.putExtra(Session.TIME_EXTRA, session.getTimeString());
        intent.putExtra(Session.FASTEST_TIME_EXTRA, session.getChallenge().getFastestTimeString());

        startActivity(intent);
    }

    // Loader Callbacks /////////////////////////////////////////////////////////////////////////
    @NonNull
    @Override
    public Loader<List<Session>> onCreateLoader(int id, @Nullable Bundle args) {
        return new SessionLoader(this);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Session>> loader, List<Session> data) {
        mSessionList = data;

        setRecyclerView();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Session>> loader) {
        // Not implemented
    }
}
