package com.example.fueldiet.Adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.fueldiet.db.FuelDietContract;
import com.example.fueldiet.R;
import com.example.fueldiet.Utils;
import com.example.fueldiet.db.FuelDietDBHelper;

import java.util.Date;


public class ConsumptionAdapter extends RecyclerView.Adapter<ConsumptionAdapter.ConsumptionViewHolder> {

    private OnItemClickListener mListener;
    private Context mContext;
    private Cursor mCursor;

    public ConsumptionAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
    }

    public interface OnItemClickListener {
        void onItemClick(long element_id);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class ConsumptionViewHolder extends RecyclerView.ViewHolder {

        public TextView date;
        public TextView time;
        public TextView odo;
        public TextView trip;
        public TextView litres;
        public TextView cons;
        public TextView unit_cons;
        public TextView price_l;
        public TextView price_full;

        public ImageView fuel_drop;
        public ImageView fuel_trend;


        public ConsumptionViewHolder(final View itemView, final OnItemClickListener listener) {
            super(itemView);
            date = itemView.findViewById(R.id.view_day);
            time = itemView.findViewById(R.id.view_time);
            odo = itemView.findViewById(R.id.view_odo);
            trip = itemView.findViewById(R.id.view_trip);
            cons = itemView.findViewById(R.id.view_cons);
            litres = itemView.findViewById(R.id.view_litres);
            unit_cons = itemView.findViewById(R.id.view_cons_unit);
            price_l = itemView.findViewById(R.id.view_e_p_l);
            price_full = itemView.findViewById(R.id.view_total_price);

            fuel_drop = itemView.findViewById(R.id.litres_img);
            fuel_trend = itemView.findViewById(R.id.view_fuel_up_down);
        }
    }

    @Override
    public ConsumptionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.consumption_template, parent, false);
        return new ConsumptionViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(ConsumptionViewHolder holder, int position) {
        if (!mCursor.moveToPosition(position)) {
            return;
        }

        String secFromEpoch = mCursor.getString(mCursor.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_DATE));
        Long h = Long.decode(secFromEpoch);
        Date date = new Date(h*1000);
        int odo_km = mCursor.getInt(mCursor.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_ODO_KM));
        int trip_km = mCursor.getInt(mCursor.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_TRIP_KM));
        double liters = mCursor.getDouble(mCursor.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_LITRES));
        double pricePerLitre = mCursor.getDouble(mCursor.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_PRICE_LITRE));
        double consumption = Utils.calculateConsumption(trip_km, liters);

        long id = mCursor.getLong(mCursor.getColumnIndex(FuelDietContract.DriveEntry._ID));

        FuelDietDBHelper dbHelper = new FuelDietDBHelper(mContext);
        Cursor cursor = dbHelper.getPrevDriveSelection(mCursor.getInt(mCursor.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_CAR)), odo_km);
        int prev_km_trip = cursor.getInt(2);
        double prev_l = cursor.getDouble(1);
        double prev_price_l = cursor.getDouble(3);
        if (prev_km_trip != 0 && Double.compare(prev_l, 0.0) != 0) {
            double prev = Utils.calculateConsumption(prev_km_trip, prev_l);
            if (Double.compare(prev, consumption) > 0)
                holder.fuel_drop.setImageTintList(ColorStateList.valueOf(mContext.getColor(R.color.colorPrimary)));
            else if (Double.compare(prev, consumption) < 0)
                holder.fuel_drop.setImageTintList(ColorStateList.valueOf(Color.RED));
            else
                holder.fuel_drop.setImageTintList(ColorStateList.valueOf(mContext.getColor(R.color.colorAccent)));
            if (Double.compare(prev_price_l, pricePerLitre) > 0) {
                holder.fuel_trend.setImageTintList(ColorStateList.valueOf(mContext.getColor(R.color.colorPrimary)));
                holder.fuel_trend.setImageResource(R.drawable.ic_expand_more_black_24dp);
            } else if (Double.compare(prev_price_l, pricePerLitre) < 0) {
                holder.fuel_trend.setImageTintList(ColorStateList.valueOf(Color.RED));
                holder.fuel_trend.setImageResource(R.drawable.ic_expand_less_black_24dp);
            } else {
                holder.fuel_trend.setImageTintList(ColorStateList.valueOf(mContext.getColor(R.color.colorAccent)));
                holder.fuel_trend.setImageResource(R.drawable.ic_unfold_less_black_24dp);
            }
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy-HH:mm");
        holder.date.setText(dateFormat.format(date).split("-")[0]);
        holder.time.setText(dateFormat.format(date).split("-")[1]);
        holder.odo.setText(String.format("%d km", odo_km));
        holder.trip.setText(String.format("+%d km", trip_km));
        holder.litres.setText(String.format("%s l", liters));
        holder.cons.setText(Double.toString(consumption));
        holder.itemView.setTag(id);
        holder.price_l.setText(String.format("%s €/l", Double.toString(pricePerLitre)));
        holder.price_full.setText(String.format("%s €", Double.toString(Utils.calculateFullPrice(pricePerLitre, liters))));
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        if (mCursor != null) {
            mCursor.close();
        }

        mCursor = newCursor;

        if (newCursor != null) {
            notifyDataSetChanged();
        }
    }
}