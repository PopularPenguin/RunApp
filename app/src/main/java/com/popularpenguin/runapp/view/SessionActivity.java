package com.popularpenguin.runapp.view;

import android.os.PersistableBundle;
import android.os.SystemClock;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Chronometer;
import android.widget.TextView;

import com.popularpenguin.runapp.R;
import com.popularpenguin.runapp.map.RunTracker;
import com.popularpenguin.runapp.map.StopWatch;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SessionActivity extends AppCompatActivity {

    @BindView(R.id.app_bar_session) AppBarLayout mAppBar;
    @BindView(R.id.collapsing_toolbar_session) CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.tv_location_test) TextView mLocationView;
    @BindView(R.id.tv_stopwatch) TextView mTimerText;
    //@BindView(R.id.run_timer) Chronometer mTimer;
    @BindView(R.id.toolbar_session) Toolbar mToolbar;

    private RunTracker mRunTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupAppBar();

        mRunTracker = new RunTracker(this, R.id.map_fragment);
        mRunTracker.setLocationView(mLocationView);
        mRunTracker.setStopWatchView(mTimerText);

        if (savedInstanceState != null) {
            mRunTracker.setBundle(savedInstanceState);
        }

       // mTimer.setBase(SystemClock.elapsedRealtime());
       // mTimer.start();
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

        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(RunTracker.BUNDLE_KEY, mRunTracker.getBundle());
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

    private void setupTracker() {
        mRunTracker = new RunTracker(this, R.id.map_fragment);
        mRunTracker.setLocationView(mLocationView); // TODO: This won't be needed later
    }
}
