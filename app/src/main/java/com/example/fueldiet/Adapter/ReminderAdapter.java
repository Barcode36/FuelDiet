package com.example.fueldiet.Adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fueldiet.R;
import com.example.fueldiet.Utils;
import com.example.fueldiet.db.FuelDietContract;

import java.util.Date;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>{

    private ReminderAdapter.OnItemClickListener mListener;
    private Context mContext;
    private Cursor mCursor;

    public ReminderAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
    }

    public interface OnItemClickListener {
        void onItemClick(long element_id);
    }

    public void setOnItemClickListener(ReminderAdapter.OnItemClickListener listener) {
        mListener = listener;
    }

    public static class ReminderViewHolder extends RecyclerView.ViewHolder {

        public TextView when;
        public TextView title;
        public TextView desc;

        public ImageView whenImg;
        public View divider;
        public ImageView edit;
        public ImageView remove;
        public ImageView descImg;


        public ReminderViewHolder(final View itemView, final ReminderAdapter.OnItemClickListener listener) {
            super(itemView);
            when = itemView.findViewById(R.id.reminder_when_template);
            title = itemView.findViewById(R.id.reminder_title_template);
            desc = itemView.findViewById(R.id.reminder_desc_template);


            whenImg = itemView.findViewById(R.id.reminder_when_img);
            descImg = itemView.findViewById(R.id.reminder_details_img);
            edit = itemView.findViewById(R.id.reminder_edit_img);
            remove = itemView.findViewById(R.id.reminder_remove_img);
            divider = itemView.findViewById(R.id.reminder_break_template);
        }
    }

    @Override
    public ReminderAdapter.ReminderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.reminder_template, parent, false);
        return new ReminderAdapter.ReminderViewHolder(v, mListener);
    }


    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        if (!mCursor.moveToPosition(position)) {
            return;
        }

        String km = mCursor.getString(mCursor.getColumnIndex(FuelDietContract.ReminderEntry.COLUMN_ODO));
        if (km == null || km.equals("")) {
            final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            holder.whenImg.setImageResource(R.drawable.ic_today_black_24dp);
            holder.whenImg.setImageTintList(ColorStateList.valueOf(mContext.getColor(R.color.secondaryTextColor)));
            long secFromEpoch = mCursor.getLong(mCursor.getColumnIndex(FuelDietContract.ReminderEntry.COLUMN_DATE));
            Date date = new Date(secFromEpoch*1000);
            holder.when.setText(sdf.format(date));
        } else {
            holder.whenImg.setImageResource(R.drawable.ic_timeline_black_24dp);
            holder.whenImg.setImageTintList(ColorStateList.valueOf(mContext.getColor(R.color.secondaryTextColor)));
            holder.when.setText(mCursor.getInt(mCursor.getColumnIndex(FuelDietContract.ReminderEntry.COLUMN_ODO))+"km");
        }

        String title = mCursor.getString(mCursor.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_TITLE));
        String desc = mCursor.getString(mCursor.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_DETAILS));
        long id = mCursor.getLong(mCursor.getColumnIndex(FuelDietContract.CostsEntry._ID));

        if (desc == null || desc.equals("")) {
            holder.desc.setVisibility(View.GONE);
            holder.descImg.setVisibility(View.GONE);
            holder.divider.setVisibility(View.GONE);
        } else {
            holder.desc.setText(desc);
        }
        holder.title.setText(title);
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
