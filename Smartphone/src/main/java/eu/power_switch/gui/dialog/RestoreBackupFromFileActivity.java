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
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.gui.activity.ButterKnifeDialogActivity;
import eu.power_switch.shared.log.Log;

/**
 * Small Activity that handles opening PowerSwitch backup files from outside of the app
 * <p>
 * Created by Markus on 27.09.2016.
 */

public class RestoreBackupFromFileActivity extends ButterKnifeDialogActivity {

    @BindView(R.id.button_restore)
    Button buttonRestore;
    @BindView(R.id.button_cancel)
    Button buttonCancel;

    /**
     * Start new instance of this activity
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
        super.onCreate(savedInstanceState);

        setFinishOnTouchOutside(false); // prevent close dialog on touch outside window
        setTitle(R.string.are_you_sure);

        Intent intent = getIntent();
        Log.d(intent);

        final Uri fileUri = intent.getData();
        Log.d("Uri: " + String.valueOf(fileUri));

        if (fileUri == null) {
            Toast.makeText(getApplicationContext(), R.string.unknown_error, Toast.LENGTH_LONG)
                    .show();
            finish();
        }

        buttonRestore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RestoreBackupProcessingDialog restoreBackupProcessingDialog = RestoreBackupProcessingDialog.newInstance(fileUri.getPath());
                restoreBackupProcessingDialog.show(getSupportFragmentManager(), null);
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_restore_backup_confirm;
    }

}
