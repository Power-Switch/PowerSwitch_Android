package eu.power_switch.dagger;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import eu.power_switch.gui.activity.MainActivity;
import eu.power_switch.gui.dialog.UnknownErrorDialog;
import eu.power_switch.gui.fragment.main.RoomSceneTabFragment;
import eu.power_switch.gui.fragment.phone.CallEventsFragment;
import eu.power_switch.gui.fragment.phone.PhoneTabFragment;
import eu.power_switch.gui.fragment.phone.SmsEventsFragment;

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

    // Rooms/Scenes Tabs
    @ContributesAndroidInjector
    abstract RoomSceneTabFragment roomSceneTabFragment();

    // Phone
    @ContributesAndroidInjector
    abstract PhoneTabFragment phoneTabFragment();

    @ContributesAndroidInjector
    abstract CallEventsFragment callEventsFragment();

    @ContributesAndroidInjector
    abstract SmsEventsFragment smsEventsFragment();

}
