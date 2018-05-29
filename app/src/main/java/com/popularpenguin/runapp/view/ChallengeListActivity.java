package com.popularpenguin.runapp.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.popularpenguin.runapp.R;
import com.popularpenguin.runapp.adapter.ChallengeAdapter;
import com.popularpenguin.runapp.data.Challenge;
import com.popularpenguin.runapp.loader.ChallengeLoader;
import com.popularpenguin.runapp.map.RunTracker;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChallengeListActivity extends AppCompatActivity implements
        ChallengeAdapter.ChallengeAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<List<Challenge>> {

    @BindView(R.id.app_bar_challenge_list) AppBarLayout mAppBar;
    @BindView(R.id.collapsing_toolbar_challenge_list) CollapsingToolbarLayout mCollapsingToolbar;
    @BindView(R.id.toolbar_challenge_list) Toolbar mToolbar;
    @BindView(R.id.rv_challenge_list) RecyclerView mRecyclerView;
    @BindView(R.id.ad_view_challenge_list) AdView mAdView;

    private List<Challenge> mChallengeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_list);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

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
                    mCollapsingToolbar.setTitle(appName);
                    isShowing = true;
                }
                else if (isShowing) {
                    mCollapsingToolbar.setTitle(" ");
                    isShowing = false;
                }
            }
        });

        getSupportLoaderManager().initLoader(0, null, this);

        setAdView();

        checkPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkIfGpsEnabled();
    }

    private void checkIfGpsEnabled() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            enableGps();
        }
    }

    private void enableGps() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            String[] permissions = new String[] { Manifest.permission.ACCESS_FINE_LOCATION };
            ActivityCompat.requestPermissions(this, permissions, 0);
        }
    }

    private void setRecyclerView() {
        ChallengeAdapter adapter = new ChallengeAdapter(mChallengeList, this);
        mRecyclerView.setAdapter(adapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
    }

    // TODO: Load real ads after submitting project before uploading to Google Play
    private void setAdView() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);
    }

    @Override
    public void onClick(int position) {
        checkPermissions();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Snackbar.make(mAppBar, R.string.snackbar_location_services, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.snackbar_dismiss, view -> {})
                    .show();

            return;
        }

        Intent intent = new Intent(this, ChallengeActivity.class);
        intent.putExtra(RunTracker.CHALLENGE_BUNDLE_KEY, mChallengeList.get(position));

        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_challenge_list, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.action_launch_session_list:
                Intent intent = new Intent(this, SessionListActivity.class);
                startActivity(intent);

                break;

            case R.id.action_show_info:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_about_title)
                        .setMessage(R.string.dialog_about_body)
                        .setPositiveButton(R.string.dialog_about_close, (dialog, which) ->
                            dialog.dismiss())
                        .show();

                break;

            default:
                throw new IllegalArgumentException("Invalid menu id");
        }

        return true;
    }

    // Loader Callbacks //////////////////////////////////////////////////////////////////////////
    @NonNull
    @Override
    public Loader<List<Challenge>> onCreateLoader(int id, @Nullable Bundle args) {
        return new ChallengeLoader(this);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Challenge>> loader, List<Challenge> data) {
        mChallengeList = data;

        setRecyclerView();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Challenge>> loader) {
        // Not implemented
    }
}
