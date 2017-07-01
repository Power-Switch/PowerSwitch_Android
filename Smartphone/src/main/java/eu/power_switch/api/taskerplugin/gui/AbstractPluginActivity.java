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

package eu.power_switch.api.taskerplugin.gui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.twofortyfouram.locale.BreadCrumber;

import eu.power_switch.R;
import timber.log.Timber;

/**
 * Superclass for plug-in Activities. This class takes care of initializing aspects of the plug-in's UI to
 * look more integrated with the plug-in localHost.
 */
public abstract class AbstractPluginActivity extends Activity {
    /**
     * Flag boolean that can only be set to true via the "Don't Save"
     * {@link com.twofortyfouram.locale.R.id#twofortyfouram_locale_menu_dontsave} menu item in
     * {@link #onMenuItemSelected(int, MenuItem)}.
     */
    /*
     * There is no need to save/restore this field's state.
     */
    private boolean mIsCancelled = false;
    private Menu optionsMenu;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setupTitleApi11();
        } else {
            setTitle(BreadCrumber.generateBreadcrumb(getApplicationContext(), getIntent(),
                    getString(R.string.powerswitch_plugin_name)));
        }
    }

    @Override
    public boolean onMenuItemSelected(final int featureId, final MenuItem item) {
        final int id = item.getItemId();

        if (android.R.id.home == id) {
            finish();
            return true;
        } else if (R.id.twofortyfouram_locale_menu_dontsave == id) {
            mIsCancelled = true;
            finish();
            return true;
        } else if (R.id.twofortyfouram_locale_menu_save == id) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        optionsMenu = menu;
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.twofortyfouram_locale_help_save_dontsave, menu);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setupActionBarApi11();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            setupActionBarApi14();
        }

        return true;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupTitleApi11() {
        CharSequence callingApplicationLabel = null;
        try {
            callingApplicationLabel =
                    getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(getCallingPackage(),
                            0));
        } catch (final NameNotFoundException e) {
            Timber.e("Calling package couldn't be found", e); //$NON-NLS-1$
        }
        if (null != callingApplicationLabel) {
            setTitle(callingApplicationLabel);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBarApi11() {
        getActionBar().setSubtitle(BreadCrumber.generateBreadcrumb(getApplicationContext(), getIntent(),
                getString(R.string.powerswitch_plugin_name)));
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setupActionBarApi14() {
        getActionBar().setDisplayHomeAsUpEnabled(true);

        /*
         * Note: There is a small TOCTOU error here, in that the localHost could be uninstalled right after
         * launching the plug-in. That would cause getApplicationIcon() to return the default application
         * icon. It won't fail, but it will return an incorrect icon.
         *
         * In practice, the chances that the localHost will be uninstalled while the plug-in UI is running are very
         * slim.
         */
        try {
            getActionBar().setIcon(getPackageManager().getApplicationIcon(getCallingPackage()));
        } catch (final NameNotFoundException e) {
            Timber.e("An error occurred loading the localHost's icon", e); //$NON-NLS-1$
        }
    }

    /**
     * During {@link #finish()}, subclasses can call this method to determine whether the Activity was
     * canceled.
     *
     * @return True if the Activity was canceled. False if the Activity was not canceled.
     */
    protected boolean isCanceled() {
        return mIsCancelled;
    }

    protected Menu getOptionsMenu() {
        return optionsMenu;
    }
}
