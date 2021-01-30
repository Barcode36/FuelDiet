package com.fueldiet.fueldiet.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.object.PetrolStationObject;

import java.io.File;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class PetrolStationAdapter extends RecyclerView.Adapter<PetrolStationAdapter.PetrolStationViewHolder> {

    private PetrolStationAdapter.OnItemClickListener mListener;
    private final Context context;
    private final List<PetrolStationObject> stationObjects;

    public PetrolStationAdapter(Context context, List<PetrolStationObject> list) {
        this.context = context;
        stationObjects = list;
    }

    public interface OnItemClickListener {
        void onItemEdit(int position, long id);
        void onItemDelete(int position, long id);
    }

    public void setOnItemClickListener(PetrolStationAdapter.OnItemClickListener listener) {
        mListener = listener;
    }

    public static class PetrolStationViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        ImageView logo;
        ImageView edit;
        ImageView remove;


        public PetrolStationViewHolder(final View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.petrol_station_name);
            logo = itemView.findViewById(R.id.petrol_station_logo);
            remove = itemView.findViewById(R.id.petrol_station_remove);
            edit = itemView.findViewById(R.id.petrol_station_edit);
        }
    }

    @NonNull
    @Override
    public PetrolStationAdapter.PetrolStationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_template_petrol_station, parent, false);
        return new PetrolStationAdapter.PetrolStationViewHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull PetrolStationViewHolder holder, int position) {
        if (position >= getItemCount()) {
            return;
        }

        PetrolStationObject station = stationObjects.get(position);

        String name = station.getName();
        if (name.equals("Other")) {
            holder.name.setText(context.getString(R.string.other));
            Glide.with(context).load(ContextCompat.getDrawable(context, R.drawable.ic_help_outline_black_24dp)).into(holder.logo);
        } else {
            holder.name.setText(name);
            String fileName = station.getFileName();
            File storageDIR = context.getDir("Images", MODE_PRIVATE);
            Glide.with(context).load(storageDIR+"/"+fileName).diskCacheStrategy(DiskCacheStrategy.NONE).into(holder.logo);
        }

        if (station.getOrigin() == 0) {
            holder.remove.setOnClickListener(v -> Toast.makeText(context, context.getString(R.string.action_not_allowed), Toast.LENGTH_SHORT).show());
            holder.edit.setOnClickListener(v -> Toast.makeText(context, context.getString(R.string.action_not_allowed), Toast.LENGTH_SHORT).show());

        } else {
            holder.remove.setOnClickListener(v -> mListener.onItemDelete(position, station.getId()));
            holder.edit.setOnClickListener(v -> mListener.onItemEdit(position, station.getId()));
        }

    }

    @Override
    public int getItemCount() {
        return stationObjects.size();
    }
}
