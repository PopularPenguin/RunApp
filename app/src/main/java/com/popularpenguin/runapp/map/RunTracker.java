package com.popularpenguin.runapp.map;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RunTracker implements LocationService.ConnectionStatus,
        LocationService.OnLocationChangedListener,
        MapService.OnReadyListener,
        StopWatchService.StopWatchListener {

    public static final String BUNDLE_KEY = "latlng";
    private static final String LATITUDE_KEY = "latitudes";
    private static final String LONGITUDE_KEY = "longitudes";

    private Context mContext;

    private LocationService mLocationService;
    private boolean isLocationBound = false;

    private MapService mMapService;

    private GoogleMap mGoogleMap;
    private Location mLocation;
    private Marker mCurrentLocationMarker;
    private List<LatLng> mLocationList = new ArrayList<>();

    private StopWatchService mStopWatchService;
    private long mStartTime;
    private boolean isStopWatchBound = false;

    private TextView mLocationView;
    private TextView mStopWatchView;

    public RunTracker(AppCompatActivity activity, int resId) {
        mMapService = new MapService(activity.getFragmentManager(), resId);
        mMapService.setOnReadyListener(this);

        mContext = activity;
    }

    /** Create a bundle to pass to the parent activity's onSaveInstanceState */
    public Bundle getBundle() {
        Bundle bundle = new Bundle();
        ArrayList<String> latList = new ArrayList<>();
        ArrayList<String> longList = new ArrayList<>();

        for (LatLng latlng : mLocationList) {
            String latitude = Double.toString(latlng.latitude);
            String longitude = Double.toString(latlng.longitude);

            latList.add(latitude);
            longList.add(longitude);
        }

        bundle.putStringArrayList(LATITUDE_KEY, latList);
        bundle.putStringArrayList(LONGITUDE_KEY, longList);

        bundle.putLong(StopWatchService.START_TIME_EXTRA, mStopWatchService.getTime());

        return bundle;
    }

    /** Extract data from the parent activity's saved instance state */
    public void setBundle(Bundle bundle) {
        Bundle locationBundle = bundle.getBundle(BUNDLE_KEY);

        List<String> latList = locationBundle.getStringArrayList("latitudes");
        List<String> longList = locationBundle.getStringArrayList("longitudes");

        List<LatLng> latLngList = new ArrayList<>();

        for (int i = 0; i < latList.size() && i < longList.size(); i++) {
            Double latitude = Double.parseDouble(latList.get(i));
            Double longitude = Double.parseDouble(longList.get(i));

            latLngList.add(new LatLng(latitude, longitude));
        }

        mStartTime = locationBundle.getLong(StopWatchService.START_TIME_EXTRA, 0L);

        mLocationList = latLngList;
    }

    public void setLocationView(TextView view) {
        mLocationView = view;
    }

    public void setStopWatchView(TextView view) {
        mStopWatchView = view;
    }

    /** Start the StopWatch service */
    public void startStopWatch() {
        if (!isStopWatchBound) {
            Intent intent = new Intent(mContext, StopWatchService.class);
            intent.putExtra(StopWatchService.START_TIME_EXTRA, mStartTime);
            mContext.startService(intent);
            mContext.bindService(intent, mStopWatchServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    /** Stop the StopWatch service */
    public void stopStopWatch() {
        if (isStopWatchBound) {
            mContext.unbindService(mStopWatchServiceConnection);
            isStopWatchBound = false;
        }

        Intent intent = new Intent(mContext, StopWatchService.class);
        mContext.stopService(intent);
    }

    // TODO: Move maybe? Delete later if not needed
    private String getLocationText() {
        if (mLocation == null) {
            return "---";
        }

         return String.format(Locale.US,
                "%f, %f",
                mLocation.getLatitude(),
                mLocation.getLongitude());
    }

    @Override
    public void start() {
        startLocationService();
        startStopWatch();
    }

    @Override
    public void stop() {
        // ...
    }

    public void destroy() {
        stopStopWatch();
        stopLocationService();
    }

    @Override
    public void onLocationUpdate(Location location) {
        mLocation = location;

        if (mLocationView != null) {
            mLocationView.setText(getLocationText());
        }

        if (mGoogleMap == null) {
            return;
        }

        updateMarkerPosition(location);
        updatePolylines();
        updateCamera(location);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mGoogleMap = map;
    }

    @Override
    public void onStopWatchUpdate(String time) {
        mStopWatchView.setText(time);
    }

    /** Starts the Location service */
    private void startLocationService() {
        if (!isLocationBound) {
            Intent intent = new Intent(mContext, LocationService.class);
            mContext.startService(intent);
            mContext.bindService(intent, mLocationServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    /** Stops the Location service */
    private void stopLocationService() {
        if (isLocationBound) {
            mContext.unbindService(mLocationServiceConnection);
            isLocationBound = false;
        }

        Intent intent = new Intent(mContext, LocationService.class);
        mContext.stopService(intent);
    }

    private void updateMarkerPosition(Location location) {
        if (mCurrentLocationMarker != null) {
            mCurrentLocationMarker.remove();
        }

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title("Here I am!");
                //.icon(BitmapDescriptorFactory.fromResource(R.drawable.runicon));

        mCurrentLocationMarker = mGoogleMap.addMarker(markerOptions);
    }

    private void updatePolylines() {
        // TODO: Store the location list inside the LocationService (which runs as a service)
        List<LatLng> list = mLocationService.getLocationList();

        if (list.size() < 2) {
            return;
        }

        PolylineOptions polyline = new PolylineOptions()
                .geodesic(true)
                .addAll(list)
                //.add(mLocationList.get(mLocationList.size() - 2))
                //.add(mLocationList.get(mLocationList.size() - 1))
                .color(Color.BLACK)
                .visible(true);

        mGoogleMap.addPolyline(polyline);
    }

    private void updateCamera(Location location) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(location.getLatitude(), location.getLongitude()))
                .bearing(0f)
                .tilt(45f)
                .zoom(17f)
                .build();

        mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    /** Service connection for location updates */
    private ServiceConnection mLocationServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            LocationService.LocationBinder binder = (LocationService.LocationBinder) iBinder;
            mLocationService = binder.getService();
            mLocationService.connect();
            mLocationService.setOnLocationChangedListener(RunTracker.this);
            isLocationBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isLocationBound = false;
            mLocationService.disconnect();
        }
    };

    /** Service connection for the stopwatch */
    private ServiceConnection mStopWatchServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            StopWatchService.StopWatchBinder binder = (StopWatchService.StopWatchBinder) iBinder;
            mStopWatchService = binder.getService();
            mStopWatchService.setStopWatchListener(RunTracker.this);
            isStopWatchBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isStopWatchBound = false;
        }
    };
}