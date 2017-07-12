package eu.power_switch.dagger;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import eu.power_switch.alarm_clock.sleep_as_android.SleepAsAndroidIntentReceiver;
import eu.power_switch.alarm_clock.stock.StockAlarmClockIntentReceiver;
import eu.power_switch.timer.alarm.AlarmIntentReceiver;

/**
 * Created by Markus on 12.07.2017.
 */
@Module
public abstract class AlarmBindingsModule {

    @ContributesAndroidInjector
    abstract AlarmIntentReceiver alarmIntentReceiver();

    @ContributesAndroidInjector
    abstract StockAlarmClockIntentReceiver stockAlarmClockIntentReceiver();

    @ContributesAndroidInjector
    abstract SleepAsAndroidIntentReceiver sleepAsAndroidIntentReceiver();

}
