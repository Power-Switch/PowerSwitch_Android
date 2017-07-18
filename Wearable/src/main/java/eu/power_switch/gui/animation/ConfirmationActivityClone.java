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

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.ConfirmationOverlay;
import android.util.SparseIntArray;


/**
 * Custom implementation of ConfirmationActivity
 * <p/>
 * Used to shorten animation duration which cant be changed below about 1 second in the official implementation
 * <p/>
 * Created by Markus on 25.08.2015.
 */
@TargetApi(21)
public class ConfirmationActivityClone extends Activity implements ConfirmationOverlay.FinishedAnimationListener {

    public static final  String         EXTRA_MESSAGE              = "android.support.wearable.activity.extra.MESSAGE";
    public static final  String         EXTRA_ANIMATION_TYPE       = "android.support.wearable.activity.extra.ANIMATION_TYPE";
    public static final  int            SUCCESS_ANIMATION          = 1;
    public static final  int            OPEN_ON_PHONE_ANIMATION    = 2;
    public static final  int            FAILURE_ANIMATION          = 3;
    private static final SparseIntArray CONFIRMATION_OVERLAY_TYPES = new SparseIntArray();

    static {
        CONFIRMATION_OVERLAY_TYPES.append(1, 0);
        CONFIRMATION_OVERLAY_TYPES.append(2, 2);
        CONFIRMATION_OVERLAY_TYPES.append(3, 1);
    }

    public ConfirmationActivityClone() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTheme(android.support.wearable.R.style.ConfirmationActivity);
        Intent intent        = this.getIntent();
        int    requestedType = intent.getIntExtra("android.support.wearable.activity.extra.ANIMATION_TYPE", SUCCESS_ANIMATION);
        if (CONFIRMATION_OVERLAY_TYPES.indexOfKey(requestedType) < 0) {
            throw new IllegalArgumentException((new StringBuilder(38)).append("Unknown type of animation: ")
                    .append(requestedType)
                    .toString());
        } else {
            int    type    = CONFIRMATION_OVERLAY_TYPES.get(requestedType);
            String message = intent.getStringExtra("android.support.wearable.activity.extra.MESSAGE");
            (new ConfirmationOverlay()).setType(type)
                    .setMessage(message)
                    .setFinishedAnimationListener(this)
                    .showOn(this);
        }
    }

    public void onAnimationFinished() {
        this.finish();
    }
}
