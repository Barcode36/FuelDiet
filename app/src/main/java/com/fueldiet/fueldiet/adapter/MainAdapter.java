package com.fueldiet.fueldiet.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.Utils;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.fueldiet.fueldiet.fragment.MainFragment;
import com.fueldiet.fueldiet.object.CostObject;
import com.fueldiet.fueldiet.object.DriveObject;
import com.fueldiet.fueldiet.object.VehicleObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private OnItemClickListener mListener;

    private Context mContext;
    private List<Object> objectsList;
    private FuelDietDBHelper dbHelper;
    private final int TYPE_SPINNER = 0;
    private final int TYPE_TITLE = 1;
    private final int TYPE_DATA_FUEL = 2;
    private final int TYPE_DATA_COST = 3;
    private final int TYPE_DATA_ENTRY = 4;
    private final String KMPL = "km/l";
    private Locale locale;

    public MainAdapter(Context context, List<Object> vehicles, FuelDietDBHelper dbHelper) {
        mContext = context;
        objectsList = vehicles;
        this.dbHelper = dbHelper;
        Configuration configuration = context.getResources().getConfiguration();
        locale = configuration.getLocales().get(0);
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onItemSelected(long vehicleID);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    /* checks which type it is */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_SPINNER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_template_spinner, parent, false);
            return new SpinnerViewHolder(v, mListener);
        } else if (viewType == TYPE_TITLE) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_template_type_title, parent, false);
            return new TitleViewHolder(v, mListener);
        } else if (viewType == TYPE_DATA_FUEL){
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_template_type_data_fuel, parent, false);
            return new DataViewHolder(v, mListener, viewType);
        } else if (viewType == TYPE_DATA_COST){
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_template_type_data_cost, parent, false);
            return new DataViewHolder(v, mListener, viewType);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_template_type_data_entry, parent, false);
            return new DataViewHolder(v, mListener, viewType);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (objectsList.get(position) instanceof SpinnerAdapter)
            return TYPE_SPINNER;
        else if (objectsList.get(position) instanceof MainFragment.TitleType)
            return TYPE_TITLE;
        else if (position == 2)
            return TYPE_DATA_FUEL;
        else if (position == 4)
            return TYPE_DATA_COST;
        else
            return TYPE_DATA_ENTRY;
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_SPINNER) {
            ((SpinnerViewHolder) holder).setUp(objectsList.get(position));
        } else if (getItemViewType(position) == TYPE_TITLE) {
            ((TitleViewHolder) holder).setUp(objectsList.get(position));
        } else if (getItemViewType(position) == TYPE_DATA_FUEL) {
            ((DataViewHolder) holder).setUpFuel(objectsList.get(position));
        } else if (getItemViewType(position) == TYPE_DATA_COST) {
            ((DataViewHolder) holder).setUpCost(objectsList.get(position));
        } else if (getItemViewType(position) == TYPE_DATA_ENTRY) {
            ((DataViewHolder) holder).setUpEntry(objectsList.get(position));
        }
    }

    class SpinnerViewHolder extends RecyclerView.ViewHolder {
        Spinner spinner;

        SpinnerViewHolder(final View itemView, final OnItemClickListener listener) {
            super(itemView);
            spinner = itemView.findViewById(R.id.vehicle_select);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    VehicleObject clickedItem = (VehicleObject) parent.getItemAtPosition(position);
                    mListener.onItemSelected(clickedItem.getId());
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

        void setUp(Object object) {
            //VehicleSelectAdapter adapter = (VehicleSelectAdapter) object;
            //spinner.setAdapter(adapter);

            ArrayList<VehicleObject> vehicles = new ArrayList<>();
            List<VehicleObject> vehicleObjects = dbHelper.getAllVehicles();

            if (vehicleObjects == null || vehicleObjects.size() == 0) {
                vehicles.add(new VehicleObject(mContext.getString(R.string.no_vehicle), mContext.getString(R.string.added), -1));
                VehicleSelectAdapter spinnerAdapter = new VehicleSelectAdapter(mContext, vehicles);
                spinner.setAdapter(spinnerAdapter);
            } else {
                vehicles.addAll(vehicleObjects);
                VehicleSelectAdapter spinnerAdapter = new VehicleSelectAdapter(mContext, vehicles);
                spinner.setAdapter(spinnerAdapter);

                long vehicleID = (long)objectsList.get(2);
                int pos = 0;
                if (vehicleID != -1) {
                    for (int i = 0; i < spinnerAdapter.list.size(); i++) {
                        if (spinnerAdapter.list.get(i).getId() == vehicleID) {
                            pos = i;
                            break;
                        }
                    }
                    spinner.setSelection(pos);
                }
            }


            //VehicleSelectAdapter spinnerAdapter = new VehicleSelectAdapter(mContext, vehicles);
            //spinner.setAdapter(spinnerAdapter);
        }
    }

    class TitleViewHolder extends RecyclerView.ViewHolder {

        ImageView logo;
        TextView title;

        TitleViewHolder(final View itemView, final OnItemClickListener listener) {
            super(itemView);
            logo = itemView.findViewById(R.id.type_image);
            title = itemView.findViewById(R.id.type_title);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });
        }

        void setUp(Object object) {
            MainFragment.TitleType type = (MainFragment.TitleType) object;

            switch (type) {
                case Cost:
                    logo.setImageResource(R.drawable.ic_euro_symbol_black_24dp);
                    title.setText(R.string.cost);
                    break;
                case Fuel:
                    logo.setImageResource(R.drawable.ic_local_gas_station_black_24dp);
                    title.setText(R.string.fuel);
                    break;
                case Last_Entries:
                    logo.setImageResource(R.drawable.ic_timeline_black_24dp);
                    title.setText(R.string.last_entries);
                    break;
                default:
                    //no vehicles
                    logo.setImageResource(R.drawable.ic_info_outline_black_24dp);
                    title.setText(String.format("%s %s", mContext.getString(R.string.no_vehicle), mContext.getString(R.string.added)));
            }
        }
    }

    class DataViewHolder extends RecyclerView.ViewHolder {

        int which;
        String units;
        TextView avgCons, rcntCons, rcntPrice, date, fuelCost, otherCost, prevFuelCost, prevOtherCost;
        TextView entryWarning;
        TextView unit1, unit2, unit3,  unit4;
        RecyclerView entry;
        ImageView trend;

        DataViewHolder(final View itemView, final OnItemClickListener listener, int which) {
            super(itemView);
            this.which = which;
            if (PreferenceManager.getDefaultSharedPreferences(mContext).getString("selected_unit", "litres_per_km").equals("litres_per_km"))
                units = "litres_per_km";
            else
                units = "km_per_litre";

            if (which == 2) {
                //fuel
                avgCons = itemView.findViewById(R.id.avg_fuel_cons_value);
                rcntCons = itemView.findViewById(R.id.rcnt_fuel_cons_value);
                rcntPrice = itemView.findViewById(R.id.rcnt_fuel_price_value);
                date = itemView.findViewById(R.id.date);
                unit1 = itemView.findViewById(R.id.unit1);
                unit2 = itemView.findViewById(R.id.unit2);
                trend = itemView.findViewById(R.id.price_fuel_trend);
            } else if (which == 3) {
                //cost
                fuelCost = itemView.findViewById(R.id.fuel_cost_value);
                otherCost = itemView.findViewById(R.id.other_costs_value);
                prevFuelCost = itemView.findViewById(R.id.prev_fuel_cost_value);
                prevOtherCost = itemView.findViewById(R.id.prev_other_cost_value);
                unit1 = itemView.findViewById(R.id.unit1);
                unit2 = itemView.findViewById(R.id.unit2);
                unit3 = itemView.findViewById(R.id.unit3);
                unit4 = itemView.findViewById(R.id.unit4);
            } else {
                //entry
                entry = itemView.findViewById(R.id.entry_recycler);
                entryWarning = itemView.findViewById(R.id.entry_warning);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });
        }

        void setUpFuel(Object object) {
            long vehicleID = (long) object;
            if (vehicleID != -1) {
                DriveObject correctPrev = null;
                double correctPrice = 0.0;
                SimpleDateFormat format = new SimpleDateFormat("dd. MM. yyyy");

                List<DriveObject> allDrives = dbHelper.getAllDrives(vehicleID);
                if (allDrives == null || allDrives.size() == 0) {
                    rcntCons.setText(mContext.getString(R.string.no_data_yet));
                    avgCons.setText(mContext.getString(R.string.no_data_yet));
                    rcntPrice.setText(mContext.getString(R.string.no_data_yet));
                    date.setText("");
                    itemView.findViewById(R.id.unit1).setVisibility(View.INVISIBLE);
                    itemView.findViewById(R.id.unit2).setVisibility(View.INVISIBLE);
                    itemView.findViewById(R.id.unit3).setVisibility(View.INVISIBLE);
                    correctPrev = null;
                    correctPrice = 0.0;
                } else {
                    DriveObject latest = allDrives.get(0);
                    if (latest.getFirst() == 1 && allDrives.size() == 1) {
                        rcntCons.setText(mContext.getString(R.string.no_data_yet));
                        avgCons.setText(mContext.getString(R.string.no_data_yet));

                        correctPrice = latest.getCostPerLitre();
                        rcntPrice.setText(String.format(locale, "%.3f", latest.getCostPerLitre()));

                        date.setText(format.format(latest.getDate().getTime()));
                        itemView.findViewById(R.id.unit1).setVisibility(View.INVISIBLE);
                        itemView.findViewById(R.id.unit2).setVisibility(View.INVISIBLE);
                        correctPrev = null;
                    } else if (latest.getNotFull() == 1 || (latest.getFirst() == 1 && allDrives.size() > 1)) {
                        //find first one that is full
                        date.setText(format.format(latest.getDate().getTime()));
                        correctPrice = latest.getCostPerLitre();
                        rcntPrice.setText(String.format(locale, "%.3f", latest.getCostPerLitre()));
                        boolean found = false;
                        int i;
                        for (i = 1; i < allDrives.size(); i++) {
                            latest = allDrives.get(i);
                            correctPrev = latest;
                            if (latest.getNotFull() == 0 && latest.getFirst() == 0) {
                                found = true;
                                break;
                            }
                        }
                        if (found) {
                            Double cons = Utils.calculateConsumption(latest.getTrip(), latest.getLitres());

                            if (units.equals("km_per_litre")) {
                                cons = Utils.convertUnitToKmPL(cons);
                                unit1.setText(KMPL);
                                unit2.setText(KMPL);
                            }

                            rcntCons.setText(String.format(locale, "%.2f", cons));
                            double avgL = 0.0;
                            int avgKm = 0;
                            for (int j = i; j < allDrives.size(); j++) {
                                DriveObject drive = allDrives.get(j);
                                if (drive.getFirst() == 0) {
                                    avgL += drive.getLitres();
                                    avgKm += drive.getTrip();
                                }
                            }
                            double avg = Utils.calculateConsumption(avgKm, avgL);

                            if (units.equals("km_per_litre")) {
                                cons = Utils.convertUnitToKmPL(cons);
                                avg = Utils.convertUnitToKmPL(avg);
                                unit1.setText(KMPL);
                                unit2.setText(KMPL);
                            }

                            rcntCons.setText(String.format(locale, "%.2f", cons));
                            avgCons.setText(String.format(locale, "%.2f", avg));
                        } else {
                            rcntCons.setText(mContext.getString(R.string.no_data_yet));
                            avgCons.setText(mContext.getString(R.string.no_data_yet));
                            correctPrice = latest.getCostPerLitre();
                            rcntPrice.setText(String.format(locale, "%.3f", latest.getCostPerLitre()));
                            date.setText(format.format(latest.getDate().getTime()));
                            itemView.findViewById(R.id.unit1).setVisibility(View.INVISIBLE);
                            itemView.findViewById(R.id.unit2).setVisibility(View.INVISIBLE);
                            correctPrev = null;
                        }
                    } else {
                        //first one is full
                        DriveObject second = allDrives.get(1);
                        correctPrev = second;
                        if (second.getNotFull() == 1) {
                            double litreAvg = allDrives.get(0).getLitres();
                            int kmAvg = allDrives.get(0).getTrip();
                            for (int i = 1; i < allDrives.size(); i++) {
                                if (allDrives.get(i).getNotFull() == 0)
                                    break;
                                litreAvg += allDrives.get(i).getLitres();
                                kmAvg += allDrives.get(i).getTrip();
                            }
                            Double cons = Utils.calculateConsumption(kmAvg, litreAvg);
                            date.setText(format.format(latest.getDate().getTime()));
                            correctPrice = latest.getCostPerLitre();
                            rcntPrice.setText(String.format(locale, "%.3f", latest.getCostPerLitre()));

                            double avgL = 0.0;
                            int avgKm = 0;
                            for (int j = 0; j < allDrives.size(); j++) {
                                DriveObject drive = allDrives.get(j);
                                if (drive.getFirst() == 0) {
                                    avgL += drive.getLitres();
                                    avgKm += drive.getTrip();
                                }
                            }
                            double avg = Utils.calculateConsumption(avgKm, avgL);

                            if (units.equals("km_per_litre")) {
                                cons = Utils.convertUnitToKmPL(cons);
                                avg = Utils.convertUnitToKmPL(avg);
                                unit1.setText(KMPL);
                                unit2.setText(KMPL);
                            }

                            rcntCons.setText(String.format(locale, "%.2f",cons));
                            avgCons.setText(String.format(locale, "%.2f", avg));
                        } else {
                            Double cons = Utils.calculateConsumption(latest.getTrip(), latest.getLitres());
                            date.setText(format.format(latest.getDate().getTime()));
                            correctPrice = latest.getCostPerLitre();
                            rcntPrice.setText(String.format(locale, "%.3f", latest.getCostPerLitre()));

                            double avgL = 0.0;
                            int avgKm = 0;
                            for (int j = 0; j < allDrives.size(); j++) {
                                DriveObject drive = allDrives.get(j);
                                if (drive.getFirst() == 0) {
                                    avgL += drive.getLitres();
                                    avgKm += drive.getTrip();
                                }
                            }
                            double avg = Utils.calculateConsumption(avgKm, avgL);

                            if (units.equals("km_per_litre")) {
                                cons = Utils.convertUnitToKmPL(cons);
                                avg = Utils.convertUnitToKmPL(avg);
                                unit1.setText(KMPL);
                                unit2.setText(KMPL);
                            }

                            rcntCons.setText(String.format(locale, "%.2f", cons));
                            avgCons.setText(String.format(locale, "%.2f", avg));
                        }
                    }
                }
                if (correctPrev != null) {
                    //trend.setImageTintList(ColorStateList.valueOf(mContext.getColor(R.color.yellow)));
                    //trend.setImageResource(R.drawable.ic_baseline_trending_flat_24);
                //} else {
                    if (Double.compare(correctPrev.getCostPerLitre(), correctPrice) > 0) {
                        trend.setImageTintList(ColorStateList.valueOf(mContext.getColor(R.color.green)));
                        trend.setImageResource(R.drawable.ic_baseline_trending_down_24);
                    } else if (Double.compare(correctPrev.getCostPerLitre(), correctPrice) < 0) {
                        trend.setImageTintList(ColorStateList.valueOf(mContext.getColor(R.color.red)));
                        trend.setImageResource(R.drawable.ic_baseline_trending_up_24);
                    } else {
                        trend.setImageTintList(ColorStateList.valueOf(mContext.getColor(R.color.yellow)));
                        trend.setImageResource(R.drawable.ic_baseline_trending_flat_24);
                    }
                }
            }
        }

        void setUpCost(Object object) {
            long vehicleID = (long) object;
            Calendar first = Calendar.getInstance();
            Calendar last = Calendar.getInstance();
            if (vehicleID != -1) {
                first.set(Calendar.DAY_OF_MONTH, 1);
                last.set(Calendar.DAY_OF_MONTH, last.getActualMaximum(Calendar.DAY_OF_MONTH));
                List<DriveObject> current = dbHelper.getAllDrivesWhereTimeBetween(vehicleID, first.getTimeInMillis()/1000, last.getTimeInMillis()/1000);
                double price = 0.0;
                for (DriveObject drive : current) {
                    price += Utils.calculateFullPrice(drive.getCostPerLitre(), drive.getLitres());
                }

                double priceA = 0.0;
                List<CostObject> currentCost = dbHelper.getAllCostsWhereTimeBetween(vehicleID, first.getTimeInMillis()/1000, last.getTimeInMillis()/1000);
                for (CostObject cost : currentCost) {
                    priceA = addCost(priceA, cost.getCost());
                }

                first.add(Calendar.MONTH, -1);
                last.add(Calendar.MONTH, -1);
                last.set(Calendar.DAY_OF_MONTH, first.getActualMaximum(Calendar.DAY_OF_MONTH));

                current = dbHelper.getAllDrivesWhereTimeBetween(vehicleID, first.getTimeInMillis()/1000, last.getTimeInMillis()/1000);
                double priceO = 0.0;
                for (DriveObject drive : current) {
                    priceO += Utils.calculateFullPrice(drive.getCostPerLitre(), drive.getLitres());
                }


                double priceOA = 0.0;
                currentCost = dbHelper.getAllCostsWhereTimeBetween(vehicleID, first.getTimeInMillis()/1000, last.getTimeInMillis()/1000);
                for (CostObject cost : currentCost) {
                    priceOA = addCost(priceOA, cost.getCost());
                }

                if (price == 0.0)
                    fuelCost.setText(String.format(locale, "%.2f", price));
                else
                    fuelCost.setText(String.format(locale, "%+.2f", price*-1));
                if (priceO == 0.0)
                    prevFuelCost.setText(String.format(locale, "%.2f", priceO));
                else
                    prevFuelCost.setText(String.format(locale, "%+.2f", priceO*-1));
                if (priceA == 0.0)
                    otherCost.setText(String.format(locale, "%.2f", priceA));
                else
                    otherCost.setText(String.format(locale, "%+.2f", priceA));
                if (priceOA == 0.0)
                    prevOtherCost.setText(String.format(locale, "%.2f", priceOA));
                else
                    prevOtherCost.setText(String.format(locale, "%+.2f", priceOA));
            }
        }

        private double addCost(double avgC, double cost) {
            if (cost + 80085 == 0)
                avgC += 0;
            else if (cost < 0.0)
                avgC += Math.abs(cost);
            else
                avgC -= cost;
            return avgC;
        }

        void setUpEntry(Object object) {

            long vehicleID = (long) object;

            //mRecyclerView.setHasFixedSize(true);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);

            List<Object> data = new ArrayList<>();
            data.addAll(dbHelper.getAllDrives(vehicleID));
            data.addAll(dbHelper.getAllCosts(vehicleID));

            if (data.size() == 0) {
                entryWarning.setVisibility(View.VISIBLE);
                entryWarning.setText(mContext.getString(R.string.no_data_yet));
            } else {
                data.sort(new Comparator<Object>() {
                    @Override
                    public int compare(Object o1, Object o2) {
                        CostObject co1 = null;
                        CostObject co2 = null;
                        DriveObject do1 = null;
                        DriveObject do2 = null;
                        boolean status = false;

                        if (o1 instanceof CostObject)
                            co1 = (CostObject) o1;
                        else
                            do1 = (DriveObject) o1;

                        if (o2 instanceof CostObject)
                            co2 = (CostObject) o2;
                        else
                            do2 = (DriveObject) o2;

                        if (co1 != null && co2 != null) {
                            //both are cost
                            status = co1.getDate().getTime().after(co2.getDate().getTime());
                        } else if (co1 != null && do2 != null) {
                            status = co1.getDate().getTime().after(do2.getDate().getTime());
                        } else if (do1 != null & co2 != null) {
                            status = do1.getDate().getTime().after(co2.getDate().getTime());
                        } else {
                            //both are drive
                            status = do1.getDate().getTime().after(do2.getDate().getTime());
                        }

                        if (status)
                            return -1;
                        else
                            return 1;
                    }
                });

                int maxLen = Math.min(data.size(), 7);
                data = data.subList(0, maxLen);
                List<Object> tmpData = new ArrayList<>(data);
                List<Calendar> months = new ArrayList<>();
                int trueCounter = 0;
                for (int i = 0; i < tmpData.size(); i++) {
                    Calendar when;
                    if (tmpData.get(i) instanceof DriveObject)
                        when = ((DriveObject) tmpData.get(i)).getDate();
                    else
                        when = ((CostObject) tmpData.get(i)).getDate();

                    boolean exists = false;
                    for (Calendar month : months) {
                        if (month.get(Calendar.MONTH) == when.get(Calendar.MONTH) &&
                                month.get(Calendar.YEAR) == when.get(Calendar.YEAR)) {
                            exists = true;
                            break;
                        }
                    }

                    if (!exists) {
                        months.add(when);
                        data.add(trueCounter, when);
                        trueCounter++;
                    }
                    trueCounter++;
                }
                tmpData.clear();
                EntryAdapter entryAdapter = new EntryAdapter(mContext, data.subList(0, maxLen+months.size()), dbHelper);
                entry.setHasFixedSize(true);

                entry.setLayoutManager(layoutManager);
                entry.setAdapter(entryAdapter);
            }
        }
    }


/*
    @Override
    public void onBindViewHolder(MainViewHolder holder, int position) {
        if (position >= getItemCount())
            return;

        VehicleObject vehicle = objectsList.get(position);

        String consUnit = PreferenceManager.getDefaultSharedPreferences(mContext).getString("language_select", "english");
        String benz;
        if (consUnit.equals("english"))
            benz = vehicle.getFuel();
        else
            benz = Utils.fromENGtoSLO(vehicle.getFuel());

        String make = vehicle.getMake();
        String model = vehicle.getModel();

        String data = vehicle.getEngine() + " " + vehicle.getHp() + "hp" + " " + benz;
        long id = vehicle.getId();

        holder.mBrand.setText(String.format("%s %s", make, model));
        holder.mData.setText(data);

        //Loads image file if exists, else predefined image
        try {
            String fileName = vehicle.getCustomImg();
            File storageDIR = mContext.getDir("Images",MODE_PRIVATE);
            if (fileName == null) {
                ManufacturerObject mo = MainActivity.manufacturers.get(toCapitalCaseWords(make));
                if (!mo.isOriginal()){
                    Utils.downloadImage(mContext.getResources(), mContext.getApplicationContext(), mo);
                }
                int idResource = mContext.getResources().getIdentifier(mo.getFileNameModNoType(), "drawable", mContext.getPackageName());
                Glide.with(mContext).load(storageDIR+"/"+mo.getFileNameMod()).error(mContext.getDrawable(idResource)).diskCacheStrategy(DiskCacheStrategy.NONE).into(holder.mImageView);
            } else {
                Glide.with(mContext).load(storageDIR+"/"+fileName).diskCacheStrategy(DiskCacheStrategy.NONE).into(holder.mImageView);
            }
        } catch (Exception e){
            Bitmap noIcon = Utils.getBitmapFromVectorDrawable(mContext, R.drawable.ic_help_outline_black_24dp);
            Glide.with(mContext).load(noIcon).fitCenter().diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.mImageView);
        }

        holder.itemView.setTag(id);
    }*/

    @Override
    public int getItemCount() {
        return objectsList.size();
    }

}
