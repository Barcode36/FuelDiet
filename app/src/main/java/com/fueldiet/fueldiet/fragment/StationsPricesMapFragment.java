package com.fueldiet.fueldiet.fragment;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.object.StationPricesObject;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class StationsPricesMapFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "StationsPricesMapFragme";
    private static final String ICON_ID = "ICON_ID";

    private ArrayList<StationPricesObject> data;
    private HashMap<Integer, String> names;

    MapView mapView;
    SymbolManager symbolManager;
    private Locale locale;
    ArrayList<Symbol> symbols;
    MapboxMap mapboxMapMain;

    public StationsPricesMapFragment(ArrayList<StationPricesObject> data, HashMap<Integer, String> names) {
        this.data = data;
        this.names = names;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Mapbox.getInstance(getContext(), getString(R.string.mapbox_access_token));
        Configuration configuration = getResources().getConfiguration();
        locale = configuration.getLocales().get(0);
        View view = inflater.inflate(R.layout.fragment_fuel_prices_map, container, false);
        mapView = view.findViewById(R.id.mapView);

        symbols = new ArrayList<>();
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        return view;
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        mapboxMapMain = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
            Bitmap marker = BitmapFactory.decodeResource(getResources(), R.drawable.mapbox_marker_icon_default);
            style.addImage(ICON_ID, marker);

            symbolManager = new SymbolManager(mapView, mapboxMap, style);
            symbolManager.addClickListener(symbol -> {
                Log.d(TAG, "onStyleLoaded: marker clicked");
                //stations.smoothScrollToPosition(symbols.indexOf(symbol));
                return false;
            });
        });
    }

    private void showMarker(LatLng latLng) {
        Symbol symbol = symbolManager.create(new SymbolOptions()
                .withLatLng(latLng)
                .withIconImage(ICON_ID)
        );
        symbols.add(symbol);
    }

    private void deleteMarker(Symbol symbol) {
        Log.d(TAG, "deleteMarker");
        symbolManager.delete(symbol);
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
