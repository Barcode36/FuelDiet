package com.example.fueldiet.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.fueldiet.activity.MainActivity;
import com.example.fueldiet.object.ManufacturerObject;
import com.example.fueldiet.object.VehicleObject;
import com.example.fueldiet.Utils;
import com.example.fueldiet.R;


import java.io.File;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.example.fueldiet.Utils.toCapitalCaseWords;

/**
 * Adapter for Vehicle Recycler View
 */
public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {
    private OnItemClickListener mListener;

    private Context mContext;
    private List<VehicleObject> vehicleObjectList;

    public VehicleAdapter(Context context, List<VehicleObject> vehicles) {
        mContext = context;
        vehicleObjectList = vehicles;
    }

    public interface OnItemClickListener {
        void onItemClick(long element_id);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class VehicleViewHolder extends RecyclerView.ViewHolder {

        public ImageView mImageView;
        public TextView mBrand;
        public TextView mData;


        public VehicleViewHolder(final View itemView, final OnItemClickListener listener) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.vehicle_logo_image);
            mBrand = itemView.findViewById(R.id.vehicle_make_model_view);
            mData = itemView.findViewById(R.id.vehicle_desc_view);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick((long)itemView.getTag());
                    }
                }
            });
        }
    }

    @Override
    public VehicleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_template_vehicle, parent, false);
        return new VehicleViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(VehicleViewHolder holder, int position) {
        if (position >= getItemCount())
            return;

        VehicleObject vehicle = vehicleObjectList.get(position);

        String consUnit = PreferenceManager.getDefaultSharedPreferences(mContext).getString("language_select", "english");
        String benz;
        if (consUnit.equals("english"))
            benz = vehicle.getFuel();
        else
            benz = Utils.fromENGtoSLO(vehicle.getFuel());

        String make = vehicle.getMake();
        String model = vehicle.getModel();

        String data = vehicle.getEngine() + " " + vehicle.getHp() + "hp" + " " + benz;
        long id = vehicle.getId();

        holder.mBrand.setText(String.format("%s %s", make, model));
        holder.mData.setText(data);

        /* Loads image file if exists, else predefined image */
        try {
            String fileName = vehicle.getCustomImg();
            File storageDIR = mContext.getDir("Images",MODE_PRIVATE);
            if (fileName == null) {
                ManufacturerObject mo = MainActivity.manufacturers.get(toCapitalCaseWords(make));
                if (!mo.isOriginal()){
                    Utils.downloadImage(mContext.getResources(), mContext.getApplicationContext(), mo);
                }
                int idResource = mContext.getResources().getIdentifier(mo.getFileNameModNoType(), "drawable", mContext.getPackageName());
                Glide.with(mContext).load(storageDIR+"/"+mo.getFileNameMod()).error(mContext.getDrawable(idResource)).diskCacheStrategy(DiskCacheStrategy.NONE).into(holder.mImageView);
            } else {
                Glide.with(mContext).load(storageDIR+"/"+fileName).diskCacheStrategy(DiskCacheStrategy.NONE).into(holder.mImageView);
            }
        } catch (Exception e){
            Bitmap noIcon = Utils.getBitmapFromVectorDrawable(mContext, R.drawable.ic_help_outline_black_24dp);
            Glide.with(mContext).load(noIcon).fitCenter().diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.mImageView);
        }

        holder.itemView.setTag(id);
    }

    @Override
    public int getItemCount() {
        return vehicleObjectList.size();
    }

}
