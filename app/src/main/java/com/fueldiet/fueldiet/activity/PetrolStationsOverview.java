package com.fueldiet.fueldiet.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.adapter.ConsumptionAdapter;
import com.fueldiet.fueldiet.adapter.PetrolStationAdapter;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.fueldiet.fueldiet.dialog.AddPetrolStationDialog;
import com.fueldiet.fueldiet.dialog.EditPetrolStationDialog;
import com.fueldiet.fueldiet.object.DriveObject;
import com.fueldiet.fueldiet.object.PetrolStationObject;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PetrolStationsOverview extends BaseActivity implements AddPetrolStationDialog.AddPetrolStationDialogListener, EditPetrolStationDialog.EditPetrolStationDialogListener {
    private static final String TAG = "PetrolStationsOverview";
    RecyclerView recyclerView;
    PetrolStationAdapter adapter;
    FuelDietDBHelper dbHelper;
    List<PetrolStationObject> data;
    FloatingActionButton fab;

    @Override
    public void onResume() {
        super.onResume();
        fillData();
        adapter.notifyDataSetChanged();
    }

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
                //startActivity(new Intent(PetrolStationsOverview.this, AddPetrolStationActivity.class));
                AddPetrolStationDialog dialog = new AddPetrolStationDialog();
                dialog.show(getSupportFragmentManager(), "AddPetrolStation");
            }
        });

        data = new ArrayList<>();
        fillData();

        adapter = new PetrolStationAdapter(this, data);
        adapter.setOnItemClickListener(new PetrolStationAdapter.OnItemClickListener() {
            @Override
            public void onItemEdit(int position, long id) {
                Bundle args = new Bundle();
                args.putLong("id", id);
                EditPetrolStationDialog dialog = new EditPetrolStationDialog();
                dialog.setArguments(args);
                dialog.show(getSupportFragmentManager(), "EditPetrolStation");
            }

            @Override
            public void onItemDelete(int position, long id) {
                PetrolStationObject deleted = data.get(position);
                File storageDIR = getApplicationContext().getDir("Images", MODE_PRIVATE);
                File img = new File(storageDIR, deleted.getFileName());
                img.delete();
                dbHelper.removePetrolStation(id);
                fillData();
                adapter.notifyItemRemoved(position);
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                if (pref.getString("default_petrol_station", "Other").equals(deleted.getName())) {
                    pref.edit().remove("default_petrol_station").apply();
                    Toast.makeText(getApplicationContext(), "Default petrol station reverted to 'Other'", Toast.LENGTH_LONG).show();
                }

                //check each fuel log for this petrol station, and change it to other
                List<DriveObject> drives = dbHelper.getReallyAllDrives();
                for (DriveObject drive : drives) {
                    if (drive.getPetrolStation().equals(deleted.getName())) {
                        drive.setPetrolStation("Other");
                        dbHelper.updateDriveODO(drive);
                    }
                }
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

    @Override
    public void getNewStation(PetrolStationObject stationObject) {
        dbHelper.addPetrolStation(stationObject);
        List<PetrolStationObject> old = new ArrayList<>(data);
        fillData();
        int changed = data.size();
        for (int i = 0; i < old.size(); i++) {
            if (!old.get(i).getName().equals(data.get(i).getName())) {
                changed = i;
                break;
            }
        }
        old.clear();
        adapter.notifyItemInserted(changed);
    }

    @Override
    public void getEditStation(PetrolStationObject stationObject) {
        dbHelper.updatePetrolStation(stationObject);
        List<PetrolStationObject> old = new ArrayList<>(data);
        fillData();
        Integer changed = null;
        for (int i = 0; i < old.size(); i++) {
            if (!old.get(i).getName().equals(data.get(i).getName())) {
                changed = i;
                break;
            }
        }
        old.clear();
        if (changed == null)
            adapter.notifyDataSetChanged();
        else
            adapter.notifyItemChanged(changed);
    }
}
