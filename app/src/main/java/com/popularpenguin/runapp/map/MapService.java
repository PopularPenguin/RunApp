package com.popularpenguin.runapp.map;

import android.app.FragmentManager;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Class to access Google's Map API
 */
public class MapService implements OnMapReadyCallback {

    private GoogleMap mGoogleMap;

    public MapService(@NonNull FragmentManager fragmentManager, int resId) {
        MapFragment mapFragment = (MapFragment)
                fragmentManager.findFragmentById(resId);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);

        mOnReadyListener.onMapReady(mGoogleMap);
    }

    // Listener + Interface ////////////////////////////////////////////////////////
    private OnReadyListener mOnReadyListener;

    public void setOnReadyListener(OnReadyListener listener) {
        mOnReadyListener = listener;
    }

    public interface OnReadyListener {
        void onMapReady(GoogleMap map);
    }
}