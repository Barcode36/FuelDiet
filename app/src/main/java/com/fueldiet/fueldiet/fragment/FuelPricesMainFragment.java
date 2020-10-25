package com.fueldiet.fueldiet.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.VolleySingleton;
import com.fueldiet.fueldiet.activity.FuelPricesDetailsActivity;
import com.fueldiet.fueldiet.object.StationPricesObject;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class FuelPricesMainFragment extends Fragment implements Response.Listener<JSONObject>, Response.ErrorListener {

    private static final String TAG = "StationsPricesFragment";
    private static final String ALL_STATIONS_API = "https://goriva.si/api/v1/franchise/?format=json";
    private static final String SEARCH_RESULTS_API = "https://goriva.si/api/v1/search/?format=json&franchise=%s&name=%s&o=%s&position=%s&radius=%s&&timestamp=%s";

    private Locale locale;
    SharedPreferences pref;
    MaterialCardView loadingAlert;
    List<StationPricesObject> data;

    ExtendedFloatingActionButton searchButton;

    String nextLink = "null";
    HashMap<String, Integer> cleanedFranchiseNames;
    HashMap<Integer, String> cleanedFranchiseId;
    ArrayList<String> availableRadius;

    private RequestQueue mQueue;
    private boolean newSearch = false;

    MaterialButton currentLocation, openResults;
    TextInputLayout cityName;
    AutoCompleteTextView franchises;
    SeekBar radius;
    TextView seekValue, petrolMinPrice, dieselMinPrice;

    Double cheapestPetrol = null;
    Double cheapestDiesel = null;

    public static FuelPricesMainFragment newInstance() {
        return new FuelPricesMainFragment();
    }


    @SuppressWarnings("ConstantConditions")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Configuration configuration = getResources().getConfiguration();
        locale = configuration.getLocales().get(0);

        mQueue = VolleySingleton.getInstance(getContext()).getRequestQueue();
        View view = inflater.inflate(R.layout.fragment_main_stations_prices, container, false);

        loadingAlert = view.findViewById(R.id.stations_prices_loading_alert);
        searchButton = view.findViewById(R.id.stations_open_search_button);

        currentLocation = view.findViewById(R.id.search_prices_location);
        cityName = view.findViewById(R.id.search_prices_city_input);
        radius = view.findViewById(R.id.search_prices_radius_seekbar);
        franchises = view.findViewById(R.id.search_prices_franchises_value);
        seekValue = view.findViewById(R.id.search_radius_value);
        petrolMinPrice = view.findViewById(R.id.stations_prices_95_price);
        dieselMinPrice = view.findViewById(R.id.stations_prices_diesel_price);
        openResults = view.findViewById(R.id.stations_prices_open_results);
        openResults.setEnabled(false);

        availableRadius = createRadiusData();
        getAvailableStations();

        fillData();

        searchButton.setOnClickListener(v -> {
            if (validateFields()) {
                getStationPrices(franchises.getText().toString(), null, null, cityName.getEditText().getText().toString(), availableRadius.get(radius.getProgress()));
            }
        });

        openResults.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FuelPricesDetailsActivity.class);
            intent.putExtra("mode", 0);
            intent.putExtra("data", (Serializable) data);
            intent.putExtra("names", cleanedFranchiseId);
            startActivity(intent);
        });

        data = new ArrayList<>();
        return view;
    }

    private void fillData() {
        radius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d(TAG, "onProgressChanged: seekbar set to " + availableRadius.get(progress));
                seekValue.setText(availableRadius.get(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        radius.setProgress(1);
        radius.setProgress(0);

        currentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //use device location
            }
        });
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

        String url = String.format(SEARCH_RESULTS_API, f, n, s, p, r, Calendar.getInstance().getTimeInMillis());
        Log.d(TAG, "getStationPrices: url: "+ url);
        loadingAlert.setVisibility(View.VISIBLE);
        newSearch = true;
        JsonObjectRequest request;
        request = new JsonObjectRequest(Request.Method.GET, url, null, this, this);
        request.setRetryPolicy(new DefaultRetryPolicy(3000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mQueue.add(request);
    }

    private boolean validateFields() {
        Log.d(TAG, "validateFields: checking if name or location is selected");
        if (cityName.getEditText().getText().toString().equals("")) {
            Log.d(TAG, "validateFields: it wasn't");
            cityName.setError("Field must be filled.");
            Log.e(TAG, "getStationPrices: city is empty");
            Snackbar.make(requireView(), "City or current location is mandatory", Snackbar.LENGTH_SHORT)
                    .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
                    .show();
            return false;
        } else if (cityName.getError() != null) {
            cityName.setError(null);
        }
        return true;
    }

    private void showMore() {
        Log.d(TAG, "showMore: trying to load additional stations");
        if (!nextLink.equals("null")) {
            Log.d(TAG, "showMore: link exists");
            newSearch = false;
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, nextLink, null, this, this);
            mQueue.add(request);
        } else {
            newSearch = true;
            Log.d(TAG, "showMore: no additional stations available");
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
            cleanedFranchiseId = new HashMap<>();
            cleanedFranchiseNames = new HashMap<>();

            cleanedFranchiseId.put(0, "/");
            cleanedFranchiseNames.put("/", 0);

            for (Integer stationId : availableFranchises.keySet()) {
                String name = availableFranchises.get(stationId);
                String n = name.split(" d.")[0].trim().replace(",", "").replace("FE - Trading", "Avanti");
                cleanedFranchiseId.put(stationId, n);
                cleanedFranchiseNames.put(n, stationId);
            }
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireContext(), R.layout.list_item,  new ArrayList<>(cleanedFranchiseId.values()));
            arrayAdapter.setDropDownViewResource(R.layout.list_item);
            franchises.setAdapter(arrayAdapter);
            franchises.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    Log.d(TAG, "afterTextChanged: new franchise selected " + s.toString());
                }
            });
            franchises.setText("/", false);
            loadingAlert.setVisibility(View.INVISIBLE);
        }, this);
        request.setRetryPolicy(new DefaultRetryPolicy(3000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mQueue.add(request);
    }


    @Override
    public void onErrorResponse(VolleyError error) {
        loadingAlert.setVisibility(View.INVISIBLE);
        if (error instanceof TimeoutError) {
            Log.e(TAG, "onErrorResponse: timeout");
            Snackbar.make(requireView(), R.string.err_timeout_goriva_si, Snackbar.LENGTH_LONG).setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE).show();
        }
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
            Log.d(TAG, "onResponse: got new " + dataJSON.length() + " stations");
            if (dataJSON.length() == 0 && newSearch) {
                Snackbar.make(requireView(), getString(R.string.no_data_chart), Snackbar.LENGTH_LONG).setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE).show();
            }
            if (newSearch) {
                data.clear();
                newSearch = false;
            }
            ArrayList<StationPricesObject> tmp = new Gson().fromJson(dataJSON.toString(), new TypeToken<List<StationPricesObject>>() {}.getType());

            for (StationPricesObject spo : tmp) {
                if ("Avanti".equals(cleanedFranchiseId.get(spo.getFranchise()))) {
                    String name = spo.getName();
                    name = name.substring(name.lastIndexOf("_")+1);
                    spo.setName(name);
                }
                if (cheapestDiesel == null) {
                    cheapestDiesel = spo.getPrices().get("dizel");
                    cheapestPetrol = spo.getPrices().get("95");
                } else {
                    Double currentDiesel = spo.getPrices().get("dizel");
                    Double currentPetrol = spo.getPrices().get("95");
                    if (currentPetrol != null)
                        cheapestPetrol = Double.compare(cheapestPetrol, currentPetrol) > 0 ? currentPetrol : cheapestPetrol;
                    if (currentDiesel != null)
                        cheapestDiesel = Double.compare(cheapestPetrol, currentDiesel) > 0 ? currentDiesel : cheapestDiesel;
                }
            }

            if (cheapestPetrol == null)
                petrolMinPrice.setText(getString(R.string.nullValue).toUpperCase());
            else
                petrolMinPrice.setText(String.format(locale, "%4.3f€", cheapestPetrol));
            if (cheapestDiesel == null)
                dieselMinPrice.setText(getString(R.string.nullValue).toUpperCase());
            else
                dieselMinPrice.setText(String.format(locale, "%4.3f€", cheapestDiesel));

            Log.d(TAG, "onResponse: old number of stations " + data.size());
            data.addAll(tmp);
            Log.d(TAG, "onResponse: new number of stations " + data.size());

            showMore();

            loadingAlert.setVisibility(View.INVISIBLE);
            openResults.setEnabled(true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}