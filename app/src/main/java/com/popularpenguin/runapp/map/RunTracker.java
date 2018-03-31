package com.popularpenguin.runapp.map;

import android.graphics.Color;
import android.location.Location;
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
        MapService.OnReadyListener {

    private LocationService mLocationService;
    private MapService mMapService;

    private GoogleMap mGoogleMap;
    private Location mLocation;
    private Marker mCurrentLocationMarker;
    private List<LatLng> mLocationList = new ArrayList<>();

    private TextView mLocationView;

    public RunTracker(AppCompatActivity activity, int resId) {
        mLocationService = new LocationService(activity);
        mLocationService.setOnLocationChangedListener(this);

        mMapService = new MapService(activity.getFragmentManager(), resId);
        mMapService.setOnReadyListener(this);
    }

    public void setLocationView(TextView view) {
        mLocationView = view;
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
        PolylineOptions polyline = new PolylineOptions()
                .geodesic(true)
                .add(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()))
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