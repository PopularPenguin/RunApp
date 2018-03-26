package com.popularpenguin.runapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SessionActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    @BindView(R.id.tv_location_test) TextView mLocationView;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        ButterKnife.bind(this);

        // TODO: Move GoogleApiClient and everything else to its own service class
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                for (Location location: locationResult.getLocations()) {
                    String locationText = String.format(Locale.US,
                            "%f, %f",
                            location.getLatitude(),
                            location.getLongitude());

                    mLocationView.setText(locationText);
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();

        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {

            return;
        }

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(500)
                .setFastestInterval(500);

        LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(mLocationRequest, mLocationCallback, null);
    }

    @Override
    public void onConnectionSuspended(int i) {
        // TODO: Implement
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // TODO: Implement
    }
}
