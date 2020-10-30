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
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.object.StationPricesObject;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class FuelPricesListAdapter extends RecyclerView.Adapter<FuelPricesListAdapter.StationsPricesListViewHolder> {

    private FuelPricesListAdapter.OnItemClickListener mListener;
    private List<StationPricesObject> data;
    private HashMap<Integer, String> names;
    private Locale locale;
    private Context mContext;

    public FuelPricesListAdapter(Context context, List<StationPricesObject> list, HashMap<Integer, String> franchises) {
        data = list;
        names = franchises;
        mContext = context;

        Configuration configuration = context.getResources().getConfiguration();
        locale = configuration.getLocales().get(0);
    }

    public interface OnItemClickListener {
        void showOnMap(int position);
        void navigateTo(int position);
    }

    public void setOnItemClickListener(FuelPricesListAdapter.OnItemClickListener listener) {
        mListener = listener;
    }

    public static class StationsPricesListViewHolder extends RecyclerView.ViewHolder {

        public TextView franchiseName, locationName, petrolPrice, dieselPrice, distance;
        public MaterialButton moreOpt;


        public StationsPricesListViewHolder(final View itemView) {
            super(itemView);
            franchiseName = itemView.findViewById(R.id.station_prices_franch_name);
            locationName = itemView.findViewById(R.id.station_prices_stat_name);
            petrolPrice = itemView.findViewById(R.id.stations_prices_95_price);
            dieselPrice = itemView.findViewById(R.id.stations_prices_diesel_price);
            distance = itemView.findViewById(R.id.stations_prices_distance);
            moreOpt = itemView.findViewById(R.id.stations_prices_more_options);
        }
    }

    @NonNull
    @Override
    public FuelPricesListAdapter.StationsPricesListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_template_fuel_prices_list, parent, false);
        return new FuelPricesListAdapter.StationsPricesListViewHolder(v);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull StationsPricesListViewHolder holder, int position) {
        if (position >= getItemCount()) {
            return;
        }

        StationPricesObject station = data.get(position);

        holder.franchiseName.setText(names.get(station.getFranchise()).toUpperCase(locale));
        holder.locationName.setText(station.getName());
        holder.dieselPrice.setText(String.format(locale, "%4.3f€", station.getPrices().get("dizel")));
        holder.petrolPrice.setText(String.format(locale, "%4.3f€", station.getPrices().get("95")));
        holder.distance.setText(String.format(locale, "%.0fm", station.getDistance()));

        holder.moreOpt.setOnClickListener(view -> {
            PopupMenu popup = new PopupMenu(mContext, holder.moreOpt);
            popup.inflate(R.menu.fuel_price_station_menu);
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.stations_prices_show_on_map:
                        mListener.showOnMap(position);
                        return true;
                    case R.id.stations_prices_navigate:
                        mListener.navigateTo(position);
                        return true;
                    default:
                        return false;
                }
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
