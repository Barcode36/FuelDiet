package com.example.fueldiet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {
    private ArrayList<VehicleObject> mVehicleList;

    public static class VehicleViewHolder extends RecyclerView.ViewHolder {

        public ImageView mImageView;
        public TextView mBrand;
        public TextView mData;


        public VehicleViewHolder(View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.imageView);
            mBrand = itemView.findViewById(R.id.textView);
            mData = itemView.findViewById(R.id.textView2);
        }
    }

    public VehicleAdapter(ArrayList<VehicleObject> vehicleList) {
        mVehicleList = vehicleList;
    }

    @Override
    public VehicleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.vehicle, parent, false);
        return new VehicleViewHolder(v);
    }

    @Override
    public void onBindViewHolder(VehicleViewHolder holder, int position) {
        VehicleObject currentItem = mVehicleList.get(position);

        Picasso.get().load(currentItem.getImageURL()).into(holder.mImageView);
        holder.mBrand.setText(currentItem.getmVehicleBrand() + " " + currentItem.getmVehicleModel());
        holder.mData.setText(currentItem.getmVehicleData());
    }

    @Override
    public int getItemCount() {
        return mVehicleList.size();
    }
}
