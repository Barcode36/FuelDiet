package com.example.fueldiet;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.example.fueldiet.MainActivity.LOGO_URL;
import static com.example.fueldiet.Utils.toCapitalCaseWords;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {
    private ArrayList<VehicleObject> mVehicleList;
    private OnItemClickListener mListener;

    private Context mContext;
    private Cursor mCursor;

    public VehicleAdapter(ArrayList<VehicleObject> vehicleList) {
        mVehicleList = vehicleList;
    }

    public VehicleAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
    }

    public interface OnItemClickListener {
        void onItemClick(long element_id);
        //void onDeleteClick(int position);
        //void onEditClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class VehicleViewHolder extends RecyclerView.ViewHolder {

        public ImageView mImageView;
        public TextView mBrand;
        public TextView mData;
        public ImageView mEdit;
        public ImageView mDelete;


        public VehicleViewHolder(final View itemView, final OnItemClickListener listener) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.logo_image);
            mBrand = itemView.findViewById(R.id.textView);
            mData = itemView.findViewById(R.id.textView2);
            mEdit = itemView.findViewById(R.id.edit_button);
            mDelete = itemView.findViewById(R.id.delete_button);

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

            /*
            mDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onDeleteClick(position);
                        }
                    }
                }
            });

            mEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onEditClick(position);
                        }
                    }
                }
            });*/
        }
    }

    @Override
    public VehicleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.vehicle, parent, false);
        return new VehicleViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(VehicleViewHolder holder, int position) {
        /*
        VehicleObject currentItem = mVehicleList.get(position);

        Picasso.get().load(currentItem.getImageURL()).into(holder.mImageView);
        holder.mBrand.setText(currentItem.getmVehicleBrand() + " " + currentItem.getmVehicleModel());
        holder.mData.setText(currentItem.getmVehicleData());*/
        if (!mCursor.moveToPosition(position)) {
            return;
        }

        String make = mCursor.getString(mCursor.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_MAKE));
        String model = mCursor.getString(mCursor.getColumnIndex((FuelDietContract.VehicleEntry.COLUMN_MODEL)));

        String data = mCursor.getString(mCursor.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_ENGINE)) +
                " " + mCursor.getString(mCursor.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_FUEL_TYPE));
        long id = mCursor.getLong(mCursor.getColumnIndex(FuelDietContract.VehicleEntry._ID));

        holder.mBrand.setText(make + " " + model);
        holder.mData.setText(data);

        String img_url = String.format(LOGO_URL, toCapitalCaseWords(make));
        Picasso.get().load(img_url).into(holder.mImageView);

        holder.itemView.setTag(id);
    }

    @Override
    public int getItemCount() {
        //return mVehicleList.size();
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
