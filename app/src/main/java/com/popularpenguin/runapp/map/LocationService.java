package com.popularpenguin.runapp.map;

import android.Manifest;
import android.app.IntentService;
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
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.popularpenguin.runapp.R;
import com.popularpenguin.runapp.data.Challenge;
import com.popularpenguin.runapp.notification.RunNotification;

import java.util.ArrayList;
import java.util.List;

/** Class to use Google's Location API */
public class LocationService extends IntentService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    @SuppressWarnings("WeakerAccess")
    public static final float METERS_TO_FEET = 3.2808399f;

    private static final long UPDATE_INTERVAL = 2000L; // update every 2 seconds
    private static final long UPDATE_FASTEST_INTERVAL = 2000L; // 2 seconds

    public static Intent getStartIntent(@NonNull Context context, @NonNull Challenge challenge) {
        Intent intent = new Intent(context, LocationService.class);
        intent.putExtra(Challenge.CHALLENGE_EXTRA, challenge);

        return intent;
    }

    public static Intent getStopIntent(@NonNull Context context) {
        return new Intent(context, LocationService.class);
    }

    private final IBinder mBinder = new LocationBinder();

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private Location mLocation;
    private List<LatLng> mLocationList = new ArrayList<>();
    private RunNotification mRunNotification;

    private Challenge mChallenge;
    private float mTotalDistance;
    private boolean isFinished;

    public LocationService() {
        super(LocationService.class.getSimpleName());
    }

    // connect to the API
    public void connect() {
        if (mGoogleApiClient == null) {
            return;
        }

        mGoogleApiClient.connect();
    }

    // disconnect from API
    public void disconnect() {
        if (mGoogleApiClient == null) {
            return;
        }

        mGoogleApiClient.disconnect();
    }

    /**
     * Get the list of locations built inside this class
     * @return List of LatLng objects
     */
    public List<LatLng> getLocationList() {
        return mLocationList;
    }

    public void setLocationList(List<LatLng> locationList) {
        mLocationList = locationList;
    }

    /**
     * Service starts here
     * @param intent Intent from getStartIntent() which contains a Challenge object to work with
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        setClient();
        setLocationCallbackListener();

        mChallenge = intent.getParcelableExtra(Challenge.CHALLENGE_EXTRA);
    }

    @Nullable
    @Override
    public IBinder onBind(@NonNull Intent intent) {
        return mBinder;
    }

    /**
     * Once connected, make a notification and start requesting location updates
     * @param bundle param not used
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, R.string.error_location_permissions, Toast.LENGTH_LONG)
                    .show();

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
        Toast.makeText(this,R.string.error_location_suspended, Toast.LENGTH_LONG)
                .show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, R.string.error_location_connection, Toast.LENGTH_LONG)
                .show();
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
                mLocation = locationResult.getLastLocation();
                mLocationList.add(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()));
                if (mLocationList.size() >= 2) {
                    PolylineOptions polyline = getPolyline();
                    mOnLocationChangedListener.onLocationUpdate(mLocation, polyline);
                    updateDistance();
                }
            }
        };
    }

    private void setLocationRequest() {
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(UPDATE_FASTEST_INTERVAL);

        LocationSettingsRequest request = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest)
                .build();

        LocationServices.getSettingsClient(this)
                .checkLocationSettings(request);

    }

    /**
     * Get the polylines to display in the RunTracker
     * @return the polylines
     */
    private PolylineOptions getPolyline() {
        return new PolylineOptions()
                .geodesic(true)
                .add(mLocationList.get(mLocationList.size() - 2))
                .add(mLocationList.get(mLocationList.size() - 1))
                .color(Color.BLACK)
                .visible(true);
    }

    public float getDistance() {
        return mTotalDistance;
    }

    /**
     * Measure the new polyline and update the total distance
     */
    private void updateDistance() {
        if (mLocationList.size() < 2) {
            return;
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
    }

    /**
     * Recalculate the distance from all the polylines in the list and set the total distance
     */
    public void recalculateDistance() {
        if (mLocationList.size() < 2) {
            return;
        }

        float distance = 0f;
        for (int i = 0; i < mLocationList.size() - 1; i++) {
            LatLng startPoint = mLocationList.get(i);
            LatLng endPoint = mLocationList.get(i + 1);
            double startLat = startPoint.latitude;
            double startLong = startPoint.longitude;
            double endLat = endPoint.latitude;
            double endLong = endPoint.longitude;
            float[] results = new float[2];

            Location.distanceBetween(startLat, startLong, endLat, endLong, results);
            distance += results[0] * METERS_TO_FEET;
        }

        mTotalDistance = distance;
    }

    /**
     * @return is the challenge's goal distance met?
     */
    public boolean isGoalReached() {
        if (mChallenge == null) {
            return false;
        }

        return getDistance() >= mChallenge.getDistance();
    }

    /**
     * Notify the service that the run has finished
     * @param isFinished is the run finished?
     */
    public void setFinished(boolean isFinished) {
        this.isFinished = isFinished;
    }

    /**
     * Class to bind the service to the RunTracker
     */
    public class LocationBinder extends Binder {
        LocationService getService() {
            return LocationService.this;
        }
    }

    // Interfaces + Listener ///////////////////////////////////////////////////////////////////
    public interface ConnectionStatus {
        void start();
        @SuppressWarnings("EmptyMethod")
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