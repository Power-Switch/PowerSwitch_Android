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

package eu.power_switch.gui.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.adapter.ConfigurationDialogTabAdapter;
import eu.power_switch.gui.fragment.configure_gateway.ConfigureGatewayDialogPage1;
import eu.power_switch.gui.fragment.configure_gateway.ConfigureGatewayDialogPage2;
import eu.power_switch.gui.fragment.configure_gateway.ConfigureGatewayDialogPage3;
import eu.power_switch.gui.fragment.configure_gateway.ConfigureGatewayDialogPage4Summary;
import eu.power_switch.gui.fragment.settings.GatewaySettingsFragment;
import eu.power_switch.shared.log.Log;

/**
 * Dialog to edit a Gateway
 */
public class ConfigureGatewayDialog extends ConfigurationDialogTabbed {

    /**
     * ID of existing Gateway to Edit
     */
    public static final String GATEWAY_ID_KEY = "GatewayId";

    private long gatewayId = -1;

    public static ConfigureGatewayDialog newInstance(long gatewayId) {
        Bundle args = new Bundle();
        args.putLong(GATEWAY_ID_KEY, gatewayId);

        ConfigureGatewayDialog fragment = new ConfigureGatewayDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void init(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("Opening " + getClass().getSimpleName() + "...");
    }

    @Override
    protected boolean initializeFromExistingData(Bundle arguments) {
        if (arguments != null && arguments.containsKey(GATEWAY_ID_KEY)) {
            gatewayId = arguments.getLong(GATEWAY_ID_KEY);
            setTabAdapter(new CustomTabAdapter(this, getChildFragmentManager(), getTargetFragment(), gatewayId));
            return true;
        } else {
            setTabAdapter(new CustomTabAdapter(this, getChildFragmentManager(), getTargetFragment()));
            return false;
        }
    }

    @Override
    protected int getDialogTitle() {
        return R.string.configure_gateway;
    }

    @Override
    protected void deleteExistingConfigurationFromDatabase() {
        new AlertDialog.Builder(getActivity()).setTitle(R.string.are_you_sure).setMessage(R.string
                .gateway_will_be_gone_forever)
                .setPositiveButton
                        (android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    DatabaseHandler.deleteGateway(gatewayId);
                                    GatewaySettingsFragment.sendGatewaysChangedBroadcast(getActivity());
                                    StatusMessageHandler.showInfoMessage(getTargetFragment(),
                                            R.string.gateway_removed, Snackbar.LENGTH_LONG);
                                } catch (Exception e) {
                                    StatusMessageHandler.showErrorMessage(getActivity(), e);
                                }

                                // close dialog
                                getDialog().dismiss();
                            }
                        }).setNeutralButton(android.R.string.cancel, null).show();
    }

    private static class CustomTabAdapter extends ConfigurationDialogTabAdapter {

        private ConfigurationDialogTabbed parentDialog;
        private long gatewayId;
        private ConfigurationDialogTabbedSummaryFragment setupFragment;
        private Fragment targetFragment;

        public CustomTabAdapter(ConfigurationDialogTabbed parentDialog, FragmentManager fm, Fragment targetFragment) {
            super(fm);
            this.parentDialog = parentDialog;
            this.gatewayId = -1;
            this.targetFragment = targetFragment;
        }

        public CustomTabAdapter(ConfigurationDialogTabbed parentDialog, FragmentManager fm, Fragment targetFragment, long id) {
            super(fm);
            this.parentDialog = parentDialog;
            this.gatewayId = id;
            this.targetFragment = targetFragment;
        }

        public ConfigurationDialogTabbedSummaryFragment getSummaryFragment() {
            return setupFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return parentDialog.getString(R.string.address);
                case 1:
                    return parentDialog.getString(R.string.ssids);
                case 2:
                    return parentDialog.getString(R.string.apartments);
                case 3:
                    return parentDialog.getString(R.string.summary);
            }

            return "" + (position + 1);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = null;

            switch (i) {
                case 0:
                    fragment = ConfigurationDialogPage.newInstance(ConfigureGatewayDialogPage1.class, parentDialog);
                    fragment.setTargetFragment(targetFragment, 0);
                    break;
                case 1:
                    fragment = ConfigurationDialogPage.newInstance(ConfigureGatewayDialogPage2.class, parentDialog);
                    fragment.setTargetFragment(targetFragment, 0);
                    break;
                case 2:
                    fragment = ConfigurationDialogPage.newInstance(ConfigureGatewayDialogPage3.class, parentDialog);
                    fragment.setTargetFragment(targetFragment, 0);
                    break;
                case 3:
                    fragment = ConfigurationDialogPage.newInstance(ConfigureGatewayDialogPage4Summary.class, parentDialog);
                    fragment.setTargetFragment(targetFragment, 0);

                    setupFragment = (ConfigurationDialogTabbedSummaryFragment) fragment;
                    break;
            }

            if (fragment != null && gatewayId != -1) {
                Bundle bundle = new Bundle();
                bundle.putLong(GATEWAY_ID_KEY, gatewayId);
                fragment.setArguments(bundle);
            }

            return fragment;
        }

        /**
         * @return the number of pages to display
         */
        @Override
        public int getCount() {
            return 4;
        }
    }
}