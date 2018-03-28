package com.popularpenguin.runapp.view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.popularpenguin.runapp.R;
import com.popularpenguin.runapp.map.LocationService;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SessionActivity extends AppCompatActivity implements OnMapReadyCallback {

    @BindView(R.id.app_bar_session)
    AppBarLayout mAppBar;
    @BindView(R.id.collapsing_toolbar_session)
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.tv_location_test)
    TextView mLocationView;
    @BindView(R.id.toolbar_session)
    Toolbar mToolbar;

    // TODO: Just bind views and call methods in this activity, move logic and data OUT

    private LocationService mLocationService;
    private GoogleMap mGoogleMap;
    private Location mLocation;
    private Marker mCurrentLocation;
    private boolean isMapReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupAppBar();

        mLocationService = new LocationService(this);
        mLocationService.setLocationView(mLocationView);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mLocationService.connect();
    }

    @Override
    protected void onStop() {
        mLocationService.disconnect();

        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        isMapReady = true;
        mGoogleMap = googleMap;
    }

    // https://stackoverflow.com/questions/33739971/how-to-show-my-current-location-in-google-map-android-using-google-api-client
    private void setMarker() {
        if (!isMapReady) {
            return;
        }

        if (mCurrentLocation != null) {
            mCurrentLocation.remove();
        }

        LatLng latLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title("Here I am!");
        mCurrentLocation = mGoogleMap.addMarker(markerOptions);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()))
                .bearing(0f)
                .tilt(45f)
                .zoom(17f)
                .build();
        mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void setupAppBar() {
        // TODO: Move this out of the activity?
        // Display text on app bar when it is totally collapsed
        // https://stackoverflow.com/questions/31662416/show-collapsingtoolbarlayout-title-only-when-collapsed
        mAppBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShowing = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = mAppBar.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    String appName = getResources().getString(R.string.app_name);
                    mCollapsingToolbarLayout.setTitle(appName);
                    isShowing = true;
                } else if (isShowing) {
                    mCollapsingToolbarLayout.setTitle(" ");
                    isShowing = false;
                }
            }
        });
    }
}
