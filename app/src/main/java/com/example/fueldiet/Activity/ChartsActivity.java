package com.example.fueldiet.Activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.fueldiet.Fragment.BarChartFragment;
import com.example.fueldiet.Fragment.LineChartFragment;
import com.example.fueldiet.Fragment.PieChartFragment;
import com.example.fueldiet.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ChartsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);

        BottomNavigationView bottomNav = findViewById(R.id.chart_bottom_nav);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFrag = null;

            switch (item.getItemId()) {
                case R.id.chart_line:
                    selectedFrag = new LineChartFragment();
                    break;
                case R.id.chart_bar:
                    selectedFrag = new BarChartFragment();
                    break;
                default:
                    //is pie
                    selectedFrag = new PieChartFragment();
                    break;
            }

            getSupportFragmentManager().beginTransaction().replace(R.id.chart_fragment_container, selectedFrag).commit();

            return true;
        });

        getSupportFragmentManager().beginTransaction().replace(R.id.chart_fragment_container, new PieChartFragment()).commit();
    }
}
