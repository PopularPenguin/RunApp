package com.popularpenguin.runapp.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.popularpenguin.runapp.R;
import com.popularpenguin.runapp.adapter.ChallengeAdapter;
import com.popularpenguin.runapp.data.Challenge;
import com.popularpenguin.runapp.map.RunTracker;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChallengeListActivity extends AppCompatActivity implements
        ChallengeAdapter.ChallengeAdapterOnClickHandler {

    @BindView(R.id.rv_list) RecyclerView mRecyclerView;
    @BindView(R.id.ad_view) AdView mAdView;

    private List<Challenge> mChallengeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_list);

        ButterKnife.bind(this);

        setRecyclerView();

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
        // TODO: Remove challenges here and make some real ones in a separate class
        List<Challenge> challengeList = new ArrayList<>();
        challengeList.add(new Challenge(0L,
                "First Challenge",
                "Run an 8 minute mile",
                1000 * 60 * 8 /* 8 minutes */,
                false));
        challengeList.add(new Challenge(1L,
                "Second Challenge",
                "Run a 6 minute mile",
                1000 * 60 * 6 /* 6 minutes */,
                false));
        challengeList.add(new Challenge(2L,
                "Test Challenge 1",
                "Run a 10 second mile LOL",
                1000 * 10,
                false));
        challengeList.add(new Challenge(3L,
                "Test Challenge 2",
                "Run a 30 second mile!!!",
                1000 * 30,
                false));

        mChallengeList = challengeList;

        ChallengeAdapter adapter = new ChallengeAdapter(mChallengeList, this);
        mRecyclerView.setAdapter(adapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
    }

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
}
