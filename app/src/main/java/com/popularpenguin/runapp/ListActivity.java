package com.popularpenguin.runapp;

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
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ListActivity extends AppCompatActivity {

    @BindView(R.id.btn_gotomap) Button launchMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        ButterKnife.bind(this);

        launchMap.setOnClickListener(view -> {
            startActivity(new Intent(this, SessionActivity.class));
        });


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
}
