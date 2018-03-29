package com.popularpenguin.runapp.map;

import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;

public class RunMap implements LocationService.ConnectionStatus,
        LocationService.OnLocationChangedListener,
        MapService.OnReadyListener {

    private LocationService mLocationService;
    private MapService mMapService;

    private GoogleMap mGoogleMap;
    private Location mLocation;
    private Marker mCurrentLocation;

    private TextView mLocationView;

    public RunMap(AppCompatActivity activity, int resId) {
        mLocationService = new LocationService(activity);
        mLocationService.setOnLocationChangedListener(this);

        mMapService = new MapService(activity.getFragmentManager(), resId);
        mMapService.setOnReadyListener(this);
    }

    public void setLocationView(TextView view) {
        mLocationView = view;
    }

    // TODO: Move maybe?
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

        if (mLocationView != null) {
            mLocationView.setText(getLocationText());
        }

        updateMarkerPosition(location);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mGoogleMap = map;
    }

    // https://stackoverflow.com/questions/33739971/
    // how-to-show-my-current-location-in-google-map-android-using-google-api-client
    private void updateMarkerPosition(Location location) {
        if (mGoogleMap == null) {
            return;
        }

        if (mCurrentLocation != null) {
            mCurrentLocation.remove();
        }

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title("Here I am!");
        mCurrentLocation = mGoogleMap.addMarker(markerOptions);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(location.getLatitude(), location.getLongitude()))
                .bearing(0f)
                .tilt(45f)
                .zoom(17f)
                .build();
        mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
}