package com.fueldiet.fueldiet.adapter;

import android.content.Context;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.object.StationPriceObject;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class StationsPricesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int SHOW_STATION = 0;
    private StationsPricesAdapter.OnItemClickListener mListener;
    private Context mContext;
    private List<StationPriceObject> stationObjects;
    ArrayMap<Integer, String> cleanedFranchiseNames;
    private static final int SHOW_MORE = 1;

    public StationsPricesAdapter(Context context, List<StationPriceObject> list, ArrayMap<Integer, String> franchises) {
        mContext = context;
        stationObjects = list;
        cleanedFranchiseNames = franchises;
    }

    public void setOnItemClickListener(StationsPricesAdapter.OnItemClickListener listener) {
        mListener = listener;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == SHOW_MORE) {
            return new ShowMoreStationsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_template_show_more, parent, false), mListener);
        } else {
            return new StationPriceAdapterViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_template_station_price, parent, false), mListener);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (stationObjects.get(position).getAddress().equals(mContext.getString(R.string.show_more)))
            return SHOW_MORE;
        else
            return SHOW_STATION;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position >= stationObjects.size())
            return;

        if (getItemViewType(position) == SHOW_MORE) {
            //((StationsPricesAdapter.ShowMoreStationsViewHolder) holder);
        } else {
            ((StationsPricesAdapter.StationPriceAdapterViewHolder) holder).setUp(position);
        }
    }

    class StationPriceAdapterViewHolder extends RecyclerView.ViewHolder {

        public TextView name, location, petrolPrice, dieselPrice, showMore;
        public MaterialCardView p95, diesel;


        StationPriceAdapterViewHolder(final View itemView, final StationsPricesAdapter.OnItemClickListener listener) {
            super(itemView);
            name = itemView.findViewById(R.id.francise_name);
            location = itemView.findViewById(R.id.franchise_location);
            petrolPrice = itemView.findViewById(R.id.p95_price);
            dieselPrice = itemView.findViewById(R.id.d_price);
            diesel = itemView.findViewById(R.id.d);
            p95 = itemView.findViewById(R.id.p95);
            showMore = itemView.findViewById(R.id.show_more_text);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onStationClick(getAdapterPosition());
                }
            });
        }

        void setUp(int position) {
            StationPriceObject station = stationObjects.get(position);
            String name1 = cleanedFranchiseNames.get(station.getFranchise());
            name.setText(name1);
            String tmpLocation = station.getName();
            if (tmpLocation.contains("_")) {
                location.setText(tmpLocation.split("_")[tmpLocation.split("_").length-1]);
            } else {
                location.setText(tmpLocation);
            }
            petrolPrice.setText(String.format("%.3f€", station.getPrices().get("95")));
            dieselPrice.setText(String.format("%.3f€", station.getPrices().get("dizel")));
        }

    }

    class ShowMoreStationsViewHolder extends RecyclerView.ViewHolder {

        ShowMoreStationsViewHolder(final View itemView, final StationsPricesAdapter.OnItemClickListener listener) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onShowMoreClick();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return stationObjects.size();
    }

    public interface OnItemClickListener {
        void onStationClick(int position);
        void onShowMoreClick();
    }
}
