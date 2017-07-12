package eu.power_switch.dagger;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import eu.power_switch.network.NetworkHandler;
import eu.power_switch.network.NetworkHandlerImpl;
import eu.power_switch.network.NetworkPackageQueueHandler;

/**
 * Created by Markus on 11.07.2017.
 */
@Module
public abstract class NetworkHandlerModule {

    @Binds
    @Singleton
    public abstract NetworkHandler provideNetworkHandler(NetworkHandlerImpl networkHandlerImpl);

//    @Provides
//    @Singleton
//    NetworkHandler provideNetworkHandler(Context context) {
//        return new NetworkHandler(context);
//    }

    @ContributesAndroidInjector
    abstract NetworkPackageQueueHandler networkPackageQueueHandler();

}
