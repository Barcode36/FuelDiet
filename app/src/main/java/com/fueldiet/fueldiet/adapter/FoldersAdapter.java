package com.fueldiet.fueldiet.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fueldiet.fueldiet.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class FoldersAdapter extends RecyclerView.Adapter<FoldersAdapter.FoldersViewHolder> {

    private FoldersAdapter.OnItemClickListener mListener;
    private Context mContext;
    private List<File> fileObjects;

    public FoldersAdapter(Context context, List<File> list) {
        mContext = context;
        fileObjects = list;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(FoldersAdapter.OnItemClickListener listener) {
        mListener = listener;
    }

    public static class FoldersViewHolder extends RecyclerView.ViewHolder {

        public TextView date, name;
        public Button restore;
        public ImageView item;


        public FoldersViewHolder(final View itemView, final FoldersAdapter.OnItemClickListener listener) {
            super(itemView);
            date = itemView.findViewById(R.id.file_date);
            name = itemView.findViewById(R.id.file_name);
            item = itemView.findViewById(R.id.file_icon);
            restore = itemView.findViewById(R.id.button_restore);
        }
    }

    @Override
    public FoldersAdapter.FoldersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_template_backup_and_restore, parent, false);
        return new FoldersAdapter.FoldersViewHolder(v, mListener);
    }


    @Override
    public void onBindViewHolder(@NonNull FoldersViewHolder holder, int position) {
        if (position >= getItemCount()) {
            return;
        }

        File file = fileObjects.get(position);
        long epoch = file.lastModified();
        Date d = new Date(epoch);
        SimpleDateFormat sdf = new SimpleDateFormat("dd. MM. yyyy, HH:mm");
        holder.date.setText(sdf.format(d));
        holder.name.setText(file.getName());

        holder.restore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return fileObjects.size();
    }
}
