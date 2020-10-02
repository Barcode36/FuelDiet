package com.fueldiet.fueldiet.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.object.StationPriceObject;

import java.util.List;

public class StationsPricesAdapter extends RecyclerView.Adapter<StationsPricesAdapter.PetrolStationViewHolder> {

    private StationsPricesAdapter.OnItemClickListener mListener;
    private Context mContext;
    private List<StationPriceObject> stationObjects;

    public StationsPricesAdapter(Context context, List<StationPriceObject> list) {
        mContext = context;
        stationObjects = list;
    }

    public void setOnItemClickListener(StationsPricesAdapter.OnItemClickListener listener) {
        mListener = listener;
    }

    public static class PetrolStationViewHolder extends RecyclerView.ViewHolder {

        public TextView name;
        public ImageView logo, edit, remove;


        public PetrolStationViewHolder(final View itemView, final StationsPricesAdapter.OnItemClickListener listener) {
            super(itemView);
            name = itemView.findViewById(R.id.textview);
        }
    }

    @Override
    public StationsPricesAdapter.PetrolStationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_template_station_price, parent, false);
        return new StationsPricesAdapter.PetrolStationViewHolder(v, mListener);
    }


    @Override
    public void onBindViewHolder(@NonNull PetrolStationViewHolder holder, int position) {
        if (position >= getItemCount()) {
            return;
        }

        StationPriceObject station = stationObjects.get(position);

        String name = station.getName();
        holder.name.setText(name);

    }

    @Override
    public int getItemCount() {
        return stationObjects.size();
    }

    public class OnItemClickListener {

    }
}
