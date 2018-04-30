package com.popularpenguin.runapp.map;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.popularpenguin.runapp.R;
import com.popularpenguin.runapp.data.Challenge;
import com.popularpenguin.runapp.data.Session;
import com.popularpenguin.runapp.utils.DataUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RunTracker implements LocationService.ConnectionStatus,
        LocationService.OnLocationChangedListener,
        MapService.OnReadyListener,
        StopwatchService.StopWatchListener {

    private static final String TAG = RunTracker.class.getSimpleName();

    public static final String CHALLENGE_BUNDLE_KEY = "challenge";
    public static final String TRACKER_BUNDLE_KEY = "trackerData";

    private static final String LATITUDE_KEY = "latitudes";
    private static final String LONGITUDE_KEY = "longitudes";

    private static final String DISTANCE_KEY = "distance";

    public static final float METERS_TO_FEET = 3.2808399f;
    public static final float FEET_PER_MILE = 5280.0f;

    private Context mContext;

    private Challenge mChallenge;

    private LocationService mLocationService;
    private boolean isLocationBound = false;

    private MapService mMapService;

    private GoogleMap mGoogleMap;
    private Location mLocation;
    private Marker mCurrentLocationMarker;
    private List<LatLng> mLocationList = new ArrayList<>();
    private float mTotalDistance;

    private StopwatchService mStopwatchService;
    private long mStartTime;
    private boolean isStopwatchBound = false;

    private Button mButtonView;
    private TextView mLocationView;
    private TextView mTotalDistanceView;
    private TextView mStopWatchView;

    private MediaPlayer mMediaPlayer;
    private boolean isAlarmPlayed = false;
    private boolean isGoalReached = false;

    private PowerManager.WakeLock mWakeLock;

    public RunTracker(AppCompatActivity activity, int resId) {
        mMapService = new MapService(activity.getFragmentManager(), resId);
        mMapService.setOnReadyListener(this);

        mChallenge = activity.getIntent().getParcelableExtra(CHALLENGE_BUNDLE_KEY);

        mContext = activity;

        // get a wake lock to be able to run the tracker without the system destroying it
        PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WL");
        mWakeLock.acquire(mChallenge.getTimeToComplete()); // TODO: Change to an hour?
    }

    /**
     * Create a bundle to pass to the parent activity's onSaveInstanceState
     */
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
        bundle.putFloat(DISTANCE_KEY, mTotalDistance);

        if (mStopwatchService != null) {
            bundle.putLong(StopwatchService.START_TIME_EXTRA, mStopwatchService.getTime());
        }

        return bundle;
    }

    /**
     * Extract data from the parent activity's saved instance state
     */
    public void setBundle(Bundle bundle) {
        Bundle locationBundle = bundle.getBundle(TRACKER_BUNDLE_KEY);

        List<String> latList = locationBundle.getStringArrayList("latitudes");
        List<String> longList = locationBundle.getStringArrayList("longitudes");

        List<LatLng> latLngList = new ArrayList<>();

        for (int i = 0; i < latList.size() && i < longList.size(); i++) {
            Double latitude = Double.parseDouble(latList.get(i));
            Double longitude = Double.parseDouble(longList.get(i));

            latLngList.add(new LatLng(latitude, longitude));
        }

        mStartTime = locationBundle.getLong(StopwatchService.START_TIME_EXTRA, 0L);
        mTotalDistance = locationBundle.getFloat(DISTANCE_KEY, 0f);

        mLocationList = latLngList;
    }

    /**
     * Set Button that starts the Stopwatch
     */
    public void setButtonView(Button button) {
        mButtonView = button;
        button.setOnClickListener(view -> {
            startLocationService();
            startStopWatch();

            // TODO: Once the button has been clicked, set button text to stop and then
            // set a new onClickListener to stop the tracker and store the session?
            if (isStopwatchBound) {
                button.setText(mContext.getResources().getString(R.string.stop_timer));
            }
        });
    }

    /**
     * Set TextView representing the latitude/longitude display
     */
    public void setLocationView(TextView view) {
        mLocationView = view;
    }

    /**
     * Set TextView representing the total distance
     */
    public void setDistanceView(TextView view) {
        mTotalDistanceView = view;
    }

    /**
     * Set TextView representing the stopwatch's time
     */
    public void setStopWatchView(TextView view) {
        mStopWatchView = view;
    }

    /**
     * Start the StopWatch service
     */
    private void startStopWatch() {
        if (!isStopwatchBound) {
            Intent intent = new Intent(mContext, StopwatchService.class);
            intent.putExtra(StopwatchService.START_TIME_EXTRA, mStartTime);
            mContext.startService(intent);
            mContext.bindService(intent, mStopwatchServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    /**
     * Stop the StopWatch service
     */
    private void stopStopWatch() {
        if (isStopwatchBound) {
            mContext.unbindService(mStopwatchServiceConnection);
            isStopwatchBound = false;
        }

        Intent intent = new Intent(mContext, StopwatchService.class);
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
        // only start services from this method if the stopwatch has been running (this should be
        // called after a screen rotation)
        if (mStartTime != 0) {
            startLocationService();
            startStopWatch();
        }
    }

    @Override
    public void stop() {
        // ...
    }

    public void destroy() {
        stopLocationService();
        stopStopWatch();

        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }

        //finishRun(false);
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
        updateDistance();
        updateCamera(location);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mGoogleMap = map;
    }

    @Override
    public void onStopWatchUpdate(String time) {
        mStopWatchView.setText(time);

        // TODO: Test this!!
        if (mTotalDistance >= mChallenge.getDistance()) {
            finishRun(true);
        }

        // check if the goal time has elapsed and the out-of-time alarm hasn't played yet
        if (mStopwatchService.getTime() > mChallenge.getTimeToComplete() && !isAlarmPlayed) {
            mStopWatchView.setTextColor(Color.RED);
            isAlarmPlayed = true;
            playAlarm(R.raw.airhorn);

            finishRun(false);
        } else if (mStopwatchService.getTime() > mChallenge.getTimeToComplete() * 0.66 &&
                !isAlarmPlayed) {

            mStopWatchView.setTextColor(Color.YELLOW);
        }
    }

    /**
     * Finish the run by stopping services and storing the challenge in the database
     */
    private void finishRun(boolean isGoalReached) {
        // TODO: Remove after testing
        /*
        DataUtils.updateFastestTime(mContext.getContentResolver(),
                mChallenge,
                mStopwatchService.getTime()); */

        // if there is a new fastest time, update the challenge in the database
        if (isGoalReached) {
            long time = mStopwatchService.getTime();
            if (time < mChallenge.getFastestTime()) {
                DataUtils.updateFastestTime(mContext.getContentResolver(), mChallenge, time);
            }
        }

        stopStopWatch();
        stopLocationService();

        this.isGoalReached = isGoalReached;

        Session session = new Session(mChallenge,
                DataUtils.getCurrentDateString(),
                mStopwatchService.getTime(),
                mLocationService.getLocationList(),
                isGoalReached);

        Log.d(TAG, "Location list size = " + mLocationService.getLocationList().size());

        DataUtils.insertSession(mContext.getContentResolver(), session);
    }

    // TODO: Attribute horn sound from http://soundbible.com/1542-Air-Horn.html
    private void playAlarm(int resId) {
        mMediaPlayer = MediaPlayer.create(mContext, resId);
        mMediaPlayer.setOnCompletionListener(MediaPlayer::release);
        mMediaPlayer.setLooping(false);
        mMediaPlayer.start();

        Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(500L);

        Snackbar.make(mStopWatchView, R.string.snackbar_challenge_failed, Snackbar.LENGTH_LONG)
                .show();
    }

    /**
     * Starts the Location service
     */
    private void startLocationService() {
        if (!isLocationBound) {
            Intent intent = new Intent(mContext, LocationService.class);
            mContext.startService(intent);
            mContext.bindService(intent, mLocationServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    /**
     * Stops the Location service
     */
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
                .position(latLng);
        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.runicon));

        mCurrentLocationMarker = mGoogleMap.addMarker(markerOptions);
    }

    private void updatePolylines() {
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

    /** Updates the total distance run every time a polyline is updated */
    // TODO: Fix this so it doesn't exponentially update!!
    private void updateDistance() {
        List<LatLng> list = mLocationService.getLocationList();

        if (list.size() < 2) {
            return;
        }

        LatLng startPoint = list.get(list.size() - 2);
        LatLng endPoint = list.get(list.size() - 1);
        double startLat = startPoint.latitude;
        double startLong = startPoint.longitude;
        double endLat = endPoint.latitude;
        double endLong = endPoint.longitude;
        float[] results = new float[2];

        Location.distanceBetween(startLat, startLong, endLat, endLong, results);
        mTotalDistance += results[0] * METERS_TO_FEET;

        mTotalDistanceView.setText(String.format(Locale.US,
                "%.2f %s",
                mTotalDistance / FEET_PER_MILE, // convert feet to miles
                mContext.getResources().getString(R.string.run_units)));
    }

    private void updateCamera(Location location) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(location.getLatitude(), location.getLongitude()))
                .bearing(0f)
                .tilt(45f)
                .zoom(15f)
                .build();

        mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    /**
     * Service connection for location updates
     */
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

    /**
     * Service connection for the stopwatch
     */
    private ServiceConnection mStopwatchServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            StopwatchService.StopWatchBinder binder = (StopwatchService.StopWatchBinder) iBinder;
            mStopwatchService = binder.getService();
            mStopwatchService.setStopWatchListener(RunTracker.this);
            isStopwatchBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isStopwatchBound = false;
        }
    };
}