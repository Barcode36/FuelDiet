package com.example.fueldiet;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link VehicleConsumptionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link VehicleConsumptionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VehicleConsumptionFragment extends Fragment {

    // TODO: Rename and change types of parameters
    private long id_vehicle;
    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;
    ConsumptionAdapter mAdapter;
    FuelDietDBHelper dbHelper;
    View view;

    private OnFragmentInteractionListener mListener;

    public VehicleConsumptionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param id Parameter 1.
     * @return A new instance of fragment VehicleConsumptionFragment.
     */
    // TODO: Rename and change types and number of parameters
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


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_vehicle_consumption, container, false);
        mRecyclerView = view.findViewById(R.id.display_cons);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager= new LinearLayoutManager(getActivity());
        mAdapter = new ConsumptionAdapter(getActivity(), dbHelper.getAllDrives(id_vehicle));

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);

        FloatingActionButton fab = view.findViewById(R.id.add_new_drive);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), AddNewDriveActivity.class);
            intent.putExtra("vehicle_id", id_vehicle);
            startActivity(intent);
        });

        return view;
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
