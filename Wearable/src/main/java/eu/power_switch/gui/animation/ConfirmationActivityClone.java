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

package eu.power_switch.gui.animation;

import android.animation.StateListAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.wearable.R.drawable;
import android.support.wearable.R.id;
import android.support.wearable.R.layout;
import android.support.wearable.view.ActionLabel;
import android.support.wearable.view.ActionPage;
import android.widget.TextView;

/**
 * Custom implementation of ConfirmationActivity
 * <p/>
 * Used to shorten animation duration which cant be changed below about 1 second in the official implementation
 * <p/>
 * Created by Markus on 25.08.2015.
 */
@TargetApi(21)
public class ConfirmationActivityClone extends Activity {
    public static final String EXTRA_MESSAGE = "message";
    public static final String EXTRA_ANIMATION_TYPE = "animation_type";
    public static final int SUCCESS_ANIMATION = 1;
    public static final int OPEN_ON_PHONE_ANIMATION = 2;
    public static final int FAILURE_ANIMATION = 3;
    private static final int TEXT_FADE_OFFSET_TIME_MS = 50;
    private static final int OPEN_ON_PHONE_ANIMATION_DURATION_MS = 1666;
    private static final int CONFIRMATION_ANIMATION_DURATION_MS = 1666;
    private ActionPage mActionPage;

    public ConfirmationActivityClone() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = this.getIntent();
        int animationType = intent.getIntExtra("animation_type", SUCCESS_ANIMATION);
        String message = intent.getStringExtra("message");
        this.mActionPage = new ActionPage(this);
        long displayDurationMs;
        if (animationType == FAILURE_ANIMATION) {
            this.setContentView(layout.error_layout);
            TextView animatedDrawable = (TextView) this.findViewById(id.message);
            animatedDrawable.setText(message);
            displayDurationMs = 2000L;
        } else {
            this.mActionPage.setColor(0);
            this.mActionPage.setStateListAnimator(new StateListAnimator());
            this.mActionPage.setImageScaleMode(ActionPage.SCALE_MODE_CENTER);
            this.setContentView(this.mActionPage);
            if (message != null) {
                this.mActionPage.setText(message);
            }

            Drawable animatedDrawable1;
            switch (animationType) {
                case SUCCESS_ANIMATION:
                    animatedDrawable1 = this.getResources().getDrawable(drawable.generic_confirmation_animation);
                    displayDurationMs = 1666L;
                    break;
                case OPEN_ON_PHONE_ANIMATION:
                    animatedDrawable1 = this.getResources().getDrawable(drawable.open_on_phone_animation);
                    displayDurationMs = 1666L;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown type of animation: " + animationType);
            }

            this.mActionPage.setImageDrawable(animatedDrawable1);
            final ActionLabel label = this.mActionPage.getLabel();
            long fadeDuration = label.animate().getDuration();
            final long fadeOutDelay = Math.max(0L, displayDurationMs - 2L * (50L + fadeDuration));
            ((Animatable) animatedDrawable1).start();
            label.setAlpha(0.0F);
            label.animate().alpha(1.0F).setStartDelay(50L).withEndAction(new Runnable() {
                public void run() {
                    ConfirmationActivityClone.this.finish();
                    ConfirmationActivityClone.this.overridePendingTransition(0, android.R.anim.fade_out);
                }
            });
        }

        this.mActionPage.setKeepScreenOn(true);
    }

    private static long getAnimationDuration(AnimationDrawable animation) {
        int count = animation.getNumberOfFrames();
        long duration = 0L;

        for (int i = 0; i < count; ++i) {
            duration += (long) animation.getDuration(i);
        }

        return duration;
    }
}
