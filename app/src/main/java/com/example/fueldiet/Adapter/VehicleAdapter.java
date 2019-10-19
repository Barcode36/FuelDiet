package com.example.fueldiet.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.fueldiet.Activity.MainActivity;
import com.example.fueldiet.db.FuelDietContract;
import com.example.fueldiet.R;


import static com.example.fueldiet.Activity.MainActivity.LOGO_URL;
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
            mImageView = itemView.findViewById(R.id.logo_image);
            mBrand = itemView.findViewById(R.id.textView);
            mData = itemView.findViewById(R.id.textView2);

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
    public VehicleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.vehicle_template, parent, false);
        return new VehicleViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(VehicleViewHolder holder, int position) {
        if (!mCursor.moveToPosition(position)) {
            return;
        }

        String make = mCursor.getString(mCursor.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_MAKE));
        String model = mCursor.getString(mCursor.getColumnIndex((FuelDietContract.VehicleEntry.COLUMN_MODEL)));

        String data = mCursor.getString(mCursor.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_ENGINE)) +
                " " + mCursor.getInt(mCursor.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_HP)) + "hp" +
                " " + mCursor.getString(mCursor.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_FUEL_TYPE));
        long id = mCursor.getLong(mCursor.getColumnIndex(FuelDietContract.VehicleEntry._ID));

        holder.mBrand.setText(make + " " + model);
        holder.mData.setText(data);

        String img_url = String.format(LOGO_URL, MainActivity.manufacturers.get(toCapitalCaseWords(make)).getFileName());

        Glide.with(mContext).load(img_url).diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.mImageView);


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
