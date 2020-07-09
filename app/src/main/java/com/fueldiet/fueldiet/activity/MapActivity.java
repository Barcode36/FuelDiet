package com.fueldiet.fueldiet.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.preference.PreferenceManager;

import com.fueldiet.fueldiet.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.osmdroid.api.IMapController;

import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.library.BuildConfig;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.util.Locale;

import static org.osmdroid.tileprovider.util.StorageUtils.getStorage;

public class MapActivity extends BaseActivity {
    private static final String TAG = "MapActivity";

    MapView map;
    private FloatingActionButton saveValues;
    private TextView latitudeValue, longitudeValue;
    ImageView clearMarker;
    private GeoPoint latLng;
    private Locale locale;
    private Marker marker;

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

        org.osmdroid.config.Configuration.getInstance().load(getBaseContext(), PreferenceManager.getDefaultSharedPreferences(getBaseContext()));
        org.osmdroid.config.Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

        ActionBar actionBar = getSupportActionBar();
        //noinspection ConstantConditions
        actionBar.setTitle(R.string.select_location);

        saveValues = findViewById(R.id.map_save_coords);
        latitudeValue = findViewById(R.id.map_latitude_value);
        longitudeValue = findViewById(R.id.map_longitude_value);
        clearMarker = findViewById(R.id.map_clear_marker);

        map = (MapView) findViewById(R.id.map);


        map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        setUpMap();

        clearMarker.setOnClickListener(v -> {
            deleteMarker();
        });

        saveValues.setOnClickListener(v -> {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("lat", latLng.getLatitude());
            returnIntent.putExtra("lon", latLng.getLongitude());
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        });
    }

    private void showMarker(GeoPoint point) {
        marker = new Marker(map);
        marker.setPosition(point);
        map.getOverlays().add(marker);
        map.invalidate();
    }

    private void setUpMap() {
        Log.d(TAG, "setUpMap");
        marker = null;
        map.setMultiTouchControls(true);
        map.setMaxZoomLevel(20.0);
        map.setMinZoomLevel(11.0);
        map.setTilesScaledToDpi(true);
        IMapController mapController = map.getController();
        mapController.setZoom(12.0);
        if (getIntent().getDoubleExtra("lat", 0) != 0 && getIntent().getDoubleExtra("lon", 0) != 0) {
            GeoPoint point = new GeoPoint(getIntent().getDoubleExtra("lat", 0),
                    getIntent().getDoubleExtra("lon", 0));
            latitudeValue.setText(String.format(locale, "%f", getIntent().getDoubleExtra("lat", 0)));
            longitudeValue.setText(String.format(locale, "%f", getIntent().getDoubleExtra("lon", 0)));
            latLng = point;
            mapController.setCenter(point);
            showMarker(point);
            mapController.setZoom(18.0);
        } else {
            GeoPoint point = new GeoPoint(46.051314, 14.501953);
            mapController.setCenter(point);
        }

        MapEventsReceiver receiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                latLng = p;
                deleteMarker();
                latitudeValue.setText(String.format(locale, "%f", p.getLatitude()));
                longitudeValue.setText(String.format(locale, "%f", p.getLongitude()));
                showMarker(p);
                saveValues.setVisibility(View.VISIBLE);
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };
        map.getOverlays().add(new MapEventsOverlay(receiver));
        map.getOverlays().add(new CopyrightOverlay(getBaseContext()));
    }

    private void deleteMarker() {
        if (marker != null)
            map.getOverlays().remove(marker);
        marker = null;
        latitudeValue.setText("");
        longitudeValue.setText("");
        map.invalidate();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
    }

    /**
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
    */
}
