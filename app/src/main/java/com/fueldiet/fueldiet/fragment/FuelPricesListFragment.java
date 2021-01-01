package com.fueldiet.fueldiet.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.adapter.FuelPricesListAdapter;
import com.fueldiet.fueldiet.object.StationPricesObject;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class FuelPricesListFragment extends Fragment {

    private static final String TAG = "FuelPricesListFragment";

    private ArrayList<StationPricesObject> data;
    private HashMap<Integer, String> names;

    private int selectedSort = 0;
    private int selectedMode = 0;
    RecyclerView recyclerView;
    FuelPricesListAdapter adapter;
    ProgressBar loadingBar;

    public FuelPricesListFragment(ArrayList<StationPricesObject> data, HashMap<Integer, String> names) {
        this.data = data;
        this.names = names;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fuel_prices_list, container, false);

        loadingBar = view.findViewById(R.id.sorting_loading_bar);
        recyclerView = view.findViewById(R.id.stations_prices_list_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireActivity());
        adapter = new FuelPricesListAdapter(requireActivity(), data, names);
        adapter.setOnItemClickListener(new FuelPricesListAdapter.OnItemClickListener() {
            @Override
            public void showOnMap(int position) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.view_pager, new FuelPricesMapFragment(data, names, position)).commit();
            }

            @Override
            public void navigateTo(int position) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + data.get(position).getLat() + "," + data.get(position).getLng());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(linearLayoutManager);
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.fuel_prices_list_sort_by) {
            Log.d(TAG, "onOptionsItemSelected: by");
            String[] singleItems = new String[]{"Distance", "Franchise", "Petrol price", "Diesel price"};

            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Select sort by")
                    .setNeutralButton("Cancel", null)
                    .setPositiveButton(requireContext().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sortData();
                            Log.d(TAG, "onClick: selected mode " + which);
                        }
                    })
                    .setSingleChoiceItems(singleItems, selectedSort, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            selectedSort = which;
                            Log.d(TAG, "onClick: selected new choice " + which);
                        }
                    })
                    .show();
        } else if (item.getItemId() == R.id.fuel_prices_list_sort_alfa) {
            Log.d(TAG, "onOptionsItemSelected: alfa");
            String[] singleItems = new String[]{"Ascending", "Descending"};

            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Select sort mode")
                    .setNeutralButton("Cancel", null)
                    .setPositiveButton(requireContext().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sortData();
                            Log.d(TAG, "onClick: selected mode " + which);
                        }
                    })
                    .setSingleChoiceItems(singleItems, selectedMode, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            selectedMode = which;
                            Log.d(TAG, "onClick: selected new mode " + which);
                        }
                    })
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void sortData() {
        SortRunnable runnable = new SortRunnable();
        runnable.run();
    }

    private void showLoadingAnimation() {
        loadingBar.setVisibility(View.VISIBLE);
    }

    private void hideLoadingAnimation() {
        loadingBar.setVisibility(View.GONE);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.fuel_prices_sort_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    class SortRunnable implements Runnable {
        private static final String TAG = "SortRunnable";

        SortRunnable() {}

        @Override
        public void run() {
            Log.d(TAG, "run: started sorting");
            showLoadingAnimation();
            data.sort((o1, o2) -> {
                int value;
                switch (selectedSort) {
                    case 0:
                        value = Double.compare(o1.getDistance(), o2.getDistance());
                        return value;
                    case 1:
                        value = names.get(o1.getFranchise()).compareTo(names.get(o2.getFranchise()));
                        return value;
                    case 2:
                        value = Double.compare(o1.getPrices().get("95"), o2.getPrices().get("95"));
                        return value;
                    default:
                        value = Double.compare(o1.getPrices().get("dizel"), o2.getPrices().get("dizel"));
                        return value;
                }
            });
            Log.d(TAG, "run: reversing data");
            if (selectedMode == 1) {
                Collections.reverse(data);
            }
            Log.d(TAG, "run: finished sorting");
            adapter.notifyDataSetChanged();
            hideLoadingAnimation();
        }
    }
}
