package eu.power_switch.dagger;

import javax.inject.Singleton;

import dagger.Component;
import dagger.android.AndroidInjectionModule;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;
import eu.power_switch.application.PowerSwitch;

@Singleton
@Component(modules = {AppModule.class,
                      ActionHandlerModule.class,
                      NetworkHandlerModule.class,
                      AndroidBindingsModule.class,
                      ApartmentBindingsModule.class,
                      BackupBindingsModule.class,
                      CallEventBindingsModule.class,
                      GatewayBindingsModule.class,
                      GeofenceBindingsModule.class,
                      ReceiverBindingsModule.class,
                      RoomBindingsModule.class,
                      SceneBindingsModule.class,
                      TimerBindingsModule.class,
                      WidgetBindingsModule.class,
                      AndroidInjectionModule.class,
                      AndroidSupportInjectionModule.class})
interface AppComponent extends AndroidInjector<PowerSwitch> {

    @Component.Builder
    abstract class Builder extends AndroidInjector.Builder<PowerSwitch> {
    }

}
