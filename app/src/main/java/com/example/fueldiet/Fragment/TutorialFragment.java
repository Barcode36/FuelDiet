package com.example.fueldiet.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.fueldiet.R;

public class TutorialFragment extends Fragment {

    View view;
    private int position;

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

        if (position != 2) {
            view.findViewById(R.id.tutorial_accept_cond).setVisibility(View.GONE);
            view.findViewById(R.id.tutorial_finish).setVisibility(View.GONE);
        }

        return view;
    }
}
