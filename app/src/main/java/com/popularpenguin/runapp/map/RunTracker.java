package com.popularpenguin.runapp.map;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.popularpenguin.runapp.R;
import com.popularpenguin.runapp.data.Challenge;
import com.popularpenguin.runapp.data.Session;
import com.popularpenguin.runapp.utils.DataUtils;
import com.popularpenguin.runapp.view.ChallengeActivity;
import com.popularpenguin.runapp.widget.RunWidget;

import java.util.List;
import java.util.Locale;

public class RunTracker implements LocationService.ConnectionStatus,
        LocationService.OnLocationChangedListener,
        MapService.OnReadyListener,
        StopwatchService.StopWatchListener {

    public static final String CHALLENGE_BUNDLE_KEY = "challenge";
    public static final String TRACKER_BUNDLE_KEY = "trackerData";

    private static final String PATH_KEY = "path";
    private static final String DISTANCE_KEY = "distance";
    private static final String ALARM_KEY = "alarm";
    private static final String GOAL_KEY = "isGoalReached";
    private static final String BUTTON_KEY = "isButtonShown";
    private static final String FINISHED_KEY = "isSessionFinished";
    private static final String SNACKBAR_KEY = "isSnackbarShowing";

    public static final float FEET_PER_MILE = 5280.0f;

    private Context mContext;

    private Challenge mChallenge;

    private LocationService mLocationService;
    private boolean isLocationBound = false;

    private MapService mMapService;

    private GoogleMap mGoogleMap;
    private Marker mCurrentLocationMarker;
    private List<LatLng> mLocationList;
    private float mTotalDistance;

    private StopwatchService mStopwatchService;
    private long mStartTime;
    private long mEndTime;
    private boolean isStopwatchBound = false;

    private Button mButtonView;
    private TextView mDescriptionView;
    private TextView mTotalDistanceView;
    private TextView mStopWatchView;
    private FloatingActionButton mCenterMapFab;
    private boolean isButtonShown = true;

    private MediaPlayer mMediaPlayer;
    private boolean isAlarmPlayed = false;
    private boolean isGoalReached = false;
    private boolean isSessionFinished = false;
    private boolean isSnackbarShowing = false;

    public RunTracker(AppCompatActivity activity, int resId) {
        mMapService = new MapService(activity.getFragmentManager(), resId);
        mMapService.setOnReadyListener(this);

        mChallenge = activity.getIntent().getParcelableExtra(CHALLENGE_BUNDLE_KEY);

        mContext = activity;
    }

    /**
     * Create a bundle to pass to the parent activity's onSaveInstanceState
     */
    public Bundle getBundle() {
        Bundle bundle = new Bundle();

        String path;
        if (mLocationService != null) {
            path = Session.getPathString(mLocationService.getLocationList());
        }
        else {
            path = Session.getPathString(mLocationList);
        }

        bundle.putString(PATH_KEY, path);

        if (mLocationService != null) {
            bundle.putFloat(DISTANCE_KEY, mLocationService.getDistance());
        }
        else {
            bundle.putFloat(DISTANCE_KEY, mTotalDistance);
        }

        bundle.putBoolean(ALARM_KEY, isAlarmPlayed);
        bundle.putBoolean(GOAL_KEY, isGoalReached);
        bundle.putBoolean(BUTTON_KEY, isButtonShown);
        bundle.putBoolean(FINISHED_KEY, isSessionFinished);
        bundle.putBoolean(SNACKBAR_KEY, isSnackbarShowing);

        if (mStopwatchService != null) {
            // insert the current time from the stopwatch
            bundle.putLong(StopwatchService.START_TIME_EXTRA, mStopwatchService.getTime());
        }
        else if (mEndTime > 0L) {
            // if the challenge is over insert the final time recorded (since the stopwatch could be
            // null at this point)
            bundle.putLong(StopwatchService.START_TIME_EXTRA, mEndTime);
        }
        else {
            // put the challenge's goal time
            bundle.putLong(StopwatchService.START_TIME_EXTRA, mChallenge.getTimeToComplete());
        }

        return bundle;
    }

    /**
     * Extract data from the parent activity's saved instance state
     */
    public void setBundle(Bundle bundle) {
        Bundle locationBundle = bundle.getBundle(TRACKER_BUNDLE_KEY);
        Resources resources = mContext.getResources();

        String path = locationBundle.getString(PATH_KEY, "");
        mStartTime = locationBundle.getLong(StopwatchService.START_TIME_EXTRA, 0L);
        mTotalDistance = locationBundle.getFloat(DISTANCE_KEY, 0f);
        isAlarmPlayed = locationBundle.getBoolean(ALARM_KEY, false);
        isGoalReached = locationBundle.getBoolean(GOAL_KEY, false);
        isButtonShown = locationBundle.getBoolean(BUTTON_KEY, true);
        isSnackbarShowing = locationBundle.getBoolean(SNACKBAR_KEY, false);
        isSessionFinished = locationBundle.getBoolean(FINISHED_KEY, false);

        mLocationList = Session.getPathLatLng(path);
        if (mLocationService != null) {
            mLocationService.setLocationList(mLocationList);
        }

        updateDistance(mTotalDistance);

        mStopWatchView.setText(DataUtils.getFormattedTime(mStartTime));

        if (mStartTime >= mChallenge.getTimeToComplete()) {
            mStopWatchView.setTextColor(resources.getColor(R.color.red));
        }
        else if (mStartTime >= mChallenge.getTimeToComplete() * 0.66) {
            mStopWatchView.setTextColor(resources.getColor(R.color.yellow));
        }

        // redisplay snackbar (if showing) after a rotation
        if (isSnackbarShowing) {
            showSnackbar();
        }

        if (!isButtonShown) {
            mButtonView.setVisibility(View.GONE);
        }

        if (isSessionFinished) {
            mCenterMapFab.setVisibility(View.VISIBLE);
        }

        mDescriptionView.setText(mChallenge.getDescription());
    }

    public boolean isFinished() {
        return isSessionFinished;
    }

    /**
     * Set Button that starts the Stopwatch
     */
    public void setButtonView(Button button) {
        mButtonView = button;

        button.setOnClickListener(view -> {
            startLocationService();
            startStopWatch();

            // Once the button has been clicked, remove it
            mButtonView.setVisibility(View.GONE);
            isButtonShown = false;
        });
    }

    public void setFab(FloatingActionButton fab) {
        mCenterMapFab = fab;
        mCenterMapFab.setVisibility(View.INVISIBLE);
        mCenterMapFab.setOnClickListener(view -> positionMapAtEnd());
    }

    private void positionMapAtEnd() {
        if (mLocationList == null || mGoogleMap == null || mLocationList.isEmpty()) {
            return;
        }

        int position = mLocationList.size() - 1;
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(mLocationList.get(position).latitude,
                        mLocationList.get(position).longitude))
                .bearing(0f)
                .tilt(45f)
                .zoom(15f)
                .build();

        mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    /**
     * Set TextView representing the latitude/longitude display
     */
    public void setDescriptionView(TextView view) {
        mDescriptionView = view;
        mDescriptionView.setText(mChallenge.getDescription());
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
        if (!isStopwatchBound && !isAlarmPlayed && !isGoalReached) {
            Intent intent = StopwatchService.getIntent(mContext,
                    mStartTime,
                    mChallenge.getTimeToComplete());

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
    }

    @Override
    public void onLocationUpdate(Location location, PolylineOptions polyline) {
        mLocationList = mLocationService.getLocationList();
        mTotalDistance = mLocationService.getDistance();

        if (mGoogleMap == null) {
            return;
        }

        updateMarkerPosition(location);
        updatePolylines(polyline);
        updateDistance(mTotalDistance);
        updateCamera(location);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mGoogleMap = map;
    }

    @Override
    public void onStopwatchUpdate(String timeString) {
        mStopWatchView.setText(timeString);

        if (mTotalDistance >= mChallenge.getDistance()) {
            finishRun();

            return;
        }

        long time = mStopwatchService.getTime();
        checkTime(time);
    }

    /**
     * check if the goal time has elapsed and the out-of-time alarm hasn't played yet
     * @param time the time from the stopwatch to check
     */
    private void checkTime(long time) {
        // check if the goal time has elapsed and the out-of-time alarm hasn't played yet
        if (time > mChallenge.getTimeToComplete() && !isAlarmPlayed) {
            mStopWatchView.setTextColor(mContext.getResources().getColor(R.color.red));
            isAlarmPlayed = true;
            playAlarm(R.raw.airhorn);
            finishRun();
        } else if (time > mChallenge.getTimeToComplete() * 0.66 &&
                !isAlarmPlayed) {

            mStopWatchView.setTextColor(mContext.getResources().getColor(R.color.yellow));
        }
    }

    /**
     * Finish the run by stopping services and storing the challenge in the database
     */
    private void finishRun() {
        if (isSessionFinished) {
            return;
        }

        mCenterMapFab.setVisibility(View.VISIBLE);

        mLocationService.setFinished(true); // tell the location service to stop updating
        isGoalReached = mLocationService.isGoalReached();

        // if there is a new fastest time, update the challenge in the database
        if (isGoalReached) {
            long time = mStopwatchService.getTime();
            long fastestTime = mChallenge.getFastestTime();
            if (fastestTime == 0L || time < fastestTime) {
                DataUtils.updateFastestTime(mContext.getContentResolver(), mChallenge, time);
                mStopWatchView.setTextColor(mContext.getResources().getColor(R.color.green));
            }

            playAlarm(R.raw.applause); // play win sound
        }

        mEndTime = mStopwatchService.getTime();

        Session session = new Session(mChallenge,
                DataUtils.getCurrentDateString(),
                mStopwatchService.getTime(),
                mLocationService.getLocationList(),
                isGoalReached);

        stopStopWatch();
        stopLocationService();

        DataUtils.insertSession(mContext.getContentResolver(), session);

        // update the widget now
        broadcastSession(isGoalReached, session.getTime(), session.getChallenge());

        isSessionFinished = true;
    }

    // TODO: Attribute horn sound from http://soundbible.com/1542-Air-Horn.html
    // Attribute applause from http://soundbible.com/988-Applause.html
    /**
     * Plays a sound
     * @param soundId resource id of sound
     */
    private void playAlarm(int soundId) {
        mMediaPlayer = MediaPlayer.create(mContext, soundId);
        mMediaPlayer.setOnCompletionListener(MediaPlayer::release);
        mMediaPlayer.setLooping(false);
        mMediaPlayer.start();

        Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(500L);

        showSnackbar();
    }

    private void showSnackbar() {
        int messageId = isGoalReached ? R.string.snackbar_challenge_success :
                R.string.snackbar_challenge_failed;

        isSnackbarShowing = true;

        Snackbar.make(mStopWatchView, messageId, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.snackbar_dismiss, view -> isSnackbarShowing = false)
                .show();
    }

    /**
     * Starts the Location service
     */
    private void startLocationService() {
        if (!isLocationBound && !isAlarmPlayed  && !isGoalReached) {
            Intent intent = LocationService.getStartIntent(mContext, mChallenge);
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

        Intent intent = LocationService.getStopIntent(mContext);
        mContext.stopService(intent);
    }

    private void updateMarkerPosition(Location location) {
        if (mCurrentLocationMarker != null) {
            mCurrentLocationMarker.remove();
        }

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        mCurrentLocationMarker = mGoogleMap.addMarker(markerOptions);
    }

    private void updatePolylines(PolylineOptions polyline) {
        List<LatLng> list = mLocationService.getLocationList();

        if (list.size() < 2) {
            return;
        }

        mGoogleMap.addPolyline(polyline);
    }

    /** Updates the total distance run every time a polyline is updated */
    private void updateDistance(float totalDistance) {
        mTotalDistanceView.setText(String.format(Locale.US,
                "%.2f %s",
                totalDistance / FEET_PER_MILE, // convert feet to miles
                mContext.getResources().getString(R.string.run_units)));
    }

    /**
     * Update the camera's position when the user moves
     * @param location user's location
     */
    private void updateCamera(Location location) {
        // stop updating if the run is over
        if (isAlarmPlayed) {
            return;
        }

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
            if (mLocationList != null) {
                mLocationService.setLocationList(mLocationList);
                mLocationService.recalculateDistance();
                updateDistance(mLocationService.getDistance());
            }
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

    /**
     * Update the widget
     * @param isGoalReached Has the challenge been successfully completed?
     * @param time the current session's time
     * @param challenge the current session's challenge
     */
    private void broadcastSession(boolean isGoalReached, long time, Challenge challenge) {
        Intent intent = RunWidget.getIntent(isGoalReached, time, challenge);
        mContext.sendBroadcast(intent);
    }
}