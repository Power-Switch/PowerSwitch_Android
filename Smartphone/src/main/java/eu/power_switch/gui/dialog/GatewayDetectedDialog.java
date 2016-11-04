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

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;

import java.util.ArrayList;
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.gui.activity.SmartphoneThemeHelper;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.settings.DeveloperPreferencesHandler;

/**
 * Dialog showing information about new gateways found by autodetect
 * <p>
 * Created by mre on 20.10.2016.
 */
public class GatewayDetectedDialog extends AppCompatActivity {

    private static final String KEY_GATEWAYS = "gateways";

    private ArrayList<Gateway> gateways;

    public static Intent getNewInstanceIntent(List<Gateway> gateways) {
        Intent intent = new Intent();
        intent.setAction("eu.power_switch.gateway_detected_activity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_GATEWAYS, new ArrayList<>(gateways));
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // set Theme before anything else in onCreate();
        SmartphoneThemeHelper.applyDialogTheme(this);
        // apply forced locale (if set in developer options)
        applyLocale();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_gateway_detected);
        setFinishOnTouchOutside(false); // prevent close dialog on touch outside window
        setTitle(R.string.gateway_found);

        Intent intent = getIntent();
        if (intent.hasExtra(KEY_GATEWAYS)) {
            gateways = (ArrayList<Gateway>) intent.getSerializableExtra(KEY_GATEWAYS);
        } else {
            finish();
        }
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