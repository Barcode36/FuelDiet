package com.example.fueldiet.Adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fueldiet.R;
import com.example.fueldiet.db.FuelDietContract;

import java.util.Date;

public class ReminderDoneAdapter extends RecyclerView.Adapter<ReminderDoneAdapter.ReminderViewHolder>{

    private ReminderDoneAdapter.OnItemClickListener mListener;
    private Context mContext;
    private Cursor mCursor;

    public ReminderDoneAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
    }

    public interface OnItemClickListener {
        void onItemClick(long element_id);
    }

    public void setOnItemClickListener(ReminderDoneAdapter.OnItemClickListener listener) {
        mListener = listener;
    }

    public static class ReminderViewHolder extends RecyclerView.ViewHolder {

        public TextView date;
        public TextView km;
        public TextView title;
        public TextView desc;

        public View divider;
        public ImageView descImg;


        public ReminderViewHolder(final View itemView, final ReminderDoneAdapter.OnItemClickListener listener) {
            super(itemView);
            date = itemView.findViewById(R.id.reminder_done_date_template);
            km = itemView.findViewById(R.id.reminder_done_km_template);
            title = itemView.findViewById(R.id.reminder_done_title_template);
            desc = itemView.findViewById(R.id.reminder_done_desc_template);

            descImg = itemView.findViewById(R.id.reminder_done_details_img);
            divider = itemView.findViewById(R.id.reminder_done_break_template);
        }
    }

    @Override
    public ReminderDoneAdapter.ReminderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.reminder_done_template, parent, false);
        return new ReminderDoneAdapter.ReminderViewHolder(v, mListener);
    }


    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        if (!mCursor.moveToPosition(position)) {
            return;
        }

        final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        long secFromEpoch = mCursor.getLong(mCursor.getColumnIndex(FuelDietContract.ReminderEntry.COLUMN_DATE));
        Date date = new Date(secFromEpoch*1000);
        holder.date.setText(sdf.format(date));
        holder.km.setText(mCursor.getInt(mCursor.getColumnIndex(FuelDietContract.ReminderEntry.COLUMN_ODO)));

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
