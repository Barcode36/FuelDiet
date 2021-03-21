package com.fueldiet.fueldiet.adapter;

import android.content.Context;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.object.CostItemObject;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;

/**
 * Adapter for Cost Item Recycler View
 */
public class CostItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "CostItemAdapter";

    private OnItemClickListener mListener;
    private List<CostItemObject> costItemObjects;
    private Context context;
    private static final int TYPE_EDIT = 0;
    private static final int TYPE_NO_EDIT = 1;
    private final Locale locale;

    private final int location;

    public CostItemAdapter(Context context, List<CostItemObject> costItemObjects, int location) {
        this.costItemObjects = costItemObjects;
        this.location = location;
        this.context = context;
        Configuration configuration = context.getResources().getConfiguration();
        this.locale = configuration.getLocales().get(0);
    }

    public interface OnItemClickListener {
        void onEditItem(long costItemId);
        void onDeleteItem(long costItemId);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_EDIT)
            return new CostItemWithEdit(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_template_cost_item_edit, parent, false), mListener);
        else
            return new CostItemWithoutEdit(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_template_cost_item, parent, false));

    }

    @Override
    public int getItemViewType(int position) {
        if (location == 0) {
            //add / edit activity
            return TYPE_EDIT;
        } else {
            return TYPE_NO_EDIT;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position >= costItemObjects.size())
            return;

        if (getItemViewType(position) == TYPE_EDIT) {
            ((CostItemWithEdit) holder).setUp(position);
        } else {
            ((CostItemWithoutEdit) holder).setUp(position);
        }
    }

    class CostItemWithEdit extends RecyclerView.ViewHolder {

        public TextView name;
        public TextView price;
        public TextView desc;
        public MaterialButton more;


        CostItemWithEdit(final View itemView, OnItemClickListener listener) {
            super(itemView);
            name = itemView.findViewById(R.id.cost_item_title);
            price = itemView.findViewById(R.id.cost_item_price);
            desc = itemView.findViewById(R.id.cost_item_desc);
            more = itemView.findViewById(R.id.cost_item_more);
        }

        void setUp(int position) {
            CostItemObject costItemObject = costItemObjects.get(position);

            /* popup menu */
            more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //creating a popup menu
                    PopupMenu popup = new PopupMenu(context, more);
                    //inflating menu from xml resource
                    popup.inflate(R.menu.cost_item_card_menu);
                    //adding click listener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if (item.getItemId() == R.id.edit_cost_item) {
                                //handle menu1 click
                                mListener.onEditItem(costItemObject.getCostItemID());
                                return true;
                            } else if (item.getItemId() ==R.id.delete_cost_item) {
                                //handle menu2 click
                                mListener.onDeleteItem(costItemObject.getCostItemID());
                                return true;
                            } else {
                                return false;
                            }
                        }
                    });
                    //displaying the popup
                    popup.show();
                }
            });

            name.setText(costItemObject.getName());
            desc.setText(costItemObject.getDescription());
            if (!costItemObject.getDescription().isEmpty()) {
                desc.setVisibility(View.VISIBLE);
            }
            price.setText(String.format(locale, "%.2f€", costItemObject.getPrice()));
        }
    }

    class CostItemWithoutEdit extends RecyclerView.ViewHolder {

        public TextView name;
        public TextView price;
        public TextView desc;


        CostItemWithoutEdit(final View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.cost_item_title);
            price = itemView.findViewById(R.id.cost_item_price);
            desc = itemView.findViewById(R.id.cost_item_desc);
        }

        void setUp(int position) {
            CostItemObject costItemObject = costItemObjects.get(position);

            name.setText(costItemObject.getName());
            desc.setText(costItemObject.getDescription());
            if (!costItemObject.getDescription().isEmpty()) {
                desc.setVisibility(View.VISIBLE);
            }
            price.setText(String.format(locale, "%.2f€", costItemObject.getPrice()));
        }
    }

    @Override
    public int getItemCount() {
        return costItemObjects.size();
    }
}
