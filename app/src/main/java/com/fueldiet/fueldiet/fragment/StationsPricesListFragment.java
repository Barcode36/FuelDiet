package com.fueldiet.fueldiet.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.adapter.StationsPricesListAdapter;
import com.fueldiet.fueldiet.object.StationPriceObject;

import java.util.ArrayList;
import java.util.HashMap;

public class StationsPricesListFragment extends Fragment {

    private ArrayList<StationPriceObject> data;
    private HashMap<Integer, String> names;

    public StationsPricesListFragment(ArrayList<StationPriceObject> data, HashMap<Integer, String> names) {
        this.data = data;
        this.names = names;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stations_prices_list, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.stations_prices_list_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireActivity());
        StationsPricesListAdapter adapter = new StationsPricesListAdapter(requireActivity(), data, names);
        adapter.setOnItemClickListener(new StationsPricesListAdapter.OnItemClickListener() {
            @Override
            public void showOnMap(int position) {
                //TODO
            }

            @Override
            public void navigateTo(int position) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + data.get(position).getLat() + "," + data.get(position).getLon());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(linearLayoutManager);
        return view;
    }
}
