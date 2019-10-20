package com.example.fueldiet.Adapter;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.fueldiet.R;
import com.example.fueldiet.Fragment.VehicleConsumptionFragment;
import com.example.fueldiet.Fragment.VehicleCostsFragment;
import com.example.fueldiet.Fragment.VehicleReminderFragment;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_1, R.string.tab_text_2, R.string.tab_text_3};
    private final Context mContext;
    private long id;

    public SectionsPagerAdapter(Context context, FragmentManager fm, long id) {
        super(fm);
        mContext = context;
        this.id = id;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        if (position == 0)
                return VehicleConsumptionFragment.newInstance(id);
        else if (position == 1)
            return VehicleCostsFragment.newInstance(id);

        return VehicleReminderFragment.newInstance(id);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return 3;
    }
}