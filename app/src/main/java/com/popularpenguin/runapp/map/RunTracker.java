package com.popularpenguin.runapp.map;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
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
        StopWatch.StopWatchListener {

    public static final String BUNDLE_KEY = "latlng";
    private static final String LATITUDE_KEY = "latitudes";
    private static final String LONGITUDE_KEY = "longitudes";

    private LocationService mLocationService;
    private MapService mMapService;

    private GoogleMap mGoogleMap;
    private Location mLocation;
    private Marker mCurrentLocationMarker;
    private List<LatLng> mLocationList = new ArrayList<>();

    private StopWatch mStopWatch;

    private TextView mLocationView;
    private TextView mStopWatchView;

    public RunTracker(AppCompatActivity activity, int resId) {
        mLocationService = new LocationService(activity);
        mLocationService.setOnLocationChangedListener(this);

        mMapService = new MapService(activity.getFragmentManager(), resId);
        mMapService.setOnReadyListener(this);

        mStopWatch = new StopWatch(0L);
        mStopWatch.setStopWatchListener(this);
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

        mLocationList = latLngList;
    }

    public void setLocationView(TextView view) {
        mLocationView = view;
    }

    public void setStopWatchView(TextView view) {
        mStopWatchView = view;
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
        mLocationService.connect();
    }

    @Override
    public void stop() {
        mLocationService.disconnect();
    }

    @Override
    public void onLocationUpdate(Location location) {
        mLocation = location;
        mLocationList.add(new LatLng(location.getLatitude(), location.getLongitude()));

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
        mStopWatch.start();
    }

    @Override
    public void onUpdate(String time) {
        mStopWatchView.setText(time);
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

    // TODO: Move into a callback from the timer once the stopwatch is set up, ping every 2-3 seconds
    private void updatePolylines() {
        if (mLocationList.size() < 2) {
            return;
        }

        PolylineOptions polyline = new PolylineOptions()
                .geodesic(true)
                .addAll(mLocationList)
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
}