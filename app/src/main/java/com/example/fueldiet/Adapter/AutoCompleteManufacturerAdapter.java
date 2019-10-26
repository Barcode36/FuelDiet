package com.example.fueldiet.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.fueldiet.Object.ManufacturerObject;
import com.example.fueldiet.R;

import java.util.ArrayList;
import java.util.List;

public class AutoCompleteManufacturerAdapter extends ArrayAdapter<ManufacturerObject> {
    private List<ManufacturerObject> manufacturersList;

    public AutoCompleteManufacturerAdapter(@NonNull Context context, @NonNull List<ManufacturerObject> makeList) {
        super(context, 0, makeList);
        manufacturersList = new ArrayList<>(makeList);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return manufaturerFilter;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.manufacturer_autocomplete_template, parent, false
            );
        }

        TextView textViewName = convertView.findViewById(R.id.man_autocomplete_man);
        ImageView imageViewFlag = convertView.findViewById(R.id.man_autocomplete_img);

        ManufacturerObject manufacturerObject = getItem(position);

        if (manufacturerObject != null) {
            textViewName.setText(manufacturerObject.getName());
            Glide.with(getContext()).load(manufacturerObject.getUrl()).diskCacheStrategy(DiskCacheStrategy.ALL).into(imageViewFlag);
        }

        return convertView;
    }

    private Filter manufaturerFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<ManufacturerObject> suggestions = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                suggestions.addAll(manufacturersList);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (ManufacturerObject item : manufacturersList) {
                    if (item.getName().toLowerCase().contains(filterPattern)) {
                        suggestions.add(item);
                    }
                }
            }

            results.values = suggestions;
            results.count = suggestions.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            addAll((List) results.values);
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((ManufacturerObject) resultValue).getName();
        }
    };
}