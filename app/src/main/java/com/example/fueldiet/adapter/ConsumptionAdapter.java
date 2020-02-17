package com.example.fueldiet.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.icu.text.SimpleDateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.PopupMenu;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fueldiet.object.DriveObject;
import com.example.fueldiet.R;
import com.example.fueldiet.Utils;
import com.example.fueldiet.db.FuelDietDBHelper;
import com.github.mikephil.charting.formatter.IFillFormatter;

import java.util.Date;
import java.util.List;

/**
 * Adapter for Consumption Recycler View
 */
public class ConsumptionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private OnItemClickListener mListener;
    private Context mContext;
    private List<DriveObject> mDrives;
    private final static int TYPE_FINISH = 0;
    private final static int TYPE_UNFINISH = 1;

    public ConsumptionAdapter(Context context, List<DriveObject> driveObjectList) {
        mContext = context;
        mDrives = driveObjectList;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, long driveID, int option);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_FINISH)
            return new ConsumptionDoneViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_template_consumption, parent, false), mListener);
        else
            return new ConsumptionRelatedViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_template_related_consumption, parent, false), mListener);

    }

    @Override
    public int getItemViewType(int position) {
        if (mDrives.get(position).getNotFull() == 1 || mDrives.get(position).getFirst() == 1)
            return TYPE_UNFINISH;
        else
            return TYPE_FINISH;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position >= mDrives.size())
            return;

        if (getItemViewType(position) == TYPE_FINISH) {
            ((ConsumptionDoneViewHolder) holder).setUp(position);
        } else {
            ((ConsumptionRelatedViewHolder) holder).setUp(position);
        }
    }

    class ConsumptionDoneViewHolder extends RecyclerView.ViewHolder {

        public TextView date;
        public TextView time;
        public TextView odo;
        public TextView trip;
        public TextView litres;
        public TextView cons;
        public TextView unit_cons;
        public TextView price_l;
        public TextView price_full;

        public ImageView fuel_drop;
        public ImageView fuel_trend;
        public ImageView petrol_station;

        public ImageView more;

        ConsumptionDoneViewHolder(final View itemView, final ConsumptionAdapter.OnItemClickListener listener) {
            super(itemView);
            date = itemView.findViewById(R.id.consumption_view_day);
            time = itemView.findViewById(R.id.consumption_view_time);
            odo = itemView.findViewById(R.id.consumption_view_odo);
            trip = itemView.findViewById(R.id.consumption_view_trip);
            cons = itemView.findViewById(R.id.consumption_view_cons);
            litres = itemView.findViewById(R.id.consumption_view_litres);
            unit_cons = itemView.findViewById(R.id.consumption_view_cons_unit);
            price_l = itemView.findViewById(R.id.consumption_view_e_p_l);
            price_full = itemView.findViewById(R.id.consumption_view_total_price);

            fuel_drop = itemView.findViewById(R.id.consumption_img);
            fuel_trend = itemView.findViewById(R.id.consumption_view_fuel_up_down);
            petrol_station = itemView.findViewById(R.id.consumption_view_petrol_station_logo);

            more = itemView.findViewById(R.id.consumption_more);
        }

        void setUp(int position) {
            int firstF = mDrives.get(position).getFirst();
            int notFull = mDrives.get(position).getNotFull();

            Date dateV = mDrives.get(position).getDate().getTime();
            int odo_km =mDrives.get(position).getOdo();
            int trip_km = mDrives.get(position).getTrip();
            double liters = mDrives.get(position).getLitres();
            double pricePerLitre = mDrives.get(position).getCostPerLitre();
            double consumption;
            if (firstF == 1 || notFull == 1)
                consumption = 0;
            else
                consumption = Utils.calculateConsumption(trip_km, liters);
            String station = mDrives.get(position).getPetrolStation();

            long id = mDrives.get(position).getId();

            /* popup menu */
            more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //creating a popup menu
                    PopupMenu popup = new PopupMenu(mContext, more);
                    //inflating menu from xml resource
                    popup.inflate(R.menu.consumption_card_menu);
                    //adding click listener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.edit_cons:
                                    //handle menu1 click
                                    mListener.onItemClick(position, id, 0);
                                    return true;
                                case R.id.delete_cons:
                                    //handle menu2 click
                                    mListener.onItemClick(position, id, 1);
                                    return true;
                                case R.id.show_note:
                                    //open dialog with note
                                    mListener.onItemClick(position, id, 2);
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

            FuelDietDBHelper dbHelper = new FuelDietDBHelper(mContext);
            DriveObject driveObject = dbHelper.getPrevDriveSelection(mDrives.get(position).getCarID(), mDrives.get(position).getDateEpoch());

            /* Display correct colour and units */

            if (driveObject != null) {
                double prev;
                if (driveObject.getFirst() == 1) {
                    prev = consumption;
                } else if (driveObject.getNotFull() == 1 && notFull == 0) {
                    DriveObject halfs = driveObject;
                    int addKM = 0;
                    double addL = 0.0;
                    do {
                        addKM += halfs.getTrip();
                        addL += halfs.getLitres();
                        halfs = dbHelper.getPrevDriveSelection(halfs.getCarID(), halfs.getDateEpoch());
                    } while (halfs.getNotFull() == 1 && halfs.getFirst() == 0);

                    if (firstF == 1) {
                        consumption = 0;
                    } else {
                        consumption = Utils.calculateConsumption(trip_km+addKM, liters+addL);
                    }

                    if (halfs.getFirst() == 1)
                        prev = consumption;
                    else
                        prev = Utils.calculateConsumption(halfs.getTrip(), halfs.getLitres());
                } else {
                    prev = Utils.calculateConsumption(driveObject.getTrip(), driveObject.getLitres());
                }

                //cons
                if (firstF == 0 && notFull == 0) {
                    if (Double.compare(prev, consumption) > 0)
                        fuel_drop.setImageTintList(ColorStateList.valueOf(mContext.getColor(R.color.green)));
                    else if (Double.compare(prev, consumption) < 0)
                        fuel_drop.setImageTintList(ColorStateList.valueOf(mContext.getColor(R.color.red)));
                    else
                        fuel_drop.setImageTintList(ColorStateList.valueOf(mContext.getColor(R.color.yellow)));
                    if (PreferenceManager.getDefaultSharedPreferences(mContext).getString("selected_unit", "litres_per_km").equals("litres_per_km")){
                        cons.setText(Double.toString(consumption));
                        unit_cons.setText(R.string.l_per_100_km_unit);
                    } else {
                        Log.i("Consumption", "From "+consumption+" l/100km to " + Utils.convertUnitToKmPL(consumption) + " km/l");
                        cons.setText(Double.toString(Utils.convertUnitToKmPL(consumption)));
                        unit_cons.setText(R.string.km_per_l_unit);
                    }
                    trip.setText(String.format("+%d km", trip_km));
                } else {
                    if (firstF == 1) {
                        //no cons, no trip
                        trip.setVisibility(View.INVISIBLE);
                        unit_cons.setVisibility(View.INVISIBLE);
                        fuel_drop.setVisibility(View.INVISIBLE);
                        cons.setText(mContext.getString(R.string.first_fueling));
                    } else {
                        //no cons, yes trip
                        cons.setText(mContext.getString(R.string.not_full));
                        unit_cons.setVisibility(View.INVISIBLE);
                        fuel_drop.setVisibility(View.INVISIBLE);
                        trip.setText(String.format("+%d km", trip_km));
                    }
                }
                //price
                if (Double.compare(driveObject.getCostPerLitre(), pricePerLitre) > 0) {
                    fuel_trend.setImageTintList(ColorStateList.valueOf(mContext.getColor(R.color.green)));
                    fuel_trend.setImageResource(R.drawable.ic_expand_more_black_24dp);
                } else if (Double.compare(driveObject.getCostPerLitre(), pricePerLitre) < 0) {
                    fuel_trend.setImageTintList(ColorStateList.valueOf(mContext.getColor(R.color.red)));
                    fuel_trend.setImageResource(R.drawable.ic_expand_less_black_24dp);
                } else {
                    fuel_trend.setImageTintList(ColorStateList.valueOf(mContext.getColor(R.color.yellow)));
                    fuel_trend.setImageResource(R.drawable.ic_unfold_less_black_24dp);
                }
            } else {
                if (firstF == 1) {
                    //no cons, no trip
                    trip.setVisibility(View.INVISIBLE);
                    unit_cons.setVisibility(View.INVISIBLE);
                    fuel_drop.setVisibility(View.INVISIBLE);
                    cons.setText(mContext.getString(R.string.first_fueling));
                } else {
                    //no cons, yes trip
                    cons.setText(mContext.getString(R.string.not_full));
                    unit_cons.setVisibility(View.INVISIBLE);
                    fuel_drop.setVisibility(View.INVISIBLE);
                    trip.setText(String.format("+%d km", trip_km));
                }
            }

            if (station.equals("Other"))
                Glide.with(mContext).load(mContext.getDrawable(R.drawable.ic_help_outline_black_24dp)).into(petrol_station);
            else
                Glide.with(mContext).load(mContext.getResources().getIdentifier(station.toLowerCase(), "drawable", mContext.getPackageName())).fitCenter().into(petrol_station);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy-HH:mm");
            date.setText(dateFormat.format(dateV).split("-")[0]);
            time.setText(dateFormat.format(dateV).split("-")[1]);
            odo.setText(String.format("%d km", odo_km));
            litres.setText(String.format("%s l", liters));


            itemView.setTag(id);
            price_l.setText(String.format("%s €/l", Double.toString(pricePerLitre)));
            price_full.setText(String.format("%s €", Double.toString(Utils.calculateFullPrice(pricePerLitre, liters))));
        }
    }

    class ConsumptionRelatedViewHolder extends RecyclerView.ViewHolder {

        public TextView date;
        public TextView time;
        public TextView odo;
        public TextView why;
        public TextView trip;
        public TextView litres;
        public TextView price_l;
        public TextView price_full;

        public ImageView fuel_trend;
        public ImageView petrol_station;
        public ImageView more;

        ConsumptionRelatedViewHolder(final View itemView, final ConsumptionAdapter.OnItemClickListener listener) {
            super(itemView);
            date = itemView.findViewById(R.id.consumption_view_day);
            time = itemView.findViewById(R.id.consumption_view_time);
            odo = itemView.findViewById(R.id.consumption_view_odo);
            why = itemView.findViewById(R.id.consumption_why);
            trip = itemView.findViewById(R.id.consumption_view_trip);
            litres = itemView.findViewById(R.id.consumption_view_litres);
            price_l = itemView.findViewById(R.id.consumption_view_e_p_l);
            price_full = itemView.findViewById(R.id.consumption_view_total_price);

            fuel_trend = itemView.findViewById(R.id.consumption_view_fuel_up_down);
            petrol_station = itemView.findViewById(R.id.consumption_view_petrol_station_logo);
            more = itemView.findViewById(R.id.consumption_more);
        }

        void setUp(int position) {
            int firstF = mDrives.get(position).getFirst();
            int notFull = mDrives.get(position).getNotFull();

            Date dateV = mDrives.get(position).getDate().getTime();
            int odo_km =mDrives.get(position).getOdo();
            int trip_km =mDrives.get(position).getTrip();
            double liters = mDrives.get(position).getLitres();
            double pricePerLitre = mDrives.get(position).getCostPerLitre();
            String station = mDrives.get(position).getPetrolStation();

            long id = mDrives.get(position).getId();

            /* popup menu */
            more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //creating a popup menu
                    PopupMenu popup = new PopupMenu(mContext, more);
                    //inflating menu from xml resource
                    popup.inflate(R.menu.consumption_card_menu);
                    //adding click listener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.edit_cons:
                                    //handle menu1 click
                                    mListener.onItemClick(position, id, 0);
                                    return true;
                                case R.id.delete_cons:
                                    //handle menu2 click
                                    mListener.onItemClick(position, id, 1);
                                    return true;
                                case R.id.show_note:
                                    //open dialog with note
                                    mListener.onItemClick(position, id, 2);
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

            FuelDietDBHelper dbHelper = new FuelDietDBHelper(mContext);
            DriveObject driveObject = dbHelper.getPrevDriveSelection(mDrives.get(position).getCarID(), mDrives.get(position).getDateEpoch());

            /* Display correct colour and units */
            //price
            if (driveObject == null) {
                fuel_trend.setImageTintList(ColorStateList.valueOf(mContext.getColor(R.color.yellow)));
                fuel_trend.setImageResource(R.drawable.ic_unfold_less_black_24dp);
            } else {
                if (Double.compare(driveObject.getCostPerLitre(), pricePerLitre) > 0) {
                    fuel_trend.setImageTintList(ColorStateList.valueOf(mContext.getColor(R.color.green)));
                    fuel_trend.setImageResource(R.drawable.ic_expand_more_black_24dp);
                } else if (Double.compare(driveObject.getCostPerLitre(), pricePerLitre) < 0) {
                    fuel_trend.setImageTintList(ColorStateList.valueOf(mContext.getColor(R.color.red)));
                    fuel_trend.setImageResource(R.drawable.ic_expand_less_black_24dp);
                } else {
                    fuel_trend.setImageTintList(ColorStateList.valueOf(mContext.getColor(R.color.yellow)));
                    fuel_trend.setImageResource(R.drawable.ic_unfold_less_black_24dp);
                }
            }


            if (station.equals("Other"))
                Glide.with(mContext).load(mContext.getDrawable(R.drawable.ic_help_outline_black_24dp)).into(petrol_station);
            else
                Glide.with(mContext).load(mContext.getResources().getIdentifier(station.toLowerCase(), "drawable", mContext.getPackageName())).fitCenter().into(petrol_station);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy-HH:mm");
            date.setText(dateFormat.format(dateV).split("-")[0]);
            time.setText(dateFormat.format(dateV).split("-")[1]);
            odo.setText(String.format("%d km", odo_km));
            trip.setText(String.format("+ %d km", trip_km));
            litres.setText(String.format("%s l", liters));
            if (firstF == 1)
                why.setText(mContext.getString(R.string.first_fueling));
            else
                why.setText(mContext.getString(R.string.not_full));

            itemView.setTag(id);
            price_l.setText(String.format("%s €/l", Double.toString(pricePerLitre)));
            price_full.setText(String.format("%s €", Double.toString(Utils.calculateFullPrice(pricePerLitre, liters))));
        }
    }

    @Override
    public int getItemCount() {
        return mDrives.size();
    }
}