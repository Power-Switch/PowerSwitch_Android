/*
 *  PowerSwitch by Max Rosin & Markus Ressel
 *  Copyright (C) 2015  Markus Ressel
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.gui.animation;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.StringRes;

/**
 * Class to have easy access to default Android Wear response animations
 * <p/>
 * Created by Markus on 24.08.2015.
 */
public abstract class ActionResponse {

    /**
     * Show success animation in a fade in/out animation
     *
     * @param context any suitable context
     * @param message
     */
    public static void showSuccessAnimation(Context context, String message) {
        Intent intent = new Intent(context, android.support.wearable.activity.ConfirmationActivity.class);
        intent.putExtra(android.support.wearable.activity.ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                android.support.wearable.activity.ConfirmationActivity.SUCCESS_ANIMATION);
        intent.putExtra(android.support.wearable.activity.ConfirmationActivity.EXTRA_MESSAGE, message);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(context, android.R.anim.fade_in, android.R.anim.fade_out);
        context.startActivity(intent, activityOptions.toBundle());
    }

    /**
     * Show success animation in a fade in/out animation
     *
     * @param context    any suitable context
     * @param resourceId String Resource ID
     */
    public static void showSuccessAnimation(Context context, @StringRes int resourceId) {
        showSuccessAnimation(context, context.getString(resourceId));
    }

    /**
     * Show success animation in a fade in/out animation
     *
     * @param context any suitable context
     */
    public static void showSuccessAnimation(Context context) {
        Intent intent = new Intent(context, ConfirmationActivityClone.class);
        intent.putExtra(ConfirmationActivityClone.EXTRA_ANIMATION_TYPE, ConfirmationActivityClone.SUCCESS_ANIMATION);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(context, android.R.anim.fade_in, android.R.anim.fade_out);
        context.startActivity(intent, activityOptions.toBundle());

//        Intent intent = new Intent(context, ConfirmationActivity.class);
//        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
//                ConfirmationActivity.SUCCESS_ANIMATION);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(context,
//                android.R.anim.fade_in, android.R.anim.fade_out);
//        context.startActivity(intent, activityOptions.toBundle());
    }

    /**
     * Show failure animation in a fade in/out animation
     *
     * @param context any suitable context
     * @param message text message
     */
    public static void showFailureAnimation(Context context, String message) {
        Intent intent = new Intent(context, android.support.wearable.activity.ConfirmationActivity.class);
        intent.putExtra(android.support.wearable.activity.ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                android.support.wearable.activity.ConfirmationActivity.FAILURE_ANIMATION);
        intent.putExtra(android.support.wearable.activity.ConfirmationActivity.EXTRA_MESSAGE, message);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(context, android.R.anim.fade_in, android.R.anim.fade_out);
        context.startActivity(intent, activityOptions.toBundle());
    }

    /**
     * Show failure animation in a fade in/out animation
     *
     * @param context    any suitable context
     * @param resourceId String Resource ID
     */
    public static void showFailureAnimation(Context context, @StringRes int resourceId) {
        showFailureAnimation(context, context.getString(resourceId));
    }

    /**
     * Show failure animation in a fade in/out animation
     *
     * @param context any suitable context
     */
    public static void showFailureAnimation(Context context) {
        Intent intent = new Intent(context, android.support.wearable.activity.ConfirmationActivity.class);
        intent.putExtra(android.support.wearable.activity.ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                android.support.wearable.activity.ConfirmationActivity.FAILURE_ANIMATION);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(context, android.R.anim.fade_in, android.R.anim.fade_out);
        context.startActivity(intent, activityOptions.toBundle());
    }

}
