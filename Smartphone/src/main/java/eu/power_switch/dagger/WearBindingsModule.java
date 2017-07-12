package eu.power_switch.dagger;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import eu.power_switch.wear.service.ListenerService;
import eu.power_switch.wear.service.UtilityService;

/**
 * Created by Markus on 13.07.2017.
 */
@Module
public abstract class WearBindingsModule {

    @ContributesAndroidInjector
    abstract ListenerService listenerService();

    @ContributesAndroidInjector
    abstract UtilityService utilityService();

}
