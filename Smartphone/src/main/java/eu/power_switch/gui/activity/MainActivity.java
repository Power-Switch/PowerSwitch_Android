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

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.aboutlibraries.LibsConfiguration;
import com.mikepenz.aboutlibraries.entity.Library;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.MiniDrawer;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

import javax.inject.Inject;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.application.PowerSaverHelper;
import eu.power_switch.application.PowerSwitch;
import eu.power_switch.database.handler.PersistanceHandler;
import eu.power_switch.event.HistoryUpdatedEvent;
import eu.power_switch.google_play_services.chrome_custom_tabs.ChromeCustomTabHelper;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.activity.eventbus.EventBusActivity;
import eu.power_switch.gui.adapter.HistoryItemRecyclerViewAdapter;
import eu.power_switch.gui.dialog.DonationDialog;
import eu.power_switch.gui.fragment.ApartmentFragment;
import eu.power_switch.gui.fragment.AsyncTaskResult;
import eu.power_switch.gui.fragment.BackupFragment;
import eu.power_switch.gui.fragment.NfcFragment;
import eu.power_switch.gui.fragment.TimersFragment;
import eu.power_switch.gui.fragment.alarm_clock.AlarmClockTabFragment;
import eu.power_switch.gui.fragment.geofences.GeofencesTabFragment;
import eu.power_switch.gui.fragment.main.RoomSceneTabFragment;
import eu.power_switch.gui.fragment.phone.PhoneTabFragment;
import eu.power_switch.gui.fragment.settings.SettingsTabFragment;
import eu.power_switch.history.HistoryItem;
import eu.power_switch.network.NetworkHandler;
import eu.power_switch.nfc.NfcHandler;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.phone.PhoneHelper;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.ThemeHelper;
import eu.power_switch.shared.constants.SettingsConstants;
import eu.power_switch.shared.exception.gateway.GatewayAlreadyExistsException;
import eu.power_switch.shared.permission.PermissionHelper;
import eu.power_switch.special.HolidaySpecialHandler;
import timber.log.Timber;

/**
 * Main entry Activity for the app
 */
public class MainActivity extends EventBusActivity {

    public static final int IDENTIFIER_ROOMS_SCENES   = 10;
    public static final int IDENTIFIER_APARTMENTS     = 11;
    public static final int IDENTIFIER_GEOFENCES      = 12;
    public static final int IDENTIFIER_ALARM_CLOCK    = 13;
    public static final int IDENTIFIER_TIMERS         = 14;
    public static final int IDENTIFIER_BACKUP_RESTORE = 15;
    public static final int IDENTIFIER_SETTINGS       = 16;
    public static final int IDENTIFIER_ABOUT          = 17;
    public static final int IDENTIFIER_PHONE          = 18;
    public static final int IDENTIFIER_NFC            = 19;

    public static  boolean        appIsInForeground   = false;
    private static Stack<Class>   lastFragmentClasses = new Stack<>();
    private static Stack<String>  lastFragmentTitles  = new Stack<>();
    private static Stack<Integer> drawerPositionStack = new Stack<>();

    private static AppBarLayout appBarLayout;
    private static MainActivity activity;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private LinkedList<HistoryItem> historyItems = new LinkedList<>();
    private RecyclerView                   recyclerViewHistory;
    private HistoryItemRecyclerViewAdapter historyItemArrayAdapter;
    private Drawer                         navigationDrawer;
    private Drawer                         historyDrawer;
    private MiniDrawer                     miniDrawer;
    private LinearLayout                   layoutLoadingHistory;

    @Inject
    NetworkHandler networkHandler;

    @Inject
    NfcHandler nfcHandler;

    @Inject
    HolidaySpecialHandler holidaySpecialHandler;

    @Inject
    PersistanceHandler persistanceHandler;

    /**
     * Add class to Backstack
     *
     * @param newFragmentClass Classname
     * @param title            Header title
     */
    public static void addToBackstack(int menuItemIdentifier, Class newFragmentClass, String title) {
        lastFragmentClasses.push(newFragmentClass);
        lastFragmentTitles.push(title);
        drawerPositionStack.push(menuItemIdentifier);
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
     * Get this activity.
     * Used for always accessible fragmentManager in {@link StatusMessageHandler}
     *
     * @return this
     */
    public static AppCompatActivity getActivity() {
        return activity;
    }

    /**
     * Get Main App View
     *
     * @return View
     */
    public static View getMainAppView() {
        return appBarLayout;
    }

    public void startFragmentTransaction(int menuItemIdentifier, String menuItemTitle, Fragment fragment) {
        try {
            if (fragment != null && (lastFragmentClasses.isEmpty()) || !lastFragmentClasses.peek()
                    .equals(fragment.getClass())) {
                lastFragmentClasses.push(fragment.getClass());
                lastFragmentTitles.push(String.valueOf(menuItemTitle));
                drawerPositionStack.push(menuItemIdentifier);
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right,
                                R.anim.slide_out_left,
                                android.R.anim.slide_in_left,
                                android.R.anim.slide_out_right)
                        .replace(R.id.mainContentFrameLayout, fragment)
                        .addToBackStack(fragment.getTag())
                        .commit();

                setTitle(menuItemTitle);
            }

        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // set Iconics LayoutInflater for XML Icon support
        // currently disabled because of text font issues
//        LayoutInflaterCompat.setFactory(getLayoutInflater(), new IconicsLayoutInflater(getDelegate()));

        activity = this;

        super.onCreate(savedInstanceState);

        appBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);

        // Set a Toolbar to replace the ActionBar.
        setSupportActionBar(toolbar);

        // Load first Fragment
        try {
            Fragment fragment;
            if (SmartphonePreferencesHandler.<Long>get(SmartphonePreferencesHandler.KEY_CURRENT_APARTMENT_ID) == SettingsConstants.INVALID_APARTMENT_ID) {
                fragment = ApartmentFragment.class.newInstance();
                drawerPositionStack.push(IDENTIFIER_APARTMENTS);
            } else {
                fragment = RoomSceneTabFragment.newInstance(SmartphonePreferencesHandler.<Integer>get(SmartphonePreferencesHandler.KEY_STARTUP_DEFAULT_TAB));
                drawerPositionStack.push(IDENTIFIER_ROOMS_SCENES);
            }
            lastFragmentClasses.push(fragment.getClass());
            lastFragmentTitles.push(String.valueOf(getTitle()));
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.mainContentFrameLayout, fragment)
                    .commit();
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getActivity(), e);
        }

        initNavigationDrawer();
        navigationDrawer.setSelection(drawerPositionStack.peek());
        initHistoryDrawer(navigationDrawer);

        if (SmartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_SHOULD_ASK_SEND_ANONYMOUS_CRASH_DATA)) {
            new AlertDialog.Builder(this).setTitle(R.string.title_sendAnonymousCrashData)
                    .setMessage(R.string.message_sendAnonymousCrashData)
                    .setPositiveButton(R.string.enable, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SmartphonePreferencesHandler.set(SmartphonePreferencesHandler.KEY_SEND_ANONYMOUS_CRASH_DATA, true);
                            SmartphonePreferencesHandler.set(SmartphonePreferencesHandler.KEY_SHOULD_ASK_SEND_ANONYMOUS_CRASH_DATA, false);
                        }
                    })
                    .setNegativeButton(R.string.disable, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SmartphonePreferencesHandler.set(SmartphonePreferencesHandler.KEY_SEND_ANONYMOUS_CRASH_DATA, false);
                            SmartphonePreferencesHandler.set(SmartphonePreferencesHandler.KEY_SHOULD_ASK_SEND_ANONYMOUS_CRASH_DATA, false);
                        }
                    })
                    .show();
        }

        if (Build.VERSION.SDK_INT >= 23 && !PowerSaverHelper.isIgnoringBatteryOptimizations(getActivity())) {
            new AlertDialog.Builder(this).setTitle(R.string.disable_battery_optimizations_title)
                    .setMessage(R.string.disable_battery_optimizations_message)
                    .setPositiveButton(R.string.open_settings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            PowerSaverHelper.openIgnoreOptimizationSettings(getActivity());
                        }
                    })
                    .show();
        }

        if (SmartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_SHOULD_SHOW_WIZARD)) {
//            startActivity(WizardActivity.getLaunchIntent(this));
        } else {
            startGatewayAutoDiscovery();
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_main;
    }

    private void startGatewayAutoDiscovery() {
        // start automatic gateway discovery (if enabled)
        if (SmartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_AUTO_DISCOVER) && (networkHandler.isWifiConnected() || networkHandler.isEthernetConnected())) {
            new AsyncTask<Void, Void, AsyncTaskResult<Gateway>>() {

                @Override
                protected AsyncTaskResult<Gateway> doInBackground(Void... voids) {
                    try {
                        List<Gateway> foundGateways = networkHandler.searchGateways();

                        Gateway[] gatewaysArray = new Gateway[foundGateways.size()];
                        foundGateways.toArray(gatewaysArray);

                        return new AsyncTaskResult<>(gatewaysArray);
                    } catch (Exception e) {
                        return new AsyncTaskResult<>(e);
                    }
                }

                @Override
                protected void onPostExecute(AsyncTaskResult<Gateway> result) {
                    if (result.isSuccess()) {
                        List<Gateway> foundGateways = result.getResult();

                        try {
                            if (foundGateways.isEmpty() && persistanceHandler.getAllGateways()
                                    .isEmpty()) {
                                StatusMessageHandler.showInfoMessage(getActivity(), R.string.no_gateway_found, Snackbar.LENGTH_LONG);
                            } else {
                                for (Gateway gateway : foundGateways) {
                                    if (gateway == null) {
                                        continue;
                                    }
                                    try {
                                        persistanceHandler.addGateway(gateway);
                                        StatusMessageHandler.showInfoMessage(getActivity(), R.string.gateway_found, Snackbar.LENGTH_LONG);
                                    } catch (GatewayAlreadyExistsException e) {
                                        try {
                                            persistanceHandler.enableGateway(e.getIdOfExistingGateway());
                                            StatusMessageHandler.showInfoMessage(getActivity(), R.string.gateway_found, Snackbar.LENGTH_LONG);
                                        } catch (Exception e1) {
                                            Timber.e(e1);
                                            StatusMessageHandler.showInfoMessage(getActivity(),
                                                    R.string.error_enabling_gateway,
                                                    Snackbar.LENGTH_LONG);
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            StatusMessageHandler.showErrorMessage(getActivity(), e);
                        }
                    } else {
                        StatusMessageHandler.showErrorMessage(getActivity(), result.getException());
                    }
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void initNavigationDrawer() {
        // Set the menu icon instead of the launcher icon.
        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(IconicsHelper.getMenuIcon(this));
        ab.setDisplayHomeAsUpEnabled(true);

        final int accentColor = ThemeHelper.getThemeAttrColor(getActivity(), R.attr.colorAccent);
        // if you want to update the items at a later time it is recommended to keep it in a variable
        final IDrawerItem itemHome = new PrimaryDrawerItem().withName(R.string.menu_home)
                .withIcon(new IconicsDrawable(this, MaterialDesignIconic.Icon.gmi_arrow_back).color(accentColor)
                        .sizeDp(24))
                .withSelectable(false)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        try {
                            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            while (drawerPositionStack.size() > 1) {
                                drawerPositionStack.pop();
                            }
                            while (lastFragmentClasses.size() > 1) {
                                lastFragmentClasses.pop();
                            }
                            while (lastFragmentTitles.size() > 1) {
                                lastFragmentTitles.pop();
                            }

                            setTitle(R.string.powerswitch_app_name);
                            navigationDrawer.setSelection(IDENTIFIER_ROOMS_SCENES);

                            return true;
                        } catch (Exception e) {
                            StatusMessageHandler.showErrorMessage(getActivity(), e);
                            return false;
                        } finally {
                            navigationDrawer.closeDrawer();
                        }
                    }
                });
        final IDrawerItem itemRoomsScenes = new PrimaryDrawerItem().withName(R.string.menu_rooms_scenes)
                .withIcon(IconicsHelper.getRoomsScenesIcon(this))
                .withSelectable(true)
                .withIdentifier(IDENTIFIER_ROOMS_SCENES)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        try {
                            startFragmentTransaction(IDENTIFIER_ROOMS_SCENES,
                                    getString(R.string.menu_rooms_scenes),
                                    RoomSceneTabFragment.newInstance(SmartphonePreferencesHandler.<Integer>get(SmartphonePreferencesHandler.KEY_STARTUP_DEFAULT_TAB)));
                            return true;
                        } catch (Exception e) {
                            StatusMessageHandler.showErrorMessage(getActivity(), e);
                            return false;
                        } finally {
                            navigationDrawer.closeDrawer();
                        }
                    }
                });
        final IDrawerItem itemApartments = new PrimaryDrawerItem().withName(R.string.menu_apartments)
                .withIcon(IconicsHelper.getApartmentsIcon(this))
                .withSelectable(true)
                .withIdentifier(IDENTIFIER_APARTMENTS)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        try {
                            startFragmentTransaction(IDENTIFIER_APARTMENTS,
                                    getString(R.string.menu_apartments),
                                    ApartmentFragment.class.newInstance());
                            return true;
                        } catch (Exception e) {
                            StatusMessageHandler.showErrorMessage(getActivity(), e);
                            return false;
                        } finally {
                            navigationDrawer.closeDrawer();
                        }
                    }
                });
        final IDrawerItem itemGeofences = new PrimaryDrawerItem().withName(R.string.menu_geofences)
                .withIcon(IconicsHelper.getGeofencesIcon(this))
                .withSelectable(true)
                .withIdentifier(IDENTIFIER_GEOFENCES)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        try {
                            startFragmentTransaction(IDENTIFIER_GEOFENCES,
                                    getString(R.string.menu_geofences),
                                    GeofencesTabFragment.class.newInstance());
                            return true;
                        } catch (Exception e) {
                            StatusMessageHandler.showErrorMessage(getActivity(), e);
                            return false;
                        } finally {
                            navigationDrawer.closeDrawer();
                        }
                    }
                });
        final IDrawerItem itemAlarmClock = new PrimaryDrawerItem().withName(R.string.menu_alarm_clock)
                .withIcon(IconicsHelper.getAlarmClockIcon(this))
                .withSelectable(true)
                .withIdentifier(IDENTIFIER_ALARM_CLOCK)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        try {
                            startFragmentTransaction(IDENTIFIER_ALARM_CLOCK,
                                    getString(R.string.menu_alarm_clock),
                                    AlarmClockTabFragment.class.newInstance());
                            return true;
                        } catch (Exception e) {
                            StatusMessageHandler.showErrorMessage(getActivity(), e);
                            return false;
                        } finally {
                            navigationDrawer.closeDrawer();
                        }
                    }
                });
        final IDrawerItem itemPhone = new PrimaryDrawerItem().withName(R.string.phone)
                .withIcon(IconicsHelper.getPhoneIcon(this))
                .withSelectable(true)
                .withEnabled(PhoneHelper.isCallingSupported(this))
                .withIdentifier(IDENTIFIER_PHONE)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        try {
                            startFragmentTransaction(IDENTIFIER_PHONE, getString(R.string.phone), PhoneTabFragment.class.newInstance());
                            return true;
                        } catch (Exception e) {
                            StatusMessageHandler.showErrorMessage(getActivity(), e);
                            return false;
                        } finally {
                            navigationDrawer.closeDrawer();
                        }
                    }
                });
        // if you want to update the items at a later time it is recommended to keep it in a variable
        final IDrawerItem itemTimer = new PrimaryDrawerItem().withName(R.string.timers)
                .withIcon(IconicsHelper.getTimerIcon(this))
                .withSelectable(true)
                .withIdentifier(IDENTIFIER_TIMERS)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        try {
                            startFragmentTransaction(IDENTIFIER_TIMERS, getString(R.string.timers), TimersFragment.class.newInstance());
                            return true;
                        } catch (Exception e) {
                            StatusMessageHandler.showErrorMessage(getActivity(), e);
                            return false;
                        } finally {
                            navigationDrawer.closeDrawer();
                        }
                    }
                });
        final IDrawerItem itemHistory = new PrimaryDrawerItem().withName(R.string.history)
                .withIcon(IconicsHelper.getHistoryIcon(this))
                .withSelectable(false)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        try {
                            historyDrawer.openDrawer();
                            return true;
                        } catch (Exception e) {
                            StatusMessageHandler.showErrorMessage(getActivity(), e);
                            return false;
                        } finally {
                            navigationDrawer.closeDrawer();
                        }
                    }
                });
        final IDrawerItem itemNfc = new PrimaryDrawerItem().withName(R.string.nfc)
                .withIcon(IconicsHelper.getNfcIcon(this))
                .withSelectable(true)
                .withIdentifier(IDENTIFIER_NFC)
                .withEnabled(nfcHandler.isNfcSupported())
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        try {
                            startFragmentTransaction(IDENTIFIER_NFC, getString(R.string.nfc), NfcFragment.class.newInstance());
                            return true;
                        } catch (Exception e) {
                            StatusMessageHandler.showErrorMessage(getActivity(), e);
                            return false;
                        } finally {
                            navigationDrawer.closeDrawer();
                        }
                    }
                });
        final IDrawerItem itemBackupRestore = new PrimaryDrawerItem().withName(R.string.menu_backup_restore)
                .withIcon(IconicsHelper.getBackupRestoreIcon(this))
                .withSelectable(true)
                .withIdentifier(IDENTIFIER_BACKUP_RESTORE)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        try {
                            startFragmentTransaction(IDENTIFIER_BACKUP_RESTORE,
                                    getString(R.string.menu_backup_restore),
                                    BackupFragment.class.newInstance());
                            return true;
                        } catch (Exception e) {
                            StatusMessageHandler.showErrorMessage(getActivity(), e);
                            return false;
                        } finally {
                            navigationDrawer.closeDrawer();
                        }
                    }
                });
        final IDrawerItem itemSettings = new PrimaryDrawerItem().withName(R.string.menu_settings)
                .withIcon(IconicsHelper.getSettingsIcon(this))
                .withSelectable(true)
                .withIdentifier(IDENTIFIER_SETTINGS)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        try {
                            startFragmentTransaction(IDENTIFIER_SETTINGS, getString(R.string.menu_settings), SettingsTabFragment.class.newInstance());
                            return true;
                        } catch (Exception e) {
                            StatusMessageHandler.showErrorMessage(getActivity(), e);
                            return false;
                        } finally {
                            navigationDrawer.closeDrawer();
                        }
                    }
                });
        final IDrawerItem itemHelp = new SecondaryDrawerItem().withName(R.string.menu_help)
                .withIcon(IconicsHelper.getHelpIcon(this))
                .withSelectable(false)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        try {
                            String url = "http://power-switch.eu/faq/";
                            startActivity(ChromeCustomTabHelper.getBrowserIntent(getActivity(), url));
                            return true;
                        } catch (Exception e) {
                            StatusMessageHandler.showErrorMessage(getActivity(), e);
                            return false;
                        } finally {
                            navigationDrawer.closeDrawer();
                        }
                    }
                });
        final IDrawerItem itemDonate = new SecondaryDrawerItem().withName(R.string.donate)
                .withIcon(IconicsHelper.getDonateIcon(this))
                .withSelectable(false)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        try {
                            DonationDialog donationDialog = new DonationDialog();
                            donationDialog.show(getSupportFragmentManager(), null);

                            return true;
                        } catch (Exception e) {
                            StatusMessageHandler.showErrorMessage(getActivity(), e);
                            return false;
                        } finally {
                            navigationDrawer.closeDrawer();
                        }
                    }
                });
        final IDrawerItem itemAbout = new SecondaryDrawerItem().withName(R.string.menu_about)
                .withIcon(IconicsHelper.getAboutIcon(this))
                .withSelectable(true)
                .withIdentifier(IDENTIFIER_ABOUT)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        String aboutAppName = getString(R.string.powerswitch_app_name) + "\n(" + PowerSwitch.getAppBuildTime() + ")";
                        if (eu.power_switch.BuildConfig.DEBUG) {
                            aboutAppName += "\n" + "DEBUG";
                        }

                        Fragment fragment = new LibsBuilder()
                                //get the fragment
                                .withAboutAppName(aboutAppName)
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
                                        String url = "https://power-switch.eu/";
                                        startActivity(ChromeCustomTabHelper.getBrowserIntent(getActivity(), url));
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
                                            String url = "https://power-switch.eu/download/";
                                            startActivity(ChromeCustomTabHelper.getBrowserIntent(getActivity(), url));
                                            return true;
                                        } else if (specialButton == Libs.SpecialButton.SPECIAL2) {
                                            String url = "https://github.com/Power-Switch/PowerSwitch_Android";
                                            startActivity(ChromeCustomTabHelper.getBrowserIntent(getActivity(), url));
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
                        startFragmentTransaction(IDENTIFIER_ABOUT, getString(R.string.menu_about), fragment);

                        navigationDrawer.closeDrawer();
                        return true;
                    }
                });

        AccountHeader accountHeader = new AccountHeaderBuilder().withActivity(this)
                .withHeaderBackground(R.drawable.header_background)
                .withCompactStyle(false)
                .build();

        navigationDrawer = new DrawerBuilder(this).withToolbar(toolbar)
                .withTranslucentStatusBar(true)
                .withAccountHeader(accountHeader)
                .withHeaderPadding(true)
                .addDrawerItems(itemHome,
                        itemHistory,
                        new DividerDrawerItem(),
                        itemApartments,
                        itemRoomsScenes,
                        itemTimer,
                        itemAlarmClock,
                        itemGeofences,
//                        itemPhone,
                        itemNfc,
                        new DividerDrawerItem(),
                        itemBackupRestore,
                        itemSettings,
                        new DividerDrawerItem(),
                        itemHelp,
                        itemDonate,
                        itemAbout)
                .build();
    }

    private void initHistoryDrawer(Drawer navigationDrawer) {
        // parent HAS to be null, so it can be attached to navigationDrawer later on
        View historyView = LayoutInflater.from(this)
                .inflate(R.layout.drawer_history, null, false);

        IconicsImageView clearHistory = historyView.findViewById(R.id.buttonClear);
        clearHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity()).setTitle(R.string.clear)
                        .setMessage(R.string.clear_history_message)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                new AsyncTask<Void, Void, Exception>() {

                                    @Override
                                    protected Exception doInBackground(Void... params) {
                                        try {
                                            persistanceHandler.clearHistory();
                                        } catch (Exception e) {
                                            return e;
                                        }

                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(Exception exception) {
                                        updateHistory();

                                        if (exception != null) {
                                            StatusMessageHandler.showErrorMessage(getActivity(), exception);
                                        }

                                    }
                                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }
                        })
                        .setNeutralButton(android.R.string.cancel, null)
                        .show();
            }
        });

        layoutLoadingHistory = historyView.findViewById(R.id.layoutLoading);

        recyclerViewHistory = historyView.findViewById(R.id.recyclerview_history);
        historyItemArrayAdapter = new HistoryItemRecyclerViewAdapter(this, historyItems);
        historyItemArrayAdapter.setOnItemClickListener(new HistoryItemRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                HistoryItem historyItem = historyItems.get(position);

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss", Locale.getDefault());
                new AlertDialog.Builder(getActivity()).setTitle(R.string.details)
                        .setMessage(simpleDateFormat.format(historyItem.getTime()
                                .getTime()) + "\n\n" + historyItem.getShortDescription() + "\n\n" + historyItem.getLongDescription())
                        .setNeutralButton(R.string.close, null)
                        .show();
            }
        });
        recyclerViewHistory.setAdapter(historyItemArrayAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerViewHistory.setLayoutManager(layoutManager);

        historyDrawer = new DrawerBuilder().withActivity(this)
                .withCustomView(historyView)
                .withHeaderPadding(true)
                .withDrawerGravity(Gravity.END)
                .append(navigationDrawer);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onHistoryUpdated(HistoryUpdatedEvent historyUpdatedEvent) {
        updateHistory();
    }

    private void updateHistory() {
        layoutLoadingHistory.setVisibility(View.VISIBLE);
        recyclerViewHistory.setVisibility(View.GONE);

        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    historyItems.clear();
                    historyItems.addAll(persistanceHandler.getHistory());

                    return null;
                } catch (Exception e) {
                    return e;
                }
            }

            @Override
            protected void onPostExecute(Exception exception) {
                historyItemArrayAdapter.notifyDataSetChanged();
                layoutLoadingHistory.setVisibility(View.GONE);
                recyclerViewHistory.setVisibility(View.VISIBLE);

                recyclerViewHistory.scrollToPosition(historyItems.size() - 1);

                if (exception != null) {
                    StatusMessageHandler.showErrorMessage(getActivity(), exception);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // send permission change to possible listeners via local broadcast
        PermissionHelper.notifyPermissionChanged(requestCode, permissions, grantResults);

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        if (navigationDrawer.isDrawerOpen()) {
            navigationDrawer.closeDrawer();
            return;
        }

        if (historyDrawer.isDrawerOpen()) {
            historyDrawer.closeDrawer();
            return;
        }

        super.onBackPressed();

        try {
            lastFragmentClasses.pop();
            lastFragmentTitles.pop();
            drawerPositionStack.pop();

            if (!lastFragmentTitles.isEmpty()) {
                setTitle(lastFragmentTitles.peek());
                navigationDrawer.setSelection(drawerPositionStack.peek(), false);
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        appIsInForeground = true;

        StatusMessageHandler.dismissCurrentSnackbar();

        updateHistory();
        holidaySpecialHandler.showHolidaySpecial();
    }

    @Override
    protected void onPause() {
        appIsInForeground = false;
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("onActivityResult(" + requestCode + "," + resultCode + "," + data);

        if (DonationDialog.iapHelper == null) {
            return;
        }

        // Pass on the activity result to the helper for handling
        if (!DonationDialog.iapHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Timber.d("onActivityResult handled by IABUtil.");
        }
    }
}
