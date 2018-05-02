package com.popularpenguin.runapp.map;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.JobIntentService;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class LocationService extends JobIntentService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = LocationService.class.getSimpleName();

    private static final long UPDATE_INTERVAL = 2000L; // update every 20 seconds
    private static final long UPDATE_FASTEST_INTERVAL = 2000L; // 2 seconds

    private IBinder mBinder = new LocationBinder();

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private Location mLocation;
    private List<LatLng> mLocationList = new ArrayList<>();

    public void connect() {
        mGoogleApiClient.connect();
    }

    public void disconnect() {
        mGoogleApiClient.disconnect();
    }

    public List<LatLng> getLocationList() {
        return mLocationList;
    }

    public void setLocationList(List<LatLng> locationList) {
        mLocationList = locationList;
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        // TODO: Move onStartCommand code here?
    }

    @Nullable
    @Override
    public IBinder onBind(@NonNull Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setClient();
        setLocationCallbackListener();

        return START_STICKY;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {

            return;
        }

        setLocationRequest();

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

    private synchronized void setClient() {
        Log.i(TAG, "setClient()");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private void setLocationCallbackListener() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                mLocation =  locationResult.getLastLocation();
                mLocationList.add(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()));
                mOnLocationChangedListener.onLocationUpdate(mLocation);
            }
        };
    }

    private void setLocationRequest() {
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(UPDATE_FASTEST_INTERVAL);
    }

    public class LocationBinder extends Binder {
        LocationService getService() {
            return LocationService.this;
        }
    }

    // Interfaces + Listener ///////////////////////////////////////////////////////////////////
    public interface ConnectionStatus {
        void start();
        void stop();
    }

    private OnLocationChangedListener mOnLocationChangedListener;

    public void setOnLocationChangedListener(OnLocationChangedListener listener) {
        mOnLocationChangedListener = listener;
    }

    public interface OnLocationChangedListener {
        void onLocationUpdate(Location location);
    }
}