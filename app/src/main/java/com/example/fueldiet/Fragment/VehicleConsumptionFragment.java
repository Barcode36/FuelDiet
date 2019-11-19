package com.example.fueldiet.Fragment;

import android.app.ActionBar;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fueldiet.Activity.AddNewDriveActivity;
import com.example.fueldiet.Activity.EditDriveActivity;
import com.example.fueldiet.Activity.VehicleDetailsActivity;
import com.example.fueldiet.Adapter.ConsumptionAdapter;
import com.example.fueldiet.Object.DriveObject;
import com.example.fueldiet.R;
import com.example.fueldiet.db.FuelDietDBHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;



/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link VehicleConsumptionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link VehicleConsumptionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VehicleConsumptionFragment extends Fragment {

    private long id_vehicle;
    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;
    ConsumptionAdapter mAdapter;
    FuelDietDBHelper dbHelper;
    List<DriveObject> data;
    View view;
    FloatingActionButton fab;

    private int pos;
    private long cardId;

    @Override
    public void onResume() {
        super.onResume();
        fillData();
        mAdapter.notifyDataSetChanged();
    }

    private OnFragmentInteractionListener mListener;

    public VehicleConsumptionFragment() {
        // Required empty public constructor
    }

    public static VehicleConsumptionFragment newInstance(long id) {
        VehicleConsumptionFragment fragment = new VehicleConsumptionFragment();
        Bundle args = new Bundle();
        args.putLong("id", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            id_vehicle = getArguments().getLong("id");
        }
        dbHelper = new FuelDietDBHelper(getContext());
        data = new ArrayList<>();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_vehicle_consumption, container, false);
        mRecyclerView = view.findViewById(R.id.display_cons);
        //mRecyclerView.setHasFixedSize(true);
        fillData();
        mLayoutManager= new LinearLayoutManager(getActivity());
        mAdapter = new ConsumptionAdapter(getActivity(), data);
        mAdapter.setOnItemClickListener(new ConsumptionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, long driveID) {
                optionsForCard(position, driveID);
            }
        });

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Log.e("DY", dy+"");
                if (dy > 0 && fab.getVisibility() == View.VISIBLE)
                    fab.hide();
                else if (dy < 0 && fab.getVisibility() != View.VISIBLE)
                    fab.show();

            }
        });

        fab = view.findViewById(R.id.add_new_drive);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), AddNewDriveActivity.class);
            intent.putExtra("vehicle_id", id_vehicle);
            startActivity(intent);
        });

        return view;
    }

    private void fillData() {
        data.clear();
        data.addAll(dbHelper.getAllDrives(id_vehicle));
    }

    private void optionsForCard(int position, long cardID) {
        pos = position;
        cardId = cardID;
        if (position == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(getString(R.string.what_to_do))
                    .setNeutralButton(getString(R.string.edit), dialogClickListener)
                    .setNegativeButton(getString(R.string.delete), dialogClickListener)
                    .setPositiveButton(getString(R.string.cancel), dialogClickListener).show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(getString(R.string.what_to_do))
                    .setNeutralButton(getString(R.string.edit), dialogClickListener)
                    .setPositiveButton(getString(R.string.cancel), dialogClickListener).show();
        }
    }

    DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
        switch (which){
            case DialogInterface.BUTTON_NEGATIVE:
                removeLastDrive();
                break;
            case DialogInterface.BUTTON_POSITIVE:
                Toast.makeText(getContext(), getString(R.string.canceled), Toast.LENGTH_SHORT).show();
                break;
            case DialogInterface.BUTTON_NEUTRAL:
                Intent intent = new Intent(getContext(), EditDriveActivity.class);
                intent.putExtra("vehicle_id", id_vehicle);
                intent.putExtra("drive_id", cardId);
                startActivity(intent);
                break;
        }
    };

    private void removeLastDrive() {
        DriveObject deleted = dbHelper.getLastDrive(id_vehicle);
        dbHelper.removeLastDrive(id_vehicle);
        //Toast.makeText(getContext(), "Entry deleted", Toast.LENGTH_SHORT).show();
        fillData();
        mAdapter.notifyItemRemoved(pos);

        Snackbar snackbar = Snackbar.make(getView(), getString(R.string.object_deleted), Snackbar.LENGTH_LONG);
        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onShown(Snackbar sb) { }

            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) { }
        }).setAction("UNDO", v -> {
            dbHelper.addDrive(deleted);
            fillData();
            mAdapter.notifyItemInserted(pos);
            mRecyclerView.scrollToPosition(0);
            Toast.makeText(getContext(), getString(R.string.undo_pressed), Toast.LENGTH_SHORT).show();
        });
        snackbar.show();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

/*
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
    
*/

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
