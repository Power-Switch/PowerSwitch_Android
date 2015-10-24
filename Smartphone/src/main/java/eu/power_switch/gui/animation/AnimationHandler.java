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

import android.content.Context;
import android.os.Build;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import eu.power_switch.R;

/**
 * Created by Markus on 04.07.2015.
 */
public class AnimationHandler {

    private AnimationHandler() {
    }

    public static Animation getRotationClockwiseAnimation(Context context) {
        Animation anim = AnimationUtils.loadAnimation(context, R.anim.rotate);
        return anim;
    }

//    public static void enterCircularRevealAnimation(final View view) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            // get the center for the clipping circle
//            int cx = view.getMeasuredWidth() / 2;
//            int cy = view.getMeasuredHeight() / 2;
//
//            // get the final radius for the clipping circle
//            int initialRadius = 0;
//            int finalRadius = Math.max(view.getWidth(), view.getHeight()) / 2;
//
//            // create the animator for this view (the start radius is zero)
//            Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, initialRadius, finalRadius);
//
//            // make the view visible and start the animation
//            view.setVisibility(View.VISIBLE);
//            anim.start();
//        } else {
//            // just make the view visible if animations aren't supported yet
//            view.setVisibility(View.VISIBLE);
//        }
//    }
//
//    public static void exitCircularRevealAnimation(final View view) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            // get the center for the clipping circle
//            int cx = view.getMeasuredWidth() / 2;
//            int cy = view.getMeasuredHeight() / 2;
//
//            // get the initial radius for the clipping circle
//            int initialRadius = view.getWidth() / 2;
//            int finalRadius = 0;
//
//            // create the animation (the final radius is zero)
//            Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, initialRadius, finalRadius);
//
//            // make the view invisible when the animation is done
//            anim.addListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    super.onAnimationEnd(animation);
//                    view.setVisibility(View.INVISIBLE);
//                }
//            });
//
//            // start the animation
//            anim.start();
//        } else {
//            // just make the view visible if animations aren't supported yet
//            view.setVisibility(View.INVISIBLE);
//        }
//    }

    public static boolean checkTargetApi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return true;
        } else {
            return false;
        }
    }
//
//    @TargetApi(21)
//    public static Transition.TransitionListener getDefaultRevealAnimationTransistionListener(final View v) {
//        Transition.TransitionListener enterTransitionListener = new Transition.TransitionListener() {
//            @Override
//            public void onTransitionStart(Transition transition) {
//            }
//
//            @Override
//            public void onTransitionEnd(Transition transition) {
//                enterCircularRevealAnimation(v);
//            }
//
//            @Override
//            public void onTransitionCancel(Transition transition) {
//            }
//
//            @Override
//            public void onTransitionPause(Transition transition) {
//            }
//
//            @Override
//            public void onTransitionResume(Transition transition) {
//            }
//        };
//
//        return enterTransitionListener;
//    }

}
