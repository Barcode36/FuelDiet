package com.fueldiet.fueldiet.fragment;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
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
import com.fueldiet.fueldiet.object.StationPriceObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.mapboxsdk.Mapbox;
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
import java.util.List;
import java.util.Locale;

public class StationsPricesFragment extends Fragment implements OnMapReadyCallback, Response.ErrorListener, Response.Listener<JSONObject> {

    private static final String TAG = "StationsPricesFragment";
    private static final String ICON_ID = "ICON_ID";
    private static final String ALL_STATIONS_API = "https://goriva.si/api/v1/franchise/?format=json";
    private static final String SEARCH_RESULTS_API = "https://goriva.si/api/v1/search/?format=json&franchise=%s&name=%s&o=%s&position=%s&radius=%s";

    MapView mapView;
    SymbolManager symbolManager;
    private LatLng latLng;
    private Locale locale;
    Symbol symbol;
    SharedPreferences pref;

    RecyclerView stations;
    CardView loadingAlert;
    RecyclerView.LayoutManager mLayoutManager;
    LinearLayoutManager horizontalLayout;
    StationsPricesAdapter mAdapter;
    List<StationPriceObject> data;

    ImageView currentLocation, showMore, search;
    EditText cityName;
    Spinner radious, station;
    View advancedContainer;

    String nextLink;
    ArrayList<String> availableStations;

    private RequestQueue mQueue;

    public static StationsPricesFragment newInstance() {
        return new StationsPricesFragment();
    }

    @Override
    public void onStop() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        super.onStop();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        currentLocation = view.findViewById(R.id.stations_prices_location);
        search = view.findViewById(R.id.stations_prices_search_button);
        showMore = view.findViewById(R.id.stations_prices_show_more);
        cityName = view.findViewById(R.id.stations_prices_city_input);
        radious = view.findViewById(R.id.stations_prices_radious);
        station = view.findViewById(R.id.station_prices_stations);
        advancedContainer = view.findViewById(R.id.stations_prices_advances_search);
        loadingAlert = view.findViewById(R.id.stations_prices_loading_alert);

        showMore.setOnClickListener(v -> {
            Log.d(TAG, "onCreateView: toggle advanced");
            if (advancedContainer.getVisibility() == View.VISIBLE) {
                advancedContainer.setVisibility(View.GONE);
                showMore.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24);
            } else {
                advancedContainer.setVisibility(View.VISIBLE);
                showMore.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24);
            }
        });

        search.setOnClickListener(v -> {
            Log.d(TAG, "onCreateView: search clicked");
            if (advancedContainer.getVisibility() == View.VISIBLE) {
                getStationPrices(station.getSelectedItemPosition(), null, null, cityName.getText().toString(), radious.getSelectedItem().toString().replaceAll("[∞ km]", "").concat("000"));
            } else {
                if (!cityName.getText().toString().equals("")) {
                    getStationPrices(null, null, null, cityName.getText().toString(), null);
                }
            }
            if (nextLink != null) {
                //add different last card to click more
            }
        });

        currentLocation.setOnClickListener(v -> {
            Log.d(TAG, "onCreateView: current location clicked");
        });

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, createRadiousData());
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        radious.setAdapter(arrayAdapter);
        radious.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: item selected"+parent.getItemAtPosition(position).toString());
            }
            @Override
            public void onNothingSelected(AdapterView <?> parent) {
            }
        });

        availableStations = new ArrayList<>();

        getAvailableStations();

        data = new ArrayList<>();
        mAdapter = new StationsPricesAdapter(getContext(), data);
        stations = view.findViewById(R.id.station_prices_recycler_view);
        mLayoutManager = new LinearLayoutManager(getContext());
        stations.setLayoutManager(mLayoutManager);
        horizontalLayout = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,false);
        stations.setLayoutManager(horizontalLayout);
        stations.setAdapter(mAdapter);

        return view;
    }

    private void getStationPrices(Integer franchise, String name, String source, String position, String radius) {
        String url = String.format(SEARCH_RESULTS_API, franchise == null ? null : ++franchise, name, source, position, radius).replace("null", "");
        loadingAlert.setVisibility(View.VISIBLE);
        JsonObjectRequest request;
        if (nextLink != null) {
            request = new JsonObjectRequest(Request.Method.GET, nextLink, null, this, this);
        } else {
            request = new JsonObjectRequest(Request.Method.GET, url, null, this, this);
        }
        mQueue.add(request);
    }

    private List<String> createRadiousData() {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("∞ km");
        arrayList.add("1 km");
        arrayList.add("5 km");
        arrayList.add("10 km");
        arrayList.add("20 km");
        arrayList.add("50 km");
        return arrayList;
    }

    private void deleteMarker() {
        Log.d(TAG, "deleteMarker");
        symbolManager.delete(symbol);
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
            Bitmap marker = BitmapFactory.decodeResource(getResources(), R.drawable.mapbox_marker_icon_default);
            style.addImage(ICON_ID, marker);

            symbolManager = new SymbolManager(mapView, mapboxMap, style);
            symbolManager.addClickListener(symbol -> {
                Log.d(TAG, "onStyleLoaded: marker clicked");
                return false;
            });
        });

        mapboxMap.addOnMapClickListener(point -> {
            Log.d(TAG, "onMapClick: click registered: lat:"+point.getLatitude()+" ,lon:"+point.getLongitude());
            if (symbol != null) {
                deleteMarker();
            }
            latLng = point;
            showMarker();
            return false;
        });
    }

    private void showMarker() {
        symbol = null;
        symbol = symbolManager.create(new SymbolOptions()
                .withLatLng(latLng)
                .withIconImage(ICON_ID)
        );
    }

    private void getAvailableStations() {
        loadingAlert.setVisibility(View.VISIBLE);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, ALL_STATIONS_API, null, response -> {
            for (int i = 0; i < response.length(); i++) {
                try {
                    availableStations.add(response.getJSONObject(i).getString("name"));
                } catch (JSONException e) {
                    Log.e(TAG, "onResponse: ", e);
                }
            }
            ArrayList<String> cleanUpStations = new ArrayList<>();
            cleanUpStations.add("/");
            for (String station : availableStations) {
                cleanUpStations.add(station.split(" d.")[0].trim().replace(",", ""));
            }
            ArrayAdapter<String> arrayAdapter1 = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, cleanUpStations);
            arrayAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            station.setAdapter(arrayAdapter1);
            station.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Log.d(TAG, "onItemSelected: item selected"+parent.getItemAtPosition(position).toString());
                }
                @Override
                public void onNothingSelected(AdapterView <?> parent) {
                }
            });
            loadingAlert.setVisibility(View.INVISIBLE);
        }, this);
        mQueue.add(request);
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
            Log.d(TAG, "onResponse: ", e);
        }
        try {
            JSONArray dataJSON = response.getJSONArray("results");
            Log.d(TAG, "onResponse: " + dataJSON.length());
            data.addAll(new Gson().fromJson(dataJSON.toString(), new TypeToken<List<StationPriceObject>>() {}.getType()));
            /*for (int i = 0; i < dataJSON.length(); i++) {
                HashMap<String, Double> prices = new HashMap<>();
                JSONObject pricesObject = dataJSON.getJSONObject(i).getJSONObject("prices");
                for (Iterator<String> it = pricesObject.keys(); it.hasNext(); ) {
                    String key = it.next();
                    if (!pricesObject.getString(key).equals("null"))
                        prices.put(key, pricesObject.getDouble(key));
                }
                data.get(i).setPrices(prices);
            }*/
            loadingAlert.setVisibility(View.INVISIBLE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*
    class GetJSONDataRunnable implements Runnable {
        private static final String TAG = "GetJSONDataRunnable";

        String uri;
        String mode;

        GetJSONDataRunnable(String uri, String mode) {
            this.uri = uri;
            this.mode = mode;
        }

        @Override
        public void run() {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                //loadingAlert.setVisibility(View.VISIBLE);
                Log.d(TAG, "run: calling " + uri);
                URL url = new URL(uri);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuilder buffer = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                    Log.d(TAG, "> " + line);

                }
                if (mode.equals("AS")) {
                    JSONArray jsonArray = new JSONArray(buffer.toString());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        availableStations.add(jsonArray.getJSONObject(i).getString("name"));
                    }
                    ArrayList<String> cleanUpStations = new ArrayList<>();
                    cleanUpStations.add("/");
                    for (String station : availableStations) {
                        cleanUpStations.add(station.split(" d.")[0].trim().replace(",", ""));
                    }
                    ArrayAdapter<String> arrayAdapter1 = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, cleanUpStations);
                    arrayAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    station.setAdapter(arrayAdapter1);
                    station.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            Log.d(TAG, "onItemSelected: item selected"+parent.getItemAtPosition(position).toString());
                        }
                        @Override
                        public void onNothingSelected(AdapterView <?> parent) {
                        }
                    });
                } else if (mode.equals("S")) {
                    //TODO; ---data
                    Log.d(TAG, "run: reading received json data");
                    JSONArray jsonArray = new JSONArray(buffer.toString());
                    JSONArray jsonArray1 = jsonArray.getJSONArray(3);
                    List<StationPriceObject> tmp = new Gson().fromJson(buffer.toString(), new TypeToken<List<StationPriceObject>>() {}.getType());
                }
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (JSONException ex) {
                ex.printStackTrace();
            } finally {
                //loadingAlert.setVisibility(View.INVISIBLE);
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }*/
}