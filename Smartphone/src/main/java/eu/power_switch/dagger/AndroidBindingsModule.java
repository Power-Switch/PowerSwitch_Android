package eu.power_switch.dagger;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import eu.power_switch.gui.activity.MainActivity;
import eu.power_switch.gui.dialog.GatewayDetectedDialog;
import eu.power_switch.gui.dialog.RestoreBackupFromFileActivity;
import eu.power_switch.gui.dialog.UnknownErrorDialog;
import eu.power_switch.gui.dialog.WriteNfcTagDialog;
import eu.power_switch.gui.fragment.ApartmentFragment;
import eu.power_switch.gui.fragment.BackupFragment;
import eu.power_switch.gui.fragment.NfcFragment;
import eu.power_switch.gui.fragment.TimersFragment;
import eu.power_switch.gui.fragment.alarm_clock.AlarmClockTabFragment;
import eu.power_switch.gui.fragment.alarm_clock.SleepAsAndroidFragment;
import eu.power_switch.gui.fragment.alarm_clock.StockAlarmClockFragment;
import eu.power_switch.gui.fragment.geofences.ApartmentGeofencesFragment;
import eu.power_switch.gui.fragment.geofences.CustomGeofencesFragment;
import eu.power_switch.gui.fragment.geofences.GeofencesTabFragment;
import eu.power_switch.gui.fragment.main.RoomSceneTabFragment;
import eu.power_switch.gui.fragment.main.RoomsFragment;
import eu.power_switch.gui.fragment.main.ScenesFragment;
import eu.power_switch.gui.fragment.phone.CallEventsFragment;
import eu.power_switch.gui.fragment.phone.PhoneTabFragment;
import eu.power_switch.gui.fragment.phone.SmsEventsFragment;
import eu.power_switch.gui.fragment.settings.GatewaySettingsFragment;
import eu.power_switch.gui.fragment.settings.GeneralSettingsPreferenceFragment;
import eu.power_switch.gui.fragment.settings.SettingsTabFragment;
import eu.power_switch.gui.fragment.settings.WearableSettingsPreferenceFragment;
import eu.power_switch.nfc.HiddenReceiverActivity;

/**
 * Created by Markus on 12.07.2017.
 */
@Module
public abstract class AndroidBindingsModule {

    // Main

    @ContributesAndroidInjector
    abstract MainActivity mainActivity();

    @ContributesAndroidInjector
    abstract UnknownErrorDialog unknownErrorDialog();

    // Apartment
    @ContributesAndroidInjector
    abstract ApartmentFragment apartmentFragment();

    // Rooms/Scenes Tabs
    @ContributesAndroidInjector
    abstract RoomSceneTabFragment roomSceneTabFragment();

    @ContributesAndroidInjector
    abstract RoomsFragment roomsFragment();

    @ContributesAndroidInjector
    abstract ScenesFragment scenesFragment();

    // Backup
    @ContributesAndroidInjector
    abstract BackupFragment backupFragment();

    @ContributesAndroidInjector
    abstract RestoreBackupFromFileActivity restoreBackupFromFileActivity();

    // Timer
    @ContributesAndroidInjector
    abstract TimersFragment timersFragment();

    // Alarm Clock
    @ContributesAndroidInjector
    abstract AlarmClockTabFragment alarmClockTabFragment();

    @ContributesAndroidInjector
    abstract StockAlarmClockFragment stockAlarmClockFragment();

    @ContributesAndroidInjector
    abstract SleepAsAndroidFragment sleepAsAndroidFragment();

    // Gateway
    @ContributesAndroidInjector
    abstract GatewayDetectedDialog gatewayDetectedDialog();

    // Geofences
    @ContributesAndroidInjector
    abstract GeofencesTabFragment geofencesTabFragment();

    @ContributesAndroidInjector
    abstract ApartmentGeofencesFragment apartmentGeofencesFragment();

    @ContributesAndroidInjector
    abstract CustomGeofencesFragment customGeofencesFragment();

    // Phone

    @ContributesAndroidInjector
    abstract PhoneTabFragment phoneTabFragment();

    @ContributesAndroidInjector
    abstract CallEventsFragment callEventsFragment();

    @ContributesAndroidInjector
    abstract SmsEventsFragment smsEventsFragment();

    // NFC
    @ContributesAndroidInjector
    abstract NfcFragment nfcFragment();

    @ContributesAndroidInjector
    abstract WriteNfcTagDialog writeNfcTagDialog();

    @ContributesAndroidInjector
    abstract HiddenReceiverActivity hiddenReceiverActivity();

    // Settings

    @ContributesAndroidInjector
    abstract SettingsTabFragment settingsTabFragment();

    @ContributesAndroidInjector
    abstract GeneralSettingsPreferenceFragment generalSettingsPreferenceFragment();

    @ContributesAndroidInjector
    abstract GatewaySettingsFragment gatewaySettingsFragment();

    @ContributesAndroidInjector
    abstract WearableSettingsPreferenceFragment wearableSettingsPreferenceFragment();

}
