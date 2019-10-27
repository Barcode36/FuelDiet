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

import com.example.fueldiet.Object.ReminderObject;
import com.example.fueldiet.R;
import com.example.fueldiet.db.FuelDietContract;

import java.util.Date;
import java.util.List;

public class ReminderMultipleTypeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private ReminderMultipleTypeAdapter.OnItemClickListener mListener;
    private Context mContext;
    private List<ReminderObject> reminderList;
    private final static int TYPE_ACTIVE = 0;
    private final static int TYPE_DONE = 1;

    public ReminderMultipleTypeAdapter(Context context, List<ReminderObject> reminderObjectList) {
        mContext = context;
        reminderList = reminderObjectList;
    }

    public interface OnItemClickListener {
        void onItemClick(long element_id);
    }

    public void setOnItemClickListener(ReminderMultipleTypeAdapter.OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ACTIVE) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.reminder_template, parent, false);
            return new ActiveViewHolder(v, mListener);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.reminder_done_template, parent, false);
            return new DoneViewHolder(v, mListener);
        }
        //return new ReminderMultipleTypeAdapter.ReminderViewHolder(v, mListener);
    }

    @Override
    public int getItemViewType(int position) {
        if (reminderList.get(position).isActive())
            return TYPE_ACTIVE;
        else
            return TYPE_DONE;
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_ACTIVE) {
            ((ActiveViewHolder) holder).setActiveDetails(reminderList.get(position));
        } else {
            ((DoneViewHolder) holder).setDoneDetails(reminderList.get(position));
        }


    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    class ActiveViewHolder extends RecyclerView.ViewHolder {
        public TextView when;
        public TextView title;
        public TextView desc;

        public ImageView whenImg;
        public View divider;
        public ImageView edit;
        public ImageView remove;
        public ImageView descImg;


        ActiveViewHolder(final View itemView, final ReminderMultipleTypeAdapter.OnItemClickListener listener) {
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

        void setActiveDetails(ReminderObject ro) {
            Integer km = ro.getKm();
            if (km == null) {
                final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                whenImg.setImageResource(R.drawable.ic_today_black_24dp);
                whenImg.setImageTintList(ColorStateList.valueOf(mContext.getColor(R.color.secondaryTextColor)));
                Date date = ro.getDate();
                when.setText(sdf.format(date));
            } else {
                whenImg.setImageResource(R.drawable.ic_timeline_black_24dp);
                whenImg.setImageTintList(ColorStateList.valueOf(mContext.getColor(R.color.secondaryTextColor)));
                when.setText(ro.getKm()+"km");
            }

            String titleString = ro.getTitle();
            String descString = ro.getDesc();
            int id = ro.getId();

            if (descString == null || descString.equals("")) {
                desc.setVisibility(View.GONE);
                descImg.setVisibility(View.GONE);
                divider.setVisibility(View.GONE);
            } else {
                desc.setText(descString);
            }
            title.setText(titleString);
            itemView.setTag(id);
        }
    }

    class DoneViewHolder extends RecyclerView.ViewHolder {

        public TextView date;
        public TextView km;
        public TextView title;
        public TextView desc;

        public View divider;
        public ImageView descImg;


        DoneViewHolder(final View itemView, final ReminderMultipleTypeAdapter.OnItemClickListener listener) {
            super(itemView);
            date = itemView.findViewById(R.id.reminder_done_date_template);
            km = itemView.findViewById(R.id.reminder_done_km_template);
            title = itemView.findViewById(R.id.reminder_done_title_template);
            desc = itemView.findViewById(R.id.reminder_done_desc_template);

            descImg = itemView.findViewById(R.id.reminder_done_details_img);
            divider = itemView.findViewById(R.id.reminder_done_break_template);
        }

        void setDoneDetails(ReminderObject ro) {
            final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            Date dateF = ro.getDate();
            date.setText(sdf.format(dateF));
            km.setText(ro.getKm()+"km");

            String titleS = ro.getTitle();
            String descS = ro.getDesc();
            int id = ro.getId();

            if (descS == null || descS.equals("")) {
                desc.setVisibility(View.GONE);
                descImg.setVisibility(View.GONE);
                divider.setVisibility(View.GONE);
            } else {
                desc.setText(descS);
            }
            title.setText(titleS);
            itemView.setTag(id);
        }
    }
}
