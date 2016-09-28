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

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;

import eu.power_switch.R;
import eu.power_switch.gui.activity.SmartphoneThemeHelper;
import eu.power_switch.settings.DeveloperPreferencesHandler;
import eu.power_switch.shared.log.Log;

/**
 * Small Activity that handles opening PowerSwitch backup files from outside of the app
 * <p>
 * Created by Markus on 27.09.2016.
 */

public class RestoreBackupFromFileActivity extends AppCompatActivity {

    /**
     * Get launch intent for this activity
     *
     * @param context any suitable context
     * @param fileUri backup file uri
     */
    public static void newInstance(Context context, Uri fileUri) {
        Intent intent = new Intent(context, RestoreBackupFromFileActivity.class);
        intent.setData(fileUri);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // set Theme before anything else in onCreate();
        SmartphoneThemeHelper.applyDialogTheme(this);
        // apply forced locale (if set in developer options)
        applyLocale();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_restore_backup_confirm);
        setFinishOnTouchOutside(false); // prevent close dialog on touch outside window
        setTitle(R.string.are_you_sure);

        Intent intent = getIntent();
        Log.d(intent);

        final Uri fileUri = intent.getData();
        Log.d("Uri: " + fileUri.toString());

        Button buttonRestore = (Button) findViewById(R.id.button_restore);
        buttonRestore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RestoreBackupProcessingDialog restoreBackupProcessingDialog = RestoreBackupProcessingDialog.newInstance(fileUri.getPath());
                restoreBackupProcessingDialog.show(getSupportFragmentManager(), null);
            }
        });

        Button buttonCancel = (Button) findViewById(R.id.button_cancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

}
