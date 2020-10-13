package com.fueldiet.fueldiet.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.VolleySingleton;
import com.fueldiet.fueldiet.adapter.StationsPricesAdapter;
import com.fueldiet.fueldiet.dialog.StationsPricesSearchDialog;
import com.fueldiet.fueldiet.object.StationPriceObject;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StationsPricesFragment extends Fragment implements OnMapReadyCallback, Response.ErrorListener, Response.Listener<JSONObject> {

    private static final String TAG = "StationsPricesFragment";
    private static final String ICON_ID = "ICON_ID";
    private static final String ALL_STATIONS_API = "https://goriva.si/api/v1/franchise/?format=json";
    private static final String SEARCH_RESULTS_API = "https://goriva.si/api/v1/search/?format=json&franchise=%s&name=%s&o=%s&position=%s&radius=%s&&timestamp=%s";
    private static final String SEARCH_DIALOG = "SEARCH DIALOG";
    private static final String CLEANED_FRANCHISES = "CLEANED FRANCHISES";
    private static final String AVAILABLE_RADIUS = "AVAILABLE RADIUS";
    private static final int REQUEST_CODE = 1;
    private static final String CITY = "CITY";
    private static final String RADIUS = "RADIUS";
    private static final String FRANCHISE = "FRANCHISE";

    MapView mapView;
    SymbolManager symbolManager;
    private Locale locale;
    ArrayList<Symbol> symbols;
    SharedPreferences pref;

    RecyclerView stations;
    MaterialCardView loadingAlert;
    RecyclerView.LayoutManager mLayoutManager;
    LinearLayoutManager horizontalLayout;
    StationsPricesAdapter mAdapter;
    List<StationPriceObject> data;
    MapboxMap mapboxMapMain;

    ExtendedFloatingActionButton searchButton;

    String nextLink = "null";
    ArrayMap<String, Integer> cleanedFranchiseNames;
    ArrayMap<Integer, String> cleanedFranchiseId;
    ArrayList<String> availableRadius;

    private RequestQueue mQueue;
    private boolean newSearch = false;

    public static StationsPricesFragment newInstance() {
        return new StationsPricesFragment();
    }


    @SuppressWarnings("ConstantConditions")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Configuration configuration = getResources().getConfiguration();
        locale = configuration.getLocales().get(0);

        mQueue = VolleySingleton.getInstance(getContext()).getRequestQueue();

        Mapbox.getInstance(getContext(), getString(R.string.mapbox_access_token));
        View view = inflater.inflate(R.layout.fragment_stations_prices, container, false);
        mapView = view.findViewById(R.id.mapView);

        symbols = new ArrayList<>();
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        loadingAlert = view.findViewById(R.id.stations_prices_loading_alert);
        searchButton = view.findViewById(R.id.stations_open_search_button);

        availableRadius = createRadiusData();
        getAvailableStations();

        searchButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putStringArrayList(CLEANED_FRANCHISES, new ArrayList<>(cleanedFranchiseId.values()));
            bundle.putStringArrayList(AVAILABLE_RADIUS, availableRadius);

            FragmentManager fm = getActivity().getSupportFragmentManager();
            StationsPricesSearchDialog dialog = new StationsPricesSearchDialog();
            dialog.setTargetFragment(this, REQUEST_CODE);
            dialog.setArguments(bundle);
            dialog.show(fm, SEARCH_DIALOG);
        });

        data = new ArrayList<>();
        stations = view.findViewById(R.id.station_prices_recycler_view);
        mLayoutManager = new LinearLayoutManager(getContext());
        stations.setLayoutManager(mLayoutManager);
        horizontalLayout = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,false);
        stations.setLayoutManager(horizontalLayout);

        return view;
    }

    private void getStationPrices(String franchise, String name, String source, String position, String radius) {
        Log.d(TAG, "getStationPrices: before: f " + franchise + " name " + name + " source " + source + " position " + position + " radius " + radius);
        int i = cleanedFranchiseNames.get(franchise);

        String f = i == 0 ? "" : i+"";
        String n = name == null ? "" : name;
        String s = source == null ? "" : source;
        String p = position == null ? "" : position;
        String r = radius.equals("∞ km") ? "" : radius.replaceAll("\\D+", "").concat("000");
        Log.d(TAG, "getStationPrices: before: f " + f + " name " + n + " source " + s + " position " + p + " radius " + r);

        if (p.equals("")) {
            Log.e(TAG, "getStationPrices: city is empty");
            Snackbar.make(getView(), "City or current location is mandatory", Snackbar.LENGTH_SHORT)
                    .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
                    .show();
            return;
        }

        String url = String.format(SEARCH_RESULTS_API, f, n, s, p, r, Calendar.getInstance().getTimeInMillis());
        Log.d(TAG, "getStationPrices: url: "+ url);
        loadingAlert.setVisibility(View.VISIBLE);
        newSearch = true;
        JsonObjectRequest request;
        request = new JsonObjectRequest(Request.Method.GET, url, null, this, this);
        mQueue.add(request);
    }

    private void showMore() {
        Log.d(TAG, "showMore: calling to view more stations");
        if (!nextLink.equals("null")) {
            Log.d(TAG, "showMore: link exists");
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, nextLink, null, this, this);
            mQueue.add(request);
        }
    }

    private ArrayList<String> createRadiusData() {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("∞ km");
        arrayList.add("1 km");
        arrayList.add("5 km");
        arrayList.add("10 km");
        arrayList.add("20 km");
        arrayList.add("50 km");
        return arrayList;
    }

    private void deleteMarker(Symbol symbol) {
        Log.d(TAG, "deleteMarker");
        symbolManager.delete(symbol);
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
                stations.smoothScrollToPosition(symbols.indexOf(symbol));
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

    private void getAvailableStations() {
        loadingAlert.setVisibility(View.VISIBLE);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, ALL_STATIONS_API, null, response -> {
            ArrayMap<Integer, String> availableFranchises = new ArrayMap<>();
            for (int i = 0; i < response.length(); i++) {
                try {
                    availableFranchises.put(response.getJSONObject(i).getInt("pk"), response.getJSONObject(i).getString("name"));
                } catch (JSONException e) {
                    Log.e(TAG, "onResponse: ", e);
                }
            }
            cleanedFranchiseId = new ArrayMap<>();
            cleanedFranchiseNames = new ArrayMap<>();

            cleanedFranchiseId.put(0, "/");
            cleanedFranchiseNames.put("/", 0);

            for (Integer stationId : availableFranchises.keySet()) {
                String name = availableFranchises.get(stationId);
                String n = name.split(" d.")[0].trim().replace(",", "").replace("FE - Trading", "Avanti");
                cleanedFranchiseId.put(stationId, n);
                cleanedFranchiseNames.put(n, stationId);
            }
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
            loadingAlert.setVisibility(View.INVISIBLE);
        }, this);
        mQueue.add(request);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE) {
            Log.d(TAG, "onActivityResult: search dialog was closed");
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "onActivityResult: user pressed search on dialog");
                getStationPrices(data.getStringExtra(FRANCHISE), null, null, data.getStringExtra(CITY), data.getStringExtra(RADIUS));
            } else {
                Log.d(TAG, "onActivityResult: user pressed cancel on dialog");
            }
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        loadingAlert.setVisibility(View.INVISIBLE);
        Log.e(TAG, "onErrorResponse: ", error);
    }

    @Override
    public void onResponse(JSONObject response) {
        try {
            nextLink = response.getString("next");
        } catch (JSONException e) {
            Log.e(TAG, "onResponse: ", e);
        }
        try {
            JSONArray dataJSON = response.getJSONArray("results");
            Log.d(TAG, "onResponse: " + dataJSON.length());
            if (dataJSON.length() == 0) {
                Snackbar.make(getView(), getString(R.string.no_data_chart), Snackbar.LENGTH_LONG).setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE).show();
            }
            if (newSearch) {
                data.clear();
                for (Symbol symbol : symbols) {
                    deleteMarker(symbol);
                }
                newSearch = false;
            } else {
                data.remove(data.size()-1);
            }
            ArrayList<StationPriceObject> tmp = new Gson().fromJson(dataJSON.toString(), new TypeToken<List<StationPriceObject>>() {}.getType());
            data.addAll(tmp);

            for (StationPriceObject spo : tmp) {
                LatLng latLng = new LatLng();
                latLng.setLatitude(spo.getLat());
                latLng.setLongitude(spo.getLon());
                showMarker(latLng);
            }

            if (!nextLink.equals("null")) {
                data.add(new StationPriceObject(getString(R.string.show_more)));
            }

            mAdapter.notifyDataSetChanged();
            loadingAlert.setVisibility(View.INVISIBLE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}