package com.example.fueldiet.adapter;

import android.content.Context;
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
import androidx.recyclerview.widget.RecyclerView;

import com.example.fueldiet.fragment.MainFragment;
import com.example.fueldiet.object.CostObject;
import com.example.fueldiet.object.DriveObject;
import com.example.fueldiet.object.VehicleObject;
import com.example.fueldiet.R;
import com.example.fueldiet.Utils;
import com.example.fueldiet.db.FuelDietDBHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private OnItemClickListener mListener;
    String units;

    private Context mContext;
    private List<Object> objectsList;
    private FuelDietDBHelper dbHelper;
    private final int TYPE_SPINNER = 0;
    private final int TYPE_TITLE = 1;
    private final int TYPE_DATA_FUEL = 2;
    private final int TYPE_DATA_COST = 3;
    private final int TYPE_DATA_ENTRY = 4;
    private final String KMPL = "km/l";

    public MainAdapter(Context context, List<Object> vehicles, FuelDietDBHelper dbHelper) {
        mContext = context;
        objectsList = vehicles;
        this.dbHelper = dbHelper;
        if (PreferenceManager.getDefaultSharedPreferences(mContext).getString("selected_unit", "litres_per_km").equals("litres_per_km"))
            units = "litres_per_km";
        else
            units = "km_per_litre";
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
                vehicles.add(new VehicleObject(mContext.getString(R.string.no_vehicle), "added!", -1));
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
                    logo.setImageResource(R.drawable.ic_help_outline_black_24dp);
            }
        }
    }

    class DataViewHolder extends RecyclerView.ViewHolder {

        int which;
        TextView avgCons, rcntCons, rcntPrice, date, fuelCost, otherCost, prevFuelCost, prevOtherCost;
        TextView unit1, unit2, unit3,  unit4;

        DataViewHolder(final View itemView, final OnItemClickListener listener, int which) {
            super(itemView);
            this.which = which;

            if (which == 2) {
                //fuel
                avgCons = itemView.findViewById(R.id.avg_fuel_cons_value);
                rcntCons = itemView.findViewById(R.id.rcnt_fuel_cons_value);
                rcntPrice = itemView.findViewById(R.id.rcnt_fuel_price_value);
                date = itemView.findViewById(R.id.date);
                unit1 = itemView.findViewById(R.id.unit1);
                unit2 = itemView.findViewById(R.id.unit2);
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
                DriveObject latest = dbHelper.getLastDrive(vehicleID);
                if (latest == null) {
                    rcntCons.setText("No data yet");
                    rcntPrice.setText("No data yet");
                    avgCons.setText("No data yet");
                    date.setText("");
                    itemView.findViewById(R.id.unit1).setVisibility(View.INVISIBLE);
                    itemView.findViewById(R.id.unit2).setVisibility(View.INVISIBLE);
                    itemView.findViewById(R.id.unit3).setVisibility(View.INVISIBLE);
                    return;
                }
                double addL = latest.getLitres();
                int addK = latest.getTrip();
                latest = dbHelper.getPrevDriveSelection(latest.getCarID(), latest.getOdo());
                while (latest != null && (latest.getNotFull() == 1 || latest.getFirst() == 1)) {
                    if (latest.getNotFull() == 1) {
                        addK += latest.getTrip();
                        addL += latest.getLitres();
                    }
                    latest = dbHelper.getPrevDriveSelection(latest.getCarID(), latest.getOdo());
                }

                Double cons = Utils.calculateConsumption(addK, addL);


                SimpleDateFormat format = new SimpleDateFormat("dd. MM. yyyy");
                date.setText(format.format(latest.getDate().getTime()));
                rcntPrice.setText(latest.getCostPerLitre()+"");
                double avg = 0.0;
                int count = 0;
                List<DriveObject> allDrives = dbHelper.getAllDrives(vehicleID);
                double tmpL = 0.0;
                int tmpK = 0;
                for (DriveObject drive : allDrives) {
                    if (drive.getFirst() == 1) {}
                    else if (drive.getNotFull() == 1) {
                        tmpK += drive.getTrip();
                        tmpL += drive.getLitres();
                    } else {
                        avg += Utils.calculateConsumption(drive.getTrip()+tmpK, drive.getLitres()+tmpL);
                        count++;
                        tmpL = 0.0;
                        tmpK = 0;
                    }
                }
                avg = avg / count;
                avg = Math.round(avg * 100.0) / 100.0;

                if (units.equals("km_per_litre")) {
                    cons = Utils.convertUnitToKmPL(cons);
                    avg = Utils.convertUnitToKmPL(avg);
                    unit1.setText(KMPL);
                    unit2.setText(KMPL);
                }

                rcntCons.setText(cons+"");
                avgCons.setText(avg + "");
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
                    priceA += cost.getCost();
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
                    priceOA += cost.getCost();
                }

                fuelCost.setText(price+"");
                prevFuelCost.setText(priceO+"");
                otherCost.setText(priceA+"");
                prevOtherCost.setText(priceOA+"");
            }
        }

        void setUpEntry(Object object) {
            long vehicleID = (long) object;
        }
    }

    public static class MainViewHolder extends RecyclerView.ViewHolder {

        public ImageView mImageView;
        public TextView mBrand;
        public TextView mData;


        public MainViewHolder(final View itemView, final OnItemClickListener listener) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.vehicle_logo_image);
            mBrand = itemView.findViewById(R.id.vehicle_make_model_view);
            mData = itemView.findViewById(R.id.vehicle_desc_view);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        //listener.onItemClick((long)itemView.getTag());
                        listener.onItemClick(position);
                    }
                }
            });
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
