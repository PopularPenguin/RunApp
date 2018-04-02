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
import android.widget.Button;
import android.widget.LinearLayout;

import com.popularpenguin.runapp.R;
import com.popularpenguin.runapp.data.Challenge;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ListActivity extends AppCompatActivity implements
        ChallengeAdapter.ChallengeAdapterOnClickHandler {

    @BindView(R.id.rv_list) RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        ButterKnife.bind(this);

        setRecyclerView();
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
        challengeList.add(new Challenge("First",
                "Run an 8 minute mile",
                1000 * 60 * 8 /* 8 minutes */,
                false));
        challengeList.add(new Challenge("Second Challenge",
                "Run a 6 minute mile",
                1000 * 60 * 6 /* 6 minutes */,
                false));

        ChallengeAdapter adapter = new ChallengeAdapter(challengeList, this);
        mRecyclerView.setAdapter(adapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
    }

    @Override
    public void onClick(int position) {
        startActivity(new Intent(this, SessionActivity.class));
    }
}
