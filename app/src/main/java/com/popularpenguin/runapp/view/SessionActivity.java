package com.popularpenguin.runapp.view;

import android.graphics.Color;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.popularpenguin.runapp.R;
import com.popularpenguin.runapp.data.Session;
import com.popularpenguin.runapp.map.MapService;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SessionActivity extends AppCompatActivity implements
    MapService.OnReadyListener {

    @BindView(R.id.app_bar_session) AppBarLayout mAppBar;
    @BindView(R.id.collapsing_toolbar_session) CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.tv_session_description) TextView mDescriptionText;
    @BindView(R.id.tv_session_time) TextView mTimeText;
    @BindView(R.id.toolbar_session) Toolbar mToolbar;

    private GoogleMap mGoogleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupAppBar();

        // TODO: Get the polylines from the session and plot them, add move views to display data

        setViews();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mGoogleMap = map;

        String latLng = getIntent().getStringExtra(Session.LAT_LNG_EXTRA);
        List<LatLng> path = Session.getPathLatLng(latLng);
        PolylineOptions polyline = new PolylineOptions()
                .geodesic(true)
                .addAll(path)
                .color(Color.BLACK)
                .visible(true);

        mGoogleMap.addPolyline(polyline);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(path.get(0).latitude, path.get(0).longitude))
                .bearing(0f)
                .tilt(45f)
                .zoom(15f)
                .build();

        mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

    private void setupAppBar() {
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

    private void setViews() {
        String description = getIntent().getStringExtra(Session.DESCRIPTION_EXTRA);
        mDescriptionText.setText(description);

        String time = getIntent().getStringExtra(Session.TIME_EXTRA);
        String fastestTime = getIntent().getStringExtra(Session.FASTEST_TIME_EXTRA);
        String timeText = String.format(Locale.US, "%s / %s", time, fastestTime);
        mTimeText.setText(timeText);
    }
}
