package com.example.fueldiet.Adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fueldiet.Object.DriveObject;
import com.example.fueldiet.db.FuelDietContract;
import com.example.fueldiet.R;
import com.example.fueldiet.Utils;
import com.example.fueldiet.db.FuelDietDBHelper;

import java.util.Date;
import java.util.List;


public class ConsumptionAdapter extends RecyclerView.Adapter<ConsumptionAdapter.ConsumptionViewHolder> {

    private OnItemClickListener mListener;
    private Context mContext;
    private List<DriveObject> mDrives;

    public ConsumptionAdapter(Context context, List<DriveObject> driveObjectList) {
        mContext = context;
        mDrives = driveObjectList;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, long driveID);
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
            date = itemView.findViewById(R.id.consumption_view_day);
            time = itemView.findViewById(R.id.consumption_view_time);
            odo = itemView.findViewById(R.id.consumption_view_odo);
            trip = itemView.findViewById(R.id.consumption_view_trip);
            cons = itemView.findViewById(R.id.consumption_view_cons);
            litres = itemView.findViewById(R.id.consumption_view_litres);
            unit_cons = itemView.findViewById(R.id.consumption_view_cons_unit);
            price_l = itemView.findViewById(R.id.consumption_view_e_p_l);
            price_full = itemView.findViewById(R.id.consumption_view_total_price);

            fuel_drop = itemView.findViewById(R.id.consumption_img);
            fuel_trend = itemView.findViewById(R.id.consumption_view_fuel_up_down);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position, (long)itemView.getTag());
                        }
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public ConsumptionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_template_consumption, parent, false);
        return new ConsumptionViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(ConsumptionViewHolder holder, int position) {
        if (position >= mDrives.size())
            return;

        Date date = mDrives.get(position).getDate().getTime();
        int odo_km =mDrives.get(position).getOdo();
        int trip_km = mDrives.get(position).getTrip();
        double liters = mDrives.get(position).getLitres();
        double pricePerLitre = mDrives.get(position).getCostPerLitre();
        double consumption = Utils.calculateConsumption(trip_km, liters);

        long id = mDrives.get(position).getId();

        FuelDietDBHelper dbHelper = new FuelDietDBHelper(mContext);
        DriveObject driveObject = dbHelper.getPrevDriveSelection(mDrives.get(position).getCarID(), odo_km);
        if (driveObject != null) {
            double prev = Utils.calculateConsumption(driveObject.getTrip(), driveObject.getLitres());
            //cons
            if (Double.compare(prev, consumption) > 0)
                holder.fuel_drop.setImageTintList(ColorStateList.valueOf(mContext.getColor(R.color.green)));
            else if (Double.compare(prev, consumption) < 0)
                holder.fuel_drop.setImageTintList(ColorStateList.valueOf(mContext.getColor(R.color.red)));
            else
                holder.fuel_drop.setImageTintList(ColorStateList.valueOf(mContext.getColor(R.color.yellow)));
            //price
            if (Double.compare(driveObject.getCostPerLitre(), pricePerLitre) > 0) {
                holder.fuel_trend.setImageTintList(ColorStateList.valueOf(mContext.getColor(R.color.green)));
                holder.fuel_trend.setImageResource(R.drawable.ic_expand_more_black_24dp);
            } else if (Double.compare(driveObject.getCostPerLitre(), pricePerLitre) < 0) {
                holder.fuel_trend.setImageTintList(ColorStateList.valueOf(mContext.getColor(R.color.red)));
                holder.fuel_trend.setImageResource(R.drawable.ic_expand_less_black_24dp);
            } else {
                holder.fuel_trend.setImageTintList(ColorStateList.valueOf(mContext.getColor(R.color.yellow)));
                holder.fuel_trend.setImageResource(R.drawable.ic_unfold_less_black_24dp);
            }
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy-HH:mm");
        holder.date.setText(dateFormat.format(date).split("-")[0]);
        holder.time.setText(dateFormat.format(date).split("-")[1]);
        holder.odo.setText(String.format("%d km", odo_km));
        holder.trip.setText(String.format("+%d km", trip_km));
        holder.litres.setText(String.format("%s l", liters));

        if (PreferenceManager.getDefaultSharedPreferences(mContext).getString("selected_unit", "litres_per_km").equals("litres_per_km")){
            holder.cons.setText(Double.toString(consumption));
            holder.unit_cons.setText(R.string.l_per_100_km_unit);
        } else {
            Log.i("Consumption", "From "+consumption+" l/100km to " + Utils.convertUnitToKmPL(consumption) + " km/l");
            holder.cons.setText(Double.toString(Utils.convertUnitToKmPL(consumption)));
            holder.unit_cons.setText(R.string.km_per_l_unit);
        }


        holder.itemView.setTag(id);
        holder.price_l.setText(String.format("%s €/l", Double.toString(pricePerLitre)));
        holder.price_full.setText(String.format("%s €", Double.toString(Utils.calculateFullPrice(pricePerLitre, liters))));
    }

    @Override
    public int getItemCount() {
        return mDrives.size();
    }

    /*
    public void swapCursor(Cursor newCursor) {
        if (mCursor != null) {
            mCursor.close();
        }

        mCursor = newCursor;

        if (newCursor != null) {
            notifyDataSetChanged();
        }
    }*/
}