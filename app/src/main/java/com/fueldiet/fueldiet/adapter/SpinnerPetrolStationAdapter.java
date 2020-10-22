package com.fueldiet.fueldiet.adapter;

import android.content.Context;
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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.object.PetrolStationObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static android.content.Context.MODE_PRIVATE;

public class SpinnerPetrolStationAdapter extends ArrayAdapter<String> {

    public List<String> list;
    public ArrayList<PetrolStationObject> listWithObj;

    public SpinnerPetrolStationAdapter(Context context, List<PetrolStationObject> petrolList) {
        super(context, 0, petrolList.stream().map(PetrolStationObject::getName).collect(Collectors.toList()));
        list = petrolList.stream().map(PetrolStationObject::getName).collect(Collectors.toList());
        listWithObj = new ArrayList<>(petrolList);
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
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.vehicle_select_template, parent, false);
        }

        convertView.setBackgroundColor(0x00000000);
        ImageView imageViewLogo = convertView.findViewById(R.id.vehicle_select_man_img);
        TextView textViewName = convertView.findViewById(R.id.vehicle_select_make_model);
        textViewName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);

        PetrolStationObject currentItem = listWithObj.get(position);

        if (currentItem != null && !currentItem.getName().equals("Other")) {
            textViewName.setText(currentItem.getName());
            String fileName = currentItem.getFileName();
            File storageDIR = getContext().getDir("Images", MODE_PRIVATE);
            Glide.with(getContext()).load(storageDIR+"/"+fileName).diskCacheStrategy(DiskCacheStrategy.NONE).into(imageViewLogo);
            /*Glide.with(getContext()).load(getContext().getResources().getIdentifier(currentItem.getFileName().substring(0, currentItem.getFileName().length()-4),
                    "drawable",
                    getContext().getPackageName())).fitCenter().into(imageViewLogo);*/
        } else {
            textViewName.setText(getContext().getString(R.string.other));
            Glide.with(getContext()).load(getContext().getDrawable(R.drawable.ic_help_outline_black_24dp)).into(imageViewLogo);
        }

        return convertView;
    }

    @Override
    public int getPosition(@Nullable String item) {
        //return super.getPosition(item);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(item))
                return i;
        }
        return -1;
    }


}