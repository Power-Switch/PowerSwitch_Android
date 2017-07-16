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
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.action.Action;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.persistence.PersistanceHandler;
import eu.power_switch.persistence.shared_preferences.SmartphonePreferencesHandler;
import eu.power_switch.timer.Timer;
import eu.power_switch.timer.WeekdayTimer;
import eu.power_switch.timer.alarm.AndroidAlarmHandler;

/**
 * Adapter to visualize Timer items in RecyclerView
 * <p/>
 * Created by Markus on 27.07.2015.
 */
public class TimerRecyclerViewAdapter extends RecyclerView.Adapter<TimerRecyclerViewAdapter.ViewHolder> {
    private final PersistanceHandler           persistanceHandler;
    private final AndroidAlarmHandler          androidAlarmHandler;
    private final ArrayList<Timer>             timers;
    private final Context                      context;
    private final SmartphonePreferencesHandler smartphonePreferencesHandler;
    private final StatusMessageHandler         statusMessageHandler;

    private OnItemClickListener     onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    public TimerRecyclerViewAdapter(Context context, PersistanceHandler persistanceHandler, AndroidAlarmHandler androidAlarmHandler,
                                    SmartphonePreferencesHandler smartphonePreferencesHandler, StatusMessageHandler statusMessageHandler,
                                    ArrayList<Timer> timers) {
        this.timers = timers;
        this.context = context;
        this.persistanceHandler = persistanceHandler;
        this.androidAlarmHandler = androidAlarmHandler;
        this.smartphonePreferencesHandler = smartphonePreferencesHandler;
        this.statusMessageHandler = statusMessageHandler;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @Override
    public TimerRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.list_item_timer, parent, false);
        return new TimerRecyclerViewAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final TimerRecyclerViewAdapter.ViewHolder holder, int position) {
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
                    if (holder.getAdapterPosition() == RecyclerView.NO_POSITION) {
                        return false;
                    }
                    onItemLongClickListener.onItemLongClick(v, holder.getAdapterPosition());
                }
                return true;
            }
        });

        holder.name.setText(timer.getName());

        long hourOfDay = timer.getExecutionTime()
                .get(Calendar.HOUR_OF_DAY);
        long minute = timer.getExecutionTime()
                .get(Calendar.MINUTE);

        String executionTimeText = "";
        if (hourOfDay < 10) {
            executionTimeText += "0";
        }
        executionTimeText += hourOfDay + ":";
        if (minute < 10) {
            executionTimeText += "0";
        }
        executionTimeText += minute;

        if (timer.getRandomizerValue() != 0) {
            executionTimeText += " " + context.getString(R.string.plus_minus_minutes, timer.getRandomizerValue());
        }

        holder.executionTime.setText(executionTimeText);

        String executionDaysText = "";
        if (timer instanceof WeekdayTimer) {
            WeekdayTimer weekdayTimer = (WeekdayTimer) timer;

            List<WeekdayTimer.Day> executionDays = weekdayTimer.getExecutionDays();
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
                // check if user pressed the button
                if (buttonView.isPressed()) {
                    try {
                        if (isChecked) {
                            persistanceHandler.enableTimer(timer.getId());
                            androidAlarmHandler.createAlarm(timer);
                        } else {
                            persistanceHandler.disableTimer(timer.getId());
                            androidAlarmHandler.cancelAlarm(timer);
                        }
                        timer.setActive(isChecked);
                    } catch (Exception e) {
                        statusMessageHandler.showErrorMessage(context, e);
                    }
                }
            }
        });

        String         inflaterString = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater inflater       = (LayoutInflater) context.getSystemService(inflaterString);

        holder.linearLayoutTimerActions.removeAllViews();
        try {
            for (Action action : timer.getActions()) {

                String readableString = Action.createReadableString(action, persistanceHandler);

                AppCompatTextView textViewActionDescription = new AppCompatTextView(context);

                textViewActionDescription.setText(readableString);
                textViewActionDescription.setPadding(0, 0, 0, 4);
                holder.linearLayoutTimerActions.addView(textViewActionDescription);
            }
        } catch (Exception e) {
            throw new RuntimeException();
        }

        // collapse timer
        if (smartphonePreferencesHandler.get(SmartphonePreferencesHandler.KEY_AUTO_COLLAPSE_TIMERS)) {
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

    public class ViewHolder extends ButterKnifeViewHolder {
        @BindView(R.id.linearLayout_timerDescription)
        LinearLayout linearLayoutTimerDescription;
        @BindView(R.id.linearLayout_timerActions)
        LinearLayout linearLayoutTimerActions;
        @BindView(R.id.txt_timer_name)
        TextView     name;
        @BindView(R.id.txt_timer_execution_time)
        TextView     executionTime;
        @BindView(R.id.txt_timer_execution_days)
        TextView     executionDays;
        @BindView(R.id.list_footer)
        LinearLayout footer;

        @BindView(R.id.switch_timer_status)
        android.support.v7.widget.SwitchCompat timerStatus;

        public ViewHolder(final View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        if (getAdapterPosition() == RecyclerView.NO_POSITION) {
                            return;
                        }
                        onItemClickListener.onItemClick(itemView, getAdapterPosition());
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (onItemLongClickListener != null) {
                        if (getAdapterPosition() == RecyclerView.NO_POSITION) {
                            return false;
                        }
                        onItemLongClickListener.onItemLongClick(itemView, getAdapterPosition());
                    }
                    return true;
                }
            });
        }
    }
}
