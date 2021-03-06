package com.popularpenguin.runapp.view;

import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.popularpenguin.runapp.R;
import com.popularpenguin.runapp.data.Session;
import com.popularpenguin.runapp.map.MapService;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Displays a user's stored session from the database on a map
 */
@SuppressWarnings("WeakerAccess")
public class SessionActivity extends AppCompatActivity implements
    MapService.OnReadyListener {

    @BindView(R.id.tv_session_description) TextView mDescriptionText;
    @BindView(R.id.tv_session_time) TextView mTimeText;
    @BindView(R.id.fab_session) FloatingActionButton mCenterMapFab;

    private GoogleMap mGoogleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        ButterKnife.bind(this);

        MapService mapService = new MapService(getFragmentManager(), R.id.map_session_fragment);
        mapService.setOnReadyListener(this);

        setViews();
    }

    /**
     * Once the map service is ready, update it with the session's path, start and end markers
     */
    @Override
    public void onMapReady(GoogleMap map) {
        mGoogleMap = map;

        // get the runner's path (serialized)
        String latLng = getIntent().getStringExtra(Session.LAT_LNG_EXTRA);
        List<LatLng> path = Session.getPathLatLng(latLng);

        if (path.size() < 2) {
            return;
        }

        addMarkers(path);
        addPolyLines(path);

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

    /**
     * Add markers to the map to show the start and end points of the run
     * @param path the path of the run
     */
    private void addMarkers(List<LatLng> path) {
        MarkerOptions startMarker = new MarkerOptions()
                .title(getString(R.string.session_marker_start))
                .position(path.get(0))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        MarkerOptions endMarker = new MarkerOptions()
                .title(getString(R.string.session_marker_end))
                .position(path.get(path.size() - 1))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        mGoogleMap.addMarker(startMarker);
        mGoogleMap.addMarker(endMarker);
    }

    /**
     * Add the run's path to the map
     * @param path the path of the run
     */
    private void addPolyLines(List<LatLng> path) {
        PolylineOptions polyline = new PolylineOptions()
                .geodesic(true)
                .addAll(path)
                .color(Color.BLACK)
                .visible(true);

        mGoogleMap.addPolyline(polyline);
    }

    /**
     * Fab onClick() positions the map at the start of the run's path
     * @param path the path of the run
     */
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
