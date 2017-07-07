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

package eu.power_switch.gui.dialog.configuration.holder;

import android.text.TextUtils;

import java.util.Calendar;
import java.util.List;

import eu.power_switch.gui.dialog.configuration.ConfigurationHolder;
import eu.power_switch.shared.action.Action;
import eu.power_switch.timer.Timer;
import eu.power_switch.timer.WeekdayTimer;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Markus on 04.07.2017.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TimerConfigurationHolder extends ConfigurationHolder {

    private Timer timer;

    private boolean active = true;

    private String name;

    private Calendar executionTime;

    private int randomizerValue = 0;

    private long executionInterval = -1;

    private List<WeekdayTimer.Day> executionDays;

    private String executionType;

    private List<Action> actions;

    @Override
    public boolean isValid() {
        if (TextUtils.isEmpty(name)) {
            return false;
        }

        if (executionTime == null) {
            return false;
        }

        if (executionType == null) {
            return false;
        } else if (Timer.EXECUTION_TYPE_INTERVAL.equals(executionType)) {
            if (executionInterval == -1) {
                return false;
            }
        } else if (Timer.EXECUTION_TYPE_WEEKDAY.equals(executionType)) {
            if (executionDays == null || executionDays.isEmpty()) {
                return false;
            }
        }

        if (actions == null || actions.isEmpty()) {
            return false;
        }

        return true;
    }

}
