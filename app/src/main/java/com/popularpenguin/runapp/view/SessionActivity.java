package com.popularpenguin.runapp.view;

import android.graphics.Color;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

    private static final String TAG = SessionActivity.class.getSimpleName();

    @BindView(R.id.tv_session_description) TextView mDescriptionText;
    @BindView(R.id.tv_session_time) TextView mTimeText;
    @BindView(R.id.fab_session) FloatingActionButton mCenterMapFab;

    private MapService mMapService;
    private GoogleMap mGoogleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        ButterKnife.bind(this);

        mMapService = new MapService(getFragmentManager(), R.id.map_session_fragment);
        mMapService.setOnReadyListener(this);

        // TODO: add move views to display data

        setViews();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mGoogleMap = map;

        String latLng = getIntent().getStringExtra(Session.LAT_LNG_EXTRA);
        Log.d(TAG, "LatLng: " + latLng);
        List<LatLng> path = Session.getPathLatLng(latLng);
        PolylineOptions polyline = new PolylineOptions()
                .geodesic(true)
                .addAll(path)
                .color(Color.BLACK)
                .visible(true);

        mGoogleMap.addPolyline(polyline);

        if (path.isEmpty()) {
            return;
        }

        // set the center map fab here
        mCenterMapFab.setOnClickListener(view -> positionMapAtStart(path));

        positionMapAtStart(path);
    }

    private void setViews() {
        String description = getIntent().getStringExtra(Session.DESCRIPTION_EXTRA);
        mDescriptionText.setText(description);

        String time = getIntent().getStringExtra(Session.TIME_EXTRA);
        String fastestTime = getIntent().getStringExtra(Session.FASTEST_TIME_EXTRA);
        String timeText = String.format(Locale.US, "%s / %s", time, fastestTime);
        mTimeText.setText(timeText);
    }

    private void positionMapAtStart(List<LatLng> path) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(path.get(0).latitude, path.get(0).longitude))
                .bearing(0f)
                .tilt(45f)
                .zoom(15f)
                .build();

        mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
}
