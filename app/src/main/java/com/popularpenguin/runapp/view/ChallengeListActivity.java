package com.popularpenguin.runapp.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.popularpenguin.runapp.R;
import com.popularpenguin.runapp.adapter.ChallengeAdapter;
import com.popularpenguin.runapp.data.Challenge;
import com.popularpenguin.runapp.loader.ChallengeLoader;
import com.popularpenguin.runapp.map.RunTracker;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChallengeListActivity extends AppCompatActivity implements
        ChallengeAdapter.ChallengeAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<List<Challenge>> {

    @BindView(R.id.rv_challenge_list) RecyclerView mRecyclerView;
    @BindView(R.id.btn_launch_sessions) Button mSessionButton;
    @BindView(R.id.ad_view_challenge_list) AdView mAdView;

    private List<Challenge> mChallengeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_list);

        ButterKnife.bind(this);

        getSupportLoaderManager().initLoader(0, null, this);

        mSessionButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, SessionListActivity.class);
            startActivity(intent);
        });

        setAdView();
    }

    // TODO: Move location permissions to own class or an abstract parent activity
    @Override
    protected void onResume() {
        super.onResume();

        checkIfGpsEnabled();
    }

    private void checkIfGpsEnabled() {
        checkPermissions();

        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // TODO: Make a snackbar 'Location services must be enabled for this app'

            enableGps();
        }
    }

    private void enableGps() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            String[] permissions = new String[] { Manifest.permission.ACCESS_FINE_LOCATION };
            ActivityCompat.requestPermissions(this, permissions, 0);
        }
    }

    private void setRecyclerView() {
        ChallengeAdapter adapter = new ChallengeAdapter(mChallengeList, this);
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
        Intent intent = new Intent(this, ChallengeActivity.class);
        intent.putExtra(RunTracker.CHALLENGE_BUNDLE_KEY, mChallengeList.get(position));

        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_challenge_list, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.action_show_info:
                // TODO: Make an intent for the About activity

                break;

            default:
                throw new IllegalArgumentException("Invalid menu id");
        }

        return true;
    }

    // Loader Callbacks //////////////////////////////////////////////////////////////////////////
    @NonNull
    @Override
    public Loader<List<Challenge>> onCreateLoader(int id, @Nullable Bundle args) {
        return new ChallengeLoader(this);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Challenge>> loader, List<Challenge> data) {
        mChallengeList = data;

        setRecyclerView();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Challenge>> loader) {
        // Not implemented
    }
}
