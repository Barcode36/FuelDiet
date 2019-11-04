package com.example.fueldiet.Activity;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.bumptech.glide.signature.ObjectKey;
import com.example.fueldiet.BaseActivity;
import com.example.fueldiet.db.FuelDietDBHelper;
import com.example.fueldiet.Object.ManufacturerObject;
import com.example.fueldiet.R;
import com.example.fueldiet.Utils;
import com.example.fueldiet.Adapter.VehicleAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;


public class MainActivity extends BaseActivity {

    public static final String LOGO_URL = "https://raw.githubusercontent.com/filippofilip95/car-logos-dataset/master/images/%s";

    private RecyclerView mRecyclerView;
    private VehicleAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    FuelDietDBHelper dbHelper;
    public static Map<String, ManufacturerObject> manufacturers;
    private long vehicleToDelete;
    FloatingActionButton fab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setTheme(R.style.DarkTheme);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String response = loadJSONFromAsset();
        List<ManufacturerObject> tmp = new Gson().fromJson(response, new TypeToken<List<ManufacturerObject>>() {}.getType());
        manufacturers = tmp.stream().collect(Collectors.toMap(ManufacturerObject::getName, manufacturerObject -> manufacturerObject));

        int px = Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 65,getResources().getDisplayMetrics()));


        File storageDIR = getApplicationContext().getDir("Images",MODE_PRIVATE);
        if (storageDIR.list().length  < 10) {
            for (ManufacturerObject mo : tmp) {
                Glide.with(getApplicationContext())
                        .asBitmap()
                        .load(mo.getUrl())
                        .fitCenter()
                        .override(px)
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                saveImage(mo.getFileName(), resource);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                            }
                        });
            }
        }

        dbHelper = new FuelDietDBHelper(this);

        buildRecyclerView();

        fab = findViewById(R.id.main_activity_add_new);
        fab.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, AddNewVehicleActivity.class));
        });

        SharedPreferences pref = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean firstOpen = pref.getBoolean("firstOpen", true);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        preferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);

        Log.i("LOCALE", getApplicationContext().getResources().getConfiguration().getLocales().get(0).getLanguage());
        if (firstOpen)
            showWelcomeScreen();
    }

    private void showWelcomeScreen() {
        LayoutInflater inflater = LayoutInflater.from(this);
        new AlertDialog.Builder(this)
                .setView(inflater.inflate(R.layout.welcome_dialog, null))
                .setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();

        SharedPreferences pref = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("firstOpen", false);
        editor.apply();
    }

    private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("selected_unit")){
                Log.i("SHARED-PREFERENCES", "selected_unit changed to: " + sharedPreferences.getString(key, null));
            } else if (key.equals("enable_language")) {
                if (!sharedPreferences.getBoolean(key, false)) {
                    Log.i("SHARED-PREFERENCES", "enable_language is FALSE");
                    String langCode =  getApplicationContext().getResources().getConfiguration().getLocales().get(0).getLanguage();
                    Log.i("SHARED-PREFERENCES", "new locale will be " + langCode);
                    if ("sl".equals(langCode)) {
                        Log.i("SHARED-PREFERENCES", "new locale is setting to be sl");
                        sharedPreferences.edit().putString("language_select", "slovene").apply();
                    } else {
                        Log.i("SHARED-PREFERENCES", "new locale is setting to be en");
                        sharedPreferences.edit().putString("language_select", "english").apply();
                    }
                    Log.i("SHARED-PREFERENCES", "new locale is set to " + sharedPreferences.getString("language_select", null));
                }
                Log.i("SHARED-PREFERENCES", "enable_language changed to: " + sharedPreferences.getBoolean(key, false));
            } else if (key.equals("language_select")) {
                Log.i("SHARED-PREFERENCES", "language_select changed to: " + sharedPreferences.getString(key, null));
                if (sharedPreferences.getBoolean("enable_language", false)) {
                    String localSelected = sharedPreferences.getString("language_select", "english");
                    Locale locale;
                    if ("slovene".equals(localSelected)) {
                        locale = new Locale("sl", "SI");
                    } else {
                        locale = new Locale("en", "GB");
                    }
                    Resources resources = getResources();
                    Configuration configuration = resources.getConfiguration();
                    DisplayMetrics displayMetrics = resources.getDisplayMetrics();
                    configuration.setLocale(locale);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                        getApplicationContext().createConfigurationContext(configuration);
                    } else {
                        resources.updateConfiguration(configuration,displayMetrics);
                    }
                }
            }
        }
    };

    private void saveImage(String fileName, Bitmap image) {
        String savedImagePath = null;
        File storageDIR = getApplicationContext().getDir("Images",MODE_PRIVATE);
        boolean success = true;
        if (!storageDIR.exists()) {
            success = storageDIR.mkdirs();
        }
        if (success) {
            File imageFile = new File(storageDIR, fileName);
            try {
                OutputStream fOut = new FileOutputStream(imageFile);
                image.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                fOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

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
        mAdapter.swapCursor(dbHelper.getAllVehicles());
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
                Toast.makeText(this, "TODO: Settings", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            case R.id.reset_db:
                FuelDietDBHelper dbh = new FuelDietDBHelper(getBaseContext());
                Toast.makeText(this, "Reset is done.", Toast.LENGTH_SHORT).show();
                dbh.resetDb();
                SharedPreferences prefs = getPreferences(MODE_PRIVATE);
                prefs.edit().clear().apply();
                mAdapter.swapCursor(dbHelper.getAllVehicles());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void buildRecyclerView() {
        mRecyclerView = findViewById(R.id.vehicleList);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new VehicleAdapter(this, dbHelper.getAllVehicles());

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
                    vehicleToDelete = (long)viewHolder.itemView.getTag();
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
                } else if (direction == 8) {
                    editItem((long)viewHolder.itemView.getTag());
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                Bitmap icon;
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View cardView = viewHolder.itemView;
                    float height = (float) cardView.getBottom() - (float) cardView.getTop();
                    float width = height / 3;
                    Paint p = new Paint();

                    if(dX > 0){
                        p.setColor(Color.BLUE);
                        RectF background = new RectF((float) cardView.getLeft(), (float) cardView.getTop(), cardView.getLeft() + dX,(float) cardView.getBottom());
                        c.drawRect(background,p);
                        //icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_delete_24px);
                        icon = Utils.getBitmapFromVectorDrawable(getBaseContext(), R.drawable.ic_edit_24px);
                        RectF icon_dest = new RectF((float) cardView.getLeft() + width ,(float) cardView.getTop() + width,(float) cardView.getLeft()+ 2*width,(float)cardView.getBottom() - width);
                        c.drawBitmap(icon,null,icon_dest,p);
                    } else {
                        p.setColor(Color.RED);
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
        switch (which){
            case DialogInterface.BUTTON_POSITIVE:
                removeItem(vehicleToDelete);
                break;

            case DialogInterface.BUTTON_NEGATIVE:
                mAdapter.swapCursor(dbHelper.getAllVehicles());
                Toast.makeText(MainActivity.this, "Canceled", Toast.LENGTH_SHORT).show();
                break;
        }
    };

    private void removeItem(final long id) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.clayout), "Vehicle deleted!", Snackbar.LENGTH_LONG);
        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onShown(Snackbar sb) {
                super.onShown(sb);
                mAdapter.swapCursor(dbHelper.getAllVehiclesExcept(id));
            }

            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);
                if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                    dbHelper.deleteVehicle(id);
                }
            }
        }).setAction("UNDO", v -> {
            mAdapter.swapCursor(dbHelper.getAllVehicles());
            Toast.makeText(MainActivity.this, "Undo pressed", Toast.LENGTH_SHORT).show();
        });
        snackbar.show();
    }

    public void editItem(long id) {
        Intent intent = new Intent(MainActivity.this, EditVehicleActivity.class);
        intent.putExtra("vehicle_id", id);
        startActivity(intent);
        //mAdapter.notifyDataSetChanged();
    }

    public void openItem(long id) {
        Toast.makeText(this, "Position: " + id, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, VehicleDetailsActivity.class);
        intent.putExtra("vehicle_id", id);
        startActivity(intent);
        //mAdapter.notifyItemChanged(position);
    }
}
