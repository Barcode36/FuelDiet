package com.fueldiet.fueldiet.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.activity.AddNewVehicleActivity;
import com.fueldiet.fueldiet.activity.EditVehicleActivity;
import com.fueldiet.fueldiet.activity.MainActivity;
import com.fueldiet.fueldiet.adapter.VehicleManagementAdapter;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.fueldiet.fueldiet.dialog.DeletingDialog;
import com.fueldiet.fueldiet.object.ManufacturerObject;
import com.fueldiet.fueldiet.object.VehicleObject;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class VehicleManagementFragment extends Fragment {

    private static final String TAG = "VehicleManagementFragment";

    RecyclerView recyclerView;
    VehicleManagementAdapter adapter;
    FuelDietDBHelper dbHelper;
    List<VehicleObject> data;
    FloatingActionButton addNewButton;
    MaterialCardView noVehiclesWarningCard;
    Locale locale;
    SharedPreferences pref;

    private static final int EDIT_VEHICLE = 1;
    private static final int CREATE_VEHICLE = 2;

    private Long vehicleToDelete;

    public static VehicleManagementFragment newInstance() {
        return new VehicleManagementFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: started...");
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_vehicle_management, container, false);

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext());
        Configuration configuration = getResources().getConfiguration();
        locale = configuration.getLocales().get(0);

        initVariables(view);
        getData();
        initAdapter();
        finishSetUp();

        Log.d(TAG, "onCreate: finished");
        return view;
    }

    private void initVariables(View view) {
        Log.d(TAG, "initVariables: started...");
        dbHelper = FuelDietDBHelper.getInstance(requireContext());
        data = new ArrayList<>();
        recyclerView = view.findViewById(R.id.vehicle_management_recycler_view);
        addNewButton = view.findViewById(R.id.vehicle_management_add_new);
        noVehiclesWarningCard = view.findViewById(R.id.vehicle_management_no_vehicles);
        Log.d(TAG, "initVariables: finished");
    }

    private void getData() {
        Log.d(TAG, "getData: started...");
        data.clear();
        data.addAll(dbHelper.getAllVehicles());
        if (data.isEmpty()) {
            noVehiclesWarningCard.setVisibility(View.VISIBLE);
        } else {
            noVehiclesWarningCard.setVisibility(View.INVISIBLE);
        }
        Log.d(TAG, "getData: finished");
    }

    private void initAdapter() {
        Log.d(TAG, "initAdapter: started...");
        adapter = new VehicleManagementAdapter(requireContext(), locale, data);
        adapter.setOnItemClickListener(new VehicleManagementAdapter.OnItemClickListener() {
            @Override
            public void edit(long id) {
                editVehicle(id);
            }

            @Override
            public void delete(long id) {
                startToRemoveVehicle(id);
            }
        });
        Log.d(TAG, "initAdapter: finished");
    }

    private void finishSetUp() {
        Log.d(TAG, "finishSetUp: started...");
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        addNewButton.setOnClickListener(view -> {
            Intent intent = new Intent(requireContext(), AddNewVehicleActivity.class);
            startActivityForResult(intent, CREATE_VEHICLE);
        });
        Log.d(TAG, "finishSetUp: finished");
    }

    private void editVehicle(long vehicleId) {
        Log.d(TAG, "editVehicle: started...");
        Intent intent = new Intent(requireContext(), EditVehicleActivity.class);
        intent.putExtra("vehicle_id", vehicleId);
        startActivityForResult(intent, EDIT_VEHICLE);
    }

    private void startToRemoveVehicle(long vehicleId) {
        Log.d(TAG, "removeVehicle: started...");
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setMessage(getString(R.string.are_you_sure_to_delete))
                .setPositiveButton(getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getString(R.string.no), dialogClickListener)
                .show();
        vehicleToDelete = vehicleId;
        Log.d(TAG, "startToRemoveVehicle: finished");
    }

    private void finishRemovingVehicle() {
        Log.d(TAG, "finishRemovingVehicle: started...");
        DeletingDialog deletingDialog = new DeletingDialog(requireActivity());
        deletingDialog.showDialog();

        // delete manufacturer logo
        try {
            VehicleObject vo = dbHelper.getVehicle(vehicleToDelete);
            if (vo.getCustomImg() != null) {
                File storageDIR = requireContext().getDir("Images", MODE_PRIVATE);
                File img = new File(storageDIR, vo.getCustomImg());
                Files.delete(img.toPath());
            }
        } catch (Exception e) {
            Log.e(TAG, "finishRemovingVehicle: Custom image was not found", e.fillInStackTrace());
        } finally {
            List<VehicleObject> allOtherVehicles = dbHelper.getAllVehiclesExcept(vehicleToDelete);
            boolean exist = false;
            VehicleObject main = dbHelper.getVehicle(vehicleToDelete);
            //check more than one vehicle of same make
            if (allOtherVehicles != null && !allOtherVehicles.isEmpty()) {
                for (VehicleObject vo : allOtherVehicles) {
                    if (vo.getMake().equals(main.getMake()) && vo.getId() != main.getId()) {
                        exist = true;
                        break;
                    }
                }
            }
            //if not:
            if (!exist) {
                try {
                    File storageDIR = requireContext().getDir("Images", MODE_PRIVATE);
                    ManufacturerObject mo = MainActivity.manufacturers.get(main.getMake());
                    File img = new File(storageDIR, mo.getFileNameMod());
                    Files.delete(img.toPath());
                } catch (Exception e) {
                    Log.e(TAG, "finishRemovingVehicle: Vehicle img was not found, maybe custom make?", e.fillInStackTrace());
                }

            }
        }

        // delete vehicle from db
        dbHelper.deleteVehicle(vehicleToDelete);

        // remove vehicle from default if it was set as default
        String selected = pref.getString("selected_vehicle", null);
        if (selected != null && Long.parseLong(selected) == vehicleToDelete) {
            SharedPreferences.Editor editor = pref.edit();
            editor.remove("selected_vehicle").apply();
        }
        vehicleToDelete = null;

        // added 2s sleep for more dramatic effect
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            deletingDialog.setSuccessful();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                deletingDialog.hideDialog();
                getData();
                adapter.notifyDataSetChanged();
            }, 1000);
        }, 1500);



        Log.d(TAG, "finishRemovingVehicle: finished");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_VEHICLE && resultCode == RESULT_OK) {
            Log.d(TAG, "onActivityResult: EDIT_VEHICLE");
            getData();
            adapter.notifyDataSetChanged();
        } else if (requestCode == CREATE_VEHICLE) {
            Log.d(TAG, "onActivityResult: CREATE_VEHICLE");
            getData();
            adapter.notifyDataSetChanged();
        }
    }

    DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
        //result from yes/no whether to delete
        if (which == DialogInterface.BUTTON_POSITIVE) {
            finishRemovingVehicle();
        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
            Toast.makeText(requireContext(), getString(R.string.canceled), Toast.LENGTH_SHORT).show();
            vehicleToDelete = null;
        }
    };
}