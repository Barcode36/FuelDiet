package com.example.fueldiet.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.fueldiet.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class SpinnerPetrolStationAdapter extends ArrayAdapter<String> {

    public ArrayList<String> list;

    public SpinnerPetrolStationAdapter(Context context, ArrayList<String> petrolList) {
        super(context, 0, petrolList);
        list = petrolList;
    }

    public SpinnerPetrolStationAdapter(Context context, String [] petrolList) {
        super(context, 0, petrolList);
        list =  new ArrayList<String>(Arrays.asList(petrolList));
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    private View initView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.vehicle_select_template, parent, false
            );
        }

        convertView.setBackgroundColor(0x00000000);
        ImageView imageViewLogo = convertView.findViewById(R.id.vehicle_select_man_img);
        TextView textViewName = convertView.findViewById(R.id.vehicle_select_make_model);
        textViewName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);

        String currentItem = getItem(position);


        if (currentItem != null && !currentItem.equals(getContext().getString(R.string.other))) {
            textViewName.setText(currentItem);
            Glide.with(getContext()).load(getContext().getResources().getIdentifier(currentItem.toLowerCase(), "drawable", getContext().getPackageName())).fitCenter().into(imageViewLogo);
        } else {
            textViewName.setText(currentItem);
            Glide.with(getContext()).load(getContext().getDrawable(R.drawable.ic_help_outline_black_24dp)).into(imageViewLogo);
        }

        return convertView;
    }
}