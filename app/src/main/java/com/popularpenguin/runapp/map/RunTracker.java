package com.popularpenguin.runapp.map;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
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

    // TODO: Update distance correctly on rotation
    private static final String TAG = RunTracker.class.getSimpleName();

    public static final String CHALLENGE_BUNDLE_KEY = "challenge";
    public static final String TRACKER_BUNDLE_KEY = "trackerData";

    private static final String PATH_KEY = "path";
    private static final String DISTANCE_KEY = "distance";
    private static final String ALARM_KEY = "alarm";
    private static final String GOAL_KEY = "isGoalReached";
    private static final String BUTTON_KEY = "isButtonShown";
    private static final String FINISHED_KEY = "isSessionFinished";

    public static final float FEET_PER_MILE = 5280.0f;

    private ChallengeActivity mActivity;
    private Context mContext;

    private Challenge mChallenge;

    private LocationService mLocationService;
    private boolean isLocationBound = false;

    private MapService mMapService;

    private GoogleMap mGoogleMap;
    private Location mLocation;
    private Marker mCurrentLocationMarker;
    private List<LatLng> mLocationList;
    private float mTotalDistance;

    private StopwatchService mStopwatchService;
    private long mStartTime;
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

    public RunTracker(AppCompatActivity activity, int resId) {
        mMapService = new MapService(activity.getFragmentManager(), resId);
        mMapService.setOnReadyListener(this);

        mChallenge = activity.getIntent().getParcelableExtra(CHALLENGE_BUNDLE_KEY);

        mActivity = (ChallengeActivity) activity;
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

        if (mStopwatchService != null) {
            bundle.putLong(StopwatchService.START_TIME_EXTRA, mStopwatchService.getTime());
        }
        else {
            bundle.putLong(StopwatchService.START_TIME_EXTRA, mChallenge.getTimeToComplete());
        }

        return bundle;
    }

    /**
     * Extract data from the parent activity's saved instance state
     */
    public void setBundle(Bundle bundle) {
        Bundle locationBundle = bundle.getBundle(TRACKER_BUNDLE_KEY);

        String path = locationBundle.getString(PATH_KEY, "");
        mStartTime = locationBundle.getLong(StopwatchService.START_TIME_EXTRA, 0L);
        mTotalDistance = locationBundle.getFloat(DISTANCE_KEY, 0f);
        isAlarmPlayed = locationBundle.getBoolean(ALARM_KEY, false);
        isGoalReached = locationBundle.getBoolean(GOAL_KEY, false);
        isButtonShown = locationBundle.getBoolean(BUTTON_KEY, true);
        isSessionFinished = locationBundle.getBoolean(FINISHED_KEY, false);

        Log.d(TAG, "Location service is null?" + (mLocationService == null ? "true" : "false"));

        mLocationList = Session.getPathLatLng(path);
        Log.d(TAG, path);
        if (mLocationService != null) {
            mLocationService.setLocationList(mLocationList);
        }

        updateDistance(mTotalDistance);

        mStopWatchView.setText(DataUtils.getFormattedTime(mStartTime));

        if (mStartTime >= mChallenge.getTimeToComplete()) {
            mStopWatchView.setTextColor(Color.RED);
        }
        else if (mStartTime >= mChallenge.getTimeToComplete() * 0.66) {
            mStopWatchView.setTextColor(Color.YELLOW);
        }

        // redisplay finish dialog after a rotation
        if (isSessionFinished) {
            if (isGoalReached) {
                createDialog(R.string.dialog_challenge_complete);
            }
            else {
                createDialog(R.string.dialog_challenge_failed);
            }
        }

        if (!isButtonShown) {
            mButtonView.setVisibility(View.GONE);
        }

        mDescriptionView.setText(mChallenge.getDescription());
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
            if (mLocationList == null) {
                Log.d(TAG, "location list is null");
            }
            if (mGoogleMap == null) {
                Log.d(TAG, "map is null");
            }
            if (mLocationList != null && mLocationList.isEmpty()) {
                Log.d(TAG, "location list is empty");
            }
            return;
        }

        Log.d(TAG, "in positionMapAtEnd(). List size = " + mLocationList.size());

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

        //finishRun(false);
    }

    @Override
    public void onLocationUpdate(Location location, PolylineOptions polyline) {
        mLocation = location;
        mLocationList = mLocationService.getLocationList();
        mTotalDistance = mLocationService.getDistance();
        Log.d(TAG, "Distance is " + mTotalDistance);

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
            playAlarm(R.raw.airhorn, R.string.snackbar_challenge_failed);
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

            playAlarm(R.raw.applause, R.string.snackbar_challenge_success); // play win sound

            createDialog(R.string.dialog_challenge_complete);
        }
        else {
            createDialog(R.string.dialog_challenge_failed); // create a fail dialog
        }

        Session session = new Session(mChallenge,
                DataUtils.getCurrentDateString(),
                mStopwatchService.getTime(),
                mLocationService.getLocationList(),
                isGoalReached);

        stopStopWatch();
        stopLocationService();

        Log.d(TAG, "Location list size = " + mLocationService.getLocationList().size());

        DataUtils.insertSession(mContext.getContentResolver(), session);

        // update the widget now
        broadcastSession(session.getTime(), session.getChallenge());

        isSessionFinished = true;
    }

    private void createDialog(int message) {
        new AlertDialog.Builder(mContext)
                .setMessage(message)
                .setPositiveButton(R.string.dialog_about_close, (dialog, which) -> {
                    mActivity.finish();
                    destroy();
                    dialog.dismiss();
                })
                .show();
    }

    // TODO: Attribute horn sound from http://soundbible.com/1542-Air-Horn.html
    // Attribute applause from http://soundbible.com/988-Applause.html
    /**
     * Plays a sound
     * @param soundId resource id of sound
     * @param messageId resource id of the message to display on the snackbar
     */
    private void playAlarm(int soundId, int messageId) {
        mMediaPlayer = MediaPlayer.create(mContext, soundId);
        mMediaPlayer.setOnCompletionListener(MediaPlayer::release);
        mMediaPlayer.setLooping(false);
        mMediaPlayer.start();

        Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(500L);

        Snackbar.make(mStopWatchView, messageId, Snackbar.LENGTH_LONG)
                .show();
    }

    /**
     * Starts the Location service
     */
    private void startLocationService() {
        if (!isLocationBound && !isAlarmPlayed  && !isGoalReached) {
            Intent intent = LocationService.getStartIntent(mContext, mChallenge);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mContext.startService(intent);
            }
            else {
                mContext.startService(intent);
            }
            //mContext.startService(intent);
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
                .position(latLng);
        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.runicon));

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
            Log.d(TAG, "location service connected");
            LocationService.LocationBinder binder = (LocationService.LocationBinder) iBinder;
            mLocationService = binder.getService();
            mLocationService.connect();
            mLocationService.setOnLocationChangedListener(RunTracker.this);
            isLocationBound = true;
            if (mLocationList != null) {
                mLocationService.setLocationList(mLocationList);
                mLocationService.recalculateDistance();
                Log.d(TAG, "distance so far: " + mLocationService.getDistance());
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
     * @param time the current session's time
     * @param challenge the current session's challenge
     */
    private void broadcastSession(long time, Challenge challenge) {
        Intent intent = RunWidget.getIntent(time, challenge);

        mContext.sendBroadcast(intent);
    }
}