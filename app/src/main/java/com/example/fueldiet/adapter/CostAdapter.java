package com.example.fueldiet.adapter;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fueldiet.object.CostObject;
import com.example.fueldiet.R;
import com.example.fueldiet.Utils;

import java.util.List;

/**
 * Adapter for Cost Recycler View
 */
public class CostAdapter extends RecyclerView.Adapter<CostAdapter.CostViewHolder>{

    private CostAdapter.OnItemClickListener mListener;
    private Context mContext;
    private List<CostObject> costObjects;

    public CostAdapter(Context context, List<CostObject> list) {
        mContext = context;
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
        ImageView more;


        public CostViewHolder(final View itemView, final CostAdapter.OnItemClickListener listener) {
            super(itemView);
            dateTime = itemView.findViewById(R.id.costs_date);
            odo = itemView.findViewById(R.id.costs_odo);
            title = itemView.findViewById(R.id.costs_title);
            price = itemView.findViewById(R.id.costs_price);
            desc = itemView.findViewById(R.id.costs_desc);
            descImg = itemView.findViewById(R.id.cost_details_img);
            type = itemView.findViewById(R.id.costs_type);
            more = itemView.findViewById(R.id.costs_more);
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

        CostObject costObject = costObjects.get(position);

        /* popup menu */
        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //creating a popup menu
                PopupMenu popup = new PopupMenu(mContext, holder.more);
                //inflating menu from xml resource
                popup.inflate(R.menu.cost_card_menu);
                //adding click listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.edit:
                                mListener.onEditClick(position, costObject.getCostID());
                                return true;
                            case R.id.delete:
                                mListener.onDeleteClick(position, costObject.getCostID());
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                //displaying the popup
                popup.show();
            }
        });

        if (costObject.getDetails() == null) {
            /*RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)holder.price.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_TOP, R.id.costs_date);
            holder.price.setLayoutParams(params);*/
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
        holder.price.setText(costObject.getCost() +"€");
        holder.itemView.setTag(costObject.getCostID());
    }

    @Override
    public int getItemCount() {
        return costObjects.size();
    }
}
