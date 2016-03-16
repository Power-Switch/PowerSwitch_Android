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

package eu.power_switch.gui.listener;

import android.support.annotation.CallSuper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;

/**
 * SpinnerInteractionListener used to react <b>only to user selections</b>
 * <p/>
 * Usage:
 * <p/>
 * Spinner spinner = (Spinner) findViewById(R.id.spinner);
 * SpinnerInteractionListener spinnerInteractionListener = new SpinnerInteractionListener() {
 *
 * @Override public void onItemSelectedByUser(AdapterView<?> parent, View view, int pos, long id) {
 * notifyConfigurationChanged();
 * }
 * @Override public void onNothingSelected(AdapterView<?> parent) {
 * }
 * };
 * spinner.setOnTouchListener(spinnerInteractionListener);
 * spinner.setOnItemSelectedListener(spinnerInteractionListener);
 * <p/>
 * <p/>
 * Created by Markus on 19.02.2016.
 */
public abstract class SpinnerInteractionListener implements AdapterView.OnItemSelectedListener, View.OnTouchListener {

    boolean userSelect = false;

    @Override
    @CallSuper
    public boolean onTouch(View v, MotionEvent event) {
        userSelect = true;
        return false;
    }

    /**
     * This method is always called, when a change to the spinner selection was made.
     * Note: If you override this method, you have to call super.onItemSelected(...).
     * Notice that in case of user selection this will trigger both onItemSelected, as well as onItemSelectedByUser
     * and in case of system selection both onItemSelected, as well as onItemSelectedBySystem.
     * It is therefore NOT RECOMMENDED to override this method.
     *
     * @param parent parent adapter view
     * @param view   view
     * @param pos    position
     * @param id     id
     */
    @Override
    @CallSuper
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if (userSelect) {
            onItemSelectedByUser(parent, view, pos, id);

            userSelect = false;
        } else {
            onItemSelectedBySystem(parent, view, pos, id);
        }
    }

    /**
     * Empty default implementation for less code bloat
     *
     * @param adapterView
     */
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    /**
     * This Method is only called, if the spinner selection was made by the system (and not a user)
     *
     * @param parent parent adapter view
     * @param view   view
     * @param pos    position
     * @param id     id
     */
    protected void onItemSelectedBySystem(AdapterView<?> parent, View view, int pos, long id) {
    }

    /**
     * This Method is only called, if the spinner selection was made by a user (and not in code)
     *
     * @param parent parent adapter view
     * @param view   view
     * @param pos    position
     * @param id     id
     */
    public void onItemSelectedByUser(AdapterView<?> parent, View view, int pos, long id) {
    }

}
