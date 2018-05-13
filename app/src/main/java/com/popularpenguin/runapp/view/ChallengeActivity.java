package com.popularpenguin.runapp.view;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.popularpenguin.runapp.R;
import com.popularpenguin.runapp.map.RunTracker;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChallengeActivity extends AppCompatActivity {

    @BindView(R.id.tv_location_test) TextView mLocationView;
    @BindView(R.id.tv_distance) TextView mDistanceText;
    @BindView(R.id.tv_stopwatch) TextView mTimerText;
    @BindView(R.id.btn_stopwatch) Button mTimerButton;
    @BindView(R.id.fab_challenge) FloatingActionButton mCenterMapFab;

    private RunTracker mRunTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge);

        ButterKnife.bind(this);

        setupTracker(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mRunTracker.start();
    }

    @Override
    protected void onStop() {
        mRunTracker.stop();

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mRunTracker.destroy();

        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(RunTracker.TRACKER_BUNDLE_KEY, mRunTracker.getBundle());
    }

    private void setupTracker(Bundle bundle) {
        mRunTracker = new RunTracker(this, R.id.map_challenge_fragment);

        mRunTracker.setLocationView(mLocationView);
        mRunTracker.setDistanceView(mDistanceText);
        mRunTracker.setStopWatchView(mTimerText);
        mRunTracker.setButtonView(mTimerButton);
        mRunTracker.setFab(mCenterMapFab);

        if (bundle != null) {
            mRunTracker.setBundle(bundle);
        }
    }
}
