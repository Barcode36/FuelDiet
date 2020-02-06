package com.example.fueldiet.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.fueldiet.Activity.MainActivity;
import com.example.fueldiet.Object.ManufacturerObject;
import com.example.fueldiet.Object.VehicleObject;
import com.example.fueldiet.R;
import com.example.fueldiet.Utils;

import java.io.File;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;
import static com.example.fueldiet.Utils.toCapitalCaseWords;

public class VehicleSelectAdapter extends ArrayAdapter<VehicleObject> {
    public ArrayList<VehicleObject> list;

    public VehicleSelectAdapter(Context context, ArrayList<VehicleObject> vehiclesList) {
        super(context, 0, vehiclesList);
        list = vehiclesList;
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

        ImageView imageViewLogo = convertView.findViewById(R.id.vehicle_select_man_img);
        TextView textViewName = convertView.findViewById(R.id.vehicle_select_make_model);

        VehicleObject currentItem = getItem(position);


        if (currentItem != null) {
            //TODO: set image
            //imageViewFlag.setImageResource(currentItem.getFlagImage());
            textViewName.setText(String.format("%s %s", currentItem.getMake(), currentItem.getModel()));

            /* Loads image file if exists, else predefined image */
            try {
                String fileName = currentItem.getCustomImg();
                File storageDIR = getContext().getDir("Images",MODE_PRIVATE);
                if (fileName == null) {
                    ManufacturerObject mo = MainActivity.manufacturers.get(toCapitalCaseWords(currentItem.getMake()));
                    if (!mo.isOriginal()){
                        Utils.downloadImage(getContext().getResources(), getContext().getApplicationContext(), mo);
                    }
                    int idResource = getContext().getResources().getIdentifier(mo.getFileNameModNoType(), "drawable", getContext().getPackageName());
                    Glide.with(getContext()).load(storageDIR+"/"+mo.getFileNameMod()).error(getContext().getDrawable(idResource)).diskCacheStrategy(DiskCacheStrategy.NONE).into(imageViewLogo);
                } else {
                    Glide.with(getContext()).load(storageDIR+"/"+fileName).diskCacheStrategy(DiskCacheStrategy.NONE).into(imageViewLogo);
                }
            } catch (Exception e){
                Bitmap noIcon = Utils.getBitmapFromVectorDrawable(getContext(), R.drawable.ic_help_outline_black_24dp);
                Glide.with(getContext()).load(noIcon).fitCenter().diskCacheStrategy(DiskCacheStrategy.ALL).into(imageViewLogo);
            }
        }

        return convertView;
    }
}
