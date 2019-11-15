package com.example.fueldiet.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
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

import com.bumptech.glide.Glide;
import com.example.fueldiet.Activity.MainActivity;
import com.example.fueldiet.Activity.TutorialActivity;
import com.example.fueldiet.R;

import static android.content.Context.MODE_PRIVATE;

public class TutorialFragment extends Fragment {

    View view;
    private int position;
    CheckBox checkBox;
    Button button;
    ImageView img;
    ImageView imgLogo;

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
        imgLogo = view.findViewById(R.id.tutorial_app_logo);

        if (position != 11) {
            img.setVisibility(View.VISIBLE);
            checkBox.setVisibility(View.GONE);
            button.setVisibility(View.GONE);
            imgLogo.setVisibility(View.GONE);
        } else {
            img.setVisibility(View.GONE);
            checkBox.setVisibility(View.VISIBLE);
            button.setVisibility(View.VISIBLE);
            imgLogo.setVisibility(View.VISIBLE);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences pref = getActivity().getSharedPreferences("prefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    if (checkBox.isChecked()) {
                        editor.putBoolean("showTutorial", false);
                        editor.apply();
                    }
                    editor.putBoolean("tmpTutorial", false);
                    editor.apply();
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
                Glide.with(getContext()).load(R.drawable.main_inst).into(img);
                break;
            case 1:
                Log.i("Tutorial", "Loading for pos: " + position);
                Glide.with(getContext()).load(R.drawable.main_swipe_inst).into(img);
                break;
            case 2:
                Log.i("Tutorial", "Loading for pos: " + position);
                Glide.with(getContext()).load(R.drawable.add_veh_inst).into(img);
                break;
            case 3:
                Log.i("Tutorial", "Loading for pos: " + position);
                Glide.with(getContext()).load(R.drawable.cons_inst).into(img);
                break;
            case 4:
                Log.i("Tutorial", "Loading for pos: " + position);
                Glide.with(getContext()).load(R.drawable.con_long_inst).into(img);
                break;
            case 5:
                Log.i("Tutorial", "Loading for pos: " + position);
                Glide.with(getContext()).load(R.drawable.add_fuel_inst).into(img);
                break;
            case 6:
                Log.i("Tutorial", "Loading for pos: " + position);
                Glide.with(getContext()).load(R.drawable.other_cost_inst).into(img);
                break;
            case 7:
                Log.i("Tutorial", "Loading for pos: " + position);
                Glide.with(getContext()).load(R.drawable.reminders_inst).into(img);
                break;
            case 8:
                Log.i("Tutorial", "Loading for pos: " + position);
                Glide.with(getContext()).load(R.drawable.chart_pie_inst).into(img);
                break;
            case 9:
                Log.i("Tutorial", "Loading for pos: " + position);
                Glide.with(getContext()).load(R.drawable.line_chart_inst).into(img);
                break;
            case 10:
                Log.i("Tutorial", "Loading for pos: " + position);
                Glide.with(getContext()).load(R.drawable.chart_bar_inst).into(img);
                break;
            default:
                break;
        }
    }
}
