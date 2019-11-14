package com.example.fueldiet.Adapter;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.fueldiet.Fragment.TutorialFragment;
import com.example.fueldiet.Fragment.VehicleConsumptionFragment;
import com.example.fueldiet.Fragment.VehicleCostsFragment;
import com.example.fueldiet.Fragment.VehicleReminderFragment;
import com.example.fueldiet.R;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class TutorialPagerAdapter extends FragmentPagerAdapter {

    private final Context mContext;

    public TutorialPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return TutorialFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        // Show 4 total pages.
        return 8;
    }
}