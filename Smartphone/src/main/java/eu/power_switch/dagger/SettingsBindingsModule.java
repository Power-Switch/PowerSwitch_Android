package eu.power_switch.dagger;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import eu.power_switch.gui.dialog.DeveloperOptionsDialog;
import eu.power_switch.gui.dialog.PathChooserDialog;
import eu.power_switch.gui.fragment.settings.GatewaySettingsFragment;
import eu.power_switch.gui.fragment.settings.GeneralSettingsPreferenceFragment;
import eu.power_switch.gui.fragment.settings.SettingsTabFragment;
import eu.power_switch.gui.fragment.settings.WearableSettingsPreferenceFragment;

/**
 * Created by Markus on 12.07.2017.
 */
@Module
public abstract class SettingsBindingsModule {

    @ContributesAndroidInjector
    abstract SettingsTabFragment settingsTabFragment();

    @ContributesAndroidInjector
    abstract GeneralSettingsPreferenceFragment generalSettingsPreferenceFragment();

    @ContributesAndroidInjector
    abstract GatewaySettingsFragment gatewaySettingsFragment();

    @ContributesAndroidInjector
    abstract WearableSettingsPreferenceFragment wearableSettingsPreferenceFragment();

    @ContributesAndroidInjector
    abstract PathChooserDialog pathChooserDialog();

    @ContributesAndroidInjector
    abstract DeveloperOptionsDialog developerOptionsDialog();

}
