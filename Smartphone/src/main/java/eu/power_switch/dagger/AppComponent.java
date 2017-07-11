package eu.power_switch.dagger;

import javax.inject.Singleton;

import dagger.Component;
import dagger.android.support.AndroidSupportInjectionModule;
import eu.power_switch.application.PowerSwitch;
import eu.power_switch.network.NetworkHandlerModule;

@Singleton
@Component(modules = {AppModule.class, ActionHandlerModule.class, NetworkHandlerModule.class, AndroidSupportInjectionModule.class, BuildersModule.class})
public interface AppComponent {

    void inject(PowerSwitch app);

}
