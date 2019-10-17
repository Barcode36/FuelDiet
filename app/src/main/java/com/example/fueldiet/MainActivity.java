package com.example.fueldiet;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    public static final String LOGO_URL = "https://raw.githubusercontent.com/filippofilip95/car-logos-dataset/master/images/%s.png";

    private RecyclerView mRecyclerView;
    private VehicleAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    FuelDietDBHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setTheme(R.style.DarkTheme);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dbHelper = new FuelDietDBHelper(this);

        buildRecyclerView();


        FloatingActionButton fab = findViewById(R.id.add_new);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AddNewVehicleActivity.class));
                /*
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                Toast.makeText(this, "TODO: Settings", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            case R.id.reset_db:
                FuelDietDBHelper dbh = new FuelDietDBHelper(getBaseContext());
                Toast.makeText(this, "Reset is done.", Toast.LENGTH_SHORT).show();
                dbh.resetDb();
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


        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                //direction == 4 - delete
                //direction == 8 - edit
                if (direction == 4) {
                    removeItem((long)viewHolder.itemView.getTag(), viewHolder);
                } else if (direction == 8) {
                    editItem((long)viewHolder.itemView.getTag());
                }
            }
        }).attachToRecyclerView(mRecyclerView);



        mAdapter.setOnItemClickListener(new VehicleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(long element_id) {
                openItem(element_id);
            }
        });
    }

    private void removeItem(final long id, final RecyclerView.ViewHolder viewHolder) {
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
        }).setAction("UNDO", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.swapCursor(dbHelper.getAllVehicles());
                Toast.makeText(MainActivity.this, "Undo pressed", Toast.LENGTH_SHORT).show();
            }
        });
        snackbar.show();
        //mAdapter.swapCursor(dbHelper.getAllVehicles());
    }

    public void editItem(long id) {
        Intent intent = new Intent(MainActivity.this, EditVehicleActivity.class);
        intent.putExtra("vehicle_id", (long)id);
        startActivity(intent);
        //mAdapter.notifyDataSetChanged();
    }

    public void openItem(long id) {
        Toast.makeText(this, "Position: " + id, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, VehicleDetailsActivity.class);
        intent.putExtra("vehicle_id", (long)id);
        startActivity(intent);
        //mAdapter.notifyItemChanged(position);
    }
}
