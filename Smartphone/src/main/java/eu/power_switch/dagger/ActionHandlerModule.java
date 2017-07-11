package eu.power_switch.dagger;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import eu.power_switch.action.ActionHandler;
import eu.power_switch.network.NetworkHandler;

/**
 * Created by Markus on 12.07.2017.
 */
@Module
public class ActionHandlerModule {

    @Provides
    @Singleton
    ActionHandler provideActionHandler(Context context, NetworkHandler networkHandler) {
        return new ActionHandler(context, networkHandler);
    }

}
