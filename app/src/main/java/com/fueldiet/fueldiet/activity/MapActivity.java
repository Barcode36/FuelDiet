package com.fueldiet.fueldiet.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import com.fueldiet.fueldiet.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Locale;

public class MapActivity extends BaseActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {
    private static final String TAG = "MapActivity";

    SupportMapFragment mapFragment;
    private GoogleMap googleMap;
    private FloatingActionButton saveValues;
    private TextView latitudeValue, longitudeValue;
    ImageView clearMarker;
    private LatLng latLng;
    private Locale locale;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(TAG, "onBackPressed");
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Configuration configuration = getResources().getConfiguration();
        locale = configuration.getLocales().get(0);

        ActionBar actionBar = getSupportActionBar();
        //noinspection ConstantConditions
        actionBar.setTitle(R.string.select_location);

        saveValues = findViewById(R.id.map_save_coords);
        latitudeValue = findViewById(R.id.map_latitude_value);
        longitudeValue = findViewById(R.id.map_longitude_value);
        clearMarker = findViewById(R.id.map_clear_marker);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        //noinspection ConstantConditions
        mapFragment.getMapAsync(this);

        clearMarker.setOnClickListener(v -> {
            deleteMarker();
        });

        saveValues.setOnClickListener(v -> {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("lat", latLng.latitude);
            returnIntent.putExtra("lon", latLng.longitude);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        UiSettings uiSettings = this.googleMap.getUiSettings();
        uiSettings.setCompassEnabled(true);
        uiSettings.setZoomControlsEnabled(true);
        this.googleMap.setOnMapClickListener(this);
        if (getIntent().getDoubleExtra("lat", 0) != 0 && getIntent().getDoubleExtra("lon", 0) != 0) {
            latitudeValue.setText(String.format(locale, "%f", getIntent().getDoubleExtra("lat", 0)));
            longitudeValue.setText(String.format(locale, "%f", getIntent().getDoubleExtra("lon", 0)));
            LatLng tmp = new LatLng(getIntent().getDoubleExtra("lat", 0), getIntent().getDoubleExtra("lon", 0));
            googleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker()).position(tmp));
            animateCamera(tmp);
            latLng = tmp;
        }
    }

    private void animateCamera(@NonNull LatLng latLng) {
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(getCameraPositionWithBearing(latLng)));
    }
    @NonNull
    private CameraPosition getCameraPositionWithBearing(LatLng latLng) {
        return new CameraPosition.Builder().target(latLng).zoom(16).build();
    }

    private void deleteMarker() {
        googleMap.clear();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        deleteMarker();
        googleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker()).position(latLng));
        latitudeValue.setText(String.format(locale, "%f", latLng.latitude));
        longitudeValue.setText(String.format(locale, "%f", latLng.longitude));
        this.latLng = latLng;
        saveValues.setVisibility(View.VISIBLE);
        Log.d(TAG, "onMapClick: " + latLng.toString());
    }
}
