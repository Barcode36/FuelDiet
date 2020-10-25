package com.fueldiet.fueldiet.adapter;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.object.StationPriceObject;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class StationsPricesListAdapter extends RecyclerView.Adapter<StationsPricesListAdapter.StationsPricesListViewHolder> {

    private StationsPricesListAdapter.OnItemClickListener mListener;
    private List<StationPriceObject> data;
    private HashMap<Integer, String> names;
    private Locale locale;

    public StationsPricesListAdapter(Context context, List<StationPriceObject> list, HashMap<Integer, String> franchises) {
        data = list;
        names = franchises;

        Configuration configuration = context.getResources().getConfiguration();
        locale = configuration.getLocales().get(0);
    }

    public interface OnItemClickListener {
        void showOnMap(int position);
        void navigateTo(int position);
    }

    public void setOnItemClickListener(StationsPricesListAdapter.OnItemClickListener listener) {
        mListener = listener;
    }

    public static class StationsPricesListViewHolder extends RecyclerView.ViewHolder {

        public TextView franchiseName, locationName, petrolPrice, dieselPrice;
        public MaterialButton openMapButton, navigateToButton;


        public StationsPricesListViewHolder(final View itemView) {
            super(itemView);
            franchiseName = itemView.findViewById(R.id.station_prices_franch_name);
            locationName = itemView.findViewById(R.id.station_prices_stat_name);
            petrolPrice = itemView.findViewById(R.id.stations_prices_95_price);
            dieselPrice = itemView.findViewById(R.id.stations_prices_diesel_price);
            openMapButton = itemView.findViewById(R.id.stations_prices_show_on_map);
            navigateToButton = itemView.findViewById(R.id.stations_prices_navigate);
        }
    }

    @NonNull
    @Override
    public StationsPricesListAdapter.StationsPricesListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_template_stations_prices_list, parent, false);
        return new StationsPricesListAdapter.StationsPricesListViewHolder(v);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull StationsPricesListViewHolder holder, int position) {
        if (position >= getItemCount()) {
            return;
        }

        StationPriceObject station = data.get(position);

        holder.franchiseName.setText(names.get(station.getFranchise()).toUpperCase(locale));
        holder.locationName.setText(station.getName());
        holder.dieselPrice.setText(String.format(locale, "%4.3f€", station.getPrices().get("dizel")));
        holder.petrolPrice.setText(String.format(locale, "%4.3f€", station.getPrices().get("95")));

        holder.openMapButton.setOnClickListener(v -> mListener.showOnMap(position));
        holder.navigateToButton.setOnClickListener(v -> mListener.navigateTo(position));

    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
