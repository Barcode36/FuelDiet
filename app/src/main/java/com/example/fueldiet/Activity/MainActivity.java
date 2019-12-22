package com.example.fueldiet.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.fueldiet.Object.VehicleObject;
import com.example.fueldiet.db.FuelDietDBHelper;
import com.example.fueldiet.Object.ManufacturerObject;
import com.example.fueldiet.R;
import com.example.fueldiet.Utils;
import com.example.fueldiet.Adapter.VehicleAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;


public class MainActivity extends BaseActivity {

    public static final String LOGO_URL = "https://raw.githubusercontent.com/filippofilip95/car-logos-dataset/master/images/%s";

    private RecyclerView mRecyclerView;
    private List<VehicleObject> data;
    private VehicleAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    FuelDietDBHelper dbHelper;
    public static Map<String, ManufacturerObject> manufacturers;
    private long vehicleToDelete;
    private int position;
    FloatingActionButton fab;
    private long backPressedTime;
    private Toast backToast;


    /**
     * Double press back to exit
     */
    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            backToast.cancel();
            super.onBackPressed();
            return;
        } else {
            backToast = Toast.makeText(getBaseContext(), getString(R.string.double_tap_to_exit), Toast.LENGTH_LONG);
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /* Fill Map with Manufacturers Objects from json */
        String response = loadJSONFromAsset();
        List<ManufacturerObject> tmp = new Gson().fromJson(response, new TypeToken<List<ManufacturerObject>>() {}.getType());
        manufacturers = tmp.stream().collect(Collectors.toMap(ManufacturerObject::getName, manufacturerObject -> manufacturerObject));


        SharedPreferences pref = getSharedPreferences("prefs", MODE_PRIVATE);
        dbHelper = new FuelDietDBHelper(this);
        /* recyclerviewer data */
        data = new ArrayList<>();

        buildRecyclerView();

        /* Add new vehicle */
        fab = findViewById(R.id.main_activity_add_new);
        fab.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, AddNewVehicleActivity.class));
        });

        Log.i("LOCALE", getApplicationContext().getResources().getConfiguration().getLocales().get(0).getLanguage());
        Log.i("SHARED-PREFS", pref.getBoolean("showTutorial", true)+"");

        /* Show tutorial or no (sharePrefs) */
        boolean showTutorial = pref.getBoolean("showTutorial", true);
        boolean tmpTutorial = pref.getBoolean("tmpTutorial", true);
        if (showTutorial && tmpTutorial)
            showWelcomeScreen();
        else if (!tmpTutorial) {
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("tmpTutorial", true);
            editor.apply();
        }

    }

    /**
     * Show tutorial in new activity
     */
    private void showWelcomeScreen() {
        /*
        Intent intent = new Intent(MainActivity.this, TutorialActivity.class);
        intent.putExtra("first", true);
        startActivity(intent);
        */

        /*custom alert dialog */
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.custom_alert_layout);
        final AlertDialog alertdialog = builder.create();
        alertdialog.show();

        ImageButton dismiss = alertdialog.findViewById(R.id.custom_alert_close_btn);
        Button yes = alertdialog.findViewById(R.id.custom_alert_confirm_btn);
        Button cancel = alertdialog.findViewById(R.id.custom_alert_cancel_btn);
        alertdialog.getWindow().setGravity(Gravity.BOTTOM);

        dismiss.setOnClickListener(v -> alertdialog.dismiss());
        cancel.setOnClickListener(v -> {
            SharedPreferences pref = this.getSharedPreferences("prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("showTutorial", false);
            editor.putBoolean("tmpTutorial", false);
            editor.apply();
            alertdialog.dismiss();
        });
        yes.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TutorialActivity.class);
            intent.putExtra("first", true);
            alertdialog.dismiss();
            startActivity(intent);
        });
    }

    /**
     * Read JSON
     * @return json as String
     */
    private String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getResources().openRawResource(R.raw.carlogos);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        fillData();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fillData();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                //open setting screen
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            case R.id.reset_db:
                //reset db and prefs
                FuelDietDBHelper dbh = new FuelDietDBHelper(getBaseContext());
                Toast.makeText(this, "Reset is done.", Toast.LENGTH_SHORT).show();
                dbh.resetDb();
                SharedPreferences prefs = getPreferences(MODE_PRIVATE);
                prefs.edit().clear().apply();
                SharedPreferences pref = getSharedPreferences("prefs", MODE_PRIVATE);
                pref.edit().clear().apply();
                fillData();
                mAdapter.notifyDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Fill with new data for recycler view
     */
    private void fillData() {
        data.clear();
        data.addAll(dbHelper.getAllVehicles());
    }

    /**
     * Fill with new data for recycler view
     * @param vehicleID which vehicle to exclude
     */
    private void fillData(long vehicleID) {
        data.clear();
        data.addAll(dbHelper.getAllVehiclesExcept(vehicleID));
    }

    /**
     * Builds and set recycler view
     */
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
    }

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
    }

    /**
     * Opens EditVehicleActivity
     * @param id vehicle id
     */
    public void editItem(long id) {
        Intent intent = new Intent(MainActivity.this, EditVehicleActivity.class);
        intent.putExtra("vehicle_id", id);
        startActivity(intent);
    }

    /**
     * Opens VehicleDetailsActivity
     * @param id vehicle id
     */
    public void openItem(long id) {
        Intent intent = new Intent(MainActivity.this, VehicleDetailsActivity.class);
        intent.putExtra("vehicle_id", id);
        startActivity(intent);
    }
}
