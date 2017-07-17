package eu.power_switch.dagger;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import eu.power_switch.application.PowerSwitch;
import eu.power_switch.application.RunConfig;
import eu.power_switch.persistence.preferences.DeveloperPreferencesHandler;

/**
 * Created by Markus on 11.07.2017.
 */
@Module
public abstract class AppModule {

    @Binds
    abstract Application application(PowerSwitch application);

    @Provides
    @Singleton
    static Context provideContext(Application application) {
        return application;
    }

    @Provides
    @Singleton
    static RunConfig provideRunConfig(DeveloperPreferencesHandler developerPreferencesHandler) {
        Boolean enabled = developerPreferencesHandler.getValue(DeveloperPreferencesHandler.PLAY_STORE_MODE);

        if (enabled) {
            return new RunConfig(RunConfig.Mode.DEMO);
        } else {
            return new RunConfig(RunConfig.Mode.NORMAL);
        }
    }

}
