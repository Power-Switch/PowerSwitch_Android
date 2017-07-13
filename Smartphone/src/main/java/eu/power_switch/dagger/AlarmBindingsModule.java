package eu.power_switch.dagger;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import eu.power_switch.alarm_clock.sleep_as_android.SleepAsAndroidIntentReceiver;
import eu.power_switch.alarm_clock.stock.StockAlarmClockIntentReceiver;
import eu.power_switch.gui.fragment.alarm_clock.AlarmClockTabFragment;
import eu.power_switch.gui.fragment.alarm_clock.SleepAsAndroidFragment;
import eu.power_switch.gui.fragment.alarm_clock.StockAlarmClockFragment;

/**
 * Created by Markus on 12.07.2017.
 */
@Module
public abstract class AlarmBindingsModule {

    @ContributesAndroidInjector
    abstract AlarmClockTabFragment alarmClockTabFragment();

    @ContributesAndroidInjector
    abstract StockAlarmClockFragment stockAlarmClockFragment();

    @ContributesAndroidInjector
    abstract SleepAsAndroidFragment sleepAsAndroidFragment();

    @ContributesAndroidInjector
    abstract StockAlarmClockIntentReceiver stockAlarmClockIntentReceiver();

    @ContributesAndroidInjector
    abstract SleepAsAndroidIntentReceiver sleepAsAndroidIntentReceiver();

}
