package com.example.fueldiet.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fueldiet.R;
import com.example.fueldiet.Utils;
import com.example.fueldiet.db.FuelDietContract;

import java.util.Date;

public class CostAdapter extends RecyclerView.Adapter<CostAdapter.CostViewHolder>{

    private CostAdapter.OnItemClickListener mListener;
    private Context mContext;
    private Cursor mCursor;

    public CostAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
    }

    public interface OnItemClickListener {
        void onItemClick(long element_id);
    }

    public void setOnItemClickListener(CostAdapter.OnItemClickListener listener) {
        mListener = listener;
    }

    public static class CostViewHolder extends RecyclerView.ViewHolder {

        public TextView dateTime;
        public TextView odo;
        public TextView title;
        public TextView price;
        public TextView desc;
        public TextView type;


        public CostViewHolder(final View itemView, final CostAdapter.OnItemClickListener listener) {
            super(itemView);
            dateTime = itemView.findViewById(R.id.costs_date);
            odo = itemView.findViewById(R.id.costs_odo);
            title = itemView.findViewById(R.id.costs_title);
            price = itemView.findViewById(R.id.costs_price);
            desc = itemView.findViewById(R.id.costs_desc);
            type = itemView.findViewById(R.id.costs_type);
        }
    }

    @Override
    public CostAdapter.CostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cost_template, parent, false);
        return new CostAdapter.CostViewHolder(v, mListener);
    }


    @Override
    public void onBindViewHolder(@NonNull CostViewHolder holder, int position) {
        if (!mCursor.moveToPosition(position)) {
            return;
        }

        long secFromEpoch = mCursor.getLong(mCursor.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_DATE));
        Date date = new Date(secFromEpoch*1000);
        int odo_km = mCursor.getInt(mCursor.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_ODO));
        String title = mCursor.getString(mCursor.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_TITLE));
        String desc = mCursor.getString(mCursor.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_DETAILS));
        String typ = mCursor.getString(mCursor.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_TYPE));
        double pricePaid = mCursor.getDouble(mCursor.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_EXPENSE));
        long id = mCursor.getLong(mCursor.getColumnIndex(FuelDietContract.CostsEntry._ID));

        if (desc == null) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)holder.price.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_TOP, R.id.costs_date);
            holder.price.setLayoutParams(params);
            holder.desc.setVisibility(View.GONE);
        } else {
            holder.desc.setText(desc);
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        holder.dateTime.setText(dateFormat.format(date));
        holder.odo.setText(odo_km+" km");
        holder.title.setText(title);
        holder.type.setText(typ);
        holder.price.setText(Double.toString(pricePaid)+"€");
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
