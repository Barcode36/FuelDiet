package com.fueldiet.fueldiet.adapter;

import android.content.Context;
import android.util.Log;
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
import com.fueldiet.fueldiet.object.VehicleObject;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;

/**
 * Adapter for Vehicle Recycler View
 */
public class VehicleManagementAdapter extends RecyclerView.Adapter<VehicleManagementAdapter.VehicleViewHolder> {

    private static final String TAG = "VehicleManagementAdapter";

    private OnItemClickListener listener;
    private final Context context;
    private final List<VehicleObject> vehicleObjectList;
    private final Locale locale;

    public VehicleManagementAdapter(Context context, Locale locale, List<VehicleObject> vehicles) {
        this.context = context;
        this.vehicleObjectList = vehicles;
        this.locale = locale;
    }

    public interface OnItemClickListener {
        void edit(long id);
        void delete(long id);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class VehicleViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        
        TextView make;
        TextView model;
        TextView transmission;
        TextView engine;
        TextView power;
        TextView torque;
        TextView modelYear;
        TextView fuelType;
        TextView hybridType;
        
        MaterialButton edit;
        MaterialButton delete;


        public VehicleViewHolder(final View itemView, final OnItemClickListener listener) {
            super(itemView);
            Log.d(TAG, "VehicleViewHolder: started...");

            make = itemView.findViewById(R.id.vehicle_card_make);
            model = itemView.findViewById(R.id.vehicle_card_model);
            transmission = itemView.findViewById(R.id.vehicle_card_transmission);
            engine = itemView.findViewById(R.id.vehicle_card_engine);
            power = itemView.findViewById(R.id.vehicle_card_power);
            torque = itemView.findViewById(R.id.vehicle_card_torque);
            modelYear = itemView.findViewById(R.id.vehicle_card_model_year);
            fuelType = itemView.findViewById(R.id.vehicle_card_fuel_type);
            hybridType = itemView.findViewById(R.id.vehicle_card_hybrid_type);

            edit = itemView.findViewById(R.id.vehicle_card_edit);
            delete = itemView.findViewById(R.id.vehicle_card_delete);


            edit.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.edit((long)itemView.getTag());
                    }
                }
            });
            delete.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.delete((long)itemView.getTag());
                    }
                }
            });
            Log.d(TAG, "VehicleViewHolder: finished");
        }
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_template_vehicle_management, parent, false);
        return new VehicleViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: started...");
        if (position >= getItemCount())
            return;

        VehicleObject vehicle = vehicleObjectList.get(position);

        String consUnit = PreferenceManager.getDefaultSharedPreferences(context).getString("language_select", "english");
        String benz;
        if (consUnit.equals("english"))
            benz = vehicle.getFuelType();
        else
            benz = Utils.fromENGtoSLO(vehicle.getFuelType());

        holder.make.setText(vehicle.getMake());
        holder.model.setText(vehicle.getModel());
        holder.transmission.setText(vehicle.getTransmission());
        holder.fuelType.setText(benz);
        holder.hybridType.setText(vehicle.getHybridType());
        holder.modelYear.setText(String.format(locale, "%d", vehicle.getModelYear()));

        if (vehicle.getEngine() < 1.0) {
            holder.engine.setText(String.format(locale, "%3.0fccm", vehicle.getEngine() * 1000));
        } else {
            holder.engine.setText(String.format(locale, "%.1fl", vehicle.getEngine()));
        }

        holder.power.setText(String.format(locale, "%dhp", vehicle.getHp()));
        holder.torque.setText(String.format(locale, "%dNm", vehicle.getTorque()));

        holder.itemView.setTag(vehicle.getId());
        Log.d(TAG, "onBindViewHolder: finished");
    }

    @Override
    public int getItemCount() {
        return vehicleObjectList.size();
    }

}
