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

package eu.power_switch.gui.fragment.configure_gateway;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.dialog.configuration.ConfigurationDialogPage;
import eu.power_switch.gui.dialog.configuration.holder.GatewayConfigurationHolder;
import eu.power_switch.shared.constants.DatabaseConstants;
import eu.power_switch.shared.event.ConfigurationChangedEvent;

/**
 * "Name" Fragment used in Configure Apartment Dialog
 * <p/>
 * Created by Markus on 16.08.2015.
 */
public class ConfigureGatewayDialogPage4Summary extends ConfigurationDialogPage<GatewayConfigurationHolder> {

    @BindView(R.id.textView_name)
    TextView name;
    @BindView(R.id.textView_model)
    TextView model;
    @BindView(R.id.textView_localAddress)
    TextView localAddress;
    @BindView(R.id.textView_wanAddress)
    TextView wanAddress;
    @BindView(R.id.textView_ssids)
    TextView ssids;
    @BindView(R.id.textView_associatedApartments)
    TextView associatedApartments;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        updateUI();

        return rootView;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_fragment_configure_gateway_page_4_summary;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onConfigurationChanged(ConfigurationChangedEvent e) {
        updateUI();
    }

    private void updateUI() {
        name.setText(getConfiguration().getName());
        model.setText(getConfiguration().getModel());

        String  localAddr = getConfiguration().getLocalAddress();
        Integer localPort = getConfiguration().getLocalPort();
        if (!TextUtils.isEmpty(localAddr)) {
            if (!DatabaseConstants.INVALID_GATEWAY_PORT.equals(localPort)) {
                localAddress.setText(localAddr + ":" + localPort);
            } else {
                localAddress.setText(localAddr);
            }
        } else {
            localAddress.setText("");
        }

        String  wanAddr = getConfiguration().getWanAddress();
        Integer wanPort = getConfiguration().getWanPort();
        if (!TextUtils.isEmpty(wanAddr)) {
            if (!DatabaseConstants.INVALID_GATEWAY_PORT.equals(wanPort)) {
                wanAddress.setText(wanAddr + ":" + wanPort);
            } else {
                wanAddress.setText(wanAddr);
            }
        } else {
            wanAddress.setText("");
        }

        String ssidText = "";
        String[] ssids = getConfiguration().getSsids()
                .toArray(new String[0]);
        for (int i = 0, currentSsidsSize = ssids.length; i < currentSsidsSize; i++) {
            String ssid = ssids[i];
            ssidText += ssid;

            if (i < currentSsidsSize - 1) {
                ssidText += "\n";
            }
        }
        this.ssids.setText(ssidText);

        String apartmentsText = "";
        for (int i = 0, currentApartmentNamesSize = getConfiguration().getApartmentIds()
                .size(); i < currentApartmentNamesSize; i++) {
            Long apartmentId = getConfiguration().getApartmentIds()
                    .get(i);

            try {
                apartmentsText += DatabaseHandler.getApartmentName(apartmentId);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (i < currentApartmentNamesSize - 1) {
                apartmentsText += "\n";
            }
        }
        associatedApartments.setText(apartmentsText);
    }

}
