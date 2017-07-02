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

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

import java.util.Locale;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.google_play_services.geofence.GeofenceApiHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.dialog.eventbus.EventBusSupportDialogFragment;
import eu.power_switch.notification.NotificationHandler;
import eu.power_switch.settings.DeveloperPreferencesHandler;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

/**
 * Hidden Dialog Menu to access developer options
 */
public class DeveloperOptionsDialog extends EventBusSupportDialogFragment {

    private GeofenceApiHandler geofenceApiHandler;
    @BindView(R.id.checkBox_playStoreMode)
    CheckBox checkBox_playStoreMode;
    @BindView(R.id.button_resetShowcases)
    Button   resetShowcasesButton;
    @BindView(R.id.button_removeAllGeofences)
    Button   removeAllGeofences;
    @BindView(R.id.button_forceUnknownExceptionDialog)
    Button   forceUnknownExceptionDialog;
    @BindView(R.id.button_forceUnhandledException)
    Button   forceUnhandledException;
    @BindView(R.id.spinner_language)
    Spinner  spinnerLanguage;
    @BindView(R.id.checkBox_forceLanguage)
    CheckBox checkBoxForceLanguage;
    @BindView(R.id.checkBox_forceFabricEnabled)
    CheckBox checkBox_forceFabricEnabled;
    @BindView(R.id.button_testNotification)
    Button   buttonTestNotification;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        geofenceApiHandler = new GeofenceApiHandler(getActivity());

        checkBox_playStoreMode.setChecked(DeveloperPreferencesHandler.getPlayStoreMode());
        checkBox_playStoreMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DeveloperPreferencesHandler.setPlayStoreMode(isChecked);
            }
        });

        resetShowcasesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialShowcaseView.resetAll(getContext());
                SmartphonePreferencesHandler.set(SmartphonePreferencesHandler.KEY_SHOULD_ASK_SEND_ANONYMOUS_CRASH_DATA, true);
            }
        });

        removeAllGeofences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                geofenceApiHandler.removeAllGeofences();
            }
        });

        forceUnknownExceptionDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StatusMessageHandler.showErrorDialog(getContext(), new Exception("Unknown error during runtime!"));
            }
        });

        forceUnhandledException.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                throw new RuntimeException("Unhandled Exception");
            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.locales, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(adapter);
        spinnerLanguage.setSelection(getIndex(spinnerLanguage,
                DeveloperPreferencesHandler.getLocale()
                        .toString()));
        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String localeString = spinnerLanguage.getItemAtPosition(position)
                        .toString();
                DeveloperPreferencesHandler.setLocale(new Locale(localeString));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        checkBoxForceLanguage.setChecked(DeveloperPreferencesHandler.getForceLanguage());
        checkBoxForceLanguage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DeveloperPreferencesHandler.setForceLanguage(isChecked);
            }
        });

        checkBox_forceFabricEnabled.setChecked(DeveloperPreferencesHandler.getForceFabricEnabled());
        checkBox_forceFabricEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DeveloperPreferencesHandler.setForceFabricEnabled(isChecked);
            }
        });

        buttonTestNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NotificationHandler.createNotification(getActivity(), "Title", "Message");
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(rootView);
        builder.setTitle("Developer Options");
        builder.setNeutralButton(android.R.string.ok, null);

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false); // prevent close dialog on touch outside window
        dialog.show();

        return dialog;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_developer_options;
    }

    //private method of your class
    private int getIndex(Spinner spinner, String myString) {
        int index = 0;

        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i)
                    .toString()
                    .equalsIgnoreCase(myString)) {
                index = i;
                break;
            }
        }
        return index;
    }

    @Override
    public void onStart() {
        super.onStart();
        geofenceApiHandler.onStart();
    }

    @Override
    public void onStop() {
        geofenceApiHandler.onStop();
        super.onStop();
    }
}
