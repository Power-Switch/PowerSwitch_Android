/*
 *     PowerSwitch by Max Rosin & Markus Ressel
 *     Copyright (C) 2015  Markus Ressel
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.gui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.settings.SharedPreferencesHandler;
import eu.power_switch.timer.Timer;
import eu.power_switch.timer.WeekdayTimer;
import eu.power_switch.timer.action.TimerAction;
import eu.power_switch.timer.alarm.AlarmHandler;

/**
 * Adapter to visualize Timer items in RecyclerView
 * <p/>
 * Created by Markus on 27.07.2015.
 */
public class TimerRecyclerViewAdapter extends RecyclerView.Adapter<TimerRecyclerViewAdapter.ViewHolder> {
    private ArrayList<Timer> timers;
    private Context context;
    private View rootView;

    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    public TimerRecyclerViewAdapter(Context context, View rootView, ArrayList<Timer> timers) {
        this.timers = timers;
        this.context = context;
        this.rootView = rootView;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @Override
    public TimerRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.list_item_timer, parent, false);
        return new TimerRecyclerViewAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TimerRecyclerViewAdapter.ViewHolder holder, final int position) {
        final Timer timer = timers.get(position);

        final LinearLayout linearLayoutDescription = holder.linearLayoutTimerDescription;
        holder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (linearLayoutDescription.getVisibility() == View.VISIBLE) {
                    linearLayoutDescription.setVisibility(View.GONE);
                } else {
                    linearLayoutDescription.setVisibility(View.VISIBLE);
                }
            }
        });

        holder.name.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (onItemLongClickListener != null) {
                    onItemLongClickListener.onItemLongClick(v, position);
                }
                return true;
            }
        });

        holder.name.setText(timer.getName());

        long hourOfDay = timer.getExecutionTime().get(Calendar.HOUR_OF_DAY);
        long minute = timer.getExecutionTime().get(Calendar.MINUTE);

        String executionTimeText = "";
        if (hourOfDay < 10) {
            executionTimeText += "0";
        }
        executionTimeText += hourOfDay + ":";
        if (minute < 10) {
            executionTimeText += "0";
        }
        executionTimeText += minute;

        holder.executionTime.setText(executionTimeText);

        String executionDaysText = "";
        if (timer instanceof WeekdayTimer) {
            WeekdayTimer weekdayTimer = (WeekdayTimer) timer;

            ArrayList<WeekdayTimer.Day> executionDays = weekdayTimer.getExecutionDays();
            for (int i = 0; i < executionDays.size(); i++) {
                if (i < executionDays.size() - 1) {
                    executionDaysText += WeekdayTimer.Day.getWeekdayName(context, executionDays.get(i)) + ", ";
                } else {
                    executionDaysText += WeekdayTimer.Day.getWeekdayName(context, executionDays.get(i));
                }
            }
        }
        holder.executionDays.setText(executionDaysText);

        holder.timerStatus.setChecked(timer.isActive());
        holder.timerStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    DatabaseHandler.enableTimer(timer.getId());
                    AlarmHandler.createAlarm(context, timer);
                } else {
                    DatabaseHandler.disableTimer(timer.getId());
                    AlarmHandler.cancelAlarm(context, timer);
                }
            }
        });

        String inflaterString = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(inflaterString);

        holder.linearLayoutTimerActions.removeAllViews();
        for (TimerAction timerAction : timer.getActions()) {
            LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.list_item_timer_action, null, false);
            TextView textViewAction = (TextView) linearLayout.findViewById(R.id.txt_timer_action);
            textViewAction.setText(timerAction.toString());
            holder.linearLayoutTimerActions.addView(linearLayout);
        }


        SharedPreferencesHandler sharedPreferencesHandler = new SharedPreferencesHandler(context);
        // collapse timer
        if (sharedPreferencesHandler.getAutoCollapseTimers()) {
            linearLayoutDescription.setVisibility(View.GONE);
        }

        if (position == getItemCount() - 1) {
            holder.footer.setVisibility(View.VISIBLE);
        } else {
            holder.footer.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return timers.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View itemView, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout linearLayoutTimerDescription;
        public LinearLayout linearLayoutTimerActions;
        public TextView name;
        public TextView executionTime;
        public TextView executionDays;
        public android.support.v7.widget.SwitchCompat timerStatus;
        public LinearLayout footer;

        public ViewHolder(final View itemView) {
            super(itemView);
            this.linearLayoutTimerDescription = (LinearLayout) itemView.findViewById(R.id
                    .linearLayout_timerDescription);
            this.linearLayoutTimerActions = (LinearLayout) itemView.findViewById(R.id.linearLayout_timerActions);
            this.name = (TextView) itemView.findViewById(R.id.txt_timer_name);
            this.executionTime = (TextView) itemView.findViewById(R.id.txt_timer_execution_time);
            this.executionDays = (TextView) itemView.findViewById(R.id.txt_timer_execution_days);
            this.timerStatus = (android.support.v7.widget.SwitchCompat) itemView.findViewById(R.id.switch_timer_status);
            this.footer = (LinearLayout) itemView.findViewById(R.id.list_footer);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(itemView, getLayoutPosition());
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (onItemLongClickListener != null) {
                        onItemLongClickListener.onItemLongClick(itemView, getLayoutPosition());
                    }
                    return false;
                }
            });
        }
    }
}
