package com.fueldiet.fueldiet.adapter;

import android.content.Context;
import android.content.res.Configuration;
import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.Utils;
import com.fueldiet.fueldiet.object.CostObject;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;

/**
 * Adapter for Cost Recycler View
 */
public class CostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final String TAG = "CostAdapter";

    private Locale locale;
    private OnItemClickListener mListener;
    private Context mContext;
    private List<CostObject> costObjects;

    private static final int TYPE_WITH_DESC = 1;
    private static final int TYPE_NO_DESC = 0;

    public CostAdapter(Context context, List<CostObject> list) {
        mContext = context;
        costObjects = list;
        Configuration configuration = context.getResources().getConfiguration();
        locale = configuration.getLocales().get(0);
    }

    public interface OnItemClickListener {
        void onEditClick(int position, long element_id);
        void onDeleteClick(int position, long element_id);
    }

    public void setOnItemClickListener(CostAdapter.OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_WITH_DESC)
            return new CostWithDescViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_template_cost_with_desc, parent, false));
        else
            return new CostNoDescViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_template_cost_no_desc, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (costObjects.size() <= position)
            return;

        if (getItemViewType(position) == TYPE_WITH_DESC)
            ((CostWithDescViewHolder) holder).setUp(position);
        else
            ((CostNoDescViewHolder) holder).setUp(position);
    }

    @Override
    public int getItemViewType(int position) {
        String d = costObjects.get(position).getDetails();
        if (costObjects.get(position).getDetails() == null)
            return TYPE_NO_DESC;
        else
            return TYPE_WITH_DESC;
    }

    class CostWithDescViewHolder extends RecyclerView.ViewHolder {

        public TextView dateTime, odo, title, price, desc, type;
        public ImageView descImg;
        MaterialButton more;


        CostWithDescViewHolder(final View itemView) {
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


        void setUp(int position) {

            CostObject costObject = costObjects.get(position);

            /* popup menu */
            more.setOnClickListener(view -> {
                //creating a popup menu
                PopupMenu popup = new PopupMenu(mContext, more);
                //inflating menu from xml resource
                popup.inflate(R.menu.cost_card_menu);
                //adding click listener
                popup.setOnMenuItemClickListener(item -> {
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
                });
                //displaying the popup
                popup.show();
            });


            desc.setText(costObject.getDetails());

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", locale);
            dateTime.setText(dateFormat.format(costObject.getDate().getTime()));
            odo.setText(String.format(locale, "%d", costObject.getKm()));
            title.setText(costObject.getTitle());

            if (locale.getLanguage().equals("sl"))
                type.setText(Utils.fromENGtoSLO(costObject.getType()));
            else
                type.setText(costObject.getType());
            double priceValue = costObject.getCost();
            if (priceValue + 80085 == 0)
                price.setText(mContext.getString(R.string.warranty));
            else if (priceValue < 0.0)
                price.setText(String.format(locale, "%+.2f€", Math.abs(priceValue)));
            else
                price.setText(String.format(locale, "%+.2f€", priceValue * -1));
            itemView.setTag(costObject.getCostID());
        }
    }

    class CostNoDescViewHolder extends RecyclerView.ViewHolder {

        public TextView dateTime, odo, title, price, type;
        MaterialButton more;


        CostNoDescViewHolder(final View itemView) {
            super(itemView);
            dateTime = itemView.findViewById(R.id.costs_date);
            odo = itemView.findViewById(R.id.costs_odo);
            title = itemView.findViewById(R.id.costs_title);
            price = itemView.findViewById(R.id.costs_price);
            type = itemView.findViewById(R.id.costs_type);
            more = itemView.findViewById(R.id.costs_more);
        }


        void setUp(int position) {
            CostObject costObject = costObjects.get(position);

            /* popup menu */
            more.setOnClickListener(view -> {
                //creating a popup menu
                PopupMenu popup = new PopupMenu(mContext, more);
                //inflating menu from xml resource
                popup.inflate(R.menu.cost_card_menu);
                //adding click listener
                popup.setOnMenuItemClickListener(item -> {
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
                });
                //displaying the popup
                popup.show();
            });

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", locale);
            dateTime.setText(dateFormat.format(costObject.getDate().getTime()));
            odo.setText(String.format(locale, "%d", costObject.getKm()));
            title.setText(costObject.getTitle());

            if (locale.getLanguage().equals("sl"))
                type.setText(Utils.fromENGtoSLO(costObject.getType()));
            else
                type.setText(costObject.getType());
            double priceValue = costObject.getCost();
            if (priceValue + 80085 == 0)
                price.setText(mContext.getString(R.string.warranty));
            else if (priceValue < 0.0)
                price.setText(String.format(locale, "%+.2f€", Math.abs(priceValue)));
            else
                price.setText(String.format(locale, "%+.2f€", priceValue * -1));
            itemView.setTag(costObject.getCostID());
        }
    }

    @Override
    public int getItemCount() {
        return costObjects.size();
    }
}
