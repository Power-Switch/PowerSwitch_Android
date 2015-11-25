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

package eu.power_switch.gui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import java.util.List;
import java.util.Stack;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.exception.gateway.GatewayAlreadyExistsException;
import eu.power_switch.exception.gateway.GatewayHasBeenEnabledException;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.dialog.AboutDialog;
import eu.power_switch.gui.dialog.DonationDialog;
import eu.power_switch.gui.fragment.BackupFragment;
import eu.power_switch.gui.fragment.main.RoomsScenesTimersTabFragment;
import eu.power_switch.gui.fragment.settings.SettingsTabFragment;
import eu.power_switch.network.NetworkHandler;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.settings.SharedPreferencesHandler;
import eu.power_switch.shared.constants.SettingsConstants;
import eu.power_switch.shared.log.Log;
import eu.power_switch.wear.service.UtilityService;
import eu.power_switch.widget.activity.ConfigureReceiverWidgetActivity;
import eu.power_switch.widget.activity.ConfigureRoomWidgetActivity;
import eu.power_switch.widget.activity.ConfigureSceneWidgetActivity;

/**
 * Main entry Activity for the app
 */
public class MainActivity extends AppCompatActivity {

    public static boolean appIsInForeground = false;
    private static Stack<Class> lastFragmentClasses = new Stack<>();
    private static Stack<String> lastFragmentTitles = new Stack<>();
    private static NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private DrawerLayout drawerLayout;

    /**
     * Add class to Backstack
     *
     * @param newFragmentClass Classname
     * @param title            Header title
     */
    public static void addToBackstack(Class newFragmentClass, String title) {
        lastFragmentClasses.push(newFragmentClass);
        lastFragmentTitles.push(title);
    }

    /**
     * Indicates whether the App is running in Foreground or in Background
     *
     * @return true if the App is in Foreground
     */
    public static boolean isInForeground() {
        return appIsInForeground;
    }

    /**
     * Get Main App View
     *
     * @return navigationView
     */
    public static View getNavigationView() {
        return navigationView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // set Theme before anything else in onCreate
        SharedPreferencesHandler sharedPreferencesHandler = new SharedPreferencesHandler(getApplicationContext());
        switch (sharedPreferencesHandler.getTheme()) {
            case SettingsConstants.THEME_DARK_BLUE:
                getApplicationContext().setTheme(R.style.PowerSwitchTheme_Dark_Blue);
                setTheme(R.style.PowerSwitchTheme_Dark_Blue);
                break;
            case SettingsConstants.THEME_DARK_RED:
                getApplicationContext().setTheme(R.style.PowerSwitchTheme_Dark_Red);
                setTheme(R.style.PowerSwitchTheme_Dark_Red);
                break;
            case SettingsConstants.THEME_LIGHT_BLUE:
                getApplicationContext().setTheme(R.style.PowerSwitchTheme_Light_Blue);
                setTheme(R.style.PowerSwitchTheme_Light_Blue);
                break;
            case SettingsConstants.THEME_LIGHT_RED:
                getApplicationContext().setTheme(R.style.PowerSwitchTheme_Light_Red);
                setTheme(R.style.PowerSwitchTheme_Light_Red);
                break;
            default:
                getApplicationContext().setTheme(R.style.PowerSwitchTheme_Dark_Blue);
                setTheme(R.style.PowerSwitchTheme_Dark_Blue);
                break;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // One time Database Handler initialization for all later access
        DatabaseHandler.init(getApplicationContext());

        // Set a Toolbar to replace the ActionBar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Find our drawer view
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Tie DrawerLayout events to the ActionBarToggle (to animate hamburger icon)
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        // Set the menu icon instead of the launcher icon.
        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        ab.setDisplayHomeAsUpEnabled(true);

        // Find our drawer view
        navigationView = (NavigationView) findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectNavigationItem(menuItem);
                        return true;
                    }
                });

        // Load first Fragment
        try {
            Fragment tabLayoutFragment = RoomsScenesTimersTabFragment.class.newInstance();
            lastFragmentClasses.push(tabLayoutFragment.getClass());
            lastFragmentTitles.push(String.valueOf(getTitle()));
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.mainContentFrameLayout, tabLayoutFragment)
                    .commit();
        } catch (Exception e) {
            Log.e(e);
            e.printStackTrace();
        }

        boolean autoDiscoverStatus = sharedPreferencesHandler.getAutoDiscover();

        if (autoDiscoverStatus) {
            new AsyncTask<Context, Void, Void>() {

                @Override
                protected Void doInBackground(Context... contexts) {
                    Context context = contexts[0];
                    NetworkHandler nwm = new NetworkHandler(context);
                    List<Gateway> foundGateways = nwm.searchGateways();

                    if (foundGateways.isEmpty() && DatabaseHandler.getAllGateways().isEmpty()) {
                        StatusMessageHandler.showStatusMessage(context, R.string.no_gateway_found, Snackbar
                                .LENGTH_LONG);
                    } else {
                        for (Gateway gateway : foundGateways) {
                            if (gateway == null) {
                                continue;
                            }
                            try {
                                DatabaseHandler.addGateway(gateway);
                                StatusMessageHandler.showStatusMessage(context, R.string.gateway_found, Snackbar
                                        .LENGTH_LONG);
                            } catch (GatewayAlreadyExistsException e) {
                                Log.e(e);
                            } catch (GatewayHasBeenEnabledException e) {
                                Log.e(e);
                                StatusMessageHandler.showStatusMessage(context,
                                        R.string.gateway_already_exists_it_has_been_enabled, Snackbar.LENGTH_LONG);
                            } catch (Exception e) {
                                Log.e(e);
                                StatusMessageHandler.showStatusMessage(context, R.string.unknown_error, Snackbar
                                        .LENGTH_LONG);
                            }
                        }
                    }
                    return null;
                }
            }.execute(this);
        }

        new AsyncTask<Context, Void, Void>() {
            @Override
            protected Void doInBackground(Context... contexts) {
                Context context = contexts[0];

                // update wear data
                UtilityService.forceWearDataUpdate(context);
                UtilityService.forceWearSettingsUpdate(context);

                // update receiver widgets
                ConfigureReceiverWidgetActivity.forceWidgetUpdate(context);
                // update room widgets
                ConfigureRoomWidgetActivity.forceWidgetUpdate(context);
                // update scene widgets
                ConfigureSceneWidgetActivity.forceWidgetUpdate(context);
                return null;
            }
        }.execute(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void selectNavigationItem(MenuItem menuItem) {
        try {
            Class newFragmentClass = null;

            switch (menuItem.getItemId()) {
                case android.R.id.home:
                    drawerLayout.openDrawer(GravityCompat.START);
                    break;
                case R.id.home:
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    while (lastFragmentClasses.size() > 1) {
                        lastFragmentClasses.pop();
                    }
                    while (lastFragmentTitles.size() > 1) {
                        lastFragmentTitles.pop();
                    }

                    setTitle(R.string.app_name);

                    menuItem.setChecked(true);
                    drawerLayout.closeDrawers();
                    return;
                case R.id.backup_restore:
                    newFragmentClass = BackupFragment.class;
                    break;
                case R.id.settings:
                    newFragmentClass = SettingsTabFragment.class;
                    break;
                case R.id.help:
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://power-switch.eu/faq/"));
                    startActivity(browserIntent);

                    drawerLayout.closeDrawers();
                    return;
                case R.id.donate:
                    DonationDialog donationDialog = new DonationDialog();
                    donationDialog.show(getSupportFragmentManager(), null);

                    drawerLayout.closeDrawers();
                    return;
                case R.id.about:
                    AboutDialog aboutDialog = new AboutDialog();
                    aboutDialog.show(getSupportFragmentManager(), null);

                    drawerLayout.closeDrawers();
                    return;
                default:

            }

            if (newFragmentClass != null && (lastFragmentClasses.isEmpty()) || !lastFragmentClasses.peek()
                    .equals(newFragmentClass)) {
                lastFragmentClasses.push(newFragmentClass);
                lastFragmentTitles.push(String.valueOf(menuItem.getTitle()));
                Fragment fragment = (Fragment) lastFragmentClasses.peek().newInstance();
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim
                                .slide_in_right, R.anim.slide_out_left, android.R.anim
                                .slide_in_left, android.R.anim.slide_out_right)
                        .replace(R.id.mainContentFrameLayout, fragment)
                        .addToBackStack(fragment.getTag()).commit();

                setTitle(menuItem.getTitle());
            }

            // Highlight the selected item
            menuItem.setChecked(true);

            // close the drawer
            drawerLayout.closeDrawers();
        } catch (Exception e) {
            Log.e(e);
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case BackupFragment.REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    BackupFragment.sendBackupsChangedBroadcast(this);
                } else {
                    // Permission Denied
                    StatusMessageHandler.showStatusMessage(this, R.string.permission_denied, Snackbar
                            .LENGTH_LONG);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        lastFragmentClasses.pop();
        lastFragmentTitles.pop();
        if (!lastFragmentTitles.isEmpty()) {
            setTitle(lastFragmentTitles.peek());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        appIsInForeground = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        appIsInForeground = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(this, "onActivityResult(" + requestCode + "," + resultCode + ","
                + data);

        if (DonationDialog.iapHelper == null) {
            return;
        }

        // Pass on the activity result to the helper for handling
        if (!DonationDialog.iapHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.d(this, "onActivityResult handled by IABUtil.");
        }
    }
}
