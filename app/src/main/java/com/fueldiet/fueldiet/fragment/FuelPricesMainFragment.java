package com.fueldiet.fueldiet.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
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
import com.fueldiet.fueldiet.dialog.LoadingDialog;
import com.fueldiet.fueldiet.object.StationPricesObject;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import static android.app.Activity.RESULT_OK;

public class FuelPricesMainFragment extends Fragment implements Response.Listener<JSONObject>, Response.ErrorListener, EasyPermissions.PermissionCallbacks {

    private static final String TAG = "StationsPricesFragment";
    private static final String ALL_STATIONS_API = "https://goriva.si/api/v1/franchise/?format=json";
    private static final String SEARCH_RESULTS_API = "https://goriva.si/api/v1/search/?format=json&franchise=%s&name=%s&o=%s&position=%s&radius=%s&&timestamp=%s";
    private static final String MIN_MAX_API = "https://goriva.si/api/v1/search/?format=json&franchise=&name=&o=&position=Ljubljana&radius=";

    private Locale locale;
    private View view;
    SharedPreferences pref;
    LoadingDialog loadingDialog;
    List<StationPricesObject> data;
    List<StationPricesObject> dataMM;

    StationPricesObject minD, maxD, min95, max95;

    ExtendedFloatingActionButton searchButton;

    String nextLink = "null";
    String nextLinkMM = "null";
    HashMap<String, Integer> cleanedFranchiseNames;
    HashMap<Integer, String> cleanedFranchiseId;
    ArrayList<String> availableRadius;

    private RequestQueue mQueue;
    private boolean newSearch = false;
    private boolean hidden = false;

    MaterialButton currentLocation;
    TextInputLayout cityName;
    AutoCompleteTextView franchises;
    SeekBar radius;
    TextView seekValue;
    LinearProgressIndicator minIndi, maxIndi;

    private static final int REQUEST_FINE_LOCATION = 2;
    private static final int REQUEST_LOCATION = 1324;
    private static final String[] PERMISSIONS_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private FusedLocationProviderClient client;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    public static FuelPricesMainFragment newInstance() {
        return new FuelPricesMainFragment();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Configuration configuration = getResources().getConfiguration();
        locale = configuration.getLocales().get(0);

        mQueue = VolleySingleton.getInstance(getContext()).getRequestQueue();
        view = inflater.inflate(R.layout.fragment_main_fuel_prices, container, false);

        loadingDialog = new LoadingDialog(getActivity());
        searchButton = view.findViewById(R.id.stations_open_search_button);

        currentLocation = view.findViewById(R.id.search_prices_location);
        cityName = view.findViewById(R.id.search_prices_city_input);
        radius = view.findViewById(R.id.search_prices_radius_seekbar);
        franchises = view.findViewById(R.id.search_prices_franchises_value);
        seekValue = view.findViewById(R.id.search_radius_value);

        minIndi = view.findViewById(R.id.stations_min_indi);
        maxIndi = view.findViewById(R.id.stations_max_indi);

        dataMM = new ArrayList<>();

        availableRadius = createRadiusData();
        getAvailableStations();

        fillData();

        searchButton.setOnClickListener(v -> {
            if (validateFields()) {
                getStationPrices(franchises.getText().toString(), null, null, cityName.getEditText().getText().toString(), availableRadius.get(radius.getProgress()));
            }
        });

        data = new ArrayList<>();
        return view;
    }

    private void getMinMaxPrices() {
        hidden = true;
        minIndi.show();
        maxIndi.show();
        JsonObjectRequest request;
        request = new JsonObjectRequest(Request.Method.GET, MIN_MAX_API, null, this, this);
        request.setRetryPolicy(new DefaultRetryPolicy(3000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mQueue.add(request);
    }

    private void loadingDialogVisibility(boolean shown) {
        if (shown) {
            if (!loadingDialog.isDisplayed()) {
                Log.d(TAG, "loadingDialogVisibility: yes");
                loadingDialog.showDialog();
            }
        } else {
            if (loadingDialog.isDisplayed()) {
                Log.d(TAG, "loadingDialogVisibility: no");
                loadingDialog.hideDialog();
            }
        }
    }

    private void displayMinMax() {
        SortRunnable runnable = new SortRunnable();
        runnable.run();
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
                checkGPSPermissions();
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
        if (n.contains(", ")) {
            n = n.replace(", ", "%2C");
            n = n.replaceAll(",", ".");
        }
        Log.d(TAG, "getStationPrices: before: f " + f + " name " + n + " source " + s + " position " + p + " radius " + r);

        String url = String.format(SEARCH_RESULTS_API, f, n, s, p, r, Calendar.getInstance().getTimeInMillis());
        Log.d(TAG, "getStationPrices: url: "+ url);
        loadingDialogVisibility(true);
        // newSearch = true;
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

    private void loadMoreResults() {
        Log.d(TAG, "loadMoreResults: trying to load additional stations");
        if (!nextLink.equals("null") && !hidden) {
            Log.d(TAG, "loadMoreResults: link exists");
            newSearch = false;
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, nextLink, null, this, this);
            mQueue.add(request);
        } else if (!nextLinkMM.equals("null") && hidden) {
            Log.d(TAG, "loadMoreResults: linkMM exists");
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, nextLinkMM, null, this, this);
            mQueue.add(request);
        } else if (hidden) {
            hidden = false;
            displayMinMax();
        } else {
            loadingDialogVisibility(false);
            newSearch = true;
            Log.d(TAG, "loadMoreResults: no additional stations available");
            Log.d(TAG, "loadMoreResults: opening list");
            Intent intent = new Intent(getActivity(), FuelPricesDetailsActivity.class);
            intent.putExtra("mode", 0);
            intent.putExtra("data", (Serializable) data);
            intent.putExtra("names", cleanedFranchiseId);
            startActivity(intent);
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
        loadingDialogVisibility(true);
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
            getMinMaxPrices();
            loadingDialogVisibility(false);
        }, this);
        request.setRetryPolicy(new DefaultRetryPolicy(3000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mQueue.add(request);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        loadingDialogVisibility(false);
        if (error instanceof TimeoutError) {
            Log.e(TAG, "onErrorResponse: timeout");
            Snackbar.make(requireView(), R.string.err_timeout_goriva_si, Snackbar.LENGTH_LONG).setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE).show();
        }
        Log.e(TAG, "onErrorResponse: ", error);
    }

    @Override
    public void onResponse(JSONObject response) {
        try {
            if (hidden) {
                nextLinkMM = response.getString("next");
            } else {
                nextLink = response.getString("next");
            }
        } catch (JSONException e) {
            Log.e(TAG, "onResponse: ", e);
        }
        try {
            JSONArray dataJSON = response.getJSONArray("results");
            Log.d(TAG, "onResponse: got new " + dataJSON.length() + " stations");
            if (dataJSON.length() == 0 && newSearch && !hidden) {
                Snackbar.make(requireView(), getString(R.string.no_data_chart), Snackbar.LENGTH_LONG).setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE).show();
            }
            if (newSearch && !hidden) {
                data.clear();
                newSearch = false;
            }
            ArrayList<StationPricesObject> tmp = new Gson().fromJson(dataJSON.toString(), new TypeToken<List<StationPricesObject>>() {}.getType());

            for (StationPricesObject spo : tmp) {
                if ("Avanti".equals(cleanedFranchiseId.get(spo.getFranchise()))) {
                    String name = spo.getName();
                    name = name.substring(name.lastIndexOf("_") + 1);
                    spo.setName(name);
                }
            }

            if (hidden) {
                dataMM.addAll(tmp);
            } else {
                Log.d(TAG, "onResponse: old number of stations " + data.size());
                data.addAll(tmp);
                Log.d(TAG, "onResponse: new number of stations " + data.size());
            }
            loadMoreResults();

        } catch (JSONException e) {
            loadingDialogVisibility(false);
            e.printStackTrace();
        }
    }

    class SortRunnable implements Runnable {
        private static final String TAG = "SortRunnable";

        SortRunnable() {}

        @Override
        public void run() {
            Log.d(TAG, "run: started sorting - 95");
            dataMM.sort((o1, o2) -> {
                if (o1.getPrices().get("95") == null) {
                    return 1;
                }
                if (o2.getPrices().get("95") == null) {
                    return -1;
                }
                return Double.compare(o1.getPrices().get("95"), o2.getPrices().get("95"));
            });

            min95 = dataMM.get(0);
            Collections.reverse(dataMM);
            for (StationPricesObject spo : dataMM) {
                if (spo.getPrices().get("95") != null) {
                    max95 = spo;
                    break;
                }
            }

            dataMM.sort(((o1, o2) -> {
                if (o1.getPrices().get("dizel") == null) {
                    return 1;
                }
                if (o2.getPrices().get("dizel") == null) {
                    return -1;
                }
                return Double.compare(o1.getPrices().get("dizel"), o2.getPrices().get("dizel"));
            }));

            minD = dataMM.get(0);
            Collections.reverse(dataMM);
            for (StationPricesObject spo : dataMM) {
                if (spo.getPrices().get("dizel") != null) {
                    maxD = spo;
                    break;
                }
            }
            minIndi.hide();
            ((TextView)view.findViewById(R.id.station_prices_d_cheapest_name)).setText(cleanedFranchiseId.get(min95.getFranchise()));
            ((TextView)view.findViewById(R.id.stations_prices_95_cheapest_price)).setText(String.format(locale, "%4.3f€", min95.getPrices().get("95")));
            ((TextView)view.findViewById(R.id.stations_prices_95_cheapest_name)).setText(cleanedFranchiseId.get(minD.getFranchise()));
            ((TextView)view.findViewById(R.id.stations_prices_diesel_cheapest_price)).setText(String.format(locale, "%4.3f€", minD.getPrices().get("dizel")));
            maxIndi.hide();
            ((TextView)view.findViewById(R.id.station_prices_d_exp_name)).setText(cleanedFranchiseId.get(max95.getFranchise()));
            ((TextView)view.findViewById(R.id.stations_prices_95_exp_price)).setText(String.format(locale, "%4.3f€", max95.getPrices().get("95")));
            ((TextView)view.findViewById(R.id.stations_prices_95_exp_name)).setText(cleanedFranchiseId.get(maxD.getFranchise()));
            ((TextView)view.findViewById(R.id.stations_prices_diesel_exp_price)).setText(String.format(locale, "%4.3f€", maxD.getPrices().get("dizel")));


            Log.d(TAG, "run: finished sorting");
        }
    }

    /*
    GPS stuff
     */

    private void checkGPSPermissions() {
        if (EasyPermissions.hasPermissions(requireContext(), PERMISSIONS_LOCATION))
            //start async get location
            getLocationService();
        else
            EasyPermissions.requestPermissions(this, getString(R.string.why_location),
                    REQUEST_FINE_LOCATION, PERMISSIONS_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        getLocationService();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "onPermissionsDenied: " + requestCode + ":" + perms.size());
        /*if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }*/
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            if (EasyPermissions.hasPermissions(requireContext(), PERMISSIONS_LOCATION))
                getLocationService();
        } else if (requestCode == LocationRequest.PRIORITY_HIGH_ACCURACY) {
            switch (resultCode) {
                case RESULT_OK:
                    // All required changes were successfully made
                    Log.d(TAG, "onActivityResult: GPS Enabled by user");
                    getOneLocationUpdate();
                    break;
                case Activity.RESULT_CANCELED:
                    // The user was asked to change settings, but chose not to
                    Log.d(TAG, "onActivityResult: User rejected GPS request");
                    break;
                default:
                    break;
            }
        } else if (requestCode == REQUEST_LOCATION) {
            if (resultCode == RESULT_OK) {
                //change to new location
                cityName.getEditText().setText(String.format(locale, "%f, %f", data.getDoubleExtra("lat", 0), data.getDoubleExtra("lon", 0)));
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void getLocationService() {
        client = LocationServices.getFusedLocationProviderClient(requireContext());

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);


        /* Prompt to turn on gps */

        SettingsClient settingsClient = LocationServices.getSettingsClient(requireContext());
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(requireActivity(), new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
                Log.d(TAG, "onSuccess: location is already enabled");
                getOneLocationUpdate();
            }
        });

        task.addOnFailureListener(requireActivity(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    Log.d(TAG, "onFailure: location is not (yet) enabled");
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(getActivity(), LocationRequest.PRIORITY_HIGH_ACCURACY);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        cityName.getEditText().setText(String.format(locale, "%f, %f", location.getLatitude(), location.getLongitude()));
                        if (client != null) {
                            client.removeLocationUpdates(locationCallback);
                        }
                    }
                }
            }
        };
    }

    @SuppressLint("MissingPermission")
    private void getOneLocationUpdate() {
        //when code comes to here permissions are already granted with EasyPermission
        client.requestLocationUpdates(locationRequest, locationCallback, null);
    }
}

