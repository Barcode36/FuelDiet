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
import com.example.fueldiet.Utils;
import com.example.fueldiet.db.FuelDietContract;
import com.example.fueldiet.R;


import java.io.File;

import static android.content.Context.MODE_PRIVATE;
import static com.example.fueldiet.Utils.toCapitalCaseWords;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {
    private OnItemClickListener mListener;

    private Context mContext;
    private Cursor mCursor;

    public VehicleAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
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
        if (!mCursor.moveToPosition(position)) {
            return;
        }

        String consUnit = PreferenceManager.getDefaultSharedPreferences(mContext).getString("language_select", "english");
        String benz;
        if (consUnit.equals("english"))
            benz = mCursor.getString(mCursor.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_FUEL_TYPE));
        else
            benz = Utils.fromENGtoSLO(mCursor.getString(mCursor.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_FUEL_TYPE)));

        String make = mCursor.getString(mCursor.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_MAKE));
        String model = mCursor.getString(mCursor.getColumnIndex((FuelDietContract.VehicleEntry.COLUMN_MODEL)));

        String data = mCursor.getString(mCursor.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_ENGINE)) +
                " " + mCursor.getInt(mCursor.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_HP)) + "hp" +
                " " + benz;
        long id = mCursor.getLong(mCursor.getColumnIndex(FuelDietContract.VehicleEntry._ID));

        holder.mBrand.setText(String.format("%s %s", make, model));
        holder.mData.setText(data);


        try {
            ManufacturerObject mo = MainActivity.manufacturers.get(toCapitalCaseWords(make));
            if (!mo.isOriginal()){
                Utils.downloadImage(mContext.getResources(), mContext.getApplicationContext(), mo);
            }
            //String img_url = mo.getUrl();
            //Glide.with(mContext).load(img_url).diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.mImageView);
            File storageDIR = mContext.getDir("Images",MODE_PRIVATE);
            int idResource = mContext.getResources().getIdentifier(mo.getFileNameModNoType(), "drawable", mContext.getPackageName());
            Glide.with(mContext).load(storageDIR+"/"+mo.getFileNameMod()).into(holder.mImageView).onLoadFailed(mContext.getDrawable(idResource));

            //int idResource = mContext.getResources().getIdentifier(mo.getFileNameModNoType(), "drawable", mContext.getPackageName());
            //Log.i("Vehicle Adapter:", mo.getFileNameModNoType() + " = " + idResource);
            //Glide.with(mContext).load(idResource).into(holder.mImageView);
        } catch (Exception e){
            Bitmap noIcon = Utils.getBitmapFromVectorDrawable(mContext, R.drawable.ic_help_outline_black_24dp);
            Glide.with(mContext).load(noIcon).fitCenter().diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.mImageView);
        }
        //String img_url = String.format(LOGO_URL, MainActivity.manufacturers.get(toCapitalCaseWords(make)).getFileName());
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
