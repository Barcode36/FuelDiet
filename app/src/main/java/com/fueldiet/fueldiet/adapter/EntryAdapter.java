package com.fueldiet.fueldiet.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.Utils;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.fueldiet.fueldiet.object.CostObject;
import com.fueldiet.fueldiet.object.DriveObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EntryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private OnItemClickListener mListener;
    private Context mContext;
    private List<Object> objectsList;
    private FuelDietDBHelper dbHelper;
    Locale selected;

    public EntryAdapter(Context context, List<Object> data, FuelDietDBHelper dbHelper) {
        mContext = context;
        objectsList = data;
        this.dbHelper = dbHelper;

        if ("slovene".equals(PreferenceManager.getDefaultSharedPreferences(mContext).getString("language_select", "english")))
            selected = new Locale("sl", "SI");
        else
            selected = new Locale("en", "GB");

    }

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onItemSelected(long vehicleID);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0 /*New month*/) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_recycler_template_month, parent, false);
            return new EntryAdapter.MonthViewHolder(v, mListener);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_recycler_template_entry, parent, false);
            return new EntryAdapter.DataViewHolder(v, mListener);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (objectsList.get(position) instanceof Calendar)
            return 0;
        else
            return 1;
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == 0) {
            ((MonthViewHolder) holder).setUp(objectsList.get(position));
        } else if (getItemViewType(position) == 1) {
            ((DataViewHolder) holder).setUp(objectsList.get(position));
        }
    }


    @Override
    public int getItemCount() {
        return objectsList.size();
    }

    class MonthViewHolder extends RecyclerView.ViewHolder {

        TextView month;
        OnItemClickListener listener;

        MonthViewHolder(final View itemView, final OnItemClickListener listener) {
            super(itemView);
            month = itemView.findViewById(R.id.month_title);
            this.listener = listener;
        }

        void setUp(Object object) {
            Calendar calendar = (Calendar) object;
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy:", selected);
            month.setText(sdf.format(calendar.getTime()));
        }
    }

    class DataViewHolder extends RecyclerView.ViewHolder {
        ImageView logo;
        TextView price, when, what;
        OnItemClickListener listener;

        DataViewHolder(final View itemView, final OnItemClickListener listener) {
            super(itemView);
            logo = itemView.findViewById(R.id.logo);
            price = itemView.findViewById(R.id.price);
            when = itemView.findViewById(R.id.when);
            what = itemView.findViewById(R.id.what);
            this.listener = listener;
        }

        void setUp(Object object) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d.", selected);

            if (object instanceof CostObject) {
                CostObject tmp = (CostObject) object;
                logo.setImageResource(R.drawable.ic_local_offer_black_24dp);
                price.setText(tmp.getCost()+"");
                what.setText(tmp.getTitle());
                when.setText(sdf.format(tmp.getDate().getTime()));
            } else {
                DriveObject tmp = (DriveObject) object;
                logo.setImageResource(R.drawable.ic_local_gas_station_black_24dp);
                price.setText(Utils.calculateFullPrice(tmp.getCostPerLitre(), tmp.getLitres())+"");
                what.setText(mContext.getString(R.string.refueling));
                when.setText(sdf.format(tmp.getDate().getTime()));
            }
        }
    }
}
