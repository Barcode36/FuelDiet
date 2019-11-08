package com.example.fueldiet.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fueldiet.Object.CostObject;
import com.example.fueldiet.R;
import com.example.fueldiet.Utils;
import com.example.fueldiet.db.FuelDietContract;

import java.util.Date;
import java.util.List;

public class CostAdapter extends RecyclerView.Adapter<CostAdapter.CostViewHolder>{

    private CostAdapter.OnItemClickListener mListener;
    private Context mContext;
    //private Cursor mCursor;
    private List<CostObject> costObjects;

    public CostAdapter(Context context, List<CostObject> list) {
        mContext = context;
        //mCursor = cursor;
        costObjects = list;
    }

    public interface OnItemClickListener {
        void onEditClick(int position, long element_id);
        void onDeleteClick(int position, long element_id);
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
        public ImageView descImg;
        public TextView type;
        private ImageButton edit;
        private ImageButton remove;


        public CostViewHolder(final View itemView, final CostAdapter.OnItemClickListener listener) {
            super(itemView);
            dateTime = itemView.findViewById(R.id.costs_date);
            odo = itemView.findViewById(R.id.costs_odo);
            title = itemView.findViewById(R.id.costs_title);
            price = itemView.findViewById(R.id.costs_price);
            desc = itemView.findViewById(R.id.costs_desc);
            descImg = itemView.findViewById(R.id.cost_details_img);
            type = itemView.findViewById(R.id.costs_type);
            edit = itemView.findViewById(R.id.costs_edit_img);
            remove = itemView.findViewById(R.id.costs_remove_img);

            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onEditClick(position, (long)itemView.getTag());
                        }
                    }
                }
            });

            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onDeleteClick(position, (long)itemView.getTag());
                        }
                    }
                }
            });
        }
    }

    @Override
    public CostAdapter.CostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_template_cost, parent, false);
        return new CostAdapter.CostViewHolder(v, mListener);
    }


    @Override
    public void onBindViewHolder(@NonNull CostViewHolder holder, int position) {
        if (position >= getItemCount()) {
            return;
        }

        /*
        long secFromEpoch = mCursor.getLong(mCursor.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_DATE));
        Date date = new Date(secFromEpoch*1000);
        int odo_km = mCursor.getInt(mCursor.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_ODO));
        String title = mCursor.getString(mCursor.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_TITLE));
        String desc = mCursor.getString(mCursor.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_DETAILS));
        String typ = mCursor.getString(mCursor.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_TYPE));
        double pricePaid = mCursor.getDouble(mCursor.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_EXPENSE));
        long id = mCursor.getLong(mCursor.getColumnIndex(FuelDietContract.CostsEntry._ID));

         */

        CostObject costObject = costObjects.get(position);

        if (costObject.getDetails() == null) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)holder.price.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_TOP, R.id.costs_date);
            holder.price.setLayoutParams(params);
            holder.desc.setVisibility(View.GONE);
            holder.descImg.setVisibility(View.GONE);
        } else {
            holder.desc.setText(costObject.getDetails());
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        holder.dateTime.setText(dateFormat.format(costObject.getDate().getTime()));
        holder.odo.setText(String.format("%d km", costObject.getKm()));
        holder.title.setText(costObject.getTitle());
        String lang = PreferenceManager.getDefaultSharedPreferences(mContext).getString("language_select", "english");
        if (lang.equals("slovene"))
            holder.type.setText(Utils.fromENGtoSLO(costObject.getType()));
        else
            holder.type.setText(costObject.getType());
        holder.price.setText(Double.toString(costObject.getCost())+"€");
        holder.itemView.setTag(costObject.getCostID());
    }

    @Override
    public int getItemCount() {
        return costObjects.size();
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
