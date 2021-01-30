package com.fueldiet.fueldiet.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.adapter.PetrolStationAdapter;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.fueldiet.fueldiet.dialog.AddPetrolStationDialog;
import com.fueldiet.fueldiet.dialog.EditPetrolStationDialog;
import com.fueldiet.fueldiet.dialog.LoadingDialog;
import com.fueldiet.fueldiet.object.DriveObject;
import com.fueldiet.fueldiet.object.PetrolStationObject;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class PetrolStationManagementFragment extends Fragment implements AddPetrolStationDialog.AddPetrolStationDialogListener, EditPetrolStationDialog.EditPetrolStationDialogListener {
    private static final String TAG = "PetrolStationManagementFragment";

    RecyclerView recyclerView;
    PetrolStationAdapter adapter;
    FuelDietDBHelper dbHelper;
    List<PetrolStationObject> data;
    FloatingActionButton fab;
    ProgressBar loading;

    @Override
    public void onResume() {
        super.onResume();
        getData();
        adapter.notifyDataSetChanged();
    }

    public static PetrolStationManagementFragment newInstance() {
        return new PetrolStationManagementFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: started...");
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_petrol_stations_management, container, false);

        initVariables(view);
        addClickListeners();
        getData();
        initAdapter();
        finishSetUp();


        Log.d(TAG, "onCreateView: finished");
        return view;
    }

    private void initVariables(View view) {
        Log.d(TAG, "initVariables: started...");
        dbHelper = FuelDietDBHelper.getInstance(requireContext());

        recyclerView = view.findViewById(R.id.petrol_stations_recyclerview);
        fab = view.findViewById(R.id.add_new_petrol_station);
        loading = view.findViewById(R.id.petrol_station_progress_bar);
        data = new ArrayList<>();
        Log.d(TAG, "initVariables: finished");
    }

    private void addClickListeners() {
        Log.d(TAG, "addClickListeners: started...");
        fab.setOnClickListener(v -> {
            AddPetrolStationDialog dialog = new AddPetrolStationDialog();
            dialog.setNewDialogListener(this);
            dialog.show(getParentFragmentManager(), "AddPetrolStation");
        });
        Log.d(TAG, "addClickListeners: finished");
    }

    private void openEditDialog(long id) {
        Log.d(TAG, "openEditDialog: started...");
        Bundle args = new Bundle();
        args.putLong("id", id);
        EditPetrolStationDialog dialog = new EditPetrolStationDialog();
        dialog.setNewDialogListener(this);
        dialog.setArguments(args);
        dialog.show(getParentFragmentManager(), "EditPetrolStation");
        Log.d(TAG, "openEditDialog: finished");
    }

    private void deleteStation(int position, long id) {
        Log.d(TAG, "deleteStation: started...");
        PetrolStationObject deleted = data.get(position);
        File storageDIR = requireContext().getDir("Images", MODE_PRIVATE);
        File img = new File(storageDIR, deleted.getFileName());
        try {
            Files.delete(img.toPath());
        } catch (IOException e) {
            Log.e(TAG, "onItemDelete: Image failed to delete", e);
        }
        dbHelper.removePetrolStation(id);
        getData();
        adapter.notifyItemRemoved(position);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(requireContext());
        if (pref.getString("default_petrol_station", "Other").equals(deleted.getName())) {
            pref.edit().remove("default_petrol_station").apply();
            Toast.makeText(requireContext(), "Default petrol station reverted to 'Other'", Toast.LENGTH_LONG).show();
        }

        //check each fuel log for this petrol station, and change it to other
        List<DriveObject> drives = dbHelper.getReallyAllDrives();
        for (DriveObject drive : drives) {
            if (drive.getPetrolStation().equals(deleted.getName())) {
                drive.setPetrolStation("Other");
                dbHelper.updateDriveODO(drive);
            }
        }
        Log.d(TAG, "deleteStation: finished");
    }

    private void initAdapter() {
        Log.d(TAG, "initAdapter: started...");
        adapter = new PetrolStationAdapter(requireContext(), data);
        adapter.setOnItemClickListener(new PetrolStationAdapter.OnItemClickListener() {
            @Override
            public void onItemEdit(int position, long id) {
                openEditDialog(id);
            }

            @Override
            public void onItemDelete(int position, long id) {
                deleteStation(position, id);
            }
        });
        Log.d(TAG, "initAdapter: finished");
    }

    private void finishSetUp() {
        Log.d(TAG, "finishSetUp: started...");
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));

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
        Log.d(TAG, "finishSetUp: finished");
    }

    private void getData() {
        Log.d(TAG, "getData: started...");
        data.clear();
        data.addAll(dbHelper.getAllPetrolStations());
        data.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));
        Log.d(TAG, "getData: finished");
    }

    @Override
    public void getNewStation(PetrolStationObject stationObject) {
        Log.d(TAG, "getNewStation: starting...");
        LoadingDialog loadingDialog = new LoadingDialog(requireActivity());
        loadingDialog.showDialog();
        dbHelper.addPetrolStation(stationObject);
        List<PetrolStationObject> old = new ArrayList<>(data);
        getData();
        int changed = data.size();
        for (int i = 0; i < old.size(); i++) {
            if (!old.get(i).getName().equals(data.get(i).getName())) {
                changed = i;
                break;
            }
        }
        old.clear();
        int finalChanged = changed;
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            loadingDialog.hideDialog();
            adapter.notifyItemInserted(finalChanged);
        }, 1500);
        Log.d(TAG, "getNewStation: finished");
    }

    @Override
    public void getEditStation(PetrolStationObject stationObject) {
        Log.d(TAG, "getEditStation: started...");
        LoadingDialog loadingDialog = new LoadingDialog(requireActivity());
        loadingDialog.showDialog();
        dbHelper.updatePetrolStation(stationObject);
        List<PetrolStationObject> old = new ArrayList<>(data);
        getData();
        Integer changed = null;
        for (int i = 0; i < old.size(); i++) {
            if (!old.get(i).getName().equals(data.get(i).getName())) {
                changed = i;
                break;
            }
        }
        old.clear();
        Integer finalChanged = changed;
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            loadingDialog.hideDialog();
            if (finalChanged == null) {
                adapter.notifyDataSetChanged();
            } else {
                adapter.notifyItemChanged(finalChanged);
            }
        }, 1500);
    }
}

