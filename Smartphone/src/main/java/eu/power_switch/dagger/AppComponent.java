package eu.power_switch.dagger;

import javax.inject.Singleton;

import dagger.Component;
import dagger.android.AndroidInjectionModule;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;
import eu.power_switch.application.PowerSwitch;

@Singleton
@Component(modules = {AppModule.class, ActionBindingsModule.class,
                      AlarmBindingsModule.class,
                      AndroidBindingsModule.class,
                      ApartmentBindingsModule.class,
                      ApiBindingsModule.class,
                      BackupBindingsModule.class,
                      CallEventBindingsModule.class,
                      GatewayBindingsModule.class,
                      GeofenceBindingsModule.class,
                      NetworkHandlerModule.class,
                      NfcBindingsModule.class,
                      ReceiverBindingsModule.class,
                      RoomBindingsModule.class,
                      SceneBindingsModule.class,
                      SettingsBindingsModule.class,
                      TimerBindingsModule.class,
                      WearBindingsModule.class,
                      WidgetBindingsModule.class,
                      AndroidInjectionModule.class,
                      AndroidSupportInjectionModule.class})
interface AppComponent extends AndroidInjector<PowerSwitch> {

    @Component.Builder
    abstract class Builder extends AndroidInjector.Builder<PowerSwitch> {
    }

}
