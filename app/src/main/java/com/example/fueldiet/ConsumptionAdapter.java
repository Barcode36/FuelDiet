package com.example.fueldiet;

import android.content.Context;
import android.database.Cursor;
import android.icu.text.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


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


        public ConsumptionViewHolder(final View itemView, final OnItemClickListener listener) {
            super(itemView);
            date = itemView.findViewById(R.id.view_day);
            time = itemView.findViewById(R.id.view_time);
            odo = itemView.findViewById(R.id.view_odo);
            trip = itemView.findViewById(R.id.view_trip);
            cons = itemView.findViewById(R.id.view_cons);
            litres = itemView.findViewById(R.id.view_litres);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick((long)itemView.getTag());
                        }
                    }
                }
            });
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
        Date date = new Date(Long.getLong(secFromEpoch)*1000);
        int odo_km = mCursor.getInt(mCursor.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_START_KM));
        int trip_km = mCursor.getInt(mCursor.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_TRIP_KM));
        int liters = mCursor.getInt(mCursor.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_LITRES));
        int consu = mCursor.getInt(mCursor.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_CONSUMPTION));

        long id = mCursor.getLong(mCursor.getColumnIndex(FuelDietContract.DriveEntry._ID));

        holder.date.setText(date.toString());
        holder.time.setText("lol");
        holder.odo.setText(odo_km);
        holder.trip.setText(trip_km);
        holder.litres.setText(liters);
        holder.cons.setText(consu);
        holder.itemView.setTag(id);
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