package com.fueldiet.fueldiet.adapter;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fueldiet.fueldiet.R;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FoldersAdapter extends RecyclerView.Adapter<FoldersAdapter.FoldersViewHolder> {

    private FoldersAdapter.OnItemClickListener mListener;
    private Context mContext;
    private List<File> fileObjects;
    private Locale locale;

    public FoldersAdapter(Context context, List<File> list) {
        mContext = context;
        fileObjects = list;
        Configuration configuration = mContext.getResources().getConfiguration();
        locale = configuration.getLocales().get(0);
    }

    public interface OnItemClickListener {
        void onItemClickOld(int position);
        void onItemClickNew(int position);
    }

    public void setOnItemClickListener(FoldersAdapter.OnItemClickListener listener) {
        mListener = listener;
    }

    public static class FoldersViewHolder extends RecyclerView.ViewHolder {

        public TextView date, name;
        public MaterialButton restore;
        public ImageView item;


        public FoldersViewHolder(final View itemView, final FoldersAdapter.OnItemClickListener listener) {
            super(itemView);
            date = itemView.findViewById(R.id.file_date);
            name = itemView.findViewById(R.id.file_name);
            item = itemView.findViewById(R.id.file_icon);
            restore = itemView.findViewById(R.id.button_restore);
        }
    }

    @NonNull
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
        SimpleDateFormat sdf = new SimpleDateFormat("dd. MM. yyyy, HH:mm", locale);
        holder.date.setText(sdf.format(d));
        holder.name.setText(file.getName());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //user has the option of save file and / or restore
            holder.restore.setText(mContext.getString(R.string.options));
            holder.restore.setOnClickListener(v -> mListener.onItemClickNew(position));
        } else {
            //the only option is restore
            holder.restore.setOnClickListener(v -> mListener.onItemClickOld(position));
        }
    }

    @Override
    public int getItemCount() {
        return fileObjects.size();
    }
}
