/*
 *  PowerSwitch by Max Rosin & Markus Ressel
 *  Copyright (C) 2015  Markus Ressel
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.gui.dialog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.gui.activity.butterknife.ButterKnifeDialogActivity;
import eu.power_switch.obj.gateway.Gateway;

/**
 * Dialog showing information about new gateways found by autodetect
 * <p>
 * Created by mre on 20.10.2016.
 */
public class GatewayDetectedDialog extends ButterKnifeDialogActivity {

    private static final String KEY_GATEWAYS = "gateways";

    private ArrayList<Gateway> gateways;

    public static Intent getNewInstanceIntent(Context context, List<Gateway> gateways) {
        Intent intent = new Intent(context, GatewayDetectedDialog.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_GATEWAYS, new ArrayList<>(gateways));
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setFinishOnTouchOutside(false); // prevent close dialog on touch outside window
        setTitle(R.string.gateway_found);

        Intent intent = getIntent();
        if (intent.hasExtra(KEY_GATEWAYS)) {
            gateways = (ArrayList<Gateway>) intent.getSerializableExtra(KEY_GATEWAYS);
        } else {
            finish();
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_gateway_detected;
    }


}