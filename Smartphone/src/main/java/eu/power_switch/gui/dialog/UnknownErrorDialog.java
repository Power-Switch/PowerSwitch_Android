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

import android.Manifest;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Date;

import eu.power_switch.R;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.settings.DeveloperPreferencesHandler;
import eu.power_switch.shared.constants.PermissionConstants;
import eu.power_switch.shared.exception.permission.MissingPermissionException;
import eu.power_switch.shared.log.Log;
import eu.power_switch.shared.log.LogHandler;
import eu.power_switch.shared.permission.PermissionHelper;

/**
 * Shows a Dialog with details about an unknown Exception/Error that occurred during runtime
 * <p/>
 * Created by Markus on 05.02.2016.
 */
public class UnknownErrorDialog extends AppCompatActivity {

    private static final String THROWABLE_KEY = "throwable";
    private static final String TIME_KEY = "time";

    private Throwable throwable;
    private Date timeRaised;

    /**
     * Create a new instance of this Dialog while providing an argument.
     *
     * @param t                        any throwable
     * @param timeRaisedInMilliseconds time when the throwable was raised
     */
    public static Intent getNewInstanceIntent(Throwable t, long timeRaisedInMilliseconds) {
        Intent intent = new Intent();
        intent.setAction("eu.power_switch.unknown_error_activity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(THROWABLE_KEY, t);
        intent.putExtra(TIME_KEY, timeRaisedInMilliseconds);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // set Theme before anything else in onCreate();
        // SmartphoneThemeHelper.applyTheme(this); // not yet ready, missing theme definitions for dialogs
        // apply forced locale (if set in developer options)
        applyLocale();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_unknown_error);
        setFinishOnTouchOutside(false); // prevent close dialog on touch outside window

        Intent intent = getIntent();
        if (intent.hasExtra(THROWABLE_KEY)) {
            throwable = (Throwable) intent.getSerializableExtra(THROWABLE_KEY);
        }
        if (intent.hasExtra(TIME_KEY)) {
            timeRaised = new Date(intent.getLongExtra(TIME_KEY, 0));
        }

        Button buttonShareEmail = (Button) findViewById(R.id.button_share_via_mail);
        buttonShareEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    reportExceptionViaMail();
                } catch (MissingPermissionException e) {
                    PermissionHelper.showMissingPermissionDialog(
                            UnknownErrorDialog.this,
                            PermissionConstants.REQUEST_CODE_STORAGE_PERMISSION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE);
                } catch (Exception e) {
                    finish();
                    StatusMessageHandler.showErrorMessage(getApplicationContext(), e);
                }
            }
        });

        Button buttonShareText = (Button) findViewById(R.id.button_share_plain_text);
        buttonShareText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, Log.getStackTraceText(throwable));
                intent.setType("text/plain");
                startActivity(Intent.createChooser(intent, getString(R.string.send_to)));
            }
        });

        TextView textViewErrorDescription = (TextView) findViewById(R.id.editText_error_description);
        textViewErrorDescription.setText(Log.getStackTraceText(throwable));

        Button buttonClose = (Button) findViewById(R.id.button_close);
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void applyLocale() {
        if (DeveloperPreferencesHandler.getForceLanguage()) {
            Resources res = getResources();
            // Change locale settings in the app.
            DisplayMetrics dm = res.getDisplayMetrics();
            android.content.res.Configuration conf = res.getConfiguration();
            conf.locale = DeveloperPreferencesHandler.getLocale();
            res.updateConfiguration(conf, dm);
        }
    }

    private void reportExceptionViaMail() throws Exception {
        LogHandler.sendLogsAsMail(this, throwable, timeRaised);
    }

}
