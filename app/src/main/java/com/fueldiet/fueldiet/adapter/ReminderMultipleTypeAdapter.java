package com.fueldiet.fueldiet.adapter;

import android.content.Context;
import android.content.res.Configuration;
import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.fueldiet.fueldiet.object.ReminderObject;
import com.fueldiet.fueldiet.object.VehicleObject;
import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for Reminder Recycler View
 */
public class ReminderMultipleTypeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private Locale locale;
    private ReminderMultipleTypeAdapter.OnItemClickListener mListener;
    private Context mContext;
    private List<ReminderObject> reminderList;
    private final static int TYPE_ACTIVE = 0;
    private final static int TYPE_DONE = 1;
    private final static int TYPE_DIVIDER = 2;
    private final static int TYPE_REPEAT = 3;
    private final static int TYPE_DONE_REPEAT = 4;

    public ReminderMultipleTypeAdapter(Context context, List<ReminderObject> reminderObjectList) {
        mContext = context;
        reminderList = reminderObjectList;
        Configuration configuration = context.getResources().getConfiguration();
        locale = configuration.getLocales().get(0);
    }

    public interface OnItemClickListener {
        void onRepeatDoneClick(int position, int element_id);
        void onEditClick(int position, int element_id);
        void onDeleteClick(int position, int element_id);
        void onDoneClick(int position, int element_id);
    }

    public void setOnItemClickListener(ReminderMultipleTypeAdapter.OnItemClickListener listener) {
        mListener = listener;
    }

    /**
     * Checks what type of reminder it is
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ACTIVE) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_template_reminder, parent, false);
            return new ActiveViewHolder(v, mListener);
        } else if (viewType == TYPE_DONE) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_template_reminder_done, parent, false);
            return new DoneViewHolder(v, mListener);
        } else if (viewType == TYPE_REPEAT) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_template_repeat_reminder, parent, false);
            return new RepeatViewHolder(v, mListener);
        } else if (viewType == TYPE_DONE_REPEAT) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_template_rpt_reminder_done, parent, false);
            return new RepeatDoneViewHolder(v, mListener);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_template_type_title, parent, false);
            return new DividerViewHolder(v, mListener);
        }
    }

    @Override
    public int getItemViewType(int position) {
        ReminderObject selected = reminderList.get(position);
        if (selected.getRepeat() != 0 && !selected.isActive())
            return TYPE_DONE_REPEAT;
        else if (selected.getRepeat() != 0)
            return TYPE_REPEAT;
        else if (selected.getId() < 0)
            return TYPE_DIVIDER;
        else if (selected.isActive())
            return TYPE_ACTIVE;
        else
            return TYPE_DONE;
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_ACTIVE)
            ((ActiveViewHolder) holder).setActiveDetails(reminderList.get(position), position);
        else if (getItemViewType(position) == TYPE_DONE)
            ((DoneViewHolder) holder).setDoneDetails(reminderList.get(position), position);
        else if (getItemViewType(position) == TYPE_REPEAT)
            ((RepeatViewHolder) holder).setRepeatDetails(reminderList.get(position), position);
        else if (getItemViewType(position) == TYPE_DONE_REPEAT)
            ((RepeatDoneViewHolder) holder).setDoneRepeatDetails(reminderList.get(position), position);
        else
            ((DividerViewHolder) holder).setDivider(reminderList.get(position));
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    class ActiveViewHolder extends RecyclerView.ViewHolder {
        TextView when;
        ImageView whenImg;
        ImageView more;
        TextView title;
        TextView desc;

        View divider;
        ImageView descImg;

        ActiveViewHolder(final View itemView, final ReminderMultipleTypeAdapter.OnItemClickListener listener) {
            super(itemView);
            when = itemView.findViewById(R.id.reminder_when_template);
            more = itemView.findViewById(R.id.reminder_more);
            whenImg = itemView.findViewById(R.id.reminder_when_img);
            //whenImg.setImageTintList(ColorStateList.valueOf(mContext.getColor(R.color.red)));
            title = itemView.findViewById(R.id.reminder_title_template);
            desc = itemView.findViewById(R.id.reminder_desc_template);

            descImg = itemView.findViewById(R.id.reminder_details_img);
            divider = itemView.findViewById(R.id.reminder_break_template);
        }

        void setActiveDetails(ReminderObject ro, int position) {
            Calendar calendar = Calendar.getInstance();
            Integer km = ro.getKm();
            FuelDietDBHelper dbHelper = new FuelDietDBHelper(mContext);

            /* popup menu */
            more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //creating a popup menu
                    PopupMenu popup = new PopupMenu(mContext, more);
                    //inflating menu from xml resource
                    popup.inflate(R.menu.reminder_card_menu);
                    //hide unnecessary items
                    popup.getMenu().findItem(R.id.set_as_finish).setVisible(false);
                    //adding click listener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.set_as_done:
                                    mListener.onDoneClick(position, ro.getId());
                                    return true;
                                case R.id.edit:
                                    mListener.onEditClick(position, ro.getId());
                                    return true;
                                case R.id.delete:
                                    mListener.onDeleteClick(position, ro.getId());
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

            if (km == null) {
                final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                //whenImg.setImageResource(R.drawable.ic_today_black_24dp);
                Date date = ro.getDate();
                when.setText(sdf.format(date));

                if (calendar.getTimeInMillis() >= date.getTime()) {
                    when.setTextColor(mContext.getColor(R.color.red));
                    whenImg.setColorFilter(mContext.getColor(R.color.redDark));
                } else {
                    when.setTextColor(mContext.getColor(R.color.colorPrimary));
                    whenImg.setColorFilter(mContext.getColor(R.color.colorPrimaryDark));
                }

            } else {
                //whenImg.setImageResource(R.drawable.ic_timeline_black_24dp);
                when.setText(String.format(locale, "%d", ro.getKm()));

                VehicleObject vehicleObject = dbHelper.getVehicle(ro.getCarID());

                int maxOdo = Math.max(vehicleObject.getOdoCostKm(), vehicleObject.getOdoFuelKm());
                maxOdo = Math.max(maxOdo, vehicleObject.getOdoRemindKm());

                if (maxOdo >= ro.getKm()) {
                    when.setTextColor(mContext.getColor(R.color.red));
                    whenImg.setColorFilter(mContext.getColor(R.color.redDark));
                }  else {
                    when.setTextColor(mContext.getColor(R.color.colorPrimary));
                    whenImg.setColorFilter(mContext.getColor(R.color.colorPrimaryDark));
                }
            }

            String titleString = ro.getTitle();
            String descString = ro.getDesc();
            int id = ro.getId();

            if (descString == null || descString.equals("")) {
                desc.setVisibility(View.GONE);
                descImg.setVisibility(View.GONE);
                divider.setVisibility(View.GONE);
            } else {
                desc.setVisibility(View.VISIBLE);
                descImg.setVisibility(View.VISIBLE);
                divider.setVisibility(View.VISIBLE);
                desc.setText(descString);
            }
            title.setText(titleString);
            itemView.setTag(id);
        }
    }

    class DoneViewHolder extends RecyclerView.ViewHolder {

        TextView date;
        ImageView dateImg;
        TextView km;
        TextView title;
        TextView desc;

        View divider;
        ImageView descImg;

        ImageView more;


        DoneViewHolder(final View itemView, final ReminderMultipleTypeAdapter.OnItemClickListener listener) {
            super(itemView);
            date = itemView.findViewById(R.id.reminder_done_date_template);
            dateImg = itemView.findViewById(R.id.reminder_done_calendar_img);
            km = itemView.findViewById(R.id.reminder_done_km_template);
            title = itemView.findViewById(R.id.reminder_done_title_template);
            desc = itemView.findViewById(R.id.reminder_done_desc_template);

            descImg = itemView.findViewById(R.id.reminder_done_details_img);
            divider = itemView.findViewById(R.id.reminder_done_break_template);

            more = itemView.findViewById(R.id.reminder_more);
        }

        void setDoneDetails(ReminderObject ro, int position) {
            final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            Date dateF = ro.getDate();
            date.setText(sdf.format(dateF));
            if (ro.getKm() == null)
                km.setText(mContext.getString(R.string.no_km));
            else
                km.setText(String.format(locale, "%d km", ro.getKm()));

            String titleS = ro.getTitle();
            String descS = ro.getDesc();
            int id = ro.getId();

            if (descS == null || descS.equals("")) {
                desc.setVisibility(View.GONE);
                descImg.setVisibility(View.GONE);
                divider.setVisibility(View.GONE);
            } else {
                desc.setVisibility(View.VISIBLE);
                descImg.setVisibility(View.VISIBLE);
                divider.setVisibility(View.VISIBLE);
                desc.setText(descS);
            }
            title.setText(titleS);
            itemView.setTag(id);
            /* popup menu */
            more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //creating a popup menu
                    PopupMenu popup = new PopupMenu(mContext, more);
                    //inflating menu from xml resource
                    popup.inflate(R.menu.reminder_card_menu);
                    //hide mark as done
                    popup.getMenu().findItem(R.id.set_as_done).setVisible(false);
                    popup.getMenu().findItem(R.id.set_as_finish).setVisible(false);
                    //adding click listener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.edit:
                                    mListener.onEditClick(position, ro.getId());
                                    return true;
                                case R.id.delete:
                                    mListener.onDeleteClick(position, ro.getId());
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
        }
    }

    class DividerViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        ImageView logo;

        DividerViewHolder(final View itemView, final ReminderMultipleTypeAdapter.OnItemClickListener listener) {
            super(itemView);
            title = itemView.findViewById(R.id.type_title);
            logo = itemView.findViewById(R.id.type_image);
        }

        void setDivider(ReminderObject ro) {
            if (ro.getId() == -20) {
                title.setText(R.string.vehicle_reminder_active);
                logo.setImageResource(R.drawable.ic_notifications_none_black_24dp);
            } else if (ro.getId() == -10) {
                title.setText(R.string.vehicle_reminder_prev);
                logo.setImageResource(R.drawable.ic_notifications_off_black_24dp);
            } else {
                title.setText(R.string.vehicle_reminder_active_repeat);
                logo.setImageResource(R.drawable.ic_notifications_none_repeat_24px);
            }
        }
    }

    class RepeatViewHolder extends RecyclerView.ViewHolder {

        TextView when;
        ImageView whenImg;
        ImageView more;
        TextView title;
        TextView desc;
        TextView repeat;
        TextView nextRepeat;

        View divider;
        ImageView descImg;

        RepeatViewHolder(final View itemView, final OnItemClickListener listener) {
            super(itemView);
            when = itemView.findViewById(R.id.reminder_when_template);
            more = itemView.findViewById(R.id.reminder_more);
            whenImg = itemView.findViewById(R.id.reminder_when_img);
            title = itemView.findViewById(R.id.reminder_title_template);
            desc = itemView.findViewById(R.id.reminder_desc_template);
            descImg = itemView.findViewById(R.id.reminder_details_img);
            divider = itemView.findViewById(R.id.reminder_break_template);
            repeat = itemView.findViewById(R.id.reminder_repeat_every);
            nextRepeat = itemView.findViewById(R.id.reminder_next_repeat);
        }

        void setRepeatDetails(ReminderObject ro, int position) {
            Calendar calendar = Calendar.getInstance();
            Integer km = ro.getKm();
            FuelDietDBHelper dbHelper = new FuelDietDBHelper(mContext);

            /* popup menu */
            more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //creating a popup menu
                    PopupMenu popup = new PopupMenu(mContext, more);
                    //inflating menu from xml resource
                    popup.inflate(R.menu.reminder_card_menu);
                    //adding click listener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.set_as_done:
                                    mListener.onRepeatDoneClick(position, ro.getId());
                                    return true;
                                case R.id.set_as_finish:
                                    mListener.onDoneClick(position, ro.getId());
                                    return true;
                                case R.id.edit:
                                    mListener.onEditClick(position, ro.getId());
                                    return true;
                                case R.id.delete:
                                    mListener.onDeleteClick(position, ro.getId());
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

            String rpt = mContext.getString(R.string.repeat_every_x);
            String at = mContext.getString(R.string.at);

            String[] descString = ro.getDesc().split("//-");
            int repeatNumber = Integer.parseInt(descString[0]);

            if (km == null) {
                final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                final SimpleDateFormat sdfShort = new SimpleDateFormat("dd.MM.yy");
                int days = ro.getRepeat();
                //repeat.setText(rpt + " " + days + " " + mContext.getString(R.string.days));
                repeat.setText(String.format(locale, "%s %d %s", rpt, days, mContext.getString(R.string.days)));

                days *= repeatNumber;
                Date date = ro.getDate();
                Calendar c = Calendar.getInstance();
                c.setTime(date);
                c.add(Calendar.DAY_OF_MONTH, days);

                when.setText(sdf.format(c.getTime()));
                //nextRepeat.setText(at + " " + sdfShort.format(date));
                nextRepeat.setText(String.format(locale, "%s %s", at, sdfShort.format(date)));
            } else {
                int dist = ro.getRepeat();
                int newDist = ro.getKm() + (dist * repeatNumber);

                when.setText(String.format(locale, "%dkm", newDist));

                //repeat.setText(rpt + " " + dist + " km");
                repeat.setText(String.format(locale, "%s %d km",rpt, dist));
                //nextRepeat.setText(mContext.getString(R.string.at)+ " " + ro.getKm() +" km");
                nextRepeat.setText(String.format(locale, "%s %d km", mContext.getString(R.string.at), ro.getKm()));
            }

            String titleString = ro.getTitle();
            int id = ro.getId();
            String trueDesc;
            if (descString.length < 2)
                trueDesc = null;
            else
                trueDesc = descString[1];

            if (trueDesc == null || trueDesc.equals("")) {
                desc.setVisibility(View.GONE);
                descImg.setVisibility(View.GONE);
                divider.setVisibility(View.GONE);
            } else {
                desc.setVisibility(View.VISIBLE);
                descImg.setVisibility(View.VISIBLE);
                divider.setVisibility(View.VISIBLE);
                desc.setText(trueDesc);
            }
            title.setText(titleString);
            itemView.setTag(id);
        }
    }

    class RepeatDoneViewHolder extends RecyclerView.ViewHolder {

        TextView date, km, title, desc, rptNum;
        View divider;
        ImageView descImg, dateImg, more;

        RepeatDoneViewHolder(final View itemView, final ReminderMultipleTypeAdapter.OnItemClickListener listener) {
            super(itemView);
            date = itemView.findViewById(R.id.rpt_reminder_done_date_template);
            dateImg = itemView.findViewById(R.id.rpt_reminder_done_calendar_img);
            km = itemView.findViewById(R.id.rpt_reminder_done_km_template);
            title = itemView.findViewById(R.id.rpt_reminder_done_title_template);
            desc = itemView.findViewById(R.id.rpt_reminder_done_desc_template);
            rptNum = itemView.findViewById(R.id.rpt_reminder_done_rpt_template);

            descImg = itemView.findViewById(R.id.rpt_reminder_done_details_img);
            divider = itemView.findViewById(R.id.rpt_reminder_done_break_template);

            more = itemView.findViewById(R.id.reminder_more);
        }

        void setDoneRepeatDetails(ReminderObject ro, int position) {
            final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            Date dateF = ro.getDate();
            date.setText(sdf.format(dateF));
            if (ro.getKm() == null)
                km.setText(mContext.getString(R.string.no_km));
            else
                km.setText(String.format(locale, "%d", ro.getKm()));

            String titleS = ro.getTitle();
            String descS = ro.getDesc();
            int id = ro.getId();

            if (descS == null || descS.equals("")) {
                desc.setVisibility(View.GONE);
                descImg.setVisibility(View.GONE);
                divider.setVisibility(View.GONE);
            } else {
                String [] partDesc = descS.split("//-");
                if (partDesc.length == 2) {
                    desc.setVisibility(View.VISIBLE);
                    descImg.setVisibility(View.VISIBLE);
                    divider.setVisibility(View.VISIBLE);
                    desc.setText(partDesc[1]);
                } else {
                    desc.setVisibility(View.GONE);
                    descImg.setVisibility(View.GONE);
                    divider.setVisibility(View.GONE);
                }
                rptNum.setText(String.format(locale, "%s x", partDesc[0]));
            }
            title.setText(titleS);
            itemView.setTag(id);
            /* popup menu */
            more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //creating a popup menu
                    PopupMenu popup = new PopupMenu(mContext, more);
                    //inflating menu from xml resource
                    popup.inflate(R.menu.reminder_card_menu);
                    //hide mark as done
                    popup.getMenu().findItem(R.id.set_as_done).setVisible(false);
                    popup.getMenu().findItem(R.id.set_as_finish).setVisible(false);
                    //adding click listener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.edit:
                                    mListener.onEditClick(position, ro.getId());
                                    return true;
                                case R.id.delete:
                                    mListener.onDeleteClick(position, ro.getId());
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
        }
    }
}
