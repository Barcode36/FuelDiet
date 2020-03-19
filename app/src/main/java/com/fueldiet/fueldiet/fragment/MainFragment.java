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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fueldiet.fueldiet.activity.AddNewCostActivity;
import com.fueldiet.fueldiet.activity.AddNewDriveActivity;
import com.fueldiet.fueldiet.activity.AddNewReminderActivity;
import com.fueldiet.fueldiet.activity.AddNewVehicleActivity;
import com.fueldiet.fueldiet.activity.MainActivity;
import com.fueldiet.fueldiet.activity.VehicleDetailsActivity;
import com.fueldiet.fueldiet.adapter.MainAdapter;
import com.fueldiet.fueldiet.adapter.VehicleSelectAdapter;
import com.fueldiet.fueldiet.object.VehicleObject;
import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {

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
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
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
        dbHelper = new FuelDietDBHelper(getContext());
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
        fabBgTop = ((MainActivity)getActivity()).fabBgTop;

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
        mAdapter.notifyDataSetChanged();
        mAdapter.notifyItemChanged(2);
    }


    private void addNoteForFuel() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        final EditText edittext = new EditText(getContext());
        alert.setTitle(R.string.note_for_next_fuel);
        //alert.setMessage("Save a note for next fuel log");

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String oldNote = pref.getString("saved_note", "");

        alert.setView(edittext);
        edittext.setText(oldNote);

        alert.setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String newNote = edittext.getText().toString();
                newNote.trim();

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
        mAdapter = new MainAdapter(getContext(), data, dbHelper);

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

                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
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
                    fab.hide();
                    fabCost.hide();
                    fabFuel.hide();
                    fabRem.hide();
                    fabNew.hide();
                } else if (dy < 0 && fab.getVisibility() != View.VISIBLE) {
                    fabCost.show();
                    fabFuel.show();
                    fabRem.show();
                    fabNew.show();
                    fab.show();
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

    /*
    public void buildRecyclerView() {
        mRecyclerView = findViewById(R.id.vehicleList);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new VehicleAdapter(this, data);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && fab.getVisibility() == View.VISIBLE) {
                    fab.hide();
                } else if (dy < 0 && fab.getVisibility() != View.VISIBLE) {
                    fab.show();
                }
            }
        });


        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                //direction == 4 - delete
                //direction == 8 - edit
                if (direction == 4) {
                    // Yes No dialog
                    position = viewHolder.getAdapterPosition();
                    vehicleToDelete = (long)viewHolder.itemView.getTag();
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage(getString(R.string.are_you_sure))
                            .setPositiveButton(getString(R.string.yes), dialogClickListener)
                            .setNegativeButton(getString(R.string.no), dialogClickListener).show();
                } else if (direction == 8) {
                    position = viewHolder.getAdapterPosition();
                    editItem((long)viewHolder.itemView.getTag());
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                // blue and red background after slide
                Bitmap icon;
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View cardView = viewHolder.itemView;
                    float height = (float) cardView.getBottom() - (float) cardView.getTop();
                    float width = height / 3;
                    Paint p = new Paint();

                    if(dX > 0){
                        p.setColor(getColor(R.color.blue));
                         RectF background = new RectF((float) cardView.getLeft(), (float) cardView.getTop(), cardView.getLeft() + dX,(float) cardView.getBottom());
                        c.drawRect(background,p);
                        icon = Utils.getBitmapFromVectorDrawable(getBaseContext(), R.drawable.ic_edit_24px);
                        RectF icon_dest = new RectF((float) cardView.getLeft() + width ,(float) cardView.getTop() + width,(float) cardView.getLeft()+ 2*width,(float)cardView.getBottom() - width);
                        c.drawBitmap(icon,null,icon_dest,p);
                    } else {
                        p.setColor(getColor(R.color.red));
                        RectF background = new RectF((float) cardView.getRight() + dX, (float) cardView.getTop(),(float) cardView.getRight(), (float) cardView.getBottom());
                        c.drawRect(background,p);
                        icon = Utils.getBitmapFromVectorDrawable(getBaseContext(), R.drawable.ic_delete_24px);
                        RectF icon_dest = new RectF((float) cardView.getRight() - 2*width ,(float) cardView.getTop() + width,(float) cardView.getRight() - width,(float)cardView.getBottom() - width);
                        c.drawBitmap(icon,null,icon_dest,p);
                    }

                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(mRecyclerView);

        mAdapter.setOnItemClickListener(element_id -> openItem(element_id));
    }*/

    /*
    DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
        //result from yes/no whether to delete
        switch (which){
            case DialogInterface.BUTTON_POSITIVE:
                removeItem(vehicleToDelete);
                break;

            case DialogInterface.BUTTON_NEGATIVE:
                mAdapter.notifyItemChanged(position);
                Toast.makeText(MainActivity.this, getString(R.string.canceled), Toast.LENGTH_SHORT).show();
                break;
        }
    };

    private void removeItem(final long id) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.clayout), getString(R.string.vehicle_deleted), Snackbar.LENGTH_LONG);
        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onShown(Snackbar sb) {
                //show snackbar but only hide element
                super.onShown(sb);
                fillData(id);
                mAdapter.notifyItemRemoved(position);
            }

            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                //if undo was not pressed, delete vehicle, all data and img
                super.onDismissed(transientBottomBar, event);
                if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                    try {
                        VehicleObject vo = dbHelper.getVehicle(id);
                        if (vo.getCustomImg() != null) {
                            File storageDIR = getApplicationContext().getDir("Images", MODE_PRIVATE);
                            File img = new File(storageDIR, vo.getCustomImg());
                            img.delete();
                        }
                    } catch (Exception e) {
                        Log.e("MainActivity - DeleteCustomImg - "+e.getClass().getSimpleName(), "Custom image was not found");
                    }finally {
                        boolean exist = false;
                        VehicleObject main = dbHelper.getVehicle(id);
                        //check more than one vehicle of same make
                        for (VehicleObject vo : data) {
                            if (vo.getMake().equals(main.getMake()) && vo.getId() != main.getId()) {
                                exist = true;
                                break;
                            }
                        }
                        //if not:
                        if (!exist) {
                            try {
                                File storageDIR = getApplicationContext().getDir("Images",MODE_PRIVATE);
                                ManufacturerObject mo = MainActivity.manufacturers.get(main.getMake());
                                File img = new File(storageDIR, mo.getFileNameMod());
                                img.delete();
                            } catch (Exception e) {
                                Log.e("MainActivity - DeleteImg - "+e.getClass().getSimpleName(), "Vehicle img was not found, maybe custom make?");
                            }

                        }
                    }
                    dbHelper.deleteVehicle(id);
                }
            }
        }).setAction("UNDO", v -> {
            //reset vehicle
            fillData();
            mAdapter.notifyItemInserted(position);
            Toast.makeText(MainActivity.this, getString(R.string.undo_pressed), Toast.LENGTH_SHORT).show();
        });
        snackbar.show();
    }*/
}
