package com.fueldiet.fueldiet.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.adapter.ConsumptionAdapter;
import com.fueldiet.fueldiet.adapter.PetrolStationAdapter;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.fueldiet.fueldiet.object.DriveObject;
import com.fueldiet.fueldiet.object.PetrolStationObject;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PetrolStationsOverview extends BaseActivity {
    private static final String TAG = "PetrolStationsOverview";
    RecyclerView recyclerView;
    PetrolStationAdapter adapter;
    FuelDietDBHelper dbHelper;
    List<PetrolStationObject> data;
    View view;
    FloatingActionButton fab;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_petrol_stations_overview);

        dbHelper = new FuelDietDBHelper(this);
        recyclerView = findViewById(R.id.petrol_stations_recyclerview);
        fab = findViewById(R.id.add_new_petrol_station);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
                Toast.makeText(getApplicationContext(), "TODO", Toast.LENGTH_SHORT).show();
            }
        });

        data = new ArrayList<>();
        fillData();

        adapter = new PetrolStationAdapter(this, data);
        adapter.setOnItemClickListener(new PetrolStationAdapter.OnItemClickListener() {
            @Override
            public void onItemEdit(int position) {

            }

            @Override
            public void onItemDelete(int position) {

            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && fab.getVisibility() == View.VISIBLE)
                    fab.hide();
                else if (dy < 0 && fab.getVisibility() != View.VISIBLE)
                    fab.show();

            }
        });
    }

    private void fillData() {
        data.clear();
        data.addAll(dbHelper.getAllPetrolStations());

        data.sort(new Comparator<PetrolStationObject>() {
            @Override
            public int compare(PetrolStationObject o1, PetrolStationObject o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
    }
}
