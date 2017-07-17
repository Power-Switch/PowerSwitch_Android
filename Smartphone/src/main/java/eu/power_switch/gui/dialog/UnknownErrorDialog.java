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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Date;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.gui.activity.butterknife.ButterKnifeDialogActivity;
import eu.power_switch.shared.constants.PermissionConstants;
import eu.power_switch.shared.exception.permission.MissingPermissionException;
import eu.power_switch.shared.log.LogHelper;
import eu.power_switch.shared.permission.PermissionHelper;
import timber.log.Timber;

import static eu.power_switch.persistence.preferences.SmartphonePreferencesHandler.KEY_SEND_ANONYMOUS_CRASH_DATA;

/**
 * Shows a Dialog with details about an unknown Exception/Error that occurred during runtime
 * <p/>
 * Created by Markus on 05.02.2016.
 */
public class UnknownErrorDialog extends ButterKnifeDialogActivity {

    private static final String THROWABLE_KEY = "throwable";
    private static final String TIME_KEY      = "time";

    private Throwable throwable;
    private Date      timeRaised;

    @BindView(R.id.textView_automaticCrashReportingEnabledInfo)
    TextView textView_automaticCrashReportingEnabledInfo;
    @BindView(R.id.textView_automaticCrashReportingDisabledInfo)
    TextView textView_automaticCrashReportingDisabledInfo;
    @BindView(R.id.button_share_via_mail)
    Button   buttonShareEmail;
    @BindView(R.id.button_share_plain_text)
    Button   buttonShareText;
    @BindView(R.id.editText_error_description)
    TextView textViewErrorDescription;
    @BindView(R.id.button_close)
    Button   buttonClose;

    /**
     * Create a new instance of this Dialog while providing an argument.
     *
     * @param context
     * @param t                        any throwable
     * @param timeRaisedInMilliseconds time when the throwable was raised
     */
    public static Intent getNewInstanceIntent(Context context, Throwable t, long timeRaisedInMilliseconds) {
        Intent intent = new Intent(context, UnknownErrorDialog.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(THROWABLE_KEY, t);
        intent.putExtra(TIME_KEY, timeRaisedInMilliseconds);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // do everything in a try statement to prevent repeating errors if something goes wrong while reporting the previous error
        try {
            super.onCreate(savedInstanceState);

            setFinishOnTouchOutside(false); // prevent close dialog on touch outside window

            Intent intent = getIntent();
            if (intent.hasExtra(THROWABLE_KEY)) {
                throwable = (Throwable) intent.getSerializableExtra(THROWABLE_KEY);
            }
            if (intent.hasExtra(TIME_KEY)) {
                timeRaised = new Date(intent.getLongExtra(TIME_KEY, 0));
            }

            buttonShareEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        reportExceptionViaMail();
                    } catch (MissingPermissionException e) {
                        PermissionHelper.showMissingPermissionDialog(UnknownErrorDialog.this,
                                PermissionConstants.REQUEST_CODE_STORAGE_PERMISSION,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    } catch (Exception e) {
                        finish();
                        statusMessageHandler.showErrorMessage(getApplicationContext(), e);
                    }
                }
            });

            buttonShareText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_TEXT, LogHelper.getStackTraceText(throwable));
                    intent.setType("text/plain");
                    startActivity(Intent.createChooser(intent, getString(R.string.send_to)));
                }
            });

            textViewErrorDescription.setText(LogHelper.getStackTraceText(throwable));

            if (smartphonePreferencesHandler.getValue(KEY_SEND_ANONYMOUS_CRASH_DATA)) {
                textView_automaticCrashReportingEnabledInfo.setVisibility(View.VISIBLE);
                textView_automaticCrashReportingDisabledInfo.setVisibility(View.GONE);
            } else {
                textView_automaticCrashReportingEnabledInfo.setVisibility(View.GONE);
                textView_automaticCrashReportingDisabledInfo.setVisibility(View.VISIBLE);
            }

            buttonClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_unknown_error;
    }

    private void reportExceptionViaMail() throws Exception {
        LogHelper.sendLogsAsMail(this, throwable, timeRaised);
    }

}
