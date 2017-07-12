package eu.power_switch.dagger;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import eu.power_switch.api.IntentReceiver;
import eu.power_switch.api.taskerplugin.FireReceiver;

/**
 * Created by Markus on 12.07.2017.
 */
@Module
public abstract class ApiBindingsModule {

    @ContributesAndroidInjector
    abstract IntentReceiver apiIntentReceiver();

    @ContributesAndroidInjector
    abstract FireReceiver fireReceiver();

}
