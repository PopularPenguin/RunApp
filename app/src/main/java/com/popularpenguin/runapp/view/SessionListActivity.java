package com.popularpenguin.runapp.view;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.popularpenguin.runapp.R;
import com.popularpenguin.runapp.adapter.SessionAdapter;
import com.popularpenguin.runapp.data.Session;
import com.popularpenguin.runapp.loader.SessionLoader;

import com.popularpenguin.runapp.data.RunContract.SessionsEntry;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Displays a list of recent sessions that the user can review
 */
public class SessionListActivity extends AppCompatActivity implements
        SessionAdapter.SessionAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<List<Session>> {

    @BindView(R.id.app_bar_session_list) AppBarLayout mAppBar;
    @BindView(R.id.collapsing_toolbar_session_list) CollapsingToolbarLayout mCollapsingToolbar;
    @BindView(R.id.toolbar_session_list) Toolbar mToolbar;
    @BindView(R.id.rv_session_list) RecyclerView mRecyclerView;
    @BindView(R.id.ad_view_session_list) AdView mAdView;

    private List<Session> mSessionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_list);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
    }

    private void setRecyclerView() {
        SessionAdapter adapter = new SessionAdapter(mSessionList, getResources(),this);
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
        Session session = mSessionList.get(position);

        Intent intent = new Intent(this, SessionActivity.class);
        intent.putExtra(Session.LAT_LNG_EXTRA, session.getPath());
        intent.putExtra(Session.DESCRIPTION_EXTRA, session.getChallenge().getDescription());
        intent.putExtra(Session.TIME_EXTRA, session.getTimeString());
        intent.putExtra(Session.FASTEST_TIME_EXTRA, session.getChallenge().getFastestTimeString());

        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_session_list, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.action_delete_sessions:
                createDeleteDialog();
                break;

            case android.R.id.home:
                super.onBackPressed();
                break;

            default:
                throw new IllegalArgumentException("Invalid menu id");
        }

        return true;
    }

    private void createDeleteDialog() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.dialog_delete)
                .setPositiveButton(R.string.dialog_delete_positive, (dialog, which) -> {
                    getContentResolver().delete(SessionsEntry.CONTENT_URI, null, null);

                    if (mRecyclerView != null) {
                        mSessionList.clear();
                        mRecyclerView.getAdapter().notifyDataSetChanged();
                    }
                })
                .setNegativeButton(R.string.dialog_delete_negative,
                        (dialog, which) -> dialog.dismiss())
                .show();
    }

    // Loader Callbacks /////////////////////////////////////////////////////////////////////////
    @NonNull
    @Override
    public Loader<List<Session>> onCreateLoader(int id, @Nullable Bundle args) {
        return new SessionLoader(this);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Session>> loader, List<Session> data) {
        mSessionList = data;

        setRecyclerView();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Session>> loader) {
        // Not implemented
    }
}
