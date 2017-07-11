package eu.power_switch.network;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Markus on 11.07.2017.
 */
@Module
public class NetworkHandlerModule {

    @Provides
    @Singleton
    NetworkHandler provideNetworkHandler(Context context) {
        return new NetworkHandler(context);
    }

}
