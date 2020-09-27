package com.fueldiet.fueldiet.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import com.fueldiet.fueldiet.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import java.util.Locale;

public class MapActivity extends BaseActivity implements OnMapReadyCallback {
    private static final String TAG = "MapActivity";
    private static final String ICON_ID = "ICON_ID";

    MapView mapView;
    SymbolManager symbolManager;
    private FloatingActionButton saveValues;
    private TextView latitudeValue, longitudeValue;
    ImageView clearMarker;
    private LatLng latLng;
    private Locale locale;
    Symbol symbol;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(TAG, "onBackPressed");
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Log.d(TAG, "backInActionBarPressed");
            onBackPressed();
            return true;
        }
        return false;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration configuration = getResources().getConfiguration();
        locale = configuration.getLocales().get(0);

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_map);

        ActionBar actionBar = getSupportActionBar();
        //noinspection ConstantConditions
        actionBar.setTitle(R.string.select_location);

        mapView = (MapView) findViewById(R.id.mapView);

        saveValues = findViewById(R.id.map_save_coords);
        latitudeValue = findViewById(R.id.map_lat_value);
        longitudeValue = findViewById(R.id.map_lon_value);
        clearMarker = findViewById(R.id.map_clear_marker);
        //findViewById(R.id.map_values_cont).setClipToOutline(true);

        latitudeValue.setText("No value");
        longitudeValue.setText("No value");

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

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

    private void deleteMarker() {
        Log.d(TAG, "deleteMarker");
        symbolManager.delete(symbol);
        latitudeValue.setText("No value");
        longitudeValue.setText("No value");
        clearMarker.setVisibility(View.INVISIBLE);
        saveValues.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        symbolManager.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                Bitmap marker = BitmapFactory.decodeResource(getResources(), R.drawable.mapbox_marker_icon_default);
                style.addImage(ICON_ID, marker);

                symbolManager = new SymbolManager(mapView, mapboxMap, style);
                symbolManager.addClickListener(symbol -> {
                    Log.d(TAG, "onStyleLoaded: marker clicked");
                    return false;
                });

                if (getIntent().getDoubleExtra("lat", 0) != 0 && getIntent().getDoubleExtra("lon", 0) != 0) {
                    latLng = new LatLng(getIntent().getDoubleExtra("lat", 0),
                            getIntent().getDoubleExtra("lon", 0));
                    latitudeValue.setText(String.format(locale, "%f", getIntent().getDoubleExtra("lat", 0)));
                    longitudeValue.setText(String.format(locale, "%f", getIntent().getDoubleExtra("lon", 0)));
                    showMarker();
                }
            }
        });

        mapboxMap.addOnMapClickListener(new MapboxMap.OnMapClickListener() {
            @Override
            public boolean onMapClick(@NonNull LatLng point) {
                Log.d(TAG, "onMapClick: click registered: lat:"+point.getLatitude()+" ,lon:"+point.getLongitude());
                if (symbol != null) {
                    deleteMarker();
                }
                latLng = point;
                showMarker();
                return false;
            }
        });
    }

    private void showMarker() {
        latitudeValue.setText(String.format(locale, "%f", latLng.getLatitude()));
        longitudeValue.setText(String.format(locale, "%f", latLng.getLongitude()));
        clearMarker.setVisibility(View.VISIBLE);
        saveValues.setVisibility(View.VISIBLE);
        symbol = null;
        symbol = symbolManager.create(new SymbolOptions()
                .withLatLng(latLng)
                .withIconImage(ICON_ID)
        );
    }

    /*

    private void showMarker(GeoPoint point) {
        marker = new Marker(mapView);
        marker.setPosition(point);
        mapView.getOverlays().add(marker);
        mapView.invalidate();
    }

    private void setUpMap() {
        Log.d(TAG, "setUpMap");

        marker = null;
        mapView.setMultiTouchControls(true);
        mapView.setMaxZoomLevel(20.0);
        mapView.setMinZoomLevel(11.0);
        mapView.setTilesScaledToDpi(true);
        IMapController mapController = mapView.getController();
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
        mapView.getOverlays().add(new MapEventsOverlay(receiver));
        mapView.getOverlays().add(new CopyrightOverlay(getBaseContext()));
    }

    private void deleteMarker() {
        if (marker != null)
            mapView.getOverlays().remove(marker);
        marker = null;
        latitudeValue.setText("");
        longitudeValue.setText("");
        mapView.invalidate();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
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
