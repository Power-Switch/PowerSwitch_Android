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
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

/**
 * FAB Behaviour
 * Lets FAB Buttons disappear if a scroll down event is caught, and lets them reappear if a scroll up event is
 * caught.
 * <p/>
 * Created by Markus on 15.07.2015.
 */
public class ScrollAwareFABBehavior extends FloatingActionButton.Behavior {

    private static final android.view.animation.Interpolator INTERPOLATOR    = new FastOutSlowInInterpolator();
    private              boolean                             mIsAnimatingOut = false;

    public ScrollAwareFABBehavior(Context context, AttributeSet attrs) {
        super();
    }


    /**
     * Same animation that FloatingActionButton.Behavior uses to
     * hide the FAB when the AppBarLayout exits
     *
     * @param floatingActionButton FAB
     */
    private void animateOut(final FloatingActionButton floatingActionButton) {
//        if (smartphonePreferencesHandler.get(KEY_USE_OPTIONS_MENU_INSTEAD_OF_FAB)) {
//            return;
//        }

        if (Build.VERSION.SDK_INT >= 14) {
            ViewCompat.animate(floatingActionButton)
                    .scaleX(0.0F)
                    .scaleY(0.0F)
                    .alpha(0.0F)
                    .setInterpolator(INTERPOLATOR)
                    .withLayer()
                    .setListener(new ViewPropertyAnimatorListener() {
                        public void onAnimationStart(View view) {
                            ScrollAwareFABBehavior.this.mIsAnimatingOut = true;
                        }

                        public void onAnimationCancel(View view) {
                            ScrollAwareFABBehavior.this.mIsAnimatingOut = false;
                        }

                        public void onAnimationEnd(View view) {
                            ScrollAwareFABBehavior.this.mIsAnimatingOut = false;
                            view.setVisibility(View.GONE);
                        }
                    })
                    .start();
        } else {
            Animation anim = AnimationUtils.loadAnimation(floatingActionButton.getContext(), android.R.anim.fade_out);
            anim.setInterpolator(INTERPOLATOR);
            anim.setDuration(200L);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    ScrollAwareFABBehavior.this.mIsAnimatingOut = true;
                }

                public void onAnimationEnd(Animation animation) {
                    ScrollAwareFABBehavior.this.mIsAnimatingOut = false;
                    floatingActionButton.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(final Animation animation) {
                }
            });
            floatingActionButton.startAnimation(anim);
        }
    }

    /**
     * Same animation that FloatingActionButton.Behavior
     * uses to show the FAB when the AppBarLayout enters
     *
     * @param floatingActionButton FAB
     */
    //
    private void animateIn(FloatingActionButton floatingActionButton) {
//        if (smartphonePreferencesHandler.get(KEY_USE_OPTIONS_MENU_INSTEAD_OF_FAB)) {
//            return;
//        }

        floatingActionButton.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= 14) {
            ViewCompat.animate(floatingActionButton)
                    .scaleX(1.0F)
                    .scaleY(1.0F)
                    .alpha(1.0F)
                    .setInterpolator(INTERPOLATOR)
                    .withLayer()
                    .setListener(null)
                    .start();
        } else {
            Animation anim = AnimationUtils.loadAnimation(floatingActionButton.getContext(), android.R.anim.fade_in);
            anim.setDuration(200L);
            anim.setInterpolator(INTERPOLATOR);
            floatingActionButton.startAnimation(anim);
        }
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View directTargetChild, View target,
                                       int nestedScrollAxes) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL || super.onStartNestedScroll(coordinatorLayout,
                child,
                directTargetChild,
                target,
                nestedScrollAxes);
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View target, int dxConsumed, int dyConsumed,
                               int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);

        if (dyConsumed > 0 && !this.mIsAnimatingOut && child.getVisibility() == View.VISIBLE) {
            animateOut(child);
        } else if (dyConsumed < 0 && child.getVisibility() != View.VISIBLE) {
            animateIn(child);
        }
    }

}
