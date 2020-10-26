package com.fueldiet.fueldiet.fragment;

import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.object.StationPricesObject;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.JsonObject;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

public class FuelPricesMapFragment extends Fragment implements OnMapReadyCallback, MapboxMap.OnMapClickListener {

    private static final String TAG = "StationsPricesMapFragment";
    private static final String ICON_ID = "ICON_ID";
    private static final String SOURCE_ID = "SOURCE_ID";
    private static final String LAYER_ID = "LAYER_ID";

    private ArrayList<StationPricesObject> data;
    private HashMap<Integer, Feature> points;
    private HashMap<Integer, String> names;

    MapView mapView;
    private Locale locale;
    ArrayList<Symbol> symbols;
    MapboxMap mapboxMapMain;
    MaterialCardView showStationData;

    public TextView franchiseName, locationName, petrolPrice, dieselPrice;
    public MaterialButton closeStation, navigateToButton;

    public FuelPricesMapFragment(ArrayList<StationPricesObject> data, HashMap<Integer, String> names) {
        this.data = data;
        this.names = names;

        this.points = new HashMap<>();
        for (StationPricesObject spo : data) {
            points.put(spo.getPk(), Feature.fromGeometry(spo.getPoint(), new JsonObject(), Integer.toString(spo.getPk())));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Mapbox.getInstance(requireContext(), getString(R.string.mapbox_access_token));
        Configuration configuration = getResources().getConfiguration();
        locale = configuration.getLocales().get(0);
        View view = inflater.inflate(R.layout.fragment_fuel_prices_map, container, false);
        mapView = view.findViewById(R.id.mapView);


        showStationData = view.findViewById(R.id.fuel_price_show_station);
        franchiseName = view.findViewById(R.id.station_prices_franch_name);
        locationName = view.findViewById(R.id.station_prices_stat_name);
        petrolPrice = view.findViewById(R.id.stations_prices_95_price);
        dieselPrice = view.findViewById(R.id.stations_prices_diesel_price);
        closeStation = view.findViewById(R.id.stations_prices_close);
        navigateToButton = view.findViewById(R.id.stations_prices_navigate);

        symbols = new ArrayList<>();
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        return view;
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        mapboxMapMain = mapboxMap;

        List<Feature> stations = new ArrayList<>(points.values());

        mapboxMapMain.setStyle(
                new Style.Builder()
                        .fromUri("mapbox://styles/mapbox/streets-v11")
                        .withImage(ICON_ID, BitmapFactory.decodeResource(getResources(), R.drawable.mapbox_marker_icon_default))
                        .withSource(new GeoJsonSource(SOURCE_ID, FeatureCollection.fromFeatures(stations)))
                        .withLayer(new SymbolLayer(LAYER_ID, SOURCE_ID).withProperties(
                                iconImage(ICON_ID),
                                iconAllowOverlap(true),
                                iconIgnorePlacement(true)
                                )
                        ), style -> mapboxMapMain.addOnMapClickListener(FuelPricesMapFragment.this)
        );
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        return handleClickIcon(mapboxMapMain.getProjection().toScreenLocation(point));
    }

    private boolean handleClickIcon(PointF screenPoint) {
        List<Feature> features = mapboxMapMain.queryRenderedFeatures(screenPoint, LAYER_ID);
        if (!features.isEmpty()) {

            StationPricesObject station = data.stream().filter(ps -> Integer.toString(ps.getPk()).equals(features.get(0).id())).findFirst().get();

            franchiseName.setText(names.get(station.getFranchise()).toUpperCase(locale));
            locationName.setText(station.getName());
            dieselPrice.setText(String.format(locale, "%4.3f€", station.getPrices().get("dizel")));
            petrolPrice.setText(String.format(locale, "%4.3f€", station.getPrices().get("95")));

            closeStation.setOnClickListener(v -> showStationData.setVisibility(View.INVISIBLE));
            //navigateToButton.setOnClickListener(v -> //TODO);

            showStationData.setVisibility(View.VISIBLE);
            Log.d(TAG, "handleClickIcon: clicked at station " + features.get(0).id());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
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
    public void onDestroy() {
        super.onDestroy();
        if (mapboxMapMain != null) {
            mapboxMapMain.removeOnMapClickListener(this);
        }
        mapView.onDestroy();
    }
}

/*
mAdapter = new StationsPricesAdapter(getContext(), data, cleanedFranchiseId);
            mAdapter.setOnItemClickListener(new StationsPricesAdapter.OnItemClickListener() {
                @Override
                public void onStationClick(int position) {
                    CameraPosition newPosition = new CameraPosition.Builder()
                            .target(new LatLng(data.get(position).getLat(), data.get(position).getLon()))
                            .zoom(14)
                            .build();
                    mapboxMapMain.animateCamera(CameraUpdateFactory.newCameraPosition(newPosition), 1000);
                }

                @Override
                public void onShowMoreClick() {
                    showMore();
                }
            });
            stations.setAdapter(mAdapter);
 */
