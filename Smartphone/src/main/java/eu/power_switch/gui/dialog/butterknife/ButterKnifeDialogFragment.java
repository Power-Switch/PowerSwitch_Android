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

package eu.power_switch.gui.dialog.butterknife;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.persistence.preferences.SmartphonePreferencesHandler;

/**
 * Base class for a DialogFragment backed by ButterKnife
 * <p>
 * Created by Markus on 30.06.2017.
 */
public abstract class ButterKnifeDialogFragment extends DialogFragment {

    @Inject
    protected StatusMessageHandler statusMessageHandler;

    @Inject
    protected SmartphonePreferencesHandler smartphonePreferencesHandler;

    protected View     rootView;
    private   Unbinder unbinder;

    @NonNull
    @Override
    @CallSuper
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        rootView = inflater.inflate(getLayoutRes(), null);

        unbinder = ButterKnife.bind(this, rootView);

        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    /**
     * @return The layout resource to use for this fragment
     */
    @LayoutRes
    protected abstract int getLayoutRes();

}
