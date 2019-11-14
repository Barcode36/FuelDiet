package com.example.fueldiet.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.fueldiet.Activity.MainActivity;
import com.example.fueldiet.Activity.TutorialActivity;
import com.example.fueldiet.R;

public class TutorialFragment extends Fragment {

    View view;
    private int position;
    CheckBox checkBox;
    Button button;
    ImageView img;

    public TutorialFragment() {
        // Required empty public constructor
    }

    public static TutorialFragment newInstance(int pos) {
        TutorialFragment fragment = new TutorialFragment();
        Bundle args = new Bundle();
        args.putInt("pos", pos);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (getArguments() != null) {
            position = getArguments().getInt("pos");
        }
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_tutorial, container, false);

        checkBox = view.findViewById(R.id.tutorial_accept_cond);
        button = view.findViewById(R.id.tutorial_finish);
        img = view.findViewById(R.id.tutorial_image_view);

        if (position != 7) {
            checkBox.setVisibility(View.GONE);
            button.setVisibility(View.GONE);
        } else {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkBox.isChecked())
                        startActivity(new Intent(getContext(), MainActivity.class));
                }
            });
        }

        setImg();

        return view;
    }

    private void setImg() {
        switch (position) {
            case 0:
                Log.i("Tutorial", "Loading for pos: " + position);
                img.setImageResource(R.drawable.main);
                break;
            case 1:
                Log.i("Tutorial", "Loading for pos: " + position);
                img.setImageResource(R.drawable.main_swipe);
                break;
            case 2:
                Log.i("Tutorial", "Loading for pos: " + position);
                img.setImageResource(R.drawable.add_veh);
                break;
            case 3:
                Log.i("Tutorial", "Loading for pos: " + position);
                img.setImageResource(R.drawable.cons);
                break;
            case 4:
                Log.i("Tutorial", "Loading for pos: " + position);
                img.setImageResource(R.drawable.con_long);
                break;
            case 5:
                Log.i("Tutorial", "Loading for pos: " + position);
                img.setImageResource(R.drawable.other_cost);
                break;
            case 6:
                Log.i("Tutorial", "Loading for pos: " + position);
                img.setImageResource(R.drawable.modification);
                break;
            case 7:
                Log.i("Tutorial", "Loading for pos: " + position);
                img.setImageResource(R.drawable.chart);
                break;

        }
    }
}
