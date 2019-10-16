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
    private SQLiteDatabase mDatabase;
    SwipeController swipeController = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setTheme(R.style.DarkTheme);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FuelDietDBHelper dbHelper = new FuelDietDBHelper(this);
        mDatabase = dbHelper.getWritableDatabase();

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
                mAdapter.swapCursor(getAllItems());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void buildRecyclerView() {
        mRecyclerView = findViewById(R.id.vehicleList);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new VehicleAdapter(this, getAllItems());

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        swipeController = new SwipeController(new SwipeControllerActions() {
            @Override
            public void onLeftClicked(long el_id) {
                editItem(el_id);
            }

            @Override
            public void onRightClicked(long el_id) {
                removeItem(el_id);
            }
        });


        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(mRecyclerView);

        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });

        /*

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                removeItem((long) viewHolder.itemView.getTag());
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                GradientDrawable shape =  new GradientDrawable();
                final float scale = recyclerView.getContext().getResources().getDisplayMetrics().density;
                int pixels = (int) (11 * scale + 0.5f);
                shape.setCornerRadius(pixels);
                shape.setColor(Color.RED);
                shape.setBounds(pixels, viewHolder.itemView.getTop(),   Math.round(viewHolder.itemView.getLeft() + dX + 20), viewHolder.itemView.getBottom());
                shape.draw(c);
            }

        }).attachToRecyclerView(mRecyclerView);

         */



        mAdapter.setOnItemClickListener(new VehicleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(long element_id) {
                openItem(element_id);
            }
        });
    }

    private void removeItem(long id) {
        mDatabase.delete(FuelDietContract.VehicleEntry.TABLE_NAME,
                FuelDietContract.VehicleEntry._ID + "=" + id, null);
        Snackbar.make(/*mRecyclerView.getRootView()*/findViewById(R.id.clayout), "Vehicle deleted!", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
        //TODO: add undo button
        mAdapter.swapCursor(getAllItems());
    }

    public void editItem(long id) {
        //Toast.makeText(this, "Edit clicked", Toast.LENGTH_SHORT).show();
        //TODO: open new activity
        Intent intent = new Intent(MainActivity.this, EditVehicleActivity.class);
        intent.putExtra("vehicle_id", (long)id);
        startActivity(intent);
        //mAdapter.swapCursor(getAllItems());
    }

    public void openItem(long id) {
        Toast.makeText(this, "Position: " + id, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, VehicleDetailsActivity.class);
        intent.putExtra("vehicle_id", (long)id);
        startActivity(intent);
        //mAdapter.notifyItemChanged(position);
    }

    private Cursor getAllItems() {
        return mDatabase.query(
                FuelDietContract.VehicleEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                FuelDietContract.VehicleEntry.COLUMN_MAKE + " ASC"
        );
    }
}
