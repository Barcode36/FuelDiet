package com.example.fueldiet.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.fueldiet.Activity.MainActivity;
import com.example.fueldiet.Object.ManufacturerObject;
import com.example.fueldiet.Object.VehicleObject;
import com.example.fueldiet.Utils;
import com.example.fueldiet.db.FuelDietContract;
import com.example.fueldiet.R;


import java.io.File;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.example.fueldiet.Utils.toCapitalCaseWords;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {
    private OnItemClickListener mListener;

    private Context mContext;
    private List<VehicleObject> vehicleObjectList;
    //private Cursor mCursor;

    public VehicleAdapter(Context context, /*Cursor cursor*/ List<VehicleObject> vehicles) {
        mContext = context;
        //mCursor = cursor;
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
        /*if (!mCursor.moveToPosition(position)) {
            return;
        }*/
        if (position >= getItemCount())
            return;

        VehicleObject vehicle = vehicleObjectList.get(position);

        String consUnit = PreferenceManager.getDefaultSharedPreferences(mContext).getString("language_select", "english");
        String benz;
        if (consUnit.equals("english"))
            //benz = mCursor.getString(mCursor.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_FUEL_TYPE));
            benz = vehicle.getFuel();
        else
            //benz = Utils.fromENGtoSLO(mCursor.getString(mCursor.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_FUEL_TYPE)));
            benz = Utils.fromENGtoSLO(vehicle.getFuel());

        //String make = mCursor.getString(mCursor.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_MAKE));
        String make = vehicle.getMake();
        //String model = mCursor.getString(mCursor.getColumnIndex((FuelDietContract.VehicleEntry.COLUMN_MODEL)));
        String model = vehicle.getModel();

        /*
        String data = mCursor.getString(mCursor.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_ENGINE)) +
                " " + mCursor.getInt(mCursor.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_HP)) + "hp" +
                " " + benz;

         */
        String data = vehicle.getEngine() + " " + vehicle.getHp() + "hp" + " " + benz;
        //long id = mCursor.getLong(mCursor.getColumnIndex(FuelDietContract.VehicleEntry._ID));
        long id = vehicle.getId();

        holder.mBrand.setText(String.format("%s %s", make, model));
        holder.mData.setText(data);


        try {
            //String fileName = mCursor.getString(mCursor.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_CUSTOM_IMG));
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
