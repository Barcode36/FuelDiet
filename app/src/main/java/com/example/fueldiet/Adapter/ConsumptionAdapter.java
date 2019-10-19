package com.example.fueldiet.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.fueldiet.db.FuelDietContract;
import com.example.fueldiet.R;
import com.example.fueldiet.Utils;

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

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy-HH:mm");
        holder.date.setText(dateFormat.format(date).split("-")[0]);
        holder.time.setText(dateFormat.format(date).split("-")[1]);
        holder.odo.setText(odo_km+" km");
        holder.trip.setText("+"+trip_km+" km");
        holder.litres.setText(liters+" l");
        holder.cons.setText(Double.toString(consumption));
        holder.itemView.setTag(id);
        holder.price_l.setText(Double.toString(pricePerLitre)+" €/l");
        holder.price_full.setText(Double.toString(Utils.calculateFullPrice(pricePerLitre, liters))+" €");
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