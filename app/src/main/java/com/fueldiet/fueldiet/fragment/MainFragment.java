package com.fueldiet.fueldiet.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SpinnerAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.activity.AddNewCostActivity;
import com.fueldiet.fueldiet.activity.AddNewDriveActivity;
import com.fueldiet.fueldiet.activity.AddNewReminderActivity;
import com.fueldiet.fueldiet.activity.AddNewVehicleActivity;
import com.fueldiet.fueldiet.activity.MainActivity;
import com.fueldiet.fueldiet.activity.VehicleDetailsActivity;
import com.fueldiet.fueldiet.adapter.MainAdapter;
import com.fueldiet.fueldiet.adapter.VehicleSelectAdapter;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.fueldiet.fueldiet.object.VehicleObject;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {
    private static final String TAG = "MainFragment";

    public MainFragment() {
        isFABOpen = false;
    }

    public enum TitleType {
        Fuel, Cost, Last_Entries, No_Entry
    }

    public static MainFragment newInstance(long vehicle_id) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putLong("vehicleID", vehicle_id);
        fragment.setArguments(args);
        return fragment;
    }


    private List<Object> data;
    private MainAdapter mAdapter;
    private FuelDietDBHelper dbHelper;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;

    private FloatingActionButton fab, fabFuel, fabCost, fabRem, fabNew, fabNote;
    private View fabBg, fabBgTop;
    private boolean isFABOpen;
    private long vehicleID;

    public void Update() {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        if (Build.VERSION.SDK_INT >= 26) {
            transaction.setReorderingAllowed(false);
        }
        transaction.detach(this).attach(this).commit();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            vehicleID = getArguments().getLong("vehicleID");
        }
        dbHelper = FuelDietDBHelper.getInstance(getContext());
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        /* recyclerviewer data */
        data = new ArrayList<>();

        fillData();
        createRecyclerViewer(view);

        /* Add new */
        fab = view.findViewById(R.id.main_fragment_add_new);
        fabFuel = view.findViewById(R.id.main_fragment_add_new_fuel);
        fabCost = view.findViewById(R.id.main_fragment_add_new_cost);
        fabRem = view.findViewById(R.id.main_fragment_add_new_rem);
        fabNew = view.findViewById(R.id.main_fragment_add_new_vehicle);
        fabNote = view.findViewById(R.id.main_fragment_add_save_note);
        fabBg = view.findViewById(R.id.main_fragment_fab_bg);
        fabBgTop = ((MainActivity)requireActivity()).fabBgTop;

        fabFuel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFABOpen)
                    closeFABMenu();

                if (vehicleID == -1) {
                    startActivity(new Intent(getActivity(), AddNewVehicleActivity.class));
                } else {
                    Intent intent = new Intent(getActivity(), AddNewDriveActivity.class);
                    intent.putExtra("vehicle_id", vehicleID);
                    startActivity(intent);
                }
            }
        });

        fabCost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFABOpen)
                    closeFABMenu();

                if (vehicleID == -1) {
                    startActivity(new Intent(getActivity(), AddNewVehicleActivity.class));
                } else {
                    Intent intent = new Intent(getActivity(), AddNewCostActivity.class);
                    intent.putExtra("vehicle_id", vehicleID);
                    startActivity(intent);
                }
            }
        });

        fabRem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFABOpen)
                    closeFABMenu();

                if (vehicleID == -1) {
                    startActivity(new Intent(getActivity(), AddNewVehicleActivity.class));
                } else {
                    Intent intent = new Intent(getActivity(), AddNewReminderActivity.class);
                    intent.putExtra("vehicle_id", vehicleID);
                    startActivity(intent);
                }
            }
        });

        fabNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFABOpen)
                    closeFABMenu();

                startActivity(new Intent(getActivity(), AddNewVehicleActivity.class));
            }
        });

        fabNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFABOpen)
                    closeFABMenu();

                addNoteForFuel();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(getActivity(), AddNewVehicleActivity.class));
                if(!isFABOpen){
                    showFABMenu();
                }else{
                    closeFABMenu();
                }
            }
        });

        fabBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeFABMenu();
            }
        });
        fabBgTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeFABMenu();
            }
        });

        return view;
    }

    private void showFABMenu(){
        isFABOpen=true;
        fabFuel.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        fabCost.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
        fabRem.animate().translationY(-getResources().getDimension(R.dimen.standard_155));
        fabNew.animate().translationY(-getResources().getDimension(R.dimen.standard_205));
        fabNote.animate().translationY(-getResources().getDimension(R.dimen.standard_255));
        fab.animate().rotationBy(45);
        fabBg.setVisibility(View.VISIBLE);
        fabBgTop.setVisibility(View.VISIBLE);
    }

    private void closeFABMenu(){
        isFABOpen=false;
        fabFuel.animate().translationY(0);
        fabCost.animate().translationY(0);
        fabRem.animate().translationY(0);
        fabNew.animate().translationY(0);
        fabNote.animate().translationY(0);
        fab.animate().rotationBy(-45);
        fabBg.setVisibility(View.INVISIBLE);
        fabBgTop.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        fillData();
        //mAdapter.notifyDataSetChanged();
        if (mAdapter.getItemCount() > 1) {
            mAdapter.notifyItemChanged(2);
            mAdapter.notifyItemChanged(4);
            mAdapter.notifyItemChanged(6);
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }


    private void addNoteForFuel() {
        MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(requireContext());
        final EditText edittext = new EditText(getContext());
        alert.setTitle(R.string.note_for_next_fuel);
        //alert.setMessage("Save a note for next fuel log");

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String oldNote = pref.getString("saved_note", "");

        alert.setView(edittext);
        edittext.setText(oldNote);

        alert.setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String newNote = edittext.getText().toString();
                newNote = newNote.trim();

                if (!newNote.equals("")) {
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("saved_note", newNote);
                    editor.apply();
                }
                dialog.dismiss();
            }
        });

        alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });

        alert.show();
    }

    /**
     * Builds and set recycler view
     */
    public void createRecyclerViewer(View view) {
        mRecyclerView = view.findViewById(R.id.first_main_data);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mAdapter = new MainAdapter(requireContext(), data, dbHelper);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        //mAdapter.setOnItemClickListener(position -> openItem(position));
        mAdapter.setOnItemClickListener(new MainAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                openItem(position);
            }

            @Override
            public void onItemSelected(long vehicleId) {
                if (vehicleId != vehicleID) {
                    vehicleID = vehicleId;

                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(requireContext());
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putLong("last_vehicle", vehicleID);
                    editor.apply();

                    updateData();
                }
            }
        });
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && fab.getVisibility() == View.VISIBLE) {
                    fabCost.hide();
                    fabFuel.hide();
                    fabRem.hide();
                    fabNew.hide();
                    fabNote.hide();
                    fab.hide();
                } else if (dy < 0 && fab.getVisibility() != View.VISIBLE) {
                    fab.show();
                    fabCost.show();
                    fabFuel.show();
                    fabRem.show();
                    fabNew.show();
                    fabNote.show();
                }
            }
        });
        mLayoutManager.smoothScrollToPosition(mRecyclerView, null, 0);
    }

    /**
     * Fill with new data for recycler view
     */
    private void fillData() {
        data.clear();
        ArrayList<VehicleObject> vehicles = new ArrayList<>();
        List<VehicleObject> vehicleObjects = dbHelper.getAllVehicles();

        if (vehicleObjects == null || vehicleObjects.size() == 0) {
            vehicles.add(new VehicleObject("No vehicle", "added!", -1));
            SpinnerAdapter spinnerAdapter = new VehicleSelectAdapter(getContext(), vehicles);
            data.add(spinnerAdapter);
            data.add(TitleType.No_Entry);
        } else {
            vehicles.addAll(vehicleObjects);
            SpinnerAdapter spinnerAdapter = new VehicleSelectAdapter(getContext(), vehicles);
            data.add(spinnerAdapter);
            data.add(TitleType.Fuel);
            data.add(vehicleID);
            data.add(TitleType.Cost);
            data.add(vehicleID);
            data.add(TitleType.Last_Entries);
            data.add(vehicleID);
        }
    }

    private void updateData() {
        if (data.size() == 7) {
            data.remove(6);
            data.remove(4);
            data.remove(2);
            data.add(2, vehicleID);
            data.add(4, vehicleID);
            data.add(6, vehicleID);
            mAdapter.notifyItemChanged(2);
            mAdapter.notifyItemChanged(4);
            mAdapter.notifyItemChanged(6);
        }
        mLayoutManager.smoothScrollToPosition(mRecyclerView, null, 0);
    }

    /**
     * Opens VehicleDetailsActivity
     * @param pos clicked item
     */
    public void openItem(int pos) {
        if (pos == 0 || vehicleID == -1)
            startActivity(new Intent(getActivity(), AddNewVehicleActivity.class));
        else if (pos == 2 || pos == 1) {
            Intent intent = new Intent(getActivity(), VehicleDetailsActivity.class);
            intent.putExtra("vehicle_id", vehicleID);
            startActivity(intent);
        } else {
            Intent intent = new Intent(getActivity(), VehicleDetailsActivity.class);
            intent.putExtra("vehicle_id", vehicleID);
            intent.putExtra("frag", 1);
            startActivity(intent);
        }
    }
}
