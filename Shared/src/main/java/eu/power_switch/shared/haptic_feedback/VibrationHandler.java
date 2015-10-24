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

package eu.power_switch.shared.haptic_feedback;

import android.content.Context;
import android.os.Vibrator;

/**
 * Class to handle everything related to the vibration motor
 * <p/>
 * Created by Markus on 11.10.2015.
 */
public class VibrationHandler {

    /**
     * @param context      any suitable context
     * @param milliseconds time in milliseconds to vibrate
     */
    public static void vibrate(Context context, long milliseconds) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(milliseconds);
        }
    }

    /**
     * @param context any suitable context
     * @param pattern an array of longs of times for which to turn the vibrator on or off
     * @param repeat  the index into pattern at which to repeat, or -1 if you don't want to repeat
     */
    public static void vibrate(Context context, long[] pattern, int repeat) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(pattern, repeat);
        }
    }

    /**
     * Cancels the current vibration
     *
     * @param context any suitable context
     */
    public static void cancel(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.cancel();
        }
    }

}
