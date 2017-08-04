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

package eu.power_switch.gui.adapter;

import android.content.Context;
import android.os.Build;
import android.view.View;

import eu.power_switch.R;
import eu.power_switch.gui.animation.AnimationHandler;
import eu.power_switch.shared.butterknife.ButterKnifeViewHolder;

/**
 * Created by Markus on 24.07.2017.
 */
public abstract class SwipeDismissViewHolder extends ButterKnifeViewHolder implements ItemTouchHelperViewHolder {

    Context context;

    SwipeDismissViewHolder(View itemView) {
        super(itemView);
        context = itemView.getContext();
    }

    @Override
    public void onItemSelected() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            float toElevation = context.getResources()
                    .getDimension(R.dimen.list_element_elevation_while_moving);
            AnimationHandler.animateElevation(getItemLayout(), 0, toElevation, 200);
        }
    }

    @Override
    public void onItemClear() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            float fromElevation = context.getResources()
                    .getDimension(R.dimen.list_element_elevation_while_moving);
            AnimationHandler.animateElevation(getItemLayout(), fromElevation, 0, 200);
        }
    }

    protected abstract View getItemLayout();

}
