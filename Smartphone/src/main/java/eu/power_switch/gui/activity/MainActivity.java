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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.aboutlibraries.LibsConfiguration;
import com.mikepenz.aboutlibraries.entity.Library;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.exception.gateway.GatewayAlreadyExistsException;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.adapter.HistoryItemRecyclerViewAdapter;
import eu.power_switch.gui.dialog.DonationDialog;
import eu.power_switch.gui.fragment.BackupFragment;
import eu.power_switch.gui.fragment.main.MainTabFragment;
import eu.power_switch.gui.fragment.settings.SettingsTabFragment;
import eu.power_switch.history.HistoryItem;
import eu.power_switch.network.NetworkHandler;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.constants.SettingsConstants;
import eu.power_switch.shared.log.Log;
import eu.power_switch.special.HolidaySpecialHandler;
import eu.power_switch.wear.service.UtilityService;
import eu.power_switch.widget.provider.ReceiverWidgetProvider;
import eu.power_switch.widget.provider.RoomWidgetProvider;
import eu.power_switch.widget.provider.SceneWidgetProvider;

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

    private LinkedList<HistoryItem> historyItems = new LinkedList<>();
    private RecyclerView recyclerViewHistory;
    private HistoryItemRecyclerViewAdapter historyItemArrayAdapter;

    private BroadcastReceiver broadcastReceiver;

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

    /**
     * Used to notify Room Fragment (this) that Rooms have changed
     *
     * @param context any suitable context
     */
    public static void sendHistoryChangedBroadcast(Context context) {
        Log.d(MainActivity.class, "sendHistoryChangedBroadcast");
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_HISTORY_CHANGED);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // set Theme before anything else in onCreate);
        applyTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        ab.setHomeAsUpIndicator(IconicsHelper.getMenuIcon(this));
        ab.setDisplayHomeAsUpEnabled(true);


        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateHistory();
            }
        };
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
        initMenuItems(navigationView.getMenu());

        recyclerViewHistory = (RecyclerView) findViewById(R.id.recyclerview_history);
        historyItemArrayAdapter = new HistoryItemRecyclerViewAdapter(this, historyItems);
        recyclerViewHistory.setAdapter(historyItemArrayAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(
                getResources().getInteger(R.integer.backup_grid_span_count), StaggeredGridLayoutManager.VERTICAL);
        recyclerViewHistory.setLayoutManager(layoutManager);

        // Load first Fragment
        try {
            Fragment tabLayoutFragment = MainTabFragment.class.newInstance();
            Bundle arguments = new Bundle();
            arguments.putInt(MainTabFragment.TAB_INDEX_KEY, SmartphonePreferencesHandler.getStartupDefaultTab());
            tabLayoutFragment.setArguments(arguments);
            lastFragmentClasses.push(tabLayoutFragment.getClass());
            lastFragmentTitles.push(String.valueOf(getTitle()));
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.mainContentFrameLayout, tabLayoutFragment)
                    .commit();
        } catch (Exception e) {
            Log.e(e);
            e.printStackTrace();
        }


        boolean autoDiscoverStatus = SmartphonePreferencesHandler.getAutoDiscover();

        if (autoDiscoverStatus && NetworkHandler.isWifiAvailable(this)) {
            new AsyncTask<Context, Void, Void>() {

                @Override
                protected Void doInBackground(Context... contexts) {
                    Context context = contexts[0];
                    NetworkHandler.init(context);
                    List<Gateway> foundGateways = NetworkHandler.searchGateways();

                    if (foundGateways != null) {
                        if (foundGateways.isEmpty() && DatabaseHandler.getAllGateways()
                                .isEmpty()) {
                            StatusMessageHandler.showStatusMessage(context, R.string.no_gateway_found, Snackbar
                                    .LENGTH_LONG);
                        } else {
                            for (Gateway gateway : foundGateways) {
                                if (gateway == null) {
                                    continue;
                                }
                                try {
                                    DatabaseHandler.addGateway(gateway);
                                    StatusMessageHandler.showStatusMessage(context, R.string.gateway_found, Snackbar.LENGTH_LONG);
                                } catch (GatewayAlreadyExistsException e) {
                                    DatabaseHandler.enableGateway(e.getIdOfExistingGateway());
                                    StatusMessageHandler.showStatusMessage(context, R.string.gateway_found, Snackbar.LENGTH_LONG);
                                } catch (Exception e) {
                                    Log.e(e);
                                    StatusMessageHandler.showStatusMessage(context, R.string.unknown_error, Snackbar.LENGTH_LONG);
                                }
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
                ReceiverWidgetProvider.forceWidgetUpdate(context);
                // update room widgets
                RoomWidgetProvider.forceWidgetUpdate(context);
                // update scene widgets
                SceneWidgetProvider.forceWidgetUpdate(context);
                return null;
            }
        }.execute(this);
    }

    private void applyTheme() {
        switch (SmartphonePreferencesHandler.getTheme()) {
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
    }

    private void initMenuItems(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            MenuItem currentMenuItem = menu.getItem(i);

            int tintColor;
            if (SettingsConstants.THEME_DARK_BLUE == SmartphonePreferencesHandler.getTheme()) {
                tintColor = ContextCompat.getColor(this, R.color.textColorSecondary);
            } else {
                tintColor = ContextCompat.getColor(this, R.color.textColorSecondaryInverse);
            }

            IconicsDrawable iconicsDrawable = null;
            switch (currentMenuItem.getItemId()) {
                case R.id.home:
                    iconicsDrawable = new IconicsDrawable(this, MaterialDesignIconic.Icon.gmi_home);
                    iconicsDrawable.color(ContextCompat.getColor(this, R.color.accent_blue_a700));
                    break;
                case R.id.backup_restore:
                    iconicsDrawable = new IconicsDrawable(this, MaterialDesignIconic.Icon.gmi_time_restore);
                    iconicsDrawable.color(tintColor);
                    break;
                case R.id.settings:
                    iconicsDrawable = new IconicsDrawable(this, MaterialDesignIconic.Icon.gmi_settings);
                    iconicsDrawable.color(tintColor);
                    break;
                case R.id.help:
                    iconicsDrawable = new IconicsDrawable(this, MaterialDesignIconic.Icon.gmi_help);
                    iconicsDrawable.color(tintColor);
                    break;
                case R.id.donate:
                    iconicsDrawable = new IconicsDrawable(this, MaterialDesignIconic.Icon.gmi_money);
                    iconicsDrawable.color(tintColor);
                    break;
                case R.id.about:
                    iconicsDrawable = new IconicsDrawable(this, MaterialDesignIconic.Icon.gmi_info);
                    iconicsDrawable.color(tintColor);
                    break;
                default:
                    break;
            }

            if (iconicsDrawable != null) {
                iconicsDrawable.sizeDp(24);
                currentMenuItem.setIcon(iconicsDrawable);
            }
        }
    }

    private void updateHistory() {
        historyItems.clear();
        historyItems.addAll(DatabaseHandler.getHistory());

        historyItemArrayAdapter.notifyDataSetChanged();
        recyclerViewHistory.scrollToPosition(historyItems.size() - 1);
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

            Fragment fragment = null;

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
                    fragment = BackupFragment.class.newInstance();
                    break;
                case R.id.settings:
                    fragment = SettingsTabFragment.class.newInstance();
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
                    fragment = new LibsBuilder()
                            //get the fragment
                            .withAboutIconShown(true)
                            .withAboutVersionShown(true)
                            .withLicenseShown(true)
                            .withVersionShown(true)
                            .withAutoDetect(true)
                            .withAboutDescription(getString(R.string.app_description_html))
                            .withAboutSpecial1(getString(R.string.changelog))
                            .withAboutSpecial1Description(getString(R.string.changelog))
                            .withAboutSpecial2(getString(R.string.github))
                            .withAboutSpecial2Description(getString(R.string.github))
                            .withAboutSpecial3(getString(R.string.license))
                            .withAboutSpecial3Description(getString(R.string.gpl_v3_description_html))
                            .withListener(new LibsConfiguration.LibsListener() {
                                @Override
                                public void onIconClicked(View v) {
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://power-switch.eu/"));
                                    startActivity(browserIntent);
                                }

                                @Override
                                public boolean onLibraryAuthorClicked(View v, Library library) {
                                    return false;
                                }

                                @Override
                                public boolean onLibraryContentClicked(View v, Library library) {
                                    return false;
                                }

                                @Override
                                public boolean onLibraryBottomClicked(View v, Library library) {
                                    return false;
                                }

                                @Override
                                public boolean onExtraClicked(View v, Libs.SpecialButton specialButton) {
                                    if (specialButton == Libs.SpecialButton.SPECIAL1) {
                                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://power-switch.eu/download/"));
                                        startActivity(browserIntent);
                                        return true;
                                    } else if (specialButton == Libs.SpecialButton.SPECIAL2) {
                                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Power-Switch/PowerSwitch_Android"));
                                        startActivity(browserIntent);
                                        return true;
                                    } else if (specialButton == Libs.SpecialButton.SPECIAL3) {
                                        return false;
                                    }

                                    return false;
                                }

                                @Override
                                public boolean onIconLongClicked(View v) {
                                    return false;
                                }

                                @Override
                                public boolean onLibraryAuthorLongClicked(View v, Library library) {
                                    return false;
                                }

                                @Override
                                public boolean onLibraryContentLongClicked(View v, Library library) {
                                    return false;
                                }

                                @Override
                                public boolean onLibraryBottomLongClicked(View v, Library library) {
                                    return false;
                                }
                            })
                            .supportFragment();
                    fragment.setHasOptionsMenu(true);
                    break;
            }

            if (fragment != null && (lastFragmentClasses.isEmpty()) || !lastFragmentClasses.peek()
                    .equals(fragment.getClass())) {
                lastFragmentClasses.push(fragment.getClass());
                lastFragmentTitles.push(String.valueOf(menuItem.getTitle()));
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
            if (menuItem.isCheckable()) {
                menuItem.setChecked(true);
            }

            // close the drawer
            drawerLayout.closeDrawers();
        } catch (Exception e) {
            Log.e(e);
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
    protected void onStart() {
        super.onStart();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_HISTORY_CHANGED);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        appIsInForeground = true;

        updateHistory();
        HolidaySpecialHandler.showHolidaySpecial(this);
    }

    @Override
    protected void onPause() {
        appIsInForeground = false;
        super.onPause();
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onStop();
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
