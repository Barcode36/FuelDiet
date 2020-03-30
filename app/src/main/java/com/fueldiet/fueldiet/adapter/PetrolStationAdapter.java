package com.fueldiet.fueldiet.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
    private Context mContext;
    private List<PetrolStationObject> stationObjects;

    public PetrolStationAdapter(Context context, List<PetrolStationObject> list) {
        mContext = context;
        stationObjects = list;
    }

    public interface OnItemClickListener {
        void onItemEdit(int position);
        void onItemDelete(int position);
    }

    public void setOnItemClickListener(PetrolStationAdapter.OnItemClickListener listener) {
        mListener = listener;
    }

    public static class PetrolStationViewHolder extends RecyclerView.ViewHolder {

        public TextView name;
        public ImageView logo, edit, remove;


        public PetrolStationViewHolder(final View itemView, final PetrolStationAdapter.OnItemClickListener listener) {
            super(itemView);
            name = itemView.findViewById(R.id.petrol_station_name);
            logo = itemView.findViewById(R.id.petrol_station_logo);
            remove = itemView.findViewById(R.id.petrol_station_remove);
            edit = itemView.findViewById(R.id.petrol_station_edit);
        }
    }

    @Override
    public PetrolStationAdapter.PetrolStationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_template_petrol_station, parent, false);
        return new PetrolStationAdapter.PetrolStationViewHolder(v, mListener);
    }


    @Override
    public void onBindViewHolder(@NonNull PetrolStationViewHolder holder, int position) {
        if (position >= getItemCount()) {
            return;
        }

        PetrolStationObject station = stationObjects.get(position);

        String name = station.getName();
        if (name.equals("Other")) {
            holder.name.setText(mContext.getString(R.string.other));
            Glide.with(mContext).load(mContext.getDrawable(R.drawable.ic_help_outline_black_24dp)).into(holder.logo);
        } else {
            holder.name.setText(name);
            String fileName = station.getFileName();
            File storageDIR = mContext.getDir("Images", MODE_PRIVATE);
            Glide.with(mContext).load(storageDIR+"/"+fileName).diskCacheStrategy(DiskCacheStrategy.NONE).into(holder.logo);
        }

        if (station.getOrigin() == 0) {
            holder.remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext, "Action's not allowed!", Toast.LENGTH_SHORT).show();
                }
            });
            holder.edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext, "Action's not allowed!", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            holder.remove.setOnClickListener(v -> mListener.onItemDelete(position));
            holder.edit.setOnClickListener(v -> mListener.onItemEdit(position));
        }

    }

    @Override
    public int getItemCount() {
        return stationObjects.size();
    }
}
