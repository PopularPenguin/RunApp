package com.popularpenguin.runapp.map;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.popularpenguin.runapp.data.Challenge;
import com.popularpenguin.runapp.notification.RunNotification;

import java.util.ArrayList;
import java.util.List;

public class LocationService extends JobIntentService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = LocationService.class.getSimpleName();

    public static final float METERS_TO_FEET = 3.2808399f;

    private static final long UPDATE_INTERVAL = 2000L; // update every 20 seconds
    private static final long UPDATE_FASTEST_INTERVAL = 2000L; // 2 seconds

    public static Intent getStartIntent(@NonNull Context context, @NonNull Challenge challenge) {
        Intent intent = new Intent(context, LocationService.class);
        intent.putExtra(Challenge.CHALLENGE_EXTRA, challenge);

        return intent;
    }

    public static Intent getStopIntent(@NonNull Context context) {
        return new Intent(context, LocationService.class);
    }

    private IBinder mBinder = new LocationBinder();

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private Location mLocation;
    private List<LatLng> mLocationList = new ArrayList<>();
    private RunNotification mRunNotification;

    private Challenge mChallenge;
    private float mTotalDistance;
    private boolean isFinished;

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

        StopwatchService stopwatch = new StopwatchService();
        stopwatch.start(0L);

        mChallenge = intent.getParcelableExtra(Challenge.CHALLENGE_EXTRA);

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

        // create and display a notification for the duration of this service
        mRunNotification = new RunNotification(this);
        mRunNotification.createNotification();

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

    @Override
    public void onDestroy() {
        super.onDestroy();

        // cancel the notification when the location service is destroyed
        if (mRunNotification != null) {
            mRunNotification.cancelNotification();
        }
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
                if (isFinished) {
                    return;
                }
                mLocation =  locationResult.getLastLocation();
                mLocationList.add(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()));
                if (mLocationList.size() >= 2) {
                    PolylineOptions polyline = getPolyline();
                    mOnLocationChangedListener.onLocationUpdate(mLocation, polyline);
                    getDistance();
                }
            }
        };
    }

    private void setLocationRequest() {
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(UPDATE_FASTEST_INTERVAL);
    }

    private PolylineOptions getPolyline() {
        return new PolylineOptions()
                .geodesic(true)
                .addAll(mLocationList)
                //.add(mLocationList.get(mLocationList.size() - 2))
                //.add(mLocationList.get(mLocationList.size() - 1))
                .color(Color.BLACK)
                .visible(true);
    }

    public float getDistance() {
        if (mLocationList.size() < 2) {
            return 0L;
        }

        LatLng startPoint = mLocationList.get(mLocationList.size() - 2);
        LatLng endPoint = mLocationList.get(mLocationList.size() - 1);
        double startLat = startPoint.latitude;
        double startLong = startPoint.longitude;
        double endLat = endPoint.latitude;
        double endLong = endPoint.longitude;
        float[] results = new float[2];

        Location.distanceBetween(startLat, startLong, endLat, endLong, results);
        mTotalDistance += results[0] * METERS_TO_FEET;

        return mTotalDistance;
    }

    public boolean isGoalReached() {
        Log.d(TAG, "distance is " + getDistance());

        if (mChallenge == null) {
            Log.d(TAG, "Challenge is null!");
            return false;
        }

        Log.d(TAG, "is goal reached? " + (getDistance() >= mChallenge.getDistance()));
        return getDistance() >= mChallenge.getDistance();
    }

    public void setFinished(boolean isFinished) {
        this.isFinished = isFinished;
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
        void onLocationUpdate(Location location, PolylineOptions polyline);
    }
}