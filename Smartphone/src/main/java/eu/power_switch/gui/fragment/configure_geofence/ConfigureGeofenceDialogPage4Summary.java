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

package eu.power_switch.gui.fragment.configure_geofence;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.action.Action;
import eu.power_switch.event.ConfigurationChangedEvent;
import eu.power_switch.gui.dialog.configuration.ConfigurationDialogPage;
import eu.power_switch.gui.dialog.configuration.holder.GeofenceConfigurationHolder;

/**
 * Created by Markus on 29.01.2016.
 */
public class ConfigureGeofenceDialogPage4Summary extends ConfigurationDialogPage<GeofenceConfigurationHolder> {

    @BindView(R.id.textView_name)
    TextView  textViewName;
    @BindView(R.id.textView_location)
    TextView  textViewLocation;
    @BindView(R.id.imageView_locationSnapshot)
    ImageView imageViewLocationSnapshot;
    @BindView(R.id.textView_enter_actions)
    TextView  textViewEnterActions;
    @BindView(R.id.textView_exit_actions)
    TextView  textViewExitActions;
    @BindView(R.id.textView_geofence_radius)
    TextView  textViewGeofenceRadius;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        updateUi();

        return rootView;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_fragment_configure_geofence_page_4_summary;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onConfigurationChanged(ConfigurationChangedEvent e) {
        updateUi();
    }

    private void updateUi() {
        String name = getConfiguration().getName();
        if (name != null) {
            textViewName.setText(name);
        }

        LatLng location = getConfiguration().getLocation();
        if (location != null) {
            textViewLocation.setText(location.toString());
        }

        Bitmap snapshot = getConfiguration().getSnapshot();
        if (snapshot != null) {
            imageViewLocationSnapshot.setImageBitmap(snapshot);
        }

        double radius = getConfiguration().getRadius();
        if (radius != -1) {
            textViewGeofenceRadius.setText(String.format(Locale.getDefault(), "%d m", (int) radius));
        }

        String       enterActionText = "";
        List<Action> enterActions    = getConfiguration().getEnterActions();
        if (enterActions != null) {
            for (Action action : enterActions) {
                enterActionText += action.toString() + "\n";
            }
        }
        textViewEnterActions.setText(enterActionText);

        String       exitActionText = "";
        List<Action> exitActions    = getConfiguration().getExitActions();
        if (exitActions != null) {
            for (Action action : exitActions) {
                exitActionText += action.toString() + "\n";
            }
        }
        textViewExitActions.setText(exitActionText);
    }

}
